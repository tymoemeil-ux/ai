package com.ares.core.setting;

import java.util.ArrayList;
import java.util.List;

public class ItemListSetting extends Setting<List<String>> {

    public ItemListSetting(String name, String description, List<String> defaultValue) {
        super(name, description, new ArrayList<>(defaultValue));
    }

    public boolean contains(String id) {
        return get().contains(id);
    }

    public void add(String id) {
        if (!contains(id)) get().add(id);
    }

    public void remove(String id) {
        get().remove(id);
    }

    @Override
    public boolean parse(String input) {
        get().clear();
        for (String part : input.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) get().add(trimmed);
        }
        return true;
    }

    @Override
    public ItemListSetting group(String group) {
        super.group(group);
        return this;
    }
}
