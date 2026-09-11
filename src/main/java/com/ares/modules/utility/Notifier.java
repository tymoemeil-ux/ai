package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.event.events.TotemPopEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;

/** Notifier - powiadomienia o totemach, smierciach i dolaczajacych graczach. */
public final class Notifier extends Module {

    private final BoolSetting totemPops = add(new BoolSetting("Totem Pops", "Powiadomienia o totemach", true).group("General"));
    private final BoolSetting joins = add(new BoolSetting("Joins", "Gracze dolaczajacy", true).group("General"));
    private final BoolSetting deaths = add(new BoolSetting("Deaths", "Smierci graczy", true).group("General"));

    private final Map<UUID, Float> lastHealth = new HashMap<>();

    public Notifier() {
        super("Notifier", "Powiadomienia o zdarzeniach", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;

        for (PlayerEntity player : com.ares.core.util.world.EntityUtil.otherPlayers()) {
            float health = player.getHealth();
            Float previous = lastHealth.get(player.getUuid());

            if (previous != null && health > previous + 5 && totemPops.get()) {
                com.ares.Ares.get().notifications().send("Totem", player.getName().getString() + " stracil totem!", 2500);
            }

            if (previous != null && health <= 0 && deaths.get()) {
                com.ares.Ares.get().notifications().send("Death", player.getName().getString() + " zginal!", 2500);
            }

            lastHealth.put(player.getUuid(), health);
        }
    }

    @EventHandler
    private void onTotemPop(TotemPopEvent event) {
        if (!totemPops.get() || !PlayerUtil.isAlive()) return;
        com.ares.Ares.get().notifications().send("Totem",
                event.player().getName().getString() + " popnol (" + event.pops() + ")", 2500);
    }
}
