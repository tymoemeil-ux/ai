package com.ares.core.util.player;

import net.minecraft.entity.EquipmentSlot;

import com.ares.core.util.Wrapper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

/** Obsluga ekwipunku: wyszukiwanie itemow, przelaczanie slotow, klikanie w inventory. */
public final class InventoryUtil {

    private InventoryUtil() {
    }

    public static PlayerInventory inventory() {
        ClientPlayerEntity player = Wrapper.player();
        return player == null ? null : player.getInventory();
    }

    /** Zwraca slot w hotbarze (0-8) zawierajacy dany item, albo -1. */
    public static int findHotbarItem(Item item) {
        PlayerInventory inv = inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    /** Zwraca dowolny slot ekwipunku zawierajacy item, albo -1. */
    public static int findItem(Item item) {
        PlayerInventory inv = inventory();
        if (inv == null) return -1;
        for (int i = 0; i < MAIN_SIZE; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() == item) return i;
        }
        if (inv.getStack(OFFHAND_SLOT).getItem() == item) return OFFHAND_SLOT;
        return -1;
    }

    /** Szuka itemu w calej gornej czesci ekwipunku (bez hotbara). */
    public static int findInMain(Item item) {
        PlayerInventory inv = inventory();
        if (inv == null) return -1;
        for (int i = 9; i < MAIN_SIZE; i++) {
            if (inv.getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    public static int count(Item item) {
        PlayerInventory inv = inventory();
        int count = 0;
        if (inv == null) return 0;
        for (int i = 0; i < MAIN_SIZE; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    public static int countInHotbar(Item item) {
        PlayerInventory inv = inventory();
        int count = 0;
        if (inv == null) return 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    public static boolean has(Item item) {
        return findItem(item) != -1;
    }

    /** Przelacza aktywny slot (wysyla pakiet do serwera). */
    public static void selectSlot(int slot) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || slot < 0 || slot > 8) return;
        player.getInventory().setSelectedSlot(slot);
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }

    /** Przelacza na slot tylko lokalnie (silent switch). */
    public static void selectSilently(int slot) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || slot < 0 || slot > 8) return;
        player.getInventory().setSelectedSlot(slot);
    }

    /** Przelacza na pierwszy slot z danym itemem w hotbarze. */
    public static boolean switchTo(Item item) {
        int slot = findHotbarItem(item);
        if (slot == -1) return false;
        selectSlot(slot);
        return true;
    }

    public static boolean switchToSilently(Item item) {
        int slot = findHotbarItem(item);
        if (slot == -1) return false;
        selectSilently(slot);
        return true;
    }

    /** Przenosi item z inventory do offhanda (slot 45 w player screen handler). */
    public static void moveToOffhand(int slotInInventory) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || player.currentScreenHandler == null) return;
        int syncId = player.currentScreenHandler.syncId;
        int from = slotInInventory < 9 ? 36 + slotInInventory : slotInInventory;
        clickSlot(syncId, from, 40, SlotActionType.SWAP, player);
    }

