package com.ares.core.setting;

public class StringSetting extends Setting<String> {

    public StringSetting(String name, String description, String value) {
        super(name, description, value);
    }

    @Override
    public boolean parse(String input) {
        set(input);
        return true;
    }

    @Override
    public StringSetting group(String group) {
        super.group(group);
        return this;
    }
}
