package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;

/** Timer - przyspiesza lub zwalnia ticki gry (singleplayer). */
public final class TimerModule extends Module {

    private final FloatSetting multiplier = add(new FloatSetting("Multiplier", "Mnoznik szybkosci tickow", 2f, 0.1f, 20f).group("General"));

    public TimerModule() {
        super("Timer", "Zmienia szybkosc tickow gry", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (com.ares.Ares.get().timerValue() != multiplier.get()) {
            com.ares.Ares.get().setTimerValue(multiplier.get());
        }
    }

    @Override
    public void onDisable() {
        com.ares.Ares.get().setTimerValue(1f);
    }

    @Override
    public String info() {
        return String.format("%.1fx", multiplier.get());
    }
}
