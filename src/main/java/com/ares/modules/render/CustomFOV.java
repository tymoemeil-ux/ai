package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.OptionUtil;
import com.ares.core.util.Wrapper;

/** Custom FOV - wlasne pole widzenia. */
public final class CustomFOV extends Module {

    private final FloatSetting fov = add(new FloatSetting("FOV", "Wartosc FOV", 110f, 30f, 180f).group("General"));
    private final FloatSetting itemFov = add(new FloatSetting("Item FOV", "FOV przedmiotu w rece", 70f, 30f, 110f).group("General"));

    public CustomFOV() {
        super("Custom FOV", "Wlasne pole widzenia", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        OptionUtil.setValue(Wrapper.mc().options.getFov(), fov.get());
    }

    @Override
    public void onDisable() {
        if (Wrapper.nullCheck()) OptionUtil.setValue(Wrapper.mc().options.getFov(), 70);
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
