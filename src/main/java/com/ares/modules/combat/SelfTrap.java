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
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Self Trap - zamyka sie w klatce z obsydianu gdy zdrowie jest niskie. */
public final class SelfTrap extends Module {

    private final FloatSetting healthTrigger = add(new FloatSetting("Health", "Ponizej ilu HP sie zamykac", 12f, 1f, 36f).group("General"));
    private final IntSetting blocksPerTick = add(new IntSetting("Blocks / Tick", "Ile blokow na tick", 4, 1, 8).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 1, 0, 10).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj przy stawianiu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent Switch", "Ciche przelaczanie", true).group("General"));
    private final BoolSetting disableWhenSafe = add(new BoolSetting("Disable When Done", "Wylacz po zamknieciu", false).group("General"));

    private final TickTimer timer = new TickTimer();

    public SelfTrap() {
        super("Self Trap", "Zamyka sie w obsydianowej klatce przy niskim zdrowiu", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (PlayerUtil.health() > healthTrigger.get()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        BlockPos feet = Wrapper.player().getBlockPos();
        BlockPos[] positions = {
                feet.up(), feet.up().north(), feet.up().south(), feet.up().east(), feet.up().west(),
                feet.north().up(), feet.south().up(), feet.east().up(), feet.west().up()
        };

        int previous = Wrapper.player().getInventory().getSelectedSlot();
        int slot = InventoryUtil.findBlockHotbarSlot(Blocks.OBSIDIAN);
        if (slot == -1) return;

        if (silent.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        int placed = 0;
        for (BlockPos pos : positions) {
            if (placed >= blocksPerTick.get()) break;
            if (!com.ares.core.util.world.BlockUtil.isReplaceable(pos)) continue;
            if (InteractionUtil.place(pos, rotate.get(), Hand.MAIN_HAND, true)) placed++;
        }

        if (silent.get()) InventoryUtil.selectSilently(previous);
        if (placed > 0) timer.reset();
        if (placed == 0 && disableWhenSafe.get()) setEnabled(false);
    }
}
