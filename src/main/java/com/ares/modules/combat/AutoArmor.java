package com.ares.modules.combat;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Auto Armor - zaklada najlepszy pancerz z ekwipunku. */
public final class AutoArmor extends Module {

    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 1, 0, 20).group("General"));
    private final BoolSetting antiBreak = add(new BoolSetting("Anti Break", "Zdejmuj prawie zniszczone czesci", true).group("General"));
    private final IntSetting breakPercent = add(new IntSetting("Break %", "Ponizej ilu procent zdejmowac", 10, 1, 50).group("General"));
    private final BoolSetting preferNetherite = add(new BoolSetting("Prefer Netherite", "Zawsze wybieraj netheryt", true).group("General"));
    private final BoolSetting elytraMode = add(new BoolSetting("Elytra Mode", "Zakladaj elytre gdy brak klaty", false).group("General"));
    private final BoolSetting antiBinding = add(new BoolSetting("Anti Binding", "Nie zdejmuj przy klatwie wiazania", true).group("General"));

    private final TickTimer timer = new TickTimer();

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public AutoArmor() {
        super("Auto Armor", "Automatycznie zaklada najlepszy pancerz", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return;
        if (Wrapper.player().currentScreenHandler == null) return;

        int syncId = Wrapper.player().currentScreenHandler.syncId;

        for (EquipmentSlot slot : SLOTS) {
            ItemStack current = Wrapper.player().getEquippedStack(slot);
            int containerSlot = containerSlotFor(slot);

            if (antiBreak.get() && !current.isEmpty() && isBroken(current)) {
                if (antiBinding.get() && current.hasEnchantments()) continue;
                int empty = firstEmptySlot(inv);
                if (empty != -1) {
                    InventoryUtil.clickSlot(syncId, containerSlot, 0, SlotActionType.QUICK_MOVE, Wrapper.player());
                    timer.reset();
                    return;
                }
            }

            int bestSlot = findBestArmorSlot(slot, inv);
            if (bestSlot == -1) continue;

            ItemStack best = inv.getStack(bestSlot);
            if (!current.isEmpty() && score(current) >= score(best)) continue;
            if (antiBinding.get() && !current.isEmpty()
                    && current.hasEnchantments()) continue;

            // kliknij najlepsza czesc -> szybki transfer na miejsce zbroi
            InventoryUtil.clickSlot(syncId, toContainerSlot(bestSlot), 0, SlotActionType.PICKUP, Wrapper.player());
            InventoryUtil.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, Wrapper.player());
            if (bestSlot < 9) {
                // odloz stara czesc spowrotem do hotbaru
                InventoryUtil.clickSlot(syncId, toContainerSlot(bestSlot), 0, SlotActionType.PICKUP, Wrapper.player());
            }
            timer.reset();
            return;
        }
    }

    private int containerSlotFor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            default -> -1;
        };
    }

    private int toContainerSlot(int invSlot) {
        return invSlot < 9 ? 36 + invSlot : invSlot;
    }

    private int firstEmptySlot(PlayerInventory inv) {
        for (int i = 0; i < InventoryUtil.MAIN_SIZE; i++) {
            if (inv.getStack(i).isEmpty()) return i;
        }
        return -1;
    }

    private boolean isBroken(ItemStack stack) {
        if (!stack.isDamageable()) return false;
        return (double) stack.getDamage() / stack.getMaxDamage() >= (1 - breakPercent.get() / 100.0);
    }

    private int findBestArmorSlot(EquipmentSlot slot, PlayerInventory inv) {
        int bestSlot = -1;
        int bestScore = -1;
        for (int i = 0; i < InventoryUtil.MAIN_SIZE; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (elytraMode.get() && slot == EquipmentSlot.CHEST && stack.getItem() == Items.ELYTRA) return i;
            if (InventoryUtil.armorSlot(stack.getItem()) != slot) continue;
            int score = score(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private int score(ItemStack stack) {
        net.minecraft.item.Item item = stack.getItem();
        int base = 0;
        if (item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE
                || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS) base = 100;
        else if (item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE
                || item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS) base = 80;
        else if (item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE
                || item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS) base = 60;
        else if (item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE
                || item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS) base = 40;
        else if (item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE
                || item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS) base = 35;
        else if (item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE
                || item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS) base = 20;
        if (preferNetherite.get() && base == 100) base += 50;
        if (stack.hasEnchantments()) base += 6;
        return base;
    }
}
