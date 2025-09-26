package org.openhab.binding.smartthings.internal.dto;

import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;

public class ColorObject {
    public Double hue = 0.0;
    public Double saturation = 0.0;

    @Override
    public String toString() {
        return String.format("%s : %s, %s : %s", SmartthingsBindingConstants.CHANNEL_NAME_HUE, hue,
                SmartthingsBindingConstants.CHANNEL_NAME_SATURATION, saturation);
    }
}