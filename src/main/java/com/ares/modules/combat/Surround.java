package com.ares.modules.combat;

import com.ares.core.combat.HoleUtil;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Surround - obstawia sie blokami odpornymi na wybuch. */
public final class Surround extends Module {

    private final IntSetting blocksPerTick = add(new IntSetting("Blocks / Tick", "Ile blokow na tick", 2, 1, 8).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 1, 0, 10).group("General"));
    private final BoolSetting center = add(new BoolSetting("Center", "Centruj gracza na bloku", true).group("General"));
    private final BoolSetting disableOnJump = add(new BoolSetting("Disable On Jump", "Wylacz przy skoku", false).group("General"));
    private final BoolSetting disableOnMove = add(new BoolSetting("Disable On Move", "Wylacz gdy gracz sie rusza", false).group("General"));
    private final BoolSetting airPlace = add(new BoolSetting("Air Place", "Stawiaj bez podparcia", true).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj przy stawianiu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent Switch", "Ciche przelaczanie", true).group("General"));
    private final BoolSetting onlyWhenEnemies = add(new BoolSetting("Only Enemies Near", "Tylko gdy wrog jest blisko", false).group("General"));
    private final FloatSetting enemyRange = add(new FloatSetting("Enemy Range", "Zasieg wykrywania wroga", 8f, 1f, 20f).group("General"));

    private final TickTimer timer = new TickTimer();

    public Surround() {
        super("Surround", "Obstawia gracza blokami odpornymi na wybuchy", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (disableOnJump.get() && !Wrapper.player().isOnGround()) return;
        if (disableOnMove.get() && Wrapper.player().getVelocity().horizontalLengthSquared() > 0.01) return;

        boolean enemyNear = true;
        if (onlyWhenEnemies.get()) {
            enemyNear = false;
            for (net.minecraft.entity.player.PlayerEntity player : com.ares.core.util.world.EntityUtil.otherPlayers()) {
                if (player.squaredDistanceTo(Wrapper.player()) <= enemyRange.get() * enemyRange.get()) {
                    enemyNear = true;
                    break;
                }
            }
        }
        if (!enemyNear) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        BlockPos feet = Wrapper.player().getBlockPos();
        List<BlockPos> positions = HoleUtil.surroundPositions(feet);

        int previous = Wrapper.player().getInventory().selectedSlot;
        int blockSlot = InventoryUtil.findBlockHotbarSlot(Blocks.OBSIDIAN);
        if (blockSlot == -1) return;

        if (silent.get()) InventoryUtil.selectSilently(blockSlot);
        else InventoryUtil.selectSlot(blockSlot);

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
