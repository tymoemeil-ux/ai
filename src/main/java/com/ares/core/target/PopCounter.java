package com.ares.core.target;

import com.ares.Ares;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.event.events.TotemPopEvent;
import com.ares.core.util.Wrapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

/** Licznik popnietych totemow (wykrywa pop po skoku HP + absorpcja). */
public final class PopCounter {

    private static final long EXPIRE_MS = 30_000L;

    private final Map<UUID, Integer> pops = new HashMap<>();
    private final Map<UUID, Float> lastHealth = new HashMap<>();
    private final Map<UUID, Long> lastPopTime = new HashMap<>();

    @EventHandler
    private void onTick(TickEvent event) {
        if (Wrapper.player() == null || Wrapper.world() == null) return;

        long now = System.currentTimeMillis();
        lastPopTime.entrySet().removeIf(entry -> now - entry.getValue() > EXPIRE_MS);
        pops.keySet().retainAll(lastPopTime.keySet());

        Set<UUID> seen = new HashSet<>();
        for (PlayerEntity player : Wrapper.world().getPlayers()) {
            if (player == null) continue;
            UUID id = player.getUuid();
            seen.add(id);

            float health = player.getHealth();
            Float prev = lastHealth.put(id, health);
            if (prev == null || player == Wrapper.player()) continue;

            if (player.hasStatusEffect(StatusEffects.ABSORPTION) && health > prev + 2.0f) {
                int count = pops.getOrDefault(id, 0) + 1;
                pops.put(id, count);
                lastPopTime.put(id, now);
                Ares.get().eventBus().post(new TotemPopEvent(player, count));
            }
        }
        lastHealth.keySet().retainAll(seen);
    }

    public int pops(UUID uuid) {
        Long time = lastPopTime.get(uuid);
        if (time == null || System.currentTimeMillis() - time > EXPIRE_MS) return 0;
        return pops.getOrDefault(uuid, 0);
    }

    public Map<UUID, Integer> all() {
        return pops;
    }

    public void reset(UUID uuid) {
        pops.remove(uuid);
        lastPopTime.remove(uuid);
        lastHealth.remove(uuid);
    }

    public void clear() {
        pops.clear();
        lastPopTime.clear();
        lastHealth.clear();
    }
}
