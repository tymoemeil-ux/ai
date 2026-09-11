package com.ares.core.setting;

public class IntSetting extends NumberSetting<Integer> {

    public IntSetting(String name, String description, int value, int min, int max) {
        this(name, description, value, min, max, 1);
    }

    public IntSetting(String name, String description, int value, int min, int max, int step) {
        super(name, description, value, min, max, step);
    }

    @Override
    public void setFromFraction(double fraction) {
        int raw = (int) Math.round(fraction * range());
        raw = (int) Math.floor((double) raw / step) * step;
        set(Math.max(min, Math.min(max, raw)));
    }

    public void increment() {
        set(Math.min(max, get() + step));
    }

    public void decrement() {
        set(Math.max(min, get() - step));
    }

    @Override
    public boolean parse(String input) {
        try {
            int v = Integer.parseInt(input.trim());
            if (v < min || v > max) return false;
            set(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public IntSetting group(String group) {
        super.group(group);
        return this;
    }
}
