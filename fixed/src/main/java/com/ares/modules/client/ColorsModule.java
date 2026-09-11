package com.ares.modules.client;

import com.ares.core.gui.theme.Theme;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.FloatSetting;

/** Colors - pelna personalizacja motywu GUI i HUDu. */
public final class ColorsModule extends Module {

    private final ColorSetting accent = add(new ColorSetting("Accent", "Kolor glowny", 0xFF7C5CFF).group("Colors"));
    private final ColorSetting accentTwo = add(new ColorSetting("Accent 2", "Kolor gradientu", 0xFF22D3EE).group("Colors"));
    private final ColorSetting background = add(new ColorSetting("Background", "Tlo okna", 0xF20C0E14).group("Colors"));
    private final ColorSetting panel = add(new ColorSetting("Panel", "Tlo paneli", 0xF2161920).group("Colors"));
    private final ColorSetting text = add(new ColorSetting("Text", "Kolor tekstu", 0xFFE8EAF2).group("Colors"));
    private final ColorSetting textDim = add(new ColorSetting("Text Dim", "Kolor drugorzedny", 0xFF8A90A6).group("Colors"));
    private final FloatSetting radius = add(new FloatSetting("Radius", "Zaokraglenie rogow", 6f, 0f, 16f).group("Wyglad"));

    public ColorsModule() {
        super("Colors", "Motyw kolorystyczny klienta", ModuleCategory.CLIENT);
    }

    public void apply() {
        Theme.get().accent = accent.get();
        Theme.get().accentSecondary = accentTwo.get();
        Theme.get().background = background.get();
        Theme.get().panel = panel.get();
        Theme.get().text = text.get();
        Theme.get().textDim = textDim.get();
        Theme.get().radius = radius.get();
    }

    @Override
    public void onEnable() {
        apply();
    }

    public boolean themeActive() {
        return true;
    }
}
