/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.linkyd2l.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LinkyD2LConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LinkyD2LConfiguration {
    public String d2lId = "021802000392";
    public String appKey = "0a6ccc00a56c744a85b4c33584058c86";
    public String ivKey = "028dfcc0b62d4bbc7994a49be63e6499";

    public boolean seemsValid() {
        return !d2lId.isBlank() && !appKey.isBlank() && !ivKey.isBlank();
    }
}
