package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;

/** No Weather - wylacza deszcz i snieg. */
public final class NoWeather extends Module {

    private final BoolSetting clearSky = add(new BoolSetting("Clear Sky", "Czyste niebo", true).group("General"));
    private final BoolSetting noThunder = add(new BoolSetting("No Thunder", "Bez burzy", true).group("General"));

    public NoWeather() {
        super("No Weather", "Kontrola pogody", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        if (clearSky.get() && Wrapper.world().isRaining()) {
            Wrapper.world().setRainGradient(0);
        }
        if (noThunder.get() && Wrapper.world().isThundering()) {
            Wrapper.world().setThunderGradient(0);
        }
    }
}
