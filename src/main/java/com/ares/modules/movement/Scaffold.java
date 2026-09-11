package com.ares.modules.movement;

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
import com.ares.core.util.world.BlockUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Scaffold - stawia bloki pod nogami podczas biegu. */
public final class Scaffold extends Module {

    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 0, 0, 10).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj przy stawianiu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent Switch", "Ciche przelaczanie", true).group("General"));
    private final BoolSetting onlyWhenMoving = add(new BoolSetting("Only When Moving", "Tylko podczas ruchu", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public Scaffold() {
        super("Scaffold", "Stawia bloki pod nogami", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (onlyWhenMoving.get() && !com.ares.core.util.player.PlayerUtil.isMoving()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        BlockPos below = Wrapper.player().getBlockPos().down();
        if (!BlockUtil.isReplaceable(below)) return;

        int slot = findBlockSlot();
        if (slot == -1) return;

        int previous = Wrapper.player().getInventory().selectedSlot;
        if (silent.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        InteractionUtil.place(below, rotate.get(), Hand.MAIN_HAND, true);
        if (silent.get()) InventoryUtil.selectSilently(previous);
        timer.reset();
    }

    private int findBlockSlot() {
        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof net.minecraft.item.BlockItem) return i;
        }
        return -1;
    }
}
