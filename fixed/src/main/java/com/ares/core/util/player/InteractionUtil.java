package com.ares.core.util.player;

import com.ares.core.rotation.Rotation;
import com.ares.core.rotation.RotationUtil;
import com.ares.core.util.Wrapper;
import com.ares.core.util.world.BlockUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Stawianie blokow, bicie bytow i uzywanie itemow z obsluga rotacji i switcha. */
public final class InteractionUtil {

    private InteractionUtil() {
    }

    /** Znajdz strone bloku, na ktora mozna kliknac, zeby postawic przy {@code pos}. */
    public static PlaceTarget findPlaceTarget(BlockPos pos, boolean strictDirection) {
        for (Direction direction : Direction.values()) {
            BlockPos side = pos.offset(direction);
            if (!BlockUtil.isAir(side) && !BlockUtil.isReplaceable(side)) {
                return new PlaceTarget(side, direction.getOpposite());
            }
        }
        return null;
    }

    public static boolean place(BlockPos pos, boolean rotate) {
        return place(pos, rotate, Hand.MAIN_HAND, true);
    }

    public static boolean place(BlockPos pos, boolean rotate, Hand hand, boolean swing) {
        PlaceTarget target = findPlaceTarget(pos, false);
        if (target == null) return false;

        ClientPlayerEntity player = Wrapper.player();
        if (player == null) return false;

        Vec3d hitVec = new Vec3d(target.pos.getX() + 0.5, target.pos.getY() + 0.5, target.pos.getZ() + 0.5)
                .add(new Vec3d(target.side.getVector().getX() * 0.5,
                        target.side.getVector().getY() * 0.5,
                        target.side.getVector().getZ() * 0.5));

        BlockHitResult hitResult = new BlockHitResult(hitVec, target.side, target.pos, false);

        if (rotate) {
            RotationUtil.setRotation(RotationUtil.toPoint(hitVec));
        }

        net.minecraft.util.ActionResult result = Wrapper.interaction().interactBlock(player, hand, hitResult);
        if (result != null && result.isAccepted()) {
            if (swing) player.swingHand(hand);
            return true;
        }
        return false;
    }

    /** Stawia blok okreslonego typu (przelacza hotbar na ten blok). */
    public static boolean placeWith(Block block, BlockPos pos, boolean rotate, boolean silent, boolean swing) {
        int slot = InventoryUtil.findBlockHotbarSlot(block);
        if (slot == -1) return false;
        int previous = Wrapper.player().getInventory().getSelectedSlot();
        InventoryUtil.selectSilently(slot);
        boolean placed = place(pos, rotate, Hand.MAIN_HAND, swing);
        if (silent) InventoryUtil.selectSilently(previous);
        return placed;
    }

    /** Bicie bytu (krysztalu / gracza). */
    public static void attack(Entity entity, boolean swing) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || entity == null) return;
        Wrapper.interaction().attackEntity(player, entity);
        if (swing) player.swingHand(Hand.MAIN_HAND);
    }

    public static void attack(Entity entity, boolean swing, com.ares.core.rotation.Rotation rotation) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || entity == null) return;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        RotationUtil.setRotation(rotation);
        Wrapper.interaction().attackEntity(player, entity);
        player.setYaw(yaw);
        player.setPitch(pitch);
        if (swing) player.swingHand(Hand.MAIN_HAND);
    }

    /** Klikniecie prawym przyciskiem na blok (np. ladowanie anchora). */
    public static boolean rightClickBlock(BlockPos pos, Hand hand, boolean rotate) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null) return false;
        Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (rotate) RotationUtil.setRotation(RotationUtil.toPoint(center));
        BlockHitResult hitResult = new BlockHitResult(center, Direction.UP, pos, false);
        net.minecraft.util.ActionResult result = Wrapper.interaction().interactBlock(player, hand, hitResult);
        if (result != null && result.isAccepted()) {
            player.swingHand(hand);
            return true;
        }
        return false;
    }

    /** Uzycie itemu w rece. */
    public static boolean useItem(Hand hand) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null) return false;
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isEmpty()) return false;
        net.minecraft.util.ActionResult result = Wrapper.interaction().interactItem(player, hand);
        return result != null && result.isAccepted();
    }

    /** Czy w rece jest blok danego typu. */
    public static boolean holdingBlock(Block block) {
        ItemStack stack = InventoryUtil.mainHand();
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) return false;
        return ((BlockItem) stack.getItem()).getBlock() == block;
    }

    /** Lista pozycji wokol podanej pozycji (do Surround / Trap / HoleFiller). */
    public static List<BlockPos> around(BlockPos pos, boolean includeUp, boolean includeDown) {
        List<BlockPos> positions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (!includeUp && direction == Direction.UP) continue;
            if (!includeDown && direction == Direction.DOWN) continue;
            positions.add(pos.offset(direction));
        }
        return positions;
    }

    public static final class PlaceTarget {
        private final BlockPos pos;
        private final Direction side;

        public PlaceTarget(BlockPos pos, Direction side) {
            this.pos = pos;
            this.side = side;
        }

        public BlockPos pos() {
            return pos;
        }

        public Direction side() {
            return side;
        }
    }
}
