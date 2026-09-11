package com.ares.modules.client;

import com.ares.Ares;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;

/** Modul otwierajacy ClickGUI (bind: Right Shift). */
public final class ClickGuiModule extends Module {

    public enum Style { DROPDOWN, WINDOWS }

    private final ModeSetting<Style> style = add(new ModeSetting<>("Style", "Styl GUI", Style.DROPDOWN).group("General"));
    private final ColorSetting accent = add(new ColorSetting("Accent", "Kolor akcentu", 0xFF7C5CFF).group("Colors"));
    private final ColorSetting accentTwo = add(new ColorSetting("Accent 2", "Drugi kolor gradientu", 0xFF22D3EE).group("Colors"));

    public ClickGuiModule() {
        super("ClickGUI", "Otwiera interfejs klienta", ModuleCategory.CLIENT);
        setBind(org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        Wrapper.mc().setScreen(style.get() == Style.DROPDOWN
                ? new com.ares.core.gui.clickgui.ClickGuiScreen()
                : new com.ares.core.gui.windows.WindowsScreen());
        setEnabled(false);
    }

    public void applyTheme() {
        com.ares.core.gui.theme.Theme.get().accent = accent.get();
        com.ares.core.gui.theme.Theme.get().accentSecondary = accentTwo.get();
    }
}
