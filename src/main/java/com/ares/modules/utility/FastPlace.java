package com.ares.modules.utility;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;

/** Fast Place - usuwa opuznienie przy stawianiu blokow. */
public final class FastPlace extends Module {

    private final BoolSetting crystals = add(new BoolSetting("Crystals", "Szybsze stawianie krysztalow", true).group("General"));
    private final BoolSetting blocks = add(new BoolSetting("Blocks", "Szybsze stawianie blokow", true).group("General"));
    private final BoolSetting items = add(new BoolSetting("Items", "Szybsze uzywanie itemow", true).group("General"));

    public FastPlace() {
        super("Fast Place", "Usuwa opuznienia przy stawianiu", ModuleCategory.UTILITY);
    }

    public boolean crystals() {
        return isEnabled() && crystals.get();
    }

    public boolean blocks() {
        return isEnabled() && blocks.get();
    }

    public boolean items() {
        return isEnabled() && items.get();
    }
}
