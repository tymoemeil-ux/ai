package com.ares.modules.render;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import net.minecraft.util.math.Vec3d;

/** Freecam - kamera niezalezna od gracza. */
public final class Freecam extends Module {

    private final FloatSetting speed = add(new FloatSetting("Speed", "Predkosc kamery", 1f, 0.1f, 10f).group("General"));

    private Vec3d position;

    public Freecam() {
        super("Freecam", "Swobodna kamera", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        if (Wrapper.nullCheck()) position = Wrapper.player().getPos();
    }

    public Vec3d position() {
        if (position == null && Wrapper.nullCheck()) position = Wrapper.player().getPos();
        return position;
    }

    public float speed() {
        return isEnabled() ? speed.get() : 0f;
    }

    public boolean active() {
        return isEnabled() && position != null;
    }

    public void update(Vec3d delta) {
        if (position == null) return;
        position = position.add(delta);
    }
}
