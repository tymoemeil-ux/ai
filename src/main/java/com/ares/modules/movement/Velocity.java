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

/** Velocity - redukcja odrzutu (knockback). */
public final class Velocity extends Module {

    private final FloatSetting horizontal = add(new FloatSetting("Horizontal", "Redukcja pozioma (%)", 100f, 0f, 100f).group("General"));
    private final FloatSetting vertical = add(new FloatSetting("Vertical", "Redukcja pionowa (%)", 100f, 0f, 100f).group("General"));
    private final BoolSetting onlyWhenTargeted = add(new BoolSetting("Only When Targeted", "Tylko gdy jest cel", false).group("General"));

    private Vec3d lastVelocity = Vec3d.ZERO;

    public Velocity() {
        super("Velocity", "Kontrola odrzutu", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        Vec3d velocity = Wrapper.player().getVelocity();
        Vec3d previous = lastVelocity;
        lastVelocity = velocity;

        if (previous.lengthSquared() == 0) return;

        double dx = velocity.x - previous.x;
        double dy = velocity.y - previous.y;
        double dz = velocity.z - previous.z;
        double delta = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
        if (delta < 0.05) return;

        double hFactor = 1 - horizontal.get() / 100.0;
        double vFactor = 1 - vertical.get() / 100.0;

        double newX = previous.x + dx * hFactor;
        double newY = previous.y + dy * vFactor;
        double newZ = previous.z + dz * hFactor;

        Wrapper.player().setVelocity(new Vec3d(newX, newY, newZ));
    }

    @Override
    public void onDisable() {
        lastVelocity = Vec3d.ZERO;
    }
}
