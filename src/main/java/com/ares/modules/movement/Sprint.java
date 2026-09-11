package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;

/** Sprint - automatyczny sprint. */
public final class Sprint extends Module {

    private final BoolSetting onlyForward = add(new BoolSetting("Only Forward", "Tylko gdy gracz idzie do przodu", true).group("General"));
    private final BoolSetting rageMode = add(new BoolSetting("Rage", "Sprintuj nawet bez ruchu", false).group("General"));

    public Sprint() {
        super("Sprint", "Automatyczny sprint", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (Wrapper.player().isTouchingWater() && !Wrapper.player().getAbilities().flying) return;

        boolean moving = Wrapper.player().input.movementForward != 0 || Wrapper.player().input.movementSideways != 0;
        if (onlyForward.get() && Wrapper.player().input.movementForward <= 0) return;
        if (!moving && !rageMode.get()) return;

        Wrapper.player().setSprinting(true);
    }

    @Override
    public void onDisable() {
        if (Wrapper.nullCheck()) Wrapper.player().setSprinting(false);
    }
}
