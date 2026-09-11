package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;

/**
 * Zoom - przyblizenie widoku.
 * Nie zapisujemy juz FOV do opcji Minecrafta (robil to mixin GameRendererMixin),
 * wiec mozna zejsc ponizej 30 bez spamu "Illegal option value".
 */
public final class Zoom extends Module {

    private final FloatSetting fov = add(new FloatSetting("FOV", "Docelowy FOV podczas zoomu", 15f, 5f, 70f).group("General"));
    private final FloatSetting smooth = add(new FloatSetting("Smooth", "Plynosc przejscia", 0.5f, 0.05f, 1f).group("General"));

    private float current = 0;

    public Zoom() {
        super("Zoom", "Przyblizenie widoku", ModuleCategory.RENDER);
    }

    public float fov() {
        return fov.get();
    }

    public float smooth() {
        return smooth.get();
    }

    public float current() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        float target = fov.get();
        if (current <= 1f) current = 70f; // start od domyslnego FOV
        current = (float) (current + (target - current) * smooth.get() * 0.35);
    }

    @Override
    public void onEnable() {
        current = 70f;
    }

    @Override
    public void onDisable() {
        current = 0;
    }

    @Override
    public String info() {
        return String.format("%.0f", fov.get());
    }
}
