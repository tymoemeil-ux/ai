package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/** Elytra Fly - sterowanie lotem na elytrze. */
public final class ElytraFly extends Module {

    private final FloatSetting speed = add(new FloatSetting("Speed", "Predkosc", 1.8f, 0.1f, 10f).group("General"));
    private final BoolSetting autoStart = add(new BoolSetting("Auto Start", "Sam startuj przy skoku", true).group("General"));
    private final BoolSetting infinite = add(new BoolSetting("Infinite", "Bez konca utraty wytrzymalosci", true).group("General"));

    public ElytraFly() {
        super("Elytra Fly", "Lepsze sterowanie elytra", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (Wrapper.player().getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return;

        if (!Wrapper.player().isGliding()) {
            if (autoStart.get() && !Wrapper.player().isOnGround() && Wrapper.player().getVelocity().y < -0.1) {
                Wrapper.mc().options.jumpKey.setPressed(true);
            }
            return;
        }

        if (infinite.get()) {
            net.minecraft.item.ItemStack elytra = Wrapper.player().getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
            elytra.setDamage(0);
        }

        Vec3d velocity = Wrapper.player().getVelocity();
        float yaw = Wrapper.player().getYaw() * ((float) Math.PI / 180);
        double forward = Wrapper.player().input.movementForward;

        double motionX = forward * Math.sin(yaw) * -1;
        double motionZ = forward * Math.cos(yaw);

        double y = velocity.y;
        if (Wrapper.mc().options.jumpKey.isPressed()) y = speed.get() * 0.5;
        else if (Wrapper.mc().options.sneakKey.isPressed()) y = -speed.get() * 0.5;

        Wrapper.player().setVelocity(new Vec3d(motionX * speed.get() * 0.2, y, motionZ * speed.get() * 0.2));
    }
}
