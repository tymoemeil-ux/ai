package com.ares.core.target;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.util.Wrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;

/** Licznik "popnietych" totemow dla kazdego gracza. */
public final class PopCounter {

    private final Map<UUID, Integer> pops = new HashMap<>();
    private final Map<UUID, Float> lastHealth = new HashMap<>();

    public PopCounter() {
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;

        for (PlayerEntity player : com.ares.core.util.world.EntityUtil.otherPlayers()) {
            float health = player.getHealth();
            Float previous = lastHealth.get(player.getUuid());

            if (previous != null && health <= 0 && previous > 0) {
                // gracz zginal - reset licznika
                pops.put(player.getUuid(), 0);
            } else if (previous != null && health > previous + 4) {
                int count = pops.getOrDefault(player.getUuid(), 0) + 1;
                pops.put(player.getUuid(), count);
                Wrapper.mc().execute(() -> com.ares.Ares.get().eventBus().post(
                        new com.ares.core.event.events.TotemPopEvent(player, count)));
            }

            lastHealth.put(player.getUuid(), health);
        }
    }

    public int pops(UUID uuid) {
        return pops.getOrDefault(uuid, 0);
    }

    public void reset() {
        pops.clear();
        lastHealth.clear();
    }
}
