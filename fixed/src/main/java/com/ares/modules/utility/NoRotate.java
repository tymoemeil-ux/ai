package com.ares.modules.utility;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;

/** No Rotate - serwer nie wymusza obrotu gracza. */
public final class NoRotate extends Module {

    private final BoolSetting keepYaw = add(new BoolSetting("Keep Yaw", "Zachowuj yaw", true).group("General"));
    private final BoolSetting keepPitch = add(new BoolSetting("Keep Pitch", "Zachowuj pitch", true).group("General"));

    private float serverYaw;
    private float serverPitch;

    public NoRotate() {
        super("No Rotate", "Blokuje wymuszane obroty", ModuleCategory.UTILITY);
    }

    public void update() {
        if (!Wrapper.nullCheck()) return;
        if (keepYaw.get()) serverYaw = Wrapper.player().getYaw();
        if (keepPitch.get()) serverPitch = Wrapper.player().getPitch();
    }

    public void restore() {
        if (!Wrapper.nullCheck()) return;
        if (keepYaw.get()) Wrapper.player().setYaw(serverYaw);
        if (keepPitch.get()) Wrapper.player().setPitch(serverPitch);
    }

    public boolean active() {
        return isEnabled();
    }
}
