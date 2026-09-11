package com.ares.modules.client;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;

/** Glowny modul HUDu. */
public final class HudModule extends Module {

    private final BoolSetting watermark = add(new BoolSetting("Watermark", "Pokazuj watermark", true).group("Elements"));
    private final BoolSetting arrayList = add(new BoolSetting("ArrayList", "Lista modulow", true).group("Elements"));
    private final BoolSetting coordinates = add(new BoolSetting("Coordinates", "Wspolrzedne", true).group("Elements"));
    private final BoolSetting fps = add(new BoolSetting("FPS", "Licznik FPS", true).group("Elements"));
    private final BoolSetting ping = add(new BoolSetting("Ping", "Ping", true).group("Elements"));
    private final BoolSetting speed = add(new BoolSetting("Speed", "Predkosc", false).group("Elements"));
    private final BoolSetting armor = add(new BoolSetting("Armor", "Pancerz", true).group("Elements"));
    private final BoolSetting totems = add(new BoolSetting("Totems", "Licznik totemow", true).group("Elements"));
    private final BoolSetting potions = add(new BoolSetting("Potions", "Efekty", true).group("Elements"));
    private final BoolSetting keystrokes = add(new BoolSetting("Keystrokes", "Klawisze WASD", false).group("Elements"));
    private final BoolSetting targetHud = add(new BoolSetting("TargetHUD", "Informacje o celu", true).group("Elements"));
    private final BoolSetting compass = add(new BoolSetting("Compass", "Kompas", false).group("Elements"));
    private final BoolSetting radar = add(new BoolSetting("Radar", "Minimapa", false).group("Elements"));
    private final BoolSetting notifications = add(new BoolSetting("Notifications", "Powiadomienia", true).group("Elements"));
    private final BoolSetting inventory = add(new BoolSetting("Inventory", "Podglad ekwipunku", false).group("Elements"));

    public HudModule() {
        super("HUD", "Nakladka informacyjna na ekranie", ModuleCategory.HUD);
    }

    @Override
    public void onEnable() {
        apply();
    }

    public void apply() {
        com.ares.Ares.get().hud().byName("Watermark").setEnabled(watermark.get());
        com.ares.Ares.get().hud().byName("ArrayList").setEnabled(arrayList.get());
        com.ares.Ares.get().hud().byName("Coordinates").setEnabled(coordinates.get());
        com.ares.Ares.get().hud().byName("FPS").setEnabled(fps.get());
        com.ares.Ares.get().hud().byName("Ping").setEnabled(ping.get());
        com.ares.Ares.get().hud().byName("Speed").setEnabled(speed.get());
        com.ares.Ares.get().hud().byName("Armor").setEnabled(armor.get());
        com.ares.Ares.get().hud().byName("Totems").setEnabled(totems.get());
        com.ares.Ares.get().hud().byName("Potions").setEnabled(potions.get());
        com.ares.Ares.get().hud().byName("Keystrokes").setEnabled(keystrokes.get());
        com.ares.Ares.get().hud().byName("TargetHUD").setEnabled(targetHud.get());
        com.ares.Ares.get().hud().byName("Compass").setEnabled(compass.get());
        com.ares.Ares.get().hud().byName("Radar").setEnabled(radar.get());
        com.ares.Ares.get().hud().byName("Notifications").setEnabled(notifications.get());
        com.ares.Ares.get().hud().byName("Inventory").setEnabled(inventory.get());
    }

    public boolean hudEnabled() {
        return isEnabled();
    }

    @Override
    public void onDisable() {
        for (com.ares.core.gui.hud.HudElement element : com.ares.Ares.get().hud().elements()) {
            element.setEnabled(false);
        }
    }
}
