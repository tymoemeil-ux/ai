package com.ares.core.config;

import com.ares.Ares;
import com.ares.core.gui.theme.Theme;
import com.ares.core.module.Module;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.ItemListSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.setting.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Zapis / odczyt ustawien klienta do pliku JSON. */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Path file;
    private boolean dirty;

    public ConfigManager() {
    }

    public void init(Path directory) {
        this.file = directory.resolve("ares.json");
    }

    public void markDirty() {
        dirty = true;
    }

    public Path directory() {
        return file == null ? null : file.getParent();
    }

    public void save() {
        if (file == null) return;
        JsonObject root = new JsonObject();

        JsonObject modules = new JsonObject();
        for (Module module : Ares.get().modules().all()) {
            JsonObject data = new JsonObject();
            data.addProperty("enabled", module.isEnabled());
            data.addProperty("bind", module.bind());
            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.settings()) {
                settings.add(setting.name(), toJson(setting));
            }
            data.add("settings", settings);
            modules.add(module.name(), data);
        }
        root.add("modules", modules);

        JsonObject hud = new JsonObject();
        Ares.get().hud().save(hud);
        root.add("hud", hud);

        JsonObject theme = new JsonObject();
        theme.addProperty("accent", Theme.get().accent);
        theme.addProperty("accentSecondary", Theme.get().accentSecondary);
        theme.addProperty("background", Theme.get().background);
        theme.addProperty("panel", Theme.get().panel);
        root.add("theme", theme);

        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dirty = false;
    }

    public void load() {
        if (file == null || !Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return;

            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Module module : Ares.get().modules().all()) {
                    JsonElement element = modules.get(module.name());
                    if (element == null || !element.isJsonObject()) continue;
                    JsonObject data = element.getAsJsonObject();

                    if (data.has("bind")) module.setBind(data.get("bind").getAsInt());

                    if (data.has("settings")) {
                        JsonObject settings = data.getAsJsonObject("settings");
                        for (Setting<?> setting : module.settings()) {
                            JsonElement value = settings.get(setting.name());
                            if (value == null) continue;
                            apply(setting, value);
                        }
                    }

                    boolean enabled = data.has("enabled") && data.get("enabled").getAsBoolean();
                    if (enabled && !module.isEnabled()) module.setEnabled(true);
                }
            }

            if (root.has("hud")) Ares.get().hud().load(root.getAsJsonObject("hud"));

            if (root.has("theme")) {
                JsonObject theme = root.getAsJsonObject("theme");
                Theme.get().accent = getInt(theme, "accent", Theme.get().accent);
                Theme.get().accentSecondary = getInt(theme, "accentSecondary", Theme.get().accentSecondary);
                Theme.get().background = getInt(theme, "background", Theme.get().background);
                Theme.get().panel = getInt(theme, "panel", Theme.get().panel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getInt(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) return fallback;
        return element.getAsInt();
    }

    private JsonElement toJson(Setting<?> setting) {
        Object value = setting.get();
        if (value instanceof Boolean bool) return new JsonPrimitive(bool);
        if (value instanceof Number number) return new JsonPrimitive(number);
        if (value instanceof Enum<?> enumValue) return new JsonPrimitive(enumValue.name());
        if (value instanceof String string) return new JsonPrimitive(string);
        if (value instanceof List<?> list) {
            JsonArray array = new JsonArray();
            for (Object entry : list) array.add(String.valueOf(entry));
            return array;
        }
        return new JsonPrimitive(String.valueOf(value));
    }

    private void apply(Setting<?> setting, JsonElement element) {
        try {
            if (setting instanceof ItemListSetting itemList) {
                itemList.get().clear();
                for (JsonElement entry : element.getAsJsonArray()) itemList.get().add(entry.getAsString());
                return;
            }
            if (setting instanceof ColorSetting color && element.isJsonPrimitive()) {
                color.set(element.getAsInt());
                return;
            }
            if (element.isJsonPrimitive()) {
                setting.parse(element.getAsString());
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isDirty() {
        return dirty;
    }
}
