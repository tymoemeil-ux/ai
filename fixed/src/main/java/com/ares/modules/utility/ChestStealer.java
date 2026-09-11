package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.screen.slot.SlotActionType;

/** Chest Stealer - zabiera przedmioty z otwartego kontenera. */
public final class ChestStealer extends Module {

    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 2, 0, 20).group("General"));
    private final BoolSetting ignoreTrash = add(new BoolSetting("Ignore Trash", "Pomijaj smieci", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public ChestStealer() {
        super("Chest Stealer", "Zabiera przedmioty z kontenera", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        if (!(Wrapper.mc().currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) {

        timer.increment();
        if (!timer.passed(delay.get())) return;

            return;
        }

        net.minecraft.screen.ScreenHandler handler = Wrapper.player().currentScreenHandler;
        if (handler == null) return;

        int containerSlots = handler.slots.size() - 36;
        if (containerSlots <= 0) return;

        for (int i = 0; i < containerSlots; i++) {
            net.minecraft.item.ItemStack stack = handler.getSlot(i).getStack();
            if (stack.isEmpty()) continue;
            if (ignoreTrash.get() && isTrash(stack)) continue;

            InventoryUtil.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, Wrapper.player());
            timer.reset();
            return;
        }
    }

    private boolean isTrash(net.minecraft.item.ItemStack stack) {
        net.minecraft.item.Item item = stack.getItem();
        return item == net.minecraft.item.Items.DIRT || item == net.minecraft.item.Items.COBBLESTONE
                || item == net.minecraft.item.Items.GRAVEL || item == net.minecraft.item.Items.ROTTEN_FLESH;
    }
}
