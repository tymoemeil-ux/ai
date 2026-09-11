package com.ares.core.module;

import com.ares.Ares;
import com.ares.core.event.events.Render2DEvent;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.setting.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bazowa klasa kazdego modulu.
 */
public abstract class Module {

    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final Map<String, List<Setting<?>>> groups = new LinkedHashMap<>();

    private boolean enabled;
    private boolean hidden;
    private int bind = -1;
    private long lastToggleTime;

    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public ModuleCategory category() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int bind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }

    public long lastToggleTime() {
        return lastToggleTime;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        this.lastToggleTime = System.currentTimeMillis();
        if (enabled) {
            Ares.get().eventBus().register(this);
            onEnable();
        } else {
            onDisable();
            Ares.get().eventBus().unregister(this);
        }
        Ares.get().config().markDirty();
    }

    protected <T extends Setting<?>> T add(T setting) {
        settings.add(setting);
        groups.computeIfAbsent(setting.group(), k -> new ArrayList<>()).add(setting);
        return setting;
    }

    @SuppressWarnings("unchecked")
    public <T extends Setting<?>> T setting(String name) {
        for (Setting<?> setting : settings) {
            if (setting.name().equalsIgnoreCase(name)) return (T) setting;
        }
        return null;
    }

    public List<Setting<?>> settings() {
        return settings;
    }

    public Map<String, List<Setting<?>>> groups() {
        return groups;
    }

    /** Tekst wyswietlany obok nazwy modulu w arrayliscie. */
    public String info() {
        return null;
    }

    public boolean isVisibleInArray() {
        return enabled && !hidden;
    }

    public int colorIndex() {
        return Math.abs(name().hashCode());
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onWorldChange() {
    }

    @Override
    public String toString() {
        return name;
    }
}
