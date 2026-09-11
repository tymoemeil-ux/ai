package com.ares.core.macro;

import com.ares.core.util.player.PlayerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/** Zarzadza makrami (klawisz -> komenda / wiadomosc). */
public final class MacroManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<HashMap<String, String>>() {
    }.getType();

    private final Map<String, String> macros = new HashMap<>();
    private Path file;

    public MacroManager() {
    }

    public void init(Path directory) {
        this.file = directory.resolve("macros.json");
        load();
    }

    public void add(int key, String action) {
        macros.put(String.valueOf(key), action);
        save();
    }

    public void remove(int key) {
        macros.remove(String.valueOf(key));
        save();
    }

    public void clear() {
        macros.clear();
        save();
    }

    public boolean has(int key) {
        return macros.containsKey(String.valueOf(key));
    }

    public void run(int key) {
        String action = macros.get(String.valueOf(key));
        if (action == null || action.isEmpty()) return;
        if (action.startsWith("/")) PlayerUtil.sendMessage(action.substring(1));
        else PlayerUtil.sendMessage(action);
    }

    public List<Macro> all() {
        List<Macro> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : macros.entrySet()) {
            try {
                list.add(new Macro(Integer.parseInt(entry.getKey()), entry.getValue()));
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }

    public void save() {
        if (file == null) return;
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(macros, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (file == null || !Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            Map<String, String> loaded = GSON.fromJson(reader, TYPE);
            if (loaded != null) macros.putAll(loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
