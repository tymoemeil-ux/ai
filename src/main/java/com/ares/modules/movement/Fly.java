package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import net.minecraft.util.math.Vec3d;

/** Fly - latanie (tryb vanilla abilities / kreatywny). */
public final class Fly extends Module {

    public enum Mode { VANILLA, CREATIVE, GLIDE }

    private final ModeSetting<Mode> mode = add(new ModeSetting<>("Mode", "Tryb latania", Mode.VANILLA).group("General"));
    private final FloatSetting speed = add(new FloatSetting("Speed", "Predkosc latania", 1.2f, 0.1f, 10f).group("General"));
    private final FloatSetting verticalSpeed = add(new FloatSetting("Vertical Speed", "Predkosc pionowa", 0.8f, 0.1f, 5f).group("General"));

    public Fly() {
        super("Fly", "Latanie", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (!Wrapper.nullCheck()) return;
        if (mode.get() == Mode.CREATIVE || mode.get() == Mode.VANILLA) {
            Wrapper.player().getAbilities().allowFlying = true;
        }
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        boolean up = Wrapper.mc().options.jumpKey.isPressed();
        boolean down = Wrapper.mc().options.sneakKey.isPressed();

        switch (mode.get()) {
            case CREATIVE -> {
                Wrapper.player().getAbilities().flying = true;
                Wrapper.player().getAbilities().setFlySpeed(speed.get() / 10f);
            }
            case VANILLA -> {
                Wrapper.player().getAbilities().flying = true;
                Wrapper.player().getAbilities().setFlySpeed(speed.get() / 10f);
                Vec3d velocity = Wrapper.player().getVelocity();
                double y = 0;
                if (up) y = verticalSpeed.get();
                if (down) y = -verticalSpeed.get();
                if (up || down) Wrapper.player().setVelocity(velocity.x, y, velocity.z);
            }
            case GLIDE -> {
                Vec3d velocity = Wrapper.player().getVelocity();
                if (velocity.y < -0.1) {
                    Wrapper.player().setVelocity(new Vec3d(velocity.x, -0.1, velocity.z));
                }
                if (up) Wrapper.player().setVelocity(velocity.x, verticalSpeed.get(), velocity.z);
            }
        }
    }

    @Override
    public void onDisable() {
        if (!Wrapper.nullCheck()) return;
        net.minecraft.entity.player.PlayerAbilities abilities = Wrapper.player().getAbilities();
        abilities.flying = false;
        abilities.allowFlying = Wrapper.player().isCreative();
        abilities.setFlySpeed(0.05f);
    }
}
