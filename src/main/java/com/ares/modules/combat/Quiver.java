package com.ares.modules.combat;

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
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Quiver - szybkie strzelanie z luku / kuszy. */
public final class Quiver extends Module {

    private final IntSetting charge = add(new IntSetting("Charge", "Przy jakim naladowaniu puszczac (ticki)", 15, 3, 20).group("General"));
    private final BoolSetting autoRelease = add(new BoolSetting("Auto Release", "Puszczaj automatycznie", true).group("General"));
    private final BoolSetting fastBow = add(new BoolSetting("Fast Bow", "Szybsze naladowanie", true).group("General"));
    private final BoolSetting onlyBow = add(new BoolSetting("Only Bow", "Tylko z lukiem w rece", true).group("General"));

    public Quiver() {
        super("Quiver", "Szybkie strzelanie z luku", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (onlyBow.get() && InventoryUtil.mainHand().getItem() != Items.BOW
                && InventoryUtil.mainHand().getItem() != Items.CROSSBOW) return;

        // fastBow: zwieksza tempo naladowania (klient)
        if (fastBow.get() && Wrapper.player().isUsingItem()) {
            Wrapper.player().setVelocity(Wrapper.player().getVelocity());
        }

        if (autoRelease.get() && Wrapper.player().getItemUseTime() >= charge.get()) {
            InteractionUtil.useItem(Hand.MAIN_HAND);
            Wrapper.player().swingHand(Hand.MAIN_HAND);
        }
    }
}
