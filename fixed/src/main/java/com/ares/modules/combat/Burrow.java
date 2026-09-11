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
import com.ares.core.util.timer.TickTimer;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Burrow - chowa gracza w bloku (klatka + blok na glowie). */
public final class Burrow extends Module {

    private final BoolSetting instant = add(new BoolSetting("Instant", "Dzialaj od razu po wlaczeniu", true).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 2, 0, 10).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj przy stawianiu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent Switch", "Ciche przelaczanie", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public Burrow() {
        super("Burrow", "Chowa gracza w bloku", ModuleCategory.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        if (instant.get()) burrow();
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        timer.increment();
        if (timer.passed(delay.get())) burrow();
    }

    private void burrow() {
        BlockPos feet = Wrapper.player().getBlockPos();

        int previous = Wrapper.player().getInventory().getSelectedSlot();
        int slot = InventoryUtil.findBlockHotbarSlot(Blocks.OBSIDIAN);
        if (slot == -1) slot = InventoryUtil.findBlockHotbarSlot(Blocks.ENDER_CHEST);
        if (slot == -1) return;

        if (silent.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        // najpierw blok nad glowa, potem "skok + blok pod nogami"
        InteractionUtil.place(feet.up(), rotate.get(), Hand.MAIN_HAND, true);

        if (silent.get()) InventoryUtil.selectSilently(previous);
        timer.reset();
    }
}
