package com.ares.modules.combat;

import com.ares.core.combat.HoleUtil;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.InteractionUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.render.RenderUtil3D;
import com.ares.core.util.timer.TickTimer;
import com.ares.core.util.world.BlockUtil;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Hole Filler - zasypuje dziury w poblizu, zeby wrog nie mogl sie schowac. */
public final class HoleFiller extends Module {

    private final IntSetting range = add(new IntSetting("Range", "Promien szukania dziur", 5, 1, 12).group("General"));
    private final IntSetting blocksPerTick = add(new IntSetting("Blocks / Tick", "Ile blokow na tick", 2, 1, 8).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 1, 0, 10).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj przy stawianiu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent Switch", "Ciche przelaczanie", true).group("General"));
    private final BoolSetting render = add(new BoolSetting("Render", "Podswietlaj zasypywane dziury", true).group("Render"));

    private final TickTimer timer = new TickTimer();
    private BlockPos lastFilled;

    public HoleFiller() {
        super("Hole Filler", "Zasypuje dziury w okolicy", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        BlockPos center = Wrapper.player().getBlockPos();
        List<BlockPos> holes = new java.util.ArrayList<>();
        for (HoleUtil.Hole hole : HoleUtil.holesInRange(center, range.get())) {
            holes.add(hole.pos());
        }

        if (holes.isEmpty()) return;

        int previous = Wrapper.player().getInventory().selectedSlot;
        int slot = InventoryUtil.findBlockHotbarSlot(Blocks.OBSIDIAN);
        if (slot == -1) return;

        if (silent.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        int placed = 0;
        for (BlockPos pos : holes) {
            if (placed >= blocksPerTick.get()) break;
            if (!BlockUtil.isReplaceable(pos)) continue;
            if (InteractionUtil.place(pos, rotate.get(), Hand.MAIN_HAND, true)) {
                lastFilled = pos;
                placed++;
            }
        }

        if (silent.get()) InventoryUtil.selectSilently(previous);
        if (placed > 0) timer.reset();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || lastFilled == null) return;
        RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), lastFilled, 0x33FFCC00, 1f);
    }
}