    /** Przenosi item z hotbaru (0-8) do offhanda. */
    public static void moveHotbarToOffhand(int hotbarSlot) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || player.currentScreenHandler == null) return;
        int syncId = player.currentScreenHandler.syncId;
        clickSlot(syncId, 36 + hotbarSlot, 40, SlotActionType.SWAP, player);
    }

    public static void clickSlot(int syncId, int slotId, int button, SlotActionType action) {
        clickSlot(syncId, slotId, button, action, Wrapper.player());
    }

    public static void clickSlot(int syncId, int slotId, int button, SlotActionType action,
                                 net.minecraft.entity.player.PlayerEntity player) {
        if (Wrapper.interaction() != null && player != null) {
            Wrapper.interaction().clickSlot(syncId, slotId, button, action, player);
        }
    }

    /** Zamienia dwa sloty w ekwipunku (uzywane przez AutoArmor / InventoryCleaner). */
    public static void swap(int slot1, int slot2) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || player.currentScreenHandler == null) return;
        int syncId = player.currentScreenHandler.syncId;
        clickSlot(syncId, slot1, 0, SlotActionType.PICKUP, player);
        clickSlot(syncId, slot2, 0, SlotActionType.PICKUP, player);
        clickSlot(syncId, slot1, 0, SlotActionType.PICKUP, player);
    }

    public static void drop(int slot) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || player.currentScreenHandler == null) return;
        clickSlot(player.currentScreenHandler.syncId, slot, 0, SlotActionType.THROW, player);
    }

    public static void quickMove(int slot) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null || player.currentScreenHandler == null) return;
        clickSlot(player.currentScreenHandler.syncId, slot, 0, SlotActionType.QUICK_MOVE, player);
    }

    /** Identyfikatory slotow w ekwipunku gracza. */
    public static final int OFFHAND_SLOT = 40;
    public static final int MAIN_SIZE = 36;

    public static ItemStack offhand() {
        ClientPlayerEntity player = Wrapper.player();
        return player == null ? ItemStack.EMPTY : player.getOffHandStack();
    }

    public static ItemStack mainHand() {
        ClientPlayerEntity player = Wrapper.player();
        return player == null ? ItemStack.EMPTY : player.getMainHandStack();
    }

    public static boolean holding(Item item) {
        return mainHand().getItem() == item;
    }

    public static boolean offhandIs(Item item) {
        return offhand().getItem() == item;
    }

    public static boolean isTotem(ItemStack stack) {
        return stack.getItem() == Items.TOTEM_OF_UNDYING;
    }

    public static boolean isCrystal(ItemStack stack) {
        return stack.getItem() == Items.END_CRYSTAL;
    }

    public static boolean isGlowstone(ItemStack stack) {
        return stack.getItem() == Items.GLOWSTONE;
    }

    public static boolean isAnchor(ItemStack stack) {
        return stack.getItem() == Items.RESPAWN_ANCHOR;
    }

    public static boolean isObsidian(ItemStack stack) {
        return stack.getItem() == Items.OBSIDIAN;
    }

    public static int firstEmptyHotbarSlot() {
        PlayerInventory inv = inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isEmpty()) return i;
        }
        return -1;
    }

    /** Zwraca najlepszy slot w hotbarze do postawienia bloku. */
    public static int findBlockHotbarSlot(net.minecraft.block.Block block) {
        PlayerInventory inv = inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof net.minecraft.item.BlockItem blockItem
                    && blockItem.getBlock() == block) return i;
        }
        return -1;
    }

    /**
     * Slot pancerza dla danego przedmiotu (null gdy to nie pancerz).
     * Uzywamy wlasnej mapy zamiast net.minecraft.item.ArmorItem,
     * bo ta klasa nie istnieje w yarn 1.21.8.
     */
    public static EquipmentSlot armorSlot(Item item) {
        if (item == Items.NETHERITE_HELMET || item == Items.DIAMOND_HELMET || item == Items.IRON_HELMET
                || item == Items.GOLDEN_HELMET || item == Items.CHAINMAIL_HELMET || item == Items.LEATHER_HELMET
                || item == Items.TURTLE_HELMET) return EquipmentSlot.HEAD;
        if (item == Items.NETHERITE_CHESTPLATE || item == Items.DIAMOND_CHESTPLATE || item == Items.IRON_CHESTPLATE
                || item == Items.GOLDEN_CHESTPLATE || item == Items.CHAINMAIL_CHESTPLATE
                || item == Items.LEATHER_CHESTPLATE) return EquipmentSlot.CHEST;
        if (item == Items.NETHERITE_LEGGINGS || item == Items.DIAMOND_LEGGINGS || item == Items.IRON_LEGGINGS
                || item == Items.GOLDEN_LEGGINGS || item == Items.CHAINMAIL_LEGGINGS
                || item == Items.LEATHER_LEGGINGS) return EquipmentSlot.LEGS;
        if (item == Items.NETHERITE_BOOTS || item == Items.DIAMOND_BOOTS || item == Items.IRON_BOOTS
                || item == Items.GOLDEN_BOOTS || item == Items.CHAINMAIL_BOOTS
                || item == Items.LEATHER_BOOTS) return EquipmentSlot.FEET;
        return null;
    }

    /** Czy przedmiot jest czescia pancerza (elytra osobno). */
    public static boolean isArmor(Item item) {
        return armorSlot(item) != null;
    }
}
