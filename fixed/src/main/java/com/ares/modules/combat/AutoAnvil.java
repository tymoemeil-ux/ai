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

/** Auto Anvil - stawia kowadla nad glowa przeciwnika. */
public final class AutoAnvil extends Module {

    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg", 5f, 1f, 8f).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 6, 0, 20).group("General"));
    private final IntSetting height = add(new IntSetting("Height", "Ile blokow nad celem", 2, 1, 5).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj przy stawianiu", true).group("General"));
    private final BoolSetting silent = add(new BoolSetting("Silent Switch", "Ciche przelaczanie", true).group("General"));
    private final BoolSetting onlyInHole = add(new BoolSetting("Only In Hole", "Tylko gdy cel siedzi w dziurze", true).group("General"));

    private final TickTimer timer = new TickTimer();

    public AutoAnvil() {
        super("Auto Anvil", "Stawia kowadla nad przeciwnikiem", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        LivingEntity target = TargetUtil.best(range.get(), TargetUtil.SortMode.CLOSEST,
                TargetUtil.Filter.PLAYERS, true, Ares.get().friends());
        if (target == null) return;
        if (onlyInHole.get() && !com.ares.core.combat.HoleUtil.isInHole(target)) return;

        BlockPos pos = target.getBlockPos().up(height.get());
        if (!BlockUtil.isReplaceable(pos)) return;

        int previous = Wrapper.player().getInventory().selectedSlot;
        int slot = InventoryUtil.findBlockHotbarSlot(Blocks.ANVIL);
        if (slot == -1) return;

        if (silent.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        if (InteractionUtil.place(pos, rotate.get(), Hand.MAIN_HAND, true)) timer.reset();

        if (silent.get()) InventoryUtil.selectSilently(previous);
    }
}
