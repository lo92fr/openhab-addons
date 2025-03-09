/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.linky.internal;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.types.Evolution;
import org.openhab.binding.linky.internal.types.FrameType;
import org.openhab.binding.linky.internal.types.Phase;
import org.openhab.binding.linky.internal.types.Pricing;
import org.openhab.binding.linky.internal.types.TeleinfoTicMode;

/**
 * The {@link LinkyFrame} class defines common attributes for any Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class LinkyFrame implements Serializable {

    private static final long serialVersionUID = -1934715078822532494L;

    private Map<LinkyChannel, String> channelToValues = new EnumMap<>(LinkyChannel.class);
    private Map<LinkyChannel, String> channelToTimestamp = new EnumMap<>(LinkyChannel.class);

    public void put(LinkyChannel channel, String value) {
        channelToValues.put(channel, value);
    }

    public void putTimestamp(LinkyChannel channel, String timestamp) {
        channelToTimestamp.put(channel, timestamp);
    }

    public @Nullable String get(LinkyChannel channel) {
        return channelToValues.get(channel);
    }

    public @Nullable Integer getAsInt(LinkyChannel channel) {
        String value = channelToValues.get(channel);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return null;
    }

    public String getAsDateTime(LinkyChannel channel) {
        String timestamp = channelToTimestamp.get(channel);
        if (timestamp == null) {
            return "";
        }
        return "20" + timestamp.substring(1, 3) + "-" + timestamp.substring(3, 5) + "-" + timestamp.substring(5, 7)
                + "T" + timestamp.substring(7, 9) + ":" + timestamp.substring(9, 11) + ":"
                + timestamp.substring(11, 13);
    }

    public LinkyFrame() {
        // default constructor
    }

    public FrameType getType() throws InvalidFrameException {
        TeleinfoTicMode ticMode = getTicMode();
        switch (ticMode) {
            case HISTORICAL:
                return getHistoricalType();
            case STANDARD:
                return getStandardType();
            default:
                throw new InvalidFrameException();
        }
    }

    public FrameType getHistoricalType() throws InvalidFrameException {
        Phase phase = getPhase();
        Pricing pricing = getPricing();
        Evolution evolution = getEvolution();
        switch (phase) {
            case ONE_PHASED:
                switch (evolution) {
                    case ICC:
                        switch (pricing) {
                            case BASE:
                                return FrameType.CBEMM_ICC_BASE;
                            case EJP:
                                return FrameType.CBEMM_ICC_EJP;
                            case HC:
                                return FrameType.CBEMM_ICC_HC;
                            case TEMPO:
                                return FrameType.CBEMM_ICC_TEMPO;
                            default:
                                return FrameType.UNKNOWN;
                        }
                    case NONE:
                        switch (pricing) {
                            case BASE:
                                return FrameType.CBEMM_BASE;
                            case EJP:
                                return FrameType.CBEMM_EJP;
                            case HC:
                                return FrameType.CBEMM_HC;
                            case TEMPO:
                                return FrameType.CBEMM_TEMPO;
                            default:
                                return FrameType.UNKNOWN;
                        }
                    default:
                        return FrameType.UNKNOWN;

                }
            case THREE_PHASED:
                if (isShortFrame()) {
                    return FrameType.CBETM_SHORT;
                } else {
                    switch (pricing) {
                        case BASE:
                            return FrameType.CBETM_LONG_BASE;
                        case EJP:
                            return FrameType.CBETM_LONG_EJP;
                        case HC:
                            return FrameType.CBETM_LONG_HC;
                        case TEMPO:
                            return FrameType.CBETM_LONG_TEMPO;
                        default:
                            return FrameType.UNKNOWN;
                    }
                }
            default:
                return FrameType.UNKNOWN;
        }
    }

    public Phase getPhase() throws InvalidFrameException {
        if (channelToValues.containsKey(LinkyChannel.IINST)) {
            return Phase.ONE_PHASED;
        } else if (channelToValues.containsKey(LinkyChannel.IINST1)) {
            return Phase.THREE_PHASED;
        }
        throw new InvalidFrameException();
    }

    public boolean isShortFrame() {
        return !channelToValues.containsKey(LinkyChannel.ISOUSC);
    }

    public Evolution getEvolution() {
        if (channelToValues.containsKey(LinkyChannel.PAPP)) {
            return Evolution.ICC;
        }
        return Evolution.NONE;
    }

    public Pricing getPricing() throws InvalidFrameException {
        String optarif = channelToValues.get(LinkyChannel.OPTARIF);
        if (optarif == null) {
            throw new InvalidFrameException();
        }
        switch (optarif) {
            case "BASE":
                return Pricing.BASE;
            case "EJP.":
                return Pricing.EJP;
            case "HC..":
                return Pricing.HC;
            default:
                if (optarif.matches("BBR.")) {
                    return Pricing.TEMPO;
                }
                throw new InvalidFrameException();
        }
    }

    public TeleinfoTicMode getTicMode() throws InvalidFrameException {
        if (channelToValues.containsKey(LinkyChannel.ADCO)) {
            return TeleinfoTicMode.HISTORICAL;
        } else if (channelToValues.containsKey(LinkyChannel.ADSC)) {
            return TeleinfoTicMode.STANDARD;
        }
        throw new InvalidFrameException();
    }

    public FrameType getStandardType() throws InvalidFrameException {
        boolean isProd = channelToValues.containsKey(LinkyChannel.EAIT);
        boolean isThreePhase = channelToValues.containsKey(LinkyChannel.IRMS2);
        if (isProd && isThreePhase) {
            return FrameType.LSMT_PROD;
        }
        if (isProd) {
            return FrameType.LSMM_PROD;
        }
        if (isThreePhase) {
            return FrameType.LSMT;
        }
        return FrameType.LSMM;
    }

    public void clear() {
        channelToValues.clear();
        channelToTimestamp.clear();
    }

    public Map<LinkyChannel, String> getChannelToValues() {
        return channelToValues;
    }

    public Map<LinkyChannel, String> getChannelToTimestamp() {
        return channelToTimestamp;
    }

    private char getProgrammeChar() {
        String optarif = channelToValues.get(LinkyChannel.OPTARIF);
        if (optarif == null) {
            throw new IllegalStateException("No OPTARIF field in frame");
        }
        return optarif.charAt(3);
    }

    public String getProgrammeCircuit1() {
        char program = getProgrammeChar();
        return convertProgrammeCircuit1(program);
    }

    public String getProgrammeCircuit2() {
        char program = getProgrammeChar();
        return convertProgrammeCircuit2(program);
    }

    private String convertProgrammeCircuit1(char value) {
        String prgCircuit1 = computeProgrammeCircuitBinaryValue(value).substring(3, 5);
        switch (prgCircuit1) {
            case "01":
                return "A";
            case "10":
                return "B";
            case "11":
                return "C";
            default:
                final String error = String.format("Programme circuit 1 '%s' is unknown", prgCircuit1);
                throw new IllegalStateException(error);
        }
    }

    private String convertProgrammeCircuit2(char value) {
        String prgCircuit2 = computeProgrammeCircuitBinaryValue(value).substring(5, 8);
        switch (prgCircuit2) {
            case "000":
                return "P0";
            case "001":
                return "P1";
            case "010":
                return "P2";
            case "011":
                return "P3";
            case "100":
                return "P4";
            case "101":
                return "P5";
            case "110":
                return "P6";
            case "111":
                return "P7";
            default:
                final String error = String.format("Programme circuit 2 '%s' is unknown", prgCircuit2);
                throw new IllegalStateException(error);
        }
    }

    private String computeProgrammeCircuitBinaryValue(char value) {
        return String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0');
    }
}
