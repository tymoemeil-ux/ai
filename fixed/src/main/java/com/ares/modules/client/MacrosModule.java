package com.ares.modules.client;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;

/** Ustawienia makr. */
public final class MacrosModule extends Module {

    private final BoolSetting enabled = add(new BoolSetting("Enabled", "Makra aktywne", true).group("General"));

    public MacrosModule() {
        super("Macros", "Makra klawiszowe", ModuleCategory.CLIENT);
    }

    public boolean macrosEnabled() {
        return isEnabled() && enabled.get();
    }
}
