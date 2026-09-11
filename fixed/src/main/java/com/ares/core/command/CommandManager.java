package com.ares.core.command;

import com.ares.Ares;
import com.ares.core.module.Module;
import com.ares.core.setting.ModeSetting;
import com.ares.core.setting.NumberSetting;
import com.ares.core.setting.Setting;
import com.ares.core.util.player.PlayerUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

/** Komendy klienckie: /ares toggle <modul>, /ares bind, /ares set, /ares friend, /ares macro. */
public final class CommandManager {

    private static final String PREFIX = "ares";

    public CommandManager() {
    }

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal(PREFIX)
                    .then(ClientCommandManager.literal("toggle")
                            .then(ClientCommandManager.argument("module", StringArgumentType.greedyString())
                                    .suggests((context, builder) -> {
                                        for (Module module : Ares.get().modules().all()) {
                                            builder.suggest(module.name().replace(" ", ""));
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "module");
                                        Module module = Ares.get().modules().get(name.replace(" ", ""));
                                        if (module == null) {
                                            send("Nie znaleziono modulu: " + name);
                                            return 0;
                                        }
                                        module.toggle();
                                        send(module.name() + " -> " + (module.isEnabled() ? "ON" : "OFF"));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("set")
                            .then(ClientCommandManager.argument("module", StringArgumentType.word())
                                    .then(ClientCommandManager.argument("setting", StringArgumentType.word())
                                            .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                                                    .executes(context -> {
                                                        String moduleName = StringArgumentType.getString(context, "module");
                                                        Module module = Ares.get().modules().get(moduleName.replace(" ", ""));
                                                        if (module == null) {
                                                            send("Nie znaleziono modulu: " + moduleName);
                                                            return 0;
                                                        }
                                                        String settingName = StringArgumentType.getString(context, "setting");
                                                        Setting<?> setting = module.setting(settingName.replace("_", " "));
                                                        if (setting == null) {
                                                            send("Nie znaleziono ustawienia: " + settingName);
                                                            return 0;
                                                        }
                                                        String value = StringArgumentType.getString(context, "value");
                                                        if (setting.parse(value)) {
                                                            send(setting.name() + " = " + setting.get());
                                                        } else {
                                                            send("Nieprawidlowa wartosc: " + value);
                                                        }
                                                        Ares.get().config().markDirty();
                                                        return 1;
                                                    })))))
                    .then(ClientCommandManager.literal("bind")
                            .then(ClientCommandManager.argument("module", StringArgumentType.word())
                                    .then(ClientCommandManager.argument("key", StringArgumentType.word())
                                            .executes(context -> {
                                                Module module = Ares.get().modules().get(
                                                        StringArgumentType.getString(context, "module").replace(" ", ""));
                                                if (module == null) return 0;
                                                String raw = StringArgumentType.getString(context, "key");
                                                int key = parseKey(raw);
                                                if (key == -1) {
                                                    send("Nieznany klawisz: " + raw
                                                            + " (np. r, g, right_shift, key.mouse.0, 82)");
                                                    return 0;
                                                }
                                                module.setBind(key);
                                                send("Bind " + module.name() + " = " + raw + " (" + key + ")");
                                                Ares.get().config().markDirty();
                                                return 1;
                                            }))))
                    .then(ClientCommandManager.literal("friend")
                            .then(ClientCommandManager.literal("add")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                            .executes(context -> {
                                                String name = StringArgumentType.getString(context, "name");
                                                if (Ares.get().friends().add(name)) send("Dodano znajomego: " + name);
                                                else send("Juz jest znajomym: " + name);
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("remove")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                            .executes(context -> {
                                                String name = StringArgumentType.getString(context, "name");
                                                if (Ares.get().friends().remove(name)) send("Usunieto: " + name);
                                                else send("Nie znaleziono: " + name);
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("list")
                                    .executes(context -> {
                                        send("Znajomi: " + String.join(", ", Ares.get().friends().all()));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("macro")
                            .then(ClientCommandManager.literal("add")
                                    .then(ClientCommandManager.argument("key", IntegerArgumentType.integer())
                                            .then(ClientCommandManager.argument("action", StringArgumentType.greedyString())
                                                    .executes(context -> {
                                                        Ares.get().macros().add(IntegerArgumentType.getInteger(context, "key"),
                                                                StringArgumentType.getString(context, "action"));
                                                        send("Dodano makro");
                                                        return 1;
                                                    }))))
                            .then(ClientCommandManager.literal("clear")
                                    .executes(context -> {
                                        Ares.get().macros().clear();
                                        send("Wyczyszczono makra");
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("save")
                            .executes(context -> {
                                Ares.get().save();
                                send("Zapisano konfiguracje");
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("modules")
                            .executes(context -> {
                                StringBuilder builder = new StringBuilder("Moduly (");
                                builder.append(Ares.get().modules().size()).append("): ");
                                for (Module module : Ares.get().modules().allSorted()) {
                                    builder.append(module.name());
                                    if (module.isEnabled()) builder.append("*");
                                    builder.append(", ");
                                }
                                send(builder.toString());
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("gui")
                            .executes(context -> {
                                Ares.get().openClickGui();
                                return 1;
                            }))
            );
        });
    }

    public void handleSetShortcut(Module module, String settingName, String value) {
        if (module == null) return;
        Setting<?> setting = module.setting(settingName);
        if (setting == null) return;
        if (setting instanceof NumberSetting<?> number && value.startsWith("+")) {
            // wartosc wzgledna
        }
        setting.parse(value);
    }

    public void handleModeCycle(Module module, String settingName) {
        if (module == null) return;
        Setting<?> setting = module.setting(settingName);
        if (setting instanceof ModeSetting<?> mode) {
            mode.next();
        }
    }

    private void send(String message) {
        if (Ares.get().mc().player == null) return;
        Ares.get().mc().player.sendMessage(Text.literal("§7[§dAres§7] §f" + message), false);
    }

    public static String prefix() {
        return PREFIX;
    }

    public static boolean isAvailable() {
        return Ares.get().mc().player != null;
    }

    public static void sendChat(String message) {
        PlayerUtil.sendMessage(message);
    }

    public static FabricClientCommandSource source(FabricClientCommandSource source) {
        return source;
    }

    /**
     * Zamienia nazwe klawisza na kod: liczba (82), litera/klawisz (r, right_shift, left_control),
     * przycisk myszy (mouse.0 / key.mouse.0). Zwraca -1 gdy sie nie udalo.
     */
    private static int parseKey(String raw) {
        if (raw == null || raw.isEmpty()) return -1;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            // ponizej nazwa klawisza
        }
        String name = raw.toLowerCase(java.util.Locale.ROOT).replace('-', '_');
        if (name.startsWith("mouse") || name.startsWith("key.mouse")) {
            String digits = name.replaceAll("\\D+", "");
            int button = digits.isEmpty() ? 0 : Integer.parseInt(digits);
            return -(100 + button);
        }
        for (String prefix : new String[]{"", "key.", "key.keyboard."}) {
            try {
                net.minecraft.client.util.InputUtil.Key key =
                        net.minecraft.client.util.InputUtil.fromTranslationKey(prefix + name);
                if (key != null && key.getCode() != 0) return key.getCode();
            } catch (Exception ignored) {
                // probujemy kolejny prefix
            }
        }
        return -1;
    }
}
