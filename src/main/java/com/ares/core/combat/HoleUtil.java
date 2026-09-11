package com.ares.core.combat;

import com.ares.core.util.world.BlockUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Wykrywanie dziur (1x1, 2x1, 2x2) do HoleESP, Surround, AutoCity. */
public final class HoleUtil {

    private HoleUtil() {
    }

    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    /** Czy byt stoi w pelnej dziurze 1x1. */
    public static boolean isInHole(Entity entity) {
        if (entity == null) return false;
        return isHole(entity.getBlockPos());
    }

    /** Czy pozycja jest dziura 1x1 (podloga + 4 sciany wybuchoodporne). */
    public static boolean isHole(BlockPos pos) {
        if (!isBlastResistant(pos.down())) return false;
        for (Direction direction : HORIZONTAL) {
            if (!isBlastResistant(pos.offset(direction))) return false;
        }
        return BlockUtil.isReplaceable(pos) && BlockUtil.isReplaceable(pos.up());
    }

    /** Czy pozycja jest "bedrock hole". */
    public static boolean isBedrockHole(BlockPos pos) {
        if (BlockUtil.block(pos.down()) != Blocks.BEDROCK) return false;
        for (Direction direction : HORIZONTAL) {
            if (BlockUtil.block(pos.offset(direction)) != Blocks.BEDROCK) return false;
        }
        return BlockUtil.isReplaceable(pos) && BlockUtil.isReplaceable(pos.up());
    }

    /** Czy pozycja jest "obsidian hole". */
    public static boolean isObsidianHole(BlockPos pos) {
        if (BlockUtil.block(pos.down()) != Blocks.OBSIDIAN) return false;
        for (Direction direction : HORIZONTAL) {
            if (BlockUtil.block(pos.offset(direction)) != Blocks.OBSIDIAN) return false;
        }
        return BlockUtil.isReplaceable(pos) && BlockUtil.isReplaceable(pos.up());
    }

    /** Surowe sprawdzenie wybuchoodpornosci (obsydian / bedrock / anvil / itd). */
    public static boolean isBlastResistant(BlockPos pos) {
        net.minecraft.block.Block block = BlockUtil.block(pos);
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.ANVIL
                || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL
                || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ENDER_CHEST
                || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK
                || block == Blocks.ENCHANTING_TABLE || block == Blocks.END_PORTAL_FRAME;
    }

    /** Wszystkie dziury w promieniu (do HoleESP / HoleFiller). */
    private static int holeCacheTick = -1;
    private static BlockPos holeCacheCenter;
    private static int holeCacheRange = -1;
    private static List<Hole> holeCache = new ArrayList<>();

    /**
     * Dziury w zasiegu. Wynik liczony RAZ NA TICK (cache) - moduly renderujace (HoleESP)
     * wolaly to przy kazdej klatce, co przy zasiegu 8 oznaczalo ~5 tys. sprawdzanych
     * pozycji i kilkadziesiat tysiecy odczytow blokow na klatke.
     */
    public static List<Hole> holesInRange(BlockPos center, int range) {
        int tick = com.ares.Ares.get().ticks();
        if (tick == holeCacheTick && range == holeCacheRange && center.equals(holeCacheCenter)) {
            return holeCache;
        }

        List<Hole> holes = new ArrayList<>();
        for (int x = center.getX() - range; x <= center.getX() + range; x++) {
            for (int y = center.getY() - range; y <= center.getY() + range; y++) {
                for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isHole(pos)) {
                        HoleType type = isBedrockHole(pos) ? HoleType.BEDROCK : HoleType.OBSIDIAN;
                        holes.add(new Hole(pos, type));
                    }
                }
            }
        }
        holeCacheTick = tick;
        holeCacheRange = range;
        holeCacheCenter = center;
        holeCache = holes;
        return holes;
    }

    /** Bloki do obstawienia, zeby zamknac dziure wokol gracza (Surround). */
    public static List<BlockPos> surroundPositions(BlockPos pos) {
        List<BlockPos> positions = new ArrayList<>();
        for (Direction direction : HORIZONTAL) {
            BlockPos offset = pos.offset(direction);
            if (BlockUtil.isReplaceable(offset)) positions.add(offset);
        }
        return positions;
    }

    /** Bloki potrzebne do zrobienia "trap" nad celem. */
    public static List<BlockPos> trapPositions(BlockPos pos) {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(pos.up());
        positions.add(pos.up().north());
        positions.add(pos.up().south());
        positions.add(pos.up().east());
        positions.add(pos.up().west());
        return positions;
    }

    public enum HoleType { BEDROCK, OBSIDIAN }

    public static final class Hole {
        private final BlockPos pos;
        private final HoleType type;

        public Hole(BlockPos pos, HoleType type) {
            this.pos = pos;
            this.type = type;
        }

        public BlockPos pos() {
            return pos;
        }

        public HoleType type() {
            return type;
        }
    }
}
