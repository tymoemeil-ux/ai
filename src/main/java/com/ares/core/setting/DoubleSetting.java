package com.ares.core.setting;

public class DoubleSetting extends NumberSetting<Double> {

    public DoubleSetting(String name, String description, double value, double min, double max) {
        this(name, description, value, min, max, 0.1);
    }

    public DoubleSetting(String name, String description, double value, double min, double max, double step) {
        super(name, description, value, min, max, step);
    }

    @Override
    public void setFromFraction(double fraction) {
        double raw = min + fraction * range();
        raw = Math.round(raw / step) * step;
        set(Math.max(min, Math.min(max, raw)));
    }

    @Override
    public boolean parse(String input) {
        try {
            double v = Double.parseDouble(input.trim());
            if (v < min || v > max) return false;
            set(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public DoubleSetting group(String group) {
        super.group(group);
        return this;
    }
}
