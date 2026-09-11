package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.render.RenderUtil3D;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Vec3d;

/** Breadcrumbs - slad za graczem. */
public final class Breadcrumbs extends Module {

    private final IntSetting maxPoints = add(new IntSetting("Max Points", "Maksymalna liczba punktow", 500, 10, 5000).group("General"));
    private final IntSetting interval = add(new IntSetting("Interval", "Co ile tickow dodawac punkt", 2, 1, 20).group("General"));
    private final ColorSetting color = add(new ColorSetting("Color", "Kolor sladu", 0xFF7C5CFF).group("General"));

    private final List<Vec3d> points = new ArrayList<>();
    private int ticks;

    public Breadcrumbs() {
        super("Breadcrumbs", "Rysuje slad za graczem", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        if (ticks++ % interval.get() != 0) return;

        points.add(Wrapper.player().getPos());
        if (points.size() > maxPoints.get()) points.remove(0);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (points.size() < 2) return;

        for (int i = 1; i < points.size(); i++) {
            Vec3d from = points.get(i - 1).subtract(event.cameraPos());
            Vec3d to = points.get(i).subtract(event.cameraPos());
            RenderUtil3D.drawLine(event.matrices(), event.consumers(), from, to, color.get());
        }
    }

    @Override
    public void onDisable() {
        points.clear();
        ticks = 0;
    }
}
