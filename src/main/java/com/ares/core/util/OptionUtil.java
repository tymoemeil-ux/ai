package com.ares.core.util;

/** Bezpieczne ustawianie opcji gry (gamma, FOV) niezaleznie od typu wartosci. */
public final class OptionUtil {

    private OptionUtil() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setValue(Object option, double value) {
        if (!(option instanceof net.minecraft.client.option.SimpleOption raw)) return;
        Object current = raw.getValue();
        try {
            if (current instanceof Number number && Math.abs(number.doubleValue() - value) < 0.0001) return;
            if (current instanceof Double) raw.setValue(value);
            else if (current instanceof Integer) raw.setValue((int) Math.round(value));
            else if (current instanceof Float) raw.setValue((float) value);
            else raw.setValue((int) Math.round(value));
        } catch (Throwable ignored) {
        }
    }

    public static double getValue(Object option) {
        if (!(option instanceof net.minecraft.client.option.SimpleOption raw)) return 0;
        Object value = raw.getValue();
        if (value instanceof Number number) return number.doubleValue();
        return 0;
    }
}
