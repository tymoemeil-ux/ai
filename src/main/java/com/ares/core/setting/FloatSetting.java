package com.ares.core.setting;

public class FloatSetting extends NumberSetting<Float> {

    public FloatSetting(String name, String description, float value, float min, float max) {
        this(name, description, value, min, max, 0.1f);
    }

    public FloatSetting(String name, String description, float value, float min, float max, float step) {
        super(name, description, value, min, max, step);
    }

    @Override
    public void setFromFraction(double fraction) {
        float raw = (float) (min + fraction * range());
        raw = (float) (Math.round(raw / step) * step);
        set(Math.max(min, Math.min(max, raw)));
    }

    @Override
    public boolean parse(String input) {
        try {
            float v = Float.parseFloat(input.trim());
            if (v < min || v > max) return false;
            set(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
