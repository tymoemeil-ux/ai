package com.ares.core.util.world;

import com.ares.Ares;
import com.ares.core.util.Wrapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

/** Wyszukiwanie i filtrowanie bytow w swiecie. */
public final class EntityUtil {

    private EntityUtil() {
    }

    private static int cachedTick = Integer.MIN_VALUE;
    private static List<Entity> cachedAll = new ArrayList<>();
    private static List<PlayerEntity> cachedPlayers = new ArrayList<>();
    private static List<EndCrystalEntity> cachedCrystals = new ArrayList<>();

    /**
     * Skanujemy swiat TYLKO RAZ NA TICK i trzymamy wynik w cache.
     * Wczesniej kazdy modul (ESP, Radar, Target, Crystal, Surround...) robil wlasny
     * pelny przejazd po wszystkich bytach - po kilkanascie razy na tick = lagi.
     */
    private static void refresh() {
        int tick = Ares.get().ticks();
        if (tick == cachedTick) return;
        cachedTick = tick;

        List<Entity> all = new ArrayList<>();
        List<PlayerEntity> players = new ArrayList<>();
        List<EndCrystalEntity> crystals = new ArrayList<>();
        if (Wrapper.world() != null) {
            for (Entity entity : Wrapper.world().getEntities()) {
                if (entity == null) continue;
                all.add(entity);
                if (entity instanceof EndCrystalEntity crystal) crystals.add(crystal);
                else if (entity instanceof PlayerEntity player) players.add(player);
            }
        }
        cachedAll = all;
        cachedPlayers = players;
        cachedCrystals = crystals;
    }

    @SuppressWarnings("unchecked")
    /**
     * Wszystkie byty w swiecie (Z CACHE - odswiezane raz na tick).
     * Uzywaj zamiast Wrapper.world().getEntities() - tamto skanuje swiat za kazdym razem,
     * co przy modulach renderujacych (60 razy na sekunde) wycinalo klatki.
     * Lista jest tylko do odczytu.
     */
    public static List<Entity> all() {
        refresh();
        return cachedAll;
    }

    public static <T extends Entity> List<T> getEntities(Class<T> type, double range) {
        refresh();
        List<T> result = new ArrayList<>();
        net.minecraft.client.network.ClientPlayerEntity self = Wrapper.player();
        for (Entity entity : cachedAll) {
            if (!type.isInstance(entity)) continue;
            if (range > 0 && self != null && entity.squaredDistanceTo(self) > range * range) continue;
            result.add((T) entity);
        }
        return result;
    }

    public static List<PlayerEntity> players(double range) {
        refresh();
        List<PlayerEntity> result = new ArrayList<>();
        net.minecraft.client.network.ClientPlayerEntity self = Wrapper.player();
        for (PlayerEntity player : cachedPlayers) {
            if (range > 0 && self != null && player.squaredDistanceTo(self) > range * range) continue;
            result.add(player);
        }
        return result;
    }

    /** Wszyscy gracze oprocz lokalnego. */
    public static List<PlayerEntity> otherPlayers() {
        List<PlayerEntity> result = new ArrayList<>();
        for (PlayerEntity player : players(0)) {
            if (player == Wrapper.player()) continue;
            result.add(player);
        }
        return result;
    }

    public static List<EndCrystalEntity> crystals() {
        refresh();
        return new ArrayList<>(cachedCrystals);
    }

    public static List<EndCrystalEntity> crystalsInRange(Vec3dSupplier pos, double range) {
        List<EndCrystalEntity> result = new ArrayList<>();
        for (EndCrystalEntity crystal : crystals()) {
            if (pos.get().squaredDistanceTo(crystal.getPos()) <= range * range) result.add(crystal);
        }
        return result;
    }

    /** Byty zywe w zasiegu, posortowane wg odleglosci. */
    public static List<LivingEntity> livingInRange(double range) {
        List<LivingEntity> result = new ArrayList<>();
        for (PlayerEntity player : players(range)) {
            if (player == Wrapper.player()) continue;
            if (player.squaredDistanceTo(Wrapper.player()) > range * range) continue;
            result.add(player);
        }
        result.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(Wrapper.player())));
        return result;
    }

    public static double distanceTo(Entity entity) {
        return Wrapper.player() == null ? Double.MAX_VALUE : Wrapper.player().distanceTo(entity);
    }

    public static boolean isValid(Entity entity, double range) {
        if (entity == null || entity.isRemoved()) return false;
        if (entity == Wrapper.player()) return false;
        if (!entity.isAlive()) return false;
        if (range > 0 && distanceTo(entity) > range) return false;
        return true;
    }

    public interface Vec3dSupplier {
        net.minecraft.util.math.Vec3d get();
    }
}
