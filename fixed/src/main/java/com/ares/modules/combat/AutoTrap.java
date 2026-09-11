package com.ares.modules.combat;

import com.ares.Ares;
import com.ares.core.combat.TargetUtil;
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
import com.ares.core.util.world.BlockUtil;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Auto Trap - zamyka wroga w obsydianowej klatce. */
public final class AutoTrap extends Module {

    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg dzialania", 6f, 1f, 10f).group("General"));
    private final IntSetting blocksPerTick = add(new IntSetting("Blocks / Tick", "Ile blokow na tick", 3, 1, 8).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 1, 0, 10).group("General"));
    private final BoolSetting trapTop = add(new BoolSetting("Trap Top", "Zamykaj tez od gory", true).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj przy stawianiu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent Switch", "Ciche przelaczanie", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public AutoTrap() {
        super("Auto Trap", "Zamyka przeciwnika w klatce z obsydianu", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        LivingEntity target = TargetUtil.best(range.get(), TargetUtil.SortMode.CLOSEST,
                TargetUtil.Filter.PLAYERS, true, Ares.get().friends());
        if (target == null) return;

        BlockPos feet = target.getBlockPos();
        List<BlockPos> positions = new java.util.ArrayList<>();
        positions.add(feet.up());
        positions.add(feet.up().north());
        positions.add(feet.up().south());
        positions.add(feet.up().east());
        positions.add(feet.up().west());
        if (trapTop.get()) {
            positions.add(feet.up().up());
        }

        int previous = Wrapper.player().getInventory().selectedSlot;
        int slot = InventoryUtil.findBlockHotbarSlot(Blocks.OBSIDIAN);
        if (slot == -1) return;

        if (silent.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        int placed = 0;
        for (BlockPos pos : positions) {
            if (placed >= blocksPerTick.get()) break;
            if (!BlockUtil.isReplaceable(pos)) continue;
            if (InteractionUtil.place(pos, rotate.get(), Hand.MAIN_HAND, true)) placed++;
        }

        if (silent.get()) InventoryUtil.selectSilently(previous);
        if (placed > 0) timer.reset();
    }
}
