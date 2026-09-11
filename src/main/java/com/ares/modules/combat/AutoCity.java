package com.ares.modules.combat;

import com.ares.Ares;
import com.ares.core.combat.HoleUtil;
import com.ares.core.combat.TargetUtil;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.render.RenderUtil3D;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Auto City - niszczy blok oslaniajacy wroga w dziurze (pickaxe). */
public final class AutoCity extends Module {

    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg", 6f, 1f, 10f).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 2, 0, 20).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj w strone bloku", true).group("General"));
    private final BoolSetting render = add(new BoolSetting("Render", "Podswietlaj niszczony blok", true).group("Render"));
    private final ColorSetting renderColor = add(new ColorSetting("Render Color", "Kolor", 0x55FF0000).group("Render"));

    private final TickTimer timer = new TickTimer();
    private BlockPos targetBlock;

    public AutoCity() {
        super("Auto City", "Niszczy oslone przeciwnika w dziurze", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        LivingEntity target = TargetUtil.best(range.get(), TargetUtil.SortMode.CLOSEST,
                TargetUtil.Filter.PLAYERS, true, Ares.get().friends());
        if (target == null || !HoleUtil.isInHole(target)) return;

        BlockPos feet = target.getBlockPos();
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos pos = feet.offset(direction);
            if (!HoleUtil.isBlastResistant(pos)) continue;
            if (Wrapper.player().getEyePos().distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) > range.get()) {
                continue;
            }

            int pick = findPickaxeSlot();
            if (pick == -1) return;
            InventoryUtil.selectSlot(pick);

            if (rotate.get()) {
                com.ares.core.rotation.RotationUtil.setRotation(
                        com.ares.core.rotation.RotationUtil.toPoint(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
            }

            targetBlock = pos;
            Wrapper.interaction().updateBlockBreakingProgress(pos, direction);
            timer.reset();
            return;
        }
    }

    private int findPickaxeSlot() {
        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.Item item = inv.getStack(i).getItem();
            if (item == Items.NETHERITE_PICKAXE || item == Items.DIAMOND_PICKAXE
                    || item == Items.IRON_PICKAXE || item == Items.STONE_PICKAXE) return i;
        }
        return -1;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || targetBlock == null) return;
        RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), targetBlock, renderColor.get(), 1f);
        RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(),
                new net.minecraft.util.math.Box(targetBlock), 0xFFFF0000);
    }

    @Override
    public void onDisable() {
        targetBlock = null;
    }
}
