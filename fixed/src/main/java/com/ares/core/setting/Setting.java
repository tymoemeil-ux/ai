package com.ares.core.setting;

import java.util.function.Supplier;

/**
 * Bazowa klasa ustawienia modulu.
 */
public abstract class Setting<T> {
    private final String name;
    private final String description;
    private final T defaultValue;
    private T value;
    private Supplier<Boolean> visibility = () -> true;
    private String group = "General";

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public T get() {
        return value;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public void set(T value) {
        this.value = value;
    }

    public void reset() {
        this.value = defaultValue;
    }

    public Setting<T> visibleWhen(Supplier<Boolean> condition) {
        this.visibility = condition;
        return this;
    }

    public boolean isVisible() {
        return visibility.get();
    }

    public Setting<T> group(String group) {
        this.group = group;
        return this;
    }

    public String group() {
        return group;
    }

    /** Zwraca true gdy wartosc zostala zmieniona. */
    public abstract boolean parse(String input);
}
