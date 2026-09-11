package com.ares.core.util.world;

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

    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> getEntities(Class<T> type, double range) {
        List<T> result = new ArrayList<>();
        if (Wrapper.world() == null) return result;
        for (Entity entity : Wrapper.world().getEntities()) {
            if (entity == null || !type.isInstance(entity)) continue;
            result.add((T) entity);
        }
        return result;
    }

    public static List<PlayerEntity> players(double range) {
        List<PlayerEntity> result = new ArrayList<>();
        if (Wrapper.world() == null) return result;
        for (Entity entity : Wrapper.world().getEntities()) {
            if (entity instanceof PlayerEntity player) {
                result.add(player);
            }
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
        List<EndCrystalEntity> result = new ArrayList<>();
        if (Wrapper.world() == null) return result;
        for (Entity entity : Wrapper.world().getEntities()) {
            if (entity instanceof EndCrystalEntity crystal) result.add(crystal);
        }
        return result;
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
