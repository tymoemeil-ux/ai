package com.ares.core.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {

    private final Map<Class<? extends Module>, Module> byClass = new LinkedHashMap<>();
    private final Map<String, Module> byName = new LinkedHashMap<>();

    public void register(Module module) {
        byClass.put(module.getClass(), module);
        byName.put(module.name().toLowerCase(), module);
        // modul musi byc sluchaczem eventow, inaczej jego @EventHandler nigdy sie nie odpali
        com.ares.Ares.get().eventBus().register(module);
    }

    public Collection<Module> all() {
        return byClass.values();
    }

    public List<Module> allSorted() {
        List<Module> list = new ArrayList<>(byClass.values());
        list.sort(Comparator.comparing(Module::name));
        return list;
    }

    public List<Module> byCategory(ModuleCategory category) {
        List<Module> list = new ArrayList<>();
        for (Module module : byClass.values()) {
            if (module.category() == category) list.add(module);
        }
        list.sort(Comparator.comparing(Module::name));
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        return (T) byClass.get(type);
    }

    public Module get(String name) {
        return byName.get(name.toLowerCase());
    }

    public List<Module> search(String query) {
        String q = query.toLowerCase();
        List<Module> result = new ArrayList<>();
        for (Module module : byClass.values()) {
            if (module.name().toLowerCase().contains(q)
                    || module.description().toLowerCase().contains(q)
                    || module.category().displayName().toLowerCase().contains(q)) {
                result.add(module);
            }
        }
        result.sort(Comparator.comparing(Module::name));
        return result;
    }

    public int size() {
        return byClass.size();
    }
}
