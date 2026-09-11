package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;

/** No Slow - usuwa spowolnienie podczas jedzenia / blokowania / luku. */
public final class NoSlow extends Module {

    private final BoolSetting items = add(new BoolSetting("Items", "Bez spowolnienia przy uzywaniu itemow", true).group("General"));
    private final BoolSetting soulSand = add(new BoolSetting("Soul Sand", "Bez spowolnienia na soulsand", true).group("General"));
    private final BoolSetting webs = add(new BoolSetting("Webs", "Bez spowolnienia w pajeczynach", true).group("General"));
    private final BoolSetting slime = add(new BoolSetting("Slime", "Bez spowolnienia na sluzie", true).group("General"));

    public NoSlow() {
        super("No Slow", "Usuwa spowolnienia ruchu", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (items.get() && PlayerUtil.isUsingItem() && PlayerUtil.isMoving()) {
            PlayerUtil.applyMovement(PlayerUtil.forwardInput(), PlayerUtil.strafeInput(), 0.28);
        }
    }

    @Override
    public String info() {
        if (items.get() && webs.get() && soulSand.get()) return "all";
        return "items";
    }
}
