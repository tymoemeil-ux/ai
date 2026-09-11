package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.InteractionUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Auto Eat - automatycznie je, gdy poziom glodu spadnie. */
public final class AutoEat extends Module {

    private final IntSetting hunger = add(new IntSetting("Hunger", "Ponizej ilu punktow glodu jesc", 14, 1, 20).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 6, 0, 40).group("General"));
    private final BoolSetting gapples = add(new BoolSetting("Gapples", "Jedz tez zlote jablka", false).group("General"));
    private final BoolSetting switchBack = add(new BoolSetting("Switch Back", "Wroc do poprzedniego slotu", true).group("General"));

    private final TickTimer timer = new TickTimer();
    private int previousSlot = -1;

    public AutoEat() {
        super("Auto Eat", "Automatyczne jedzenie", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (PlayerUtil.hunger() >= hunger.get()) return;
        if (PlayerUtil.isEating()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        int slot = findFoodSlot();
        if (slot == -1) return;

        if (previousSlot == -1) previousSlot = Wrapper.player().getInventory().getSelectedSlot();
        InventoryUtil.selectSlot(slot);
        InteractionUtil.useItem(Hand.MAIN_HAND);
        Wrapper.player().swingHand(Hand.MAIN_HAND);
        timer.reset();
    }

    private int findFoodSlot() {
        PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            Item item = stack.getItem();
            if (item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.GOLDEN_APPLE) {
                if (gapples.get()) return i;
                continue;
            }
            if (stack.getUseAction() == net.minecraft.util.UseAction.EAT) return i;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        if (switchBack.get() && previousSlot != -1 && Wrapper.nullCheck()) {
            InventoryUtil.selectSlot(previousSlot);
        }
        previousSlot = -1;
    }
}
