package com.ares.core.setting;

/**
 * Wspolna logika dla ustawien liczbowych (min / max / krok).
 */
public abstract class NumberSetting<T extends Number> extends Setting<T> {
    protected final T min;
    protected final T max;
    protected final T step;

    protected NumberSetting(String name, String description, T value, T min, T max, T step) {
        super(name, description, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public T min() {
        return min;
    }

    public T max() {
        return max;
    }

    public T step() {
        return step;
    }

    public double range() {
        return max.doubleValue() - min.doubleValue();
    }

    /** Ustawia wartosc na podstawie ulamka (0..1) zakresu. */
    public abstract void setFromFraction(double fraction);

    /** Ulamek (0..1) aktualnej wartosci w zakresie. */
    public double fraction() {
        if (range() <= 0.0001) return 0;
        double f = (get().doubleValue() - min.doubleValue()) / range();
        return Math.max(0, Math.min(1, f));
    }
}
