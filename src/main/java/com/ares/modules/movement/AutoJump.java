package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;

/** Auto Jump - automatyczne skakanie. */
public final class AutoJump extends Module {

    public AutoJump() {
        super("Auto Jump", "Automatycznie skacze", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (!Wrapper.player().isOnGround()) return;
        if (Wrapper.player().input.movementForward == 0 && Wrapper.player().input.movementSideways == 0) return;
        Wrapper.player().jump();
    }
}
