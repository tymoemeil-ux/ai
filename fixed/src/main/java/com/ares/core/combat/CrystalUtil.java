package com.ares.core.combat;

import com.ares.core.util.world.BlockUtil;
import com.ares.core.util.Wrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

/** Wyszukiwanie pozycji pod krysztaly i najlepszych krysztalow do zniszczenia. */
public final class CrystalUtil {

    private CrystalUtil() {
    }

    /** Wszystkie pozycje wokol celu, na ktorych da sie postawic krysztal. */
    public static List<BlockPos> possiblePlacements(BlockPos center, double range) {
        List<BlockPos> positions = new ArrayList<>();
        int r = (int) Math.ceil(range);
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (!BlockUtil.canPlaceCrystal(pos)) continue;
                    positions.add(pos);
                }
            }
        }
        return positions;
    }

    /** Bazowe offsety stawiania krysztalu wokol pozycji celu. */
    public static final BlockPos[] CRYSTAL_OFFSETS = {
            new BlockPos(1, -1, 0), new BlockPos(-1, -1, 0), new BlockPos(0, -1, 1), new BlockPos(0, -1, -1),
            new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(0, 0, 0),
            new BlockPos(1, 1, 0), new BlockPos(-1, 1, 0), new BlockPos(0, 1, 1), new BlockPos(0, 1, -1),
            new BlockPos(2, -1, 0), new BlockPos(-2, -1, 0), new BlockPos(0, -1, 2), new BlockPos(0, -1, -2),
            new BlockPos(1, -1, 1), new BlockPos(1, -1, -1), new BlockPos(-1, -1, 1), new BlockPos(-1, -1, -1)
    };

    /** Pozycje wokol celu uzywane przez AutoCrystal (wzgledem pozycji stop celu). */
    public static List<BlockPos> placementsAround(BlockPos targetPos, double range, int height) {
        List<BlockPos> positions = new ArrayList<>();
        for (BlockPos offset : CRYSTAL_OFFSETS) {
            BlockPos pos = targetPos.add(offset);
            if (BlockUtil.distanceToEyes(pos) > range) continue;
            if (!BlockUtil.canPlaceCrystal(pos)) continue;
            positions.add(pos);
        }
        return positions;
    }

    /** Wszystkie krysztaly w zasiegu od punktu. */
    public static List<EndCrystalEntity> crystalsInRange(Vec3d from, double range) {
        List<EndCrystalEntity> result = new ArrayList<>();
        if (Wrapper.world() == null) return result;
        for (EndCrystalEntity crystal : com.ares.core.util.world.EntityUtil.crystals()) {
            if (crystal.squaredDistanceTo(from) <= range * range) result.add(crystal);
        }
        return result;
    }

    /** Krysztal zadajacy najwieksze obrazenia celowi. */
    public static EndCrystalEntity bestCrystal(List<EndCrystalEntity> crystals, net.minecraft.entity.LivingEntity target,
                                                float minDamage, double range) {
        EndCrystalEntity best = null;
        float bestDamage = 0;
        for (EndCrystalEntity crystal : crystals) {
            if (crystal == null || !crystal.isAlive()) continue;
            if (crystal.squaredDistanceTo(Wrapper.player().getEyePos()) > range * range) continue;
            float damage = DamageUtil.crystal(target, crystal.getPos());
            if (damage < minDamage) continue;
            if (damage > bestDamage) {
                bestDamage = damage;
                best = crystal;
            }
        }
        return best;
    }

    /** Pozycja krysztalu (dla renderu) na podstawie encji. */
    public static BlockPos crystalBlockPos(EndCrystalEntity crystal) {
        return BlockPos.ofFloored(crystal.getPos().add(0, -0.5, 0));
    }

    public static Box crystalBox(EndCrystalEntity crystal) {
        return crystal.getBoundingBox();
    }

    /** Czy krysztal kolidowalby z graczem przy stawianiu. */
    public static boolean intersectsPlayer(BlockPos pos) {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player == null) return false;
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        return box.intersects(player.getBoundingBox());
    }

    /** Czy na danej pozycji stoi juz krysztal (po encjach). */
    public static boolean isCrystalAt(BlockPos pos) {
        return BlockUtil.isCrystalAt(pos);
    }

    /** HitResult potrzebny do postawienia krysztalu. */
    public static BlockHitResult hitResultFor(BlockPos pos) {
        Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        return new BlockHitResult(center, Direction.UP, pos.down(), false);
    }

    public static boolean isBaseBlock(BlockPos pos) {
        net.minecraft.block.Block block = BlockUtil.block(pos);
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
    }
}
