package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import net.minecraft.util.math.Vec3d;

/** Jesus - chodzenie po wodzie / lawie. */
public final class Jesus extends Module {

    public enum Mode { SOLID, DOLPHIN }

    private final Mode mode = Mode.SOLID;
    private final FloatSetting height = add(new FloatSetting("Height", "Wysokosc nad woda", 0.9f, 0f, 2f).group("General"));
    private final BoolSetting lava = add(new BoolSetting("Lava", "Dzialaj tez na lawie", true).group("General"));
    private final BoolSetting swimUp = add(new BoolSetting("Swim Up", "Wynurzaj sie", true).group("General"));

    public Jesus() {
        super("Jesus", "Chodzenie po wodzie i lawie", ModuleCategory.MOVEMENT);
        add(new com.ares.core.setting.ModeSetting<>("Mode", "Tryb", Mode.SOLID).group("General"));
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        boolean inLiquid = Wrapper.player().isTouchingWater() || (lava.get() && Wrapper.player().isInLava());
        if (!inLiquid) return;

        Vec3d velocity = Wrapper.player().getVelocity();
        if (Wrapper.mc().options.jumpKey.isPressed() && swimUp.get()) {
            Wrapper.player().setVelocity(new Vec3d(velocity.x, 0.3, velocity.z));
            return;
        }

        Wrapper.player().setVelocity(new Vec3d(velocity.x, 0.11, velocity.z));
        Wrapper.player().setOnGround(true);
    }

    private enum ModeHolder {
        // miejsce na przyszle tryby
    }
}
