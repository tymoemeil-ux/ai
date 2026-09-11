package com.ares.modules.utility;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;

/** Reach - zwieksza zasieg ataku i interakcji. */
public final class Reach extends Module {

    private final FloatSetting distance = add(new FloatSetting("Distance", "Dodatkowy zasieg", 1.5f, 0f, 6f).group("General"));

    public Reach() {
        super("Reach", "Zwiekszony zasieg", ModuleCategory.UTILITY);
    }

    public float distance() {
        return isEnabled() ? distance.get() : 0f;
    }

    @Override
    public String info() {
        return String.format("%.1f", distance.get());
    }
}
