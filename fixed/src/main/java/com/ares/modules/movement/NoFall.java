package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.PlayerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** No Fall - MLG water bucket / reset obrazen od upadku. */
public final class NoFall extends Module {

    private final FloatSetting height = add(new FloatSetting("Height", "Od jakiej wysokosci dzialac", 5f, 1f, 50f).group("General"));

    public NoFall() {
        super("No Fall", "Zapobiega obrazeniom od upadku", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (Wrapper.player().fallDistance < height.get()) return;

        if (InventoryUtil.mainHand().getItem() == Items.WATER_BUCKET) {
            com.ares.core.util.player.InteractionUtil.useItem(Hand.MAIN_HAND);
            Wrapper.player().swingHand(Hand.MAIN_HAND);
            return;
        }

        if (InventoryUtil.switchToSilently(Items.WATER_BUCKET)) {
            com.ares.core.util.player.InteractionUtil.useItem(Hand.MAIN_HAND);
            Wrapper.player().swingHand(Hand.MAIN_HAND);
            return;
        }

        // ostatecznosc: zresetuj dystans upadku
        Wrapper.player().fallDistance = 0;
    }
}
