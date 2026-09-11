package com.ares.modules.utility;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;

/** Hitboxes - powieksza hitboxy bytow. */
public final class Hitboxes extends Module {

    private final FloatSetting expansion = add(new FloatSetting("Expansion", "Powiększenie hitboxa", 0.3f, 0f, 2f).group("General"));

    public Hitboxes() {
        super("Hitboxes", "Powiekszone hitboxy", ModuleCategory.UTILITY);
    }

    public float expansion() {
        return isEnabled() ? expansion.get() : 0f;
    }
}
