package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.util.Wrapper;

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

    /**
     * Minecraft trzyma licznik `itemUseCooldown` - dopoki jest > 0, nie mozna znowu
     * postawic/uzyc itemu. Zerujemy go co tick, wiec stawianie jest natychmiastowe.
     */
    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        if (Wrapper.mc().itemUseCooldown > 0) Wrapper.mc().itemUseCooldown = 0;
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
