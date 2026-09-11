package com.ares.modules.render;

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
}
