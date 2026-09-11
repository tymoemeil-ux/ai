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
        if (!com.ares.core.util.player.PlayerUtil.isMoving()) return;
        Wrapper.player().jump();
    }
}
