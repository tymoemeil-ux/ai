package com.ares.modules.combat;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/** Offhand - trzyma wybrany item w offhandzie. */
public final class Offhand extends Module {

    public enum ItemSelection { TOTEM, CRYSTAL, GAPPLE, SHIELD, OBSIDIAN, GLOWSTONE, ANCHOR }

    private final ModeSetting<ItemSelection> item = add(new ModeSetting<>("Item", "Co trzymac w offhandzie", ItemSelection.TOTEM).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 2, 0, 20).group("General"));
    private final BoolSetting onlyWhenEmpty = add(new BoolSetting("Only When Empty", "Zmieniaj tylko gdy offhand jest pusty", false).group("General"));
    private final BoolSetting waitForUse = add(new BoolSetting("Wait For Use", "Nie zmieniaj podczas uzywania itemu", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public Offhand() {
        super("Offhand", "Automatycznie utrzymuje wybrany przedmiot w offhandzie", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (waitForUse.get() && PlayerUtil.isUsingItem()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        Item wanted = toItem(item.get());
        if (wanted == null) return;

        ItemStackHolder holder = new ItemStackHolder(InventoryUtil.offhand());
        if (holder.item == wanted) return;
        if (onlyWhenEmpty.get() && !InventoryUtil.offhand().isEmpty()) return;

        int slot = InventoryUtil.findItem(wanted);
        if (slot == -1 || slot == InventoryUtil.OFFHAND_SLOT) return;

        if (slot < 9) InventoryUtil.moveHotbarToOffhand(slot);
        else InventoryUtil.moveToOffhand(slot);

        timer.reset();
    }

    private Item toItem(ItemSelection selection) {
        return switch (selection) {
            case TOTEM -> Items.TOTEM_OF_UNDYING;
            case CRYSTAL -> Items.END_CRYSTAL;
            case GAPPLE -> Items.ENCHANTED_GOLDEN_APPLE;
            case SHIELD -> Items.SHIELD;
            case OBSIDIAN -> Items.OBSIDIAN;
            case GLOWSTONE -> Items.GLOWSTONE;
            case ANCHOR -> Items.RESPAWN_ANCHOR;
        };
    }

    private static final class ItemStackHolder {
        private final Item item;

        ItemStackHolder(net.minecraft.item.ItemStack stack) {
            this.item = stack == null ? Items.AIR : stack.getItem();
        }
    }

    @Override
    public String info() {
        return item.get().name().toLowerCase();
    }
}
