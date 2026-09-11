package com.ares.core.util.player;

import com.ares.core.util.Wrapper;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Informacje o graczu i jego akcjach. */
public final class PlayerUtil {

    private PlayerUtil() {
    }

    public static double health() {
        PlayerEntity player = Wrapper.player();
        return player == null ? 0 : player.getHealth() + player.getAbsorptionAmount();
    }

    public static double maxHealth() {
        PlayerEntity player = Wrapper.player();
        return player == null ? 20 : player.getMaxHealth();
    }

    public static boolean isAlive() {
        PlayerEntity player = Wrapper.player();
        return player != null && !player.isDead() && player.isAlive();
    }

    public static boolean isCreative() {
        PlayerEntity player = Wrapper.player();
        return player != null && player.isCreative();
    }

    public static boolean isSpectator() {
        PlayerEntity player = Wrapper.player();
        return player != null && player.isSpectator();
    }

    public static boolean isEating() {
        PlayerEntity player = Wrapper.player();
        return player != null && player.isUsingItem()
                && (player.getActiveHand() == Hand.MAIN_HAND || player.getActiveHand() == Hand.OFF_HAND)
                && isFood(player.getActiveItem());
    }

    public static boolean isUsingItem() {
        PlayerEntity player = Wrapper.player();
        return player != null && player.isUsingItem();
    }

    public static boolean isMining() {
        return Wrapper.interaction() != null && Wrapper.interaction().isBreakingBlock();
    }

    private static boolean isFood(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && stack.getUseAction() == net.minecraft.util.UseAction.EAT;
    }

    public static boolean hasWeakness() {
        PlayerEntity player = Wrapper.player();
        return player != null && player.hasStatusEffect(StatusEffects.WEAKNESS);
    }

    public static boolean hasFireResistance() {
        PlayerEntity player = Wrapper.player();
        return player != null && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE);
    }

    /** Czy gracz trzyma w rekach miecz lub topor. */
    public static boolean holdingWeapon() {
        ItemStack stack = InventoryUtil.mainHand();
        net.minecraft.item.Item item = stack.getItem();
        return item == Items.NETHERITE_SWORD || item == Items.DIAMOND_SWORD || item == Items.IRON_SWORD
                || item == Items.STONE_SWORD || item == Items.WOODEN_SWORD || item == Items.GOLDEN_SWORD
                || item == Items.NETHERITE_AXE || item == Items.DIAMOND_AXE || item == Items.IRON_AXE
                || item == Items.STONE_AXE || item == Items.WOODEN_AXE || item == Items.GOLDEN_AXE
                || item == Items.MACE;
    }

    /** Wektor ruchu gracza (x = strafe, y = forward) - 1.21.8 Input API. */
    public static net.minecraft.util.math.Vec2f movementInput() {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        return player == null ? new net.minecraft.util.math.Vec2f(0, 0) : player.input.getMovementInput();
    }

    public static float forwardInput() {
        return movementInput().y;
    }

    public static float strafeInput() {
        return movementInput().x;
    }

    public static boolean isMoving() {
        return forwardInput() != 0 || strafeInput() != 0;
    }

    /** Ruch z pominieciem Input (np. NoSlow / InventoryMove). */
    public static void applyMovement(double forward, double strafe, double speed) {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player == null) return;
        if (forward == 0 && strafe == 0) return;

        float yaw = player.getYaw() * ((float) Math.PI / 180);
        double motionX = forward * Math.sin(yaw) * -1 + strafe * Math.cos(yaw);
        double motionZ = forward * Math.cos(yaw) - strafe * Math.sin(yaw);

        double length = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (length == 0) return;

        motionX = motionX / length * speed;
        motionZ = motionZ / length * speed;

        player.setVelocity(new net.minecraft.util.math.Vec3d(motionX, player.getVelocity().y, motionZ));
    }

    public static double hunger() {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player == null) return 20;
        return player.getHungerManager().getFoodLevel();
    }

    public static boolean isInHole() {
        return com.ares.core.combat.HoleUtil.isInHole(Wrapper.player());
    }

    public static void swing(net.minecraft.util.Hand hand) {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player != null) player.swingHand(hand);
    }

    public static void sendMessage(String message) {
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();
        if (player == null) return;
        player.networkHandler.sendChatMessage(message);
    }
}
