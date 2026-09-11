package com.ares.core.setting;

public class BindSetting extends Setting<Integer> {

    public BindSetting(String name, String description, int value) {
        super(name, description, value);
    }

    @Override
    public boolean parse(String input) {
        try {
            set(Integer.parseInt(input.trim()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
