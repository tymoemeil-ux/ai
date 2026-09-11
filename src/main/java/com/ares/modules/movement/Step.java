package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;

/** Step - wchodzenie na wyzsze bloki bez skoku. */
public final class Step extends Module {

    private final FloatSetting height = add(new FloatSetting("Height", "Wysokosc stopnia", 1.5f, 0.5f, 10f).group("General"));

    public Step() {
        super("Step", "Wchodzenie na bloki bez skakania", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (!Wrapper.player().isOnGround()) return;
        if (Wrapper.player().input.movementForward == 0 && Wrapper.player().input.movementSideways == 0) return;

        // 1.21.8 nie udostepnia setStepHeight - podnosimy gracza predkoscia
        if (Wrapper.player().horizontalCollision) {
            Wrapper.player().setVelocity(Wrapper.player().getVelocity().x,
                    Math.max(0.42, height.get() * 0.35), Wrapper.player().getVelocity().z);
            Wrapper.player().setOnGround(true);
        }
    }

    @Override
    public void onDisable() {
        // brak stanu do przywrocenia
    }
}
