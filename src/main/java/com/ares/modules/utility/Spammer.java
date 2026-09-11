package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.setting.StringSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;

/** Spammer - wysyla wiadomosci w petli. */
public final class Spammer extends Module {

    private final StringSetting message = add(new StringSetting("Message", "Wiadomosc", "Ares CrystalPvP on top!").group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Co ile sekund", 30, 3, 600).group("General"));
    private final BoolSetting randomize = add(new BoolSetting("Randomize", "Dodawaj losowy numer", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public Spammer() {
        super("Spammer", "Wysyla wiadomosci co okreslony czas", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(Math.max(1, delay.get()) * 20)) return;

        String text = message.get();
        if (randomize.get()) text += " " + (int) (Math.random() * 10000);
        PlayerUtil.sendMessage(text);
        timer.reset();
    }
}
