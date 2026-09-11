package com.ares.modules.combat;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.InteractionUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Auto Gap - zjada zlote jablko gdy zdrowie spadnie ponizej progu. */
public final class AutoGap extends Module {

    private final FloatSetting health = add(new FloatSetting("Health", "Ponizej ilu HP", 14f, 1f, 36f).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 10, 0, 40).group("General"));
    private final BoolSetting preferEnchanted = add(new BoolSetting("Prefer Enchanted", "Uzywaj jablek z enchantem", true).group("General"));
    private final BoolSetting switchBack = add(new BoolSetting("Switch Back", "Wroc do poprzedniego slotu", true).group("General"));

    private final TickTimer timer = new TickTimer();
    private int previousSlot = -1;
    private boolean eating;

    public AutoGap() {
        super("Auto Gap", "Automatycznie zjada zlote jablko", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (PlayerUtil.health() > health.get()) {
            if (eating && switchBack.get() && previousSlot != -1) {
                InventoryUtil.selectSlot(previousSlot);
                previousSlot = -1;
            }
            eating = false;
            return;
        }

        if (PlayerUtil.isEating()) {
            eating = true;
            return;
        }

        if (!timer.passed(delay.get())) return;

        int slot = findGapSlot();
        if (slot == -1) return;

        if (previousSlot == -1) previousSlot = Wrapper.player().getInventory().getSelectedSlot();
        InventoryUtil.selectSlot(slot);
        InteractionUtil.useItem(Hand.MAIN_HAND);
        Wrapper.player().swingHand(Hand.MAIN_HAND);
        eating = true;
        timer.reset();
    }

    private int findGapSlot() {
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.Item item = Wrapper.player().getInventory().getStack(i).getItem();
            if (preferEnchanted.get() && item == Items.ENCHANTED_GOLDEN_APPLE) return i;
            if (!preferEnchanted.get() && (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE)) return i;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        if (switchBack.get() && previousSlot != -1 && Wrapper.nullCheck()) {
            InventoryUtil.selectSlot(previousSlot);
        }
        previousSlot = -1;
        eating = false;
    }
}
