package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;

/** Time Changer - zmienia pore dnia klienta. */
public final class TimeChanger extends Module {

    private final IntSetting time = add(new IntSetting("Time", "Godzina dnia (0-24000)", 12000, 0, 24000).group("General"));
    private final BoolSetting smooth = add(new BoolSetting("Smooth", "Plynne przejscie", true).group("General"));

    public TimeChanger() {
        super("Time Changer", "Zmienia pore dnia", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        long value = time.get();
        setTimeOfDay(Wrapper.world(), value);
    }

    public int time() {
        return time.get();
    }

    public boolean smooth() {
        return smooth.get();
    }

    @Override
    public String info() {
        return String.valueOf(time.get());
    }

    /**
     * ClientWorld.setTimeOfDay(long) nie jest publiczne, wiec wolamy je po nazwie.
     * Probujemy nazwy mapowanej i obfuskowanej (dziala tez w buildzie produkcyjnym).
     */
    private static void setTimeOfDay(Object world, long value) {
        if (world == null) return;
        for (String name : new String[]{"setTimeOfDay", "method_165"}) {
            try {
                java.lang.reflect.Method method = world.getClass().getDeclaredMethod(name, long.class);
                method.setAccessible(true);
                method.invoke(world, value);
                return;
            } catch (Exception ignored) {
                // probujemy kolejna nazwe
            }
        }
    }
}
