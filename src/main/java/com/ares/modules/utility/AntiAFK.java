package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;

/** Anti AFK - zapobiega wyrzuceniu za bezczynnosc. */
public final class AntiAFK extends Module {

    private final IntSetting interval = add(new IntSetting("Interval", "Co ile sekund", 30, 3, 300).group("General"));
    private final BoolSetting jump = add(new BoolSetting("Jump", "Podskakuj", true).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Krec sie", true).group("General"));
    private final BoolSetting sneak = add(new BoolSetting("Sneak", "Kucaj", false).group("General"));

    private final TickTimer timer = new TickTimer();

    public AntiAFK() {
        super("Anti AFK", "Zapobiega wylogowaniu za bezczynnosc", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (timer.elapsed() < interval.get() * 1000L) return;

        if (jump.get() && Wrapper.player().isOnGround()) Wrapper.player().jump();
        if (rotate.get()) {
            Wrapper.player().setYaw(Wrapper.player().getYaw() + (float) (Math.random() * 60 - 30));
        }
        if (sneak.get()) Wrapper.player().setSneaking(true);

        timer.reset();
    }

    @Override
    public void onDisable() {
        if (Wrapper.nullCheck()) Wrapper.player().setSneaking(false);
    }
}
