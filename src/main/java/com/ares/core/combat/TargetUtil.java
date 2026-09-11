package com.ares.core.combat;

import com.ares.core.util.Wrapper;
import com.ares.core.util.world.EntityUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/** Wybor celu dla modulow bojowych. */
public final class TargetUtil {

    public enum SortMode { CLOSEST, FURTHEST, LOWEST_HEALTH, HIGHEST_HEALTH, CROSSHAIR, LOWEST_ARMOR }

    public enum Filter { PLAYERS, MOBS, ALL }

    private TargetUtil() {
    }

    /** Zwraca liste wszystkich potencjalnych celow w zasiegu. */
    public static List<LivingEntity> candidates(double range, Filter filter) {
        List<LivingEntity> list = new ArrayList<>();
        ClientPlayerEntity player = Wrapper.player();
        if (player == null) return list;
        if (Wrapper.world() == null) return list;

        for (EntityUtil.Vec3dSupplier ignored : new EntityUtil.Vec3dSupplier[0]) {
            // brak operacji - zachowane dla kompatybilnosci
        }

        for (net.minecraft.entity.Entity entity : Wrapper.world().getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == player) continue;
            if (!living.isAlive() || living.isDead()) continue;
            if (living.isSpectator()) continue;
            if (filter == Filter.PLAYERS && !(living instanceof PlayerEntity)) continue;
            if (filter == Filter.MOBS && living instanceof PlayerEntity) continue;
            if (living.squaredDistanceTo(player) > range * range) continue;
            list.add(living);
        }
        return list;
    }

    /** Zwraca najlepszy cel wg trybu sortowania. */
    public static LivingEntity best(double range, SortMode mode, Filter filter, boolean ignoreFriends,
                                    com.ares.core.friend.FriendManager friends) {
        List<LivingEntity> candidates = candidates(range, filter);
        if (candidates.isEmpty()) return null;

        ClientPlayerEntity player = Wrapper.player();
        candidates.removeIf(entity -> {
            if (entity instanceof PlayerEntity p && ignoreFriends && friends != null && friends.isFriend(p)) return true;
            return false;
        });

        if (candidates.isEmpty()) return null;

        Comparator<LivingEntity> comparator = switch (mode) {
            case FURTHEST -> Comparator.comparingDouble((LivingEntity e) -> e.squaredDistanceTo(player)).reversed();
            case LOWEST_HEALTH -> Comparator.comparingDouble(LivingEntity::getHealth);
            case HIGHEST_HEALTH -> Comparator.comparingDouble(LivingEntity::getHealth).reversed();
            case LOWEST_ARMOR -> Comparator.comparingInt(LivingEntity::getArmor);
            case CROSSHAIR -> Comparator.comparingDouble(TargetUtil::angleToCrosshair);
            case CLOSEST -> Comparator.comparingDouble(e -> e.squaredDistanceTo(player));
        };
        candidates.sort(comparator);
        return candidates.get(0);
    }

    private static double angleToCrosshair(LivingEntity entity) {
        com.ares.core.rotation.Rotation to = com.ares.core.rotation.RotationUtil.toEntity(entity, false);
        com.ares.core.rotation.Rotation current = new com.ares.core.rotation.Rotation(
                Wrapper.player().getYaw(), Wrapper.player().getPitch());
        return com.ares.core.rotation.RotationUtil.angleDifference(to, current);
    }

    /** Cel w zasiegu, ktory jest w dziurze (do faceplace / city). */
    public static LivingEntity bestInHole(double range, com.ares.core.friend.FriendManager friends) {
        for (LivingEntity entity : candidates(range, Filter.PLAYERS)) {
            if (entity instanceof PlayerEntity p && friends != null && friends.isFriend(p)) continue;
            if (HoleUtil.isInHole(entity)) return entity;
        }
        return null;
    }
}
