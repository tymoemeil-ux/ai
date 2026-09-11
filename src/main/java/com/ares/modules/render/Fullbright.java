package com.ares.modules.render;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.util.OptionUtil;
import com.ares.core.util.Wrapper;

/** Fullbright - pelna jasnosc bez pochodni. */
public final class Fullbright extends Module {

    private final FloatSetting level = add(new FloatSetting("Level", "Poziom jasnosci", 15f, 1f, 15f).group("General"));

    private double previous;

    public Fullbright() {
        super("Fullbright", "Stala pelna jasnosc", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        previous = com.ares.core.util.OptionUtil.getValue(Wrapper.mc().options.getGamma());
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        double gamma = Math.min(1.0, level.get() / 15.0);
        // ustawiamy tylko gdy wartosc sie zmieni - bez zbednego pisania co tick
        if (Math.abs(OptionUtil.getValue(Wrapper.mc().options.getGamma()) - gamma) > 0.001) {
            OptionUtil.setValue(Wrapper.mc().options.getGamma(), gamma);
        }
    }

    @Override
    public void onDisable() {
        if (Wrapper.mc().options != null) {
            OptionUtil.setValue(Wrapper.mc().options.getGamma(), previous);
        }
    }

    public float level() {
        return level.get();
    }
}
