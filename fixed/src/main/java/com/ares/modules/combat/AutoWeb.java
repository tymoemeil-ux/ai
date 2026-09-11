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
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Auto Web - stawia pajeczyny pod wrogiem lub pod soba. */
public final class AutoWeb extends Module {

    private final BoolSetting onTarget = add(new BoolSetting("On Target", "Stawiaj pod wrogiem", true).group("General"));
    private final BoolSetting onSelf = add(new BoolSetting("On Self", "Stawiaj pod soba", false).group("General"));
    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg", 5f, 1f, 8f).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 4, 0, 20).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj przy stawianiu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent Switch", "Ciche przelaczanie", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public AutoWeb() {
        super("Auto Web", "Automatyczne stawianie pajeczyn", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        int previous = Wrapper.player().getInventory().getSelectedSlot();
        int slot = InventoryUtil.findBlockHotbarSlot(Blocks.COBWEB);
        if (slot == -1) return;

        if (silent.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        boolean placed = false;

        if (onTarget.get()) {
            LivingEntity target = TargetUtil.best(range.get(), TargetUtil.SortMode.CLOSEST,
                    TargetUtil.Filter.PLAYERS, true, Ares.get().friends());
            if (target != null) {
                BlockPos pos = target.getBlockPos();
                if (BlockUtil.isReplaceable(pos)) {
                    placed = InteractionUtil.place(pos, rotate.get(), Hand.MAIN_HAND, true);
                }
            }
        }

        if (!placed && onSelf.get()) {
            BlockPos pos = Wrapper.player().getBlockPos();
            if (BlockUtil.isReplaceable(pos)) {
                placed = InteractionUtil.place(pos, rotate.get(), Hand.MAIN_HAND, true);
            }
        }

        if (silent.get()) InventoryUtil.selectSilently(previous);
        if (placed) timer.reset();
    }
}
