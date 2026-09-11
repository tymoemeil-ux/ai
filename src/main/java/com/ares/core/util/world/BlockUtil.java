package com.ares.core.util.world;

import com.ares.core.util.Wrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import com.ares.core.util.world.EntityUtil;
import net.minecraft.world.World;

/** Pomocnicze operacje na blokach: podstawa pod krysztal, raytracing, stawianie. */
public final class BlockUtil {

    private BlockUtil() {
    }

    public static boolean isAir(BlockPos pos) {
        World world = Wrapper.world();
        return world == null || world.getBlockState(pos).isAir();
    }

    public static boolean isReplaceable(BlockPos pos) {
        World world = Wrapper.world();
        if (world == null) return false;
        BlockState state = world.getBlockState(pos);
        return state.isAir() || state.isReplaceable() || state.isOf(Blocks.FIRE) || state.isOf(Blocks.SHORT_GRASS)
                || state.isOf(Blocks.SNOW) || state.isOf(Blocks.TALL_GRASS);
    }

    public static BlockState state(BlockPos pos) {
        World world = Wrapper.world();
        return world == null ? Blocks.AIR.getDefaultState() : world.getBlockState(pos);
    }

    public static Block block(BlockPos pos) {
        return state(pos).getBlock();
    }

    /** Czy na tej pozycji mozna postawic krysztal (podstawa = obsydian/bedrock). */
    public static boolean canPlaceCrystal(BlockPos pos) {
        return canPlaceCrystal(pos, false);
    }

    public static boolean canPlaceCrystal(BlockPos pos, boolean oldVer) {
        BlockState below = state(pos.down());
        if (below.getBlock() != Blocks.OBSIDIAN && below.getBlock() != Blocks.BEDROCK) return false;
        if (!isReplaceable(pos)) return false;
        if (!oldVer && !isAir(pos.up())) return false;
        return true;
    }

    /** Czy krysztal juz tam stoi (po encjach). */
    public static boolean isCrystalAt(BlockPos pos) {
        if (Wrapper.world() == null) return false;
        Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        // EntityUtil ma cache - bez tego kazde wywolanie skanowalo caly swiat
        for (net.minecraft.entity.decoration.EndCrystalEntity crystal : EntityUtil.crystals()) {
            if (crystal.getPos().squaredDistanceTo(center) < 2.25) return true;
        }
        return false;
    }

    /** Czy na bloku mozna stawiac (nie jest np. trawa/woda). */
    public static boolean canPlaceBlock(BlockPos pos) {
        return isReplaceable(pos);
    }

    /** Raycast: czy gracz widzi dany punkt. */
    public static boolean canSee(Vec3d target) {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player == null) return false;
        return canSee(player.getEyePos(), target);
    }

    public static boolean canSee(Vec3d from, Vec3d to) {
        World world = Wrapper.world();
        if (world == null || Wrapper.player() == null) return false;
        BlockHitResult result = world.raycast(new RaycastContext(from, to,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                Wrapper.player()));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    /** Czy punkt bloku jest widoczny (uzywane przez wallrange). */
    public static boolean canSeeBlock(BlockPos pos, boolean ignoreTerrain) {
        if (ignoreTerrain) return true;
        World world = Wrapper.world();
        if (world == null) return false;
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player == null) return false;
        Vec3d from = player.getEyePos();
        Vec3d to = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        BlockHitResult result = world.raycast(new RaycastContext(from, to,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        return result == null || result.getType() == HitResult.Type.MISS
                || result.getBlockPos().equals(pos) || result.getBlockPos().equals(pos.up());
    }

    /** Wszystkie pozycje wokol {@code center} w danym promieniu. */
    public static List<BlockPos> sphere(BlockPos center, double radius) {
        List<BlockPos> positions = new ArrayList<>();
        int r = (int) Math.ceil(radius);
        for (int x = center.getX() - r; x <= center.getX() + r; x++) {
            for (int y = center.getY() - r; y <= center.getY() + r; y++) {
                for (int z = center.getZ() - r; z <= center.getZ() + r; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    double dx = x - center.getX();
                    double dy = y - center.getY();
                    double dz = z - center.getZ();
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) positions.add(pos);
                }
            }
        }
        return positions;
    }

    /** Bloki sasiednie (bez skosow). */
    public static List<BlockPos> neighbours(BlockPos pos) {
        List<BlockPos> list = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            list.add(pos.offset(direction));
        }
        return list;
    }

    /** Czy pozycja jest w zasiegu stawiania od gracza. */
    public static boolean inRange(BlockPos pos, double range) {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player == null) return false;
        Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return player.getEyePos().squaredDistanceTo(center) <= range * range
                || player.getPos().squaredDistanceTo(center) <= range * range;
    }

    /** Odleglosc od oczu gracza do srodka bloku. */
    public static double distanceToEyes(BlockPos pos) {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player == null) return Double.MAX_VALUE;
        return player.getEyePos().distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    /** Pozycja pod nogami gracza. */
    public static BlockPos playerFeet() {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        return player == null ? BlockPos.ORIGIN : player.getBlockPos();
    }

    /** Czy blok jest "wybuchoodporny" (do wykrywania dziur). */
    public static boolean isBlastResistant(BlockPos pos) {
        Block block = block(pos);
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK
                || block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL
                || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ENDER_CHEST
                || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK
                || block == Blocks.ENCHANTING_TABLE || block == Blocks.END_PORTAL_FRAME;
    }
}
