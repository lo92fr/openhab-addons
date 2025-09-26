package org.openhab.binding.smartthings.internal.dto;

public class ColorObject {
    public Double hue = 0.0;
    public Double saturation = 0.0;

    @Override
    public String toString() {
        return "hue:" + hue + " saturation:" + saturation;
    }
}