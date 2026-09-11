package com.ares.modules.render;

import com.ares.Ares;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.render.RenderUtil3D;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/** Tracers - linie do bytow. */
public final class Tracers extends Module {

    private final BoolSetting players = add(new BoolSetting("Players", "Gracze", true).group("Targets"));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", "Moby", false).group("Targets"));
    private final ColorSetting color = add(new ColorSetting("Color", "Kolor linii", 0xFF7C5CFF).group("General"));
    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg", 128f, 8f, 256f).group("General"));

    public Tracers() {
        super("Tracers", "Linie prowadzace do bytow", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;

        Vec3d start = new Vec3d(0, Wrapper.player().getStandingEyeHeight(), 0);

        for (net.minecraft.entity.Entity entity : Wrapper.world().getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == Wrapper.player() || !living.isAlive()) continue;
            if (living.squaredDistanceTo(Wrapper.player()) > range.get() * range.get()) continue;

            boolean isPlayer = living instanceof PlayerEntity;
            if (isPlayer && !players.get()) continue;
            if (!isPlayer && !mobs.get()) continue;
            if (isPlayer && Ares.get().friends().isFriend((PlayerEntity) living)) continue;

            Vec3d end = living.getPos().add(0, living.getBoundingBox().getLengthY() * 0.5, 0)
                    .subtract(event.cameraPos());
            RenderUtil3D.drawLine(event.matrices(), event.consumers(), start, end, color.get());
        }
    }
}
