package com.ares.modules.render;

import com.ares.core.util.Wrapper;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;

/** View Clip - kamera przechodzi przez sciany (bez kolizji kamery). */
public final class ViewClip extends Module {

    private final BoolSetting thirdPerson = add(new BoolSetting("Third Person", "Wymuszaj trzecia osobe", true).group("General"));

    public ViewClip() {
        super("View Clip", "Kamera ignorujaca kolizje", ModuleCategory.RENDER);
    }

    public boolean thirdPerson() {
        return thirdPerson.get();
    }

    @Override
    public void onEnable() {
        if (thirdPerson.get()) applyPerspective(true);
    }

    @Override
    public void onDisable() {
        applyPerspective(false);
    }

    /**
     * Przelacza widok na trzecia osobe (i z powrotem przy wylaczeniu).
     * Szukamy stalej po metodach isFirstPerson()/isFrontView() - nazwy enumow
     * (FIRST_PERSON / THIRD_PERSON_BACK) nie sa pewne w mapowaniach 1.21.8.
     */
    private void applyPerspective(boolean third) {
        if (Wrapper.mc() == null || Wrapper.mc().options == null) return;
        for (net.minecraft.client.option.Perspective perspective : net.minecraft.client.option.Perspective.values()) {
            boolean firstPerson = perspective.isFirstPerson();
            boolean backView = !firstPerson && !perspective.isFrontView();
            if (third ? backView : firstPerson) {
                Wrapper.mc().options.setPerspective(perspective);
                return;
            }
        }
    }
}
