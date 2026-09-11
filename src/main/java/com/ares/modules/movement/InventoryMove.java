package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;

/** Inventory Move - pozwala sie ruszac z otwartym ekwipunkiem. */
public final class InventoryMove extends Module {

    public InventoryMove() {
        super("Inventory Move", "Ruch z otwartym ekwipunkiem", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (Wrapper.mc().currentScreen == null) return;
        if (!(Wrapper.mc().currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) return;

        float forward = 0;
        float sideways = 0;
        if (Wrapper.mc().options.forwardKey.isPressed()) forward += 1;
        if (Wrapper.mc().options.backKey.isPressed()) forward -= 1;
        if (Wrapper.mc().options.leftKey.isPressed()) sideways += 1;
        if (Wrapper.mc().options.rightKey.isPressed()) sideways -= 1;

        com.ares.core.util.player.PlayerUtil.applyMovement(forward, sideways, 0.24);

        if (Wrapper.mc().options.jumpKey.isPressed() && Wrapper.player().isOnGround()) {
            Wrapper.player().jump();
        }
    }
}
