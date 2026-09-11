package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import net.minecraft.util.math.Vec3d;

/** Speed - zwieksza predkosc poruszania sie. */
public final class Speed extends Module {

    public enum Mode { STRAFE, ON_GROUND, BHOP }

    private final ModeSetting<Mode> mode = add(new ModeSetting<>("Mode", "Tryb speeda", Mode.STRAFE).group("General"));
    private final FloatSetting speedValue = add(new FloatSetting("Speed", "Mnoznik predkosci", 1.6f, 0.1f, 10f).group("General"));
    private final BoolSetting onlyOnGround = add(new BoolSetting("Only On Ground", "Dzialaj tylko na ziemi", false).group("General"));
    private final BoolSetting sprintOnly = add(new BoolSetting("Sprint Only", "Tylko podczas sprintu", true).group("General"));
    private final BoolSetting noWater = add(new BoolSetting("Disable In Water", "Wylacz w wodzie", true).group("General"));

    public Speed() {
        super("Speed", "Zwieksza predkosc gracza", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (onlyOnGround.get() && !Wrapper.player().isOnGround()) return;
        if (sprintOnly.get() && !Wrapper.player().isSprinting()) return;
        if (noWater.get() && Wrapper.player().isTouchingWater()) return;

        Vec3d velocity = Wrapper.player().getVelocity();
        float yaw = Wrapper.player().getYaw() * ((float) Math.PI / 180);

        double forward = com.ares.core.util.player.PlayerUtil.forwardInput();
        double strafe = com.ares.core.util.player.PlayerUtil.strafeInput();

        if (forward == 0 && strafe == 0) return;

        double motionX = forward * Math.sin(yaw) * -1 + strafe * Math.cos(yaw);
        double motionZ = forward * Math.cos(yaw) - strafe * Math.sin(yaw);

        if (mode.get() == Mode.BHOP && Wrapper.player().isOnGround()) {
            Wrapper.player().setVelocity(velocity.x, 0.42, velocity.z);
        }

        double length = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (length == 0) return;

        motionX = motionX / length * speedValue.get() * 0.15;
        motionZ = motionZ / length * speedValue.get() * 0.15;

        Wrapper.player().setVelocity(new Vec3d(motionX * 1.6, Wrapper.player().getVelocity().y, motionZ * 1.6));
    }
}
