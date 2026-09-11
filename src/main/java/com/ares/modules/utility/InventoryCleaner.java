package com.ares.modules.utility;

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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Inventory Cleaner - wyrzuca zbedne przedmioty z ekwipunku. */
public final class InventoryCleaner extends Module {

    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 4, 1, 40).group("General"));
    private final BoolSetting keepBlocks = add(new BoolSetting("Keep Blocks", "Zostaw bloki", true).group("Filter"));
    private final BoolSetting keepFood = add(new BoolSetting("Keep Food", "Zostaw jedzenie", true).group("Filter"));
    private final BoolSetting keepTools = add(new BoolSetting("Keep Tools", "Zostaw narzedzia", true).group("Filter"));
    private final BoolSetting keepArmor = add(new BoolSetting("Keep Armor", "Zostaw pancerz", true).group("Filter"));
    private final BoolSetting onlyHotbar = add(new BoolSetting("Hotbar Only", "Czysc tylko hotbar", false).group("General"));

    private final TickTimer timer = new TickTimer();

    public InventoryCleaner() {
        super("Inventory Cleaner", "Czysci ekwipunek ze smieci", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (Wrapper.mc().currentScreen != null) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return;

        int max = onlyHotbar.get() ? 9 : InventoryUtil.MAIN_SIZE;

        for (int i = onlyHotbar.get() ? 0 : 9; i < max; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (shouldKeep(stack)) continue;

            InventoryUtil.drop(i);
            timer.reset();
            return;
        }
    }

    private boolean shouldKeep(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.AIR) return true;
        if (keepArmor.get() && (item instanceof net.minecraft.item.ArmorItem || item == Items.ELYTRA)) return true;
        if (keepTools.get() && (item instanceof net.minecraft.item.ToolItem || item == Items.SHIELD
                || item == Items.BOW || item == Items.CROSSBOW)) return true;
        if (keepFood.get() && stack.isFood()) return true;
        if (keepBlocks.get() && item instanceof net.minecraft.item.BlockItem) return true;
        return item == Items.END_CRYSTAL || item == Items.TOTEM_OF_UNDYING
                || item == Items.EXPERIENCE_BOTTLE || item == Items.OBSIDIAN
                || item == Items.RESPAWN_ANCHOR || item == Items.GLOWSTONE;
    }
}
