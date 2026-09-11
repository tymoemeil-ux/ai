package com.ares.core.combat;

import com.ares.core.util.world.BlockUtil;
import com.ares.core.util.Wrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

/** Logika anchorow: stawianie, ladowanie glowstonem i detonacja. */
public final class AnchorUtil {

    private AnchorUtil() {
    }

    /** Czy anchor moze w tym miejscu wybuchnac (swiat nie moze byc Netherem). */
    public static boolean canExplodeHere() {
        World world = Wrapper.world();
        if (world == null) return false;
        return !RespawnAnchorBlock.isNether(world);
    }

    /** Liczba ladunkow anchora (0-4). */
    public static int charges(BlockPos pos) {
        BlockState state = BlockUtil.state(pos);
        if (!state.isOf(Blocks.RESPAWN_ANCHOR)) return -1;
        return state.get(RespawnAnchorBlock.CHARGES);
    }

    public static boolean isAnchor(BlockPos pos) {
        return BlockUtil.state(pos).isOf(Blocks.RESPAWN_ANCHOR);
    }

    /** Czy mozna tu postawic anchora. */
    public static boolean canPlace(BlockPos pos) {
        return BlockUtil.canPlaceBlock(pos);
    }

    /** Czy anchor jest w pelni naladowany. */
    public static boolean isCharged(BlockPos pos) {
        int charges = charges(pos);
        return charges >= RespawnAnchorBlock.MAX_CHARGES;
    }

    public static boolean canCharge(BlockPos pos) {
        BlockState state = BlockUtil.state(pos);
        if (!state.isOf(Blocks.RESPAWN_ANCHOR)) return false;
        return RespawnAnchorBlock.canCharge(state);
    }

    /** Miejsca wokol celu gdzie warto postawic anchor. */
    public static List<BlockPos> placementsAround(BlockPos targetPos, double range, int maxHeight) {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= maxHeight; y++) {
                    BlockPos pos = targetPos.add(x, y, z);
                    if (!canPlace(pos)) continue;
                    if (BlockUtil.distanceToEyes(pos) > range) continue;
                    positions.add(pos);
                }
            }
        }
        return positions;
    }

    public static boolean holdingGlowstone() {
        return com.ares.core.util.player.InventoryUtil.mainHand().getItem() == Items.GLOWSTONE
                || com.ares.core.util.player.InventoryUtil.offhand().getItem() == Items.GLOWSTONE;
    }

    /** HitResult do klikniecia anchora. */
    public static BlockHitResult hitResultFor(BlockPos pos) {
        Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return new BlockHitResult(center, Direction.UP, pos, false);
    }

    /** Obrazenia wybuchu anchora dla celu. */
    public static float damage(net.minecraft.entity.Entity target, BlockPos pos) {
        return DamageUtil.anchor(target, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }
}
