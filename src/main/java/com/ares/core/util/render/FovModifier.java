package com.ares.core.util.render;

import com.ares.Ares;
import com.ares.modules.render.CustomFOV;
import com.ares.modules.render.Zoom;

/** Liczy docelowe FOV (Zoom + Custom FOV) bez grzebania w opcjach Minecrafta. */
public final class FovModifier {

    private FovModifier() {
    }

    public static boolean active() {
        try {
            CustomFOV custom = Ares.get().modules().get(CustomFOV.class);
            if (custom != null && custom.isEnabled()) return true;
            Zoom zoom = Ares.get().modules().get(Zoom.class);
            return zoom != null && zoom.isEnabled() && zoom.current() > 1f;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static float apply(float base) {
        try {
            CustomFOV custom = Ares.get().modules().get(CustomFOV.class);
            if (custom != null && custom.isEnabled()) {
                base = custom.fov();
            }
            Zoom zoom = Ares.get().modules().get(Zoom.class);
            if (zoom != null && zoom.isEnabled()) {
                float current = zoom.current();
                if (current > 1f) base = current;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return Math.max(1f, Math.min(360f, base));
    }
}
