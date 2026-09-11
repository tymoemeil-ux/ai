package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/** Hotbar Refill - uzupelnia sloty hotbara z ekwipunku. */
public final class HotbarRefill extends Module {

    private final IntSetting threshold = add(new IntSetting("Threshold", "Uzupelniaj ponizej ilu sztuk", 8, 1, 32).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 4, 1, 40).group("General"));

    private final TickTimer timer = new TickTimer();

    public HotbarRefill() {
        super("Hotbar Refill", "Uzupelnia hotbar z ekwipunku", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (Wrapper.mc().currentScreen != null) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null || Wrapper.player().currentScreenHandler == null) return;
        int syncId = Wrapper.player().currentScreenHandler.syncId;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty() || !stack.isStackable()) continue;
            if (stack.getCount() >= threshold.get()) continue;
            if (stack.getCount() >= stack.getMaxCount()) continue;

            int source = findStackInInventory(inv, stack);
            if (source == -1) continue;

            InventoryUtil.clickSlot(syncId, source, 0, SlotActionType.PICKUP, Wrapper.player());
            InventoryUtil.clickSlot(syncId, 36 + i, 0, SlotActionType.PICKUP, Wrapper.player());
            InventoryUtil.clickSlot(syncId, source, 0, SlotActionType.PICKUP, Wrapper.player());

            timer.reset();
            return;
        }
    }

    private int findStackInInventory(net.minecraft.entity.player.PlayerInventory inv, ItemStack stack) {
        for (int i = 9; i < InventoryUtil.MAIN_SIZE; i++) {
            ItemStack other = inv.getStack(i);
            if (other.isEmpty()) continue;
            if (ItemStack.areItemsEqual(stack, other) && other.getCount() > 1) return i;
        }
        return -1;
    }
}
