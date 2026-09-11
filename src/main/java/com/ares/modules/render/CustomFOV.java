package com.ares.modules.render;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;

/**
 * Custom FOV - wlasne pole widzenia (dziala przez GameRendererMixin,
 * wiec mozna wyjsc poza zakres 30-110 opcji Minecrafta).
 */
public final class CustomFOV extends Module {

    private final FloatSetting fov = add(new FloatSetting("FOV", "Wartosc FOV", 110f, 30f, 180f).group("General"));
    private final FloatSetting itemFov = add(new FloatSetting("Item FOV", "FOV przedmiotu w rece", 70f, 30f, 110f).group("General"));

    public CustomFOV() {
        super("Custom FOV", "Wlasne pole widzenia", ModuleCategory.RENDER);
    }

    public float fov() {
        return fov.get();
    }

    public float itemFov() {
        return itemFov.get();
    }

    @Override
    public String info() {
        return String.format("%.0f", fov.get());
    }
}
