package com.ares.modules.client;

import com.ares.Ares;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.util.Wrapper;

/** Otwiera edytor HUDu (przeciaganie elementow). */
public final class HudEditorModule extends Module {

    public HudEditorModule() {
        super("HUD Editor", "Ustawia pozycje elementow HUDu", ModuleCategory.CLIENT);
    }

    @Override
    public void onEnable() {
        Wrapper.mc().setScreen(new com.ares.core.gui.hud.HudEditorScreen());
        setEnabled(false);
    }
}
