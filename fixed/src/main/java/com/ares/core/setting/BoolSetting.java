package com.ares.core.setting;

public class BoolSetting extends Setting<Boolean> {

    public BoolSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public boolean enabled() {
        return get();
    }

    public void toggle() {
        set(!get());
    }

    @Override
    public boolean parse(String input) {
        if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("on") || input.equals("1")) {
            set(true);
            return true;
        }
        if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("off") || input.equals("0")) {
            set(false);
            return true;
        }
        return false;
    }

    @Override
    public BoolSetting group(String group) {
        super.group(group);
        return this;
    }
}
