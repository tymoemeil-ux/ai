package com.ares.core.setting;

import java.awt.Color;

public class ColorSetting extends Setting<Integer> {
    private boolean rainbow;

    public ColorSetting(String name, String description, int color) {
        super(name, description, color);
    }

    public int red() {
        return (get() >> 16) & 0xFF;
    }

    public int green() {
        return (get() >> 8) & 0xFF;
    }

    public int blue() {
        return get() & 0xFF;
    }

    public int alpha() {
        return (get() >> 24) & 0xFF;
    }

    public ColorSetting rainbow(boolean rainbow) {
        this.rainbow = rainbow;
        return this;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public java.awt.Color toAwt() {
        return new Color(red(), green(), blue(), alpha());
    }

    @Override
    public boolean parse(String input) {
        try {
            set(Integer.decode(input.trim()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
