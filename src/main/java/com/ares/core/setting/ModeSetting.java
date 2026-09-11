package com.ares.core.setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting<T extends Enum<T>> extends Setting<T> {
    private final List<T> values;

    public ModeSetting(String name, String description, T value) {
        super(name, description, value);
        this.values = Arrays.asList(value.getDeclaringClass().getEnumConstants());
    }

    public List<T> values() {
        return values;
    }

    public void cycle(boolean forward) {
        int index = values.indexOf(get());
        if (index == -1) return;
        int next = forward ? index + 1 : index - 1;
        if (next >= values.size()) next = 0;
        if (next < 0) next = values.size() - 1;
        set(values.get(next));
    }

    public void next() {
        cycle(true);
    }

    @Override
    public boolean parse(String input) {
        for (T v : values) {
            if (v.name().equalsIgnoreCase(input)) {
                set(v);
                return true;
            }
        }
        return false;
    }
}
