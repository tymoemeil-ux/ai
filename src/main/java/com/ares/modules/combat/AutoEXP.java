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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Auto EXP - rzuca butelkami XP, zeby naprawic pancerz (Mending). */
public final class AutoEXP extends Module {

    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 2, 0, 20).group("General"));
    private final IntSetting minDurability = add(new IntSetting("Min Durability %", "Naprawiaj ponizej tego progu", 60, 1, 100).group("General"));
    private final BoolSetting onlyWhenHolding = add(new BoolSetting("Only When Holding", "Trzymaj butelke w rece", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public AutoEXP() {
        super("Auto EXP", "Rzuca butelkami doswiadczenia by naprawic pancerz", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        boolean needsRepair = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = Wrapper.player().getEquippedStack(slot);
            if (stack.isEmpty() || !stack.isDamageable()) continue;
            double percent = (1 - (double) stack.getDamage() / stack.getMaxDamage()) * 100;
            if (percent < minDurability.get() && hasMending(stack)) {
                needsRepair = true;
                break;
            }
        }

        if (!needsRepair) return;

        if (onlyWhenHolding.get()) {
            if (InventoryUtil.mainHand().getItem() != Items.EXPERIENCE_BOTTLE) {
                InventoryUtil.switchTo(Items.EXPERIENCE_BOTTLE);
                return;
            }
            com.ares.core.util.player.InteractionUtil.useItem(Hand.MAIN_HAND);
            Wrapper.player().swingHand(Hand.MAIN_HAND);
            timer.reset();
        } else {
            if (InventoryUtil.switchToSilently(Items.EXPERIENCE_BOTTLE)) {
                com.ares.core.util.player.InteractionUtil.useItem(Hand.MAIN_HAND);
                Wrapper.player().swingHand(Hand.MAIN_HAND);
                timer.reset();
            }
        }
    }

    private boolean hasMending(ItemStack stack) {
        return net.minecraft.enchantment.EnchantmentHelper.getLevel(
                net.minecraft.enchantment.Enchantments.MENDING, stack) > 0;
    }
}
