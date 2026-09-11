package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.PlayerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/** Auto Tool - przelacza na najlepsze narzedzie do kopanego bloku. */
public final class AutoTool extends Module {

    private final BoolSetting switchBack = add(new BoolSetting("Switch Back", "Wracaj do poprzedniego slotu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent", "Ciche przelaczanie", true).group("General"));

    private int previousSlot = -1;

    public AutoTool() {
        super("Auto Tool", "Automatyczny wybor narzedzia", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (!PlayerUtil.isMining()) return;
        if (Wrapper.mc().crosshairTarget == null) return;
        if (Wrapper.mc().crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) Wrapper.mc().crosshairTarget;
        BlockState state = Wrapper.world().getBlockState(hit.getBlockPos());

        int bestSlot = -1;
        float bestSpeed = -1;

        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot == -1 || bestSpeed <= 1.0f) return;

        if (previousSlot == -1) previousSlot = Wrapper.player().getInventory().selectedSlot;
        if (silent.get()) InventoryUtil.selectSilently(bestSlot);
        else InventoryUtil.selectSlot(bestSlot);
    }

    @Override
    public void onDisable() {
        if (!switchBack.get() || previousSlot == -1 || !Wrapper.nullCheck()) return;
        InventoryUtil.selectSlot(previousSlot);
        previousSlot = -1;
    }
}
