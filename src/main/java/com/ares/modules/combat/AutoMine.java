package com.ares.modules.combat;

import com.ares.Ares;
import com.ares.core.combat.TargetUtil;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.render.RenderUtil3D;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Auto Mine - kopie blok przy celu (stopach / glowie), zeby go odslonic. */
public final class AutoMine extends Module {

    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg", 6f, 1f, 10f).group("General"));
    private final BoolSetting feet = add(new BoolSetting("Feet", "Kop przy stopach", true).group("General"));
    private final BoolSetting head = add(new BoolSetting("Head", "Kop przy glowie", false).group("General"));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj", true).group("General"));
    private final BoolSetting render = add(new BoolSetting("Render", "Podswietlaj blok", true).group("Render"));
    private final ColorSetting renderColor = add(new ColorSetting("Render Color", "Kolor", 0x55FF8800).group("Render"));

    private final TickTimer timer = new TickTimer();
    private BlockPos targetBlock;

    public AutoMine() {
        super("Auto Mine", "Kopie blok przy przeciwniku", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(2)) return;

        LivingEntity target = TargetUtil.best(range.get(), TargetUtil.SortMode.CLOSEST,
                TargetUtil.Filter.PLAYERS, true, Ares.get().friends());
        if (target == null) return;

        BlockPos feetPos = target.getBlockPos();
        BlockPos chosen = null;

        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos candidate = feet ? feetPos.offset(direction) : feetPos.up().offset(direction);
            if (!com.ares.core.util.world.BlockUtil.isAir(candidate)) {
                chosen = candidate;
                break;
            }
            if (head.get()) {
                BlockPos headCandidate = feetPos.up().offset(direction);
                if (!com.ares.core.util.world.BlockUtil.isAir(headCandidate)) {
                    chosen = headCandidate;
                    break;
                }
            }
        }

        if (chosen == null) return;

        int pick = findPickaxeSlot();
        if (pick == -1) return;
        InventoryUtil.selectSlot(pick);

        if (rotate.get()) {
            com.ares.core.rotation.RotationUtil.setRotation(
                    com.ares.core.rotation.RotationUtil.toPoint(new Vec3d(chosen.getX() + 0.5, chosen.getY() + 0.5, chosen.getZ() + 0.5)));
        }

        targetBlock = chosen;
        Wrapper.interaction().updateBlockBreakingProgress(chosen, Direction.UP);
        timer.reset();
    }

    private int findPickaxeSlot() {
        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.Item item = inv.getStack(i).getItem();
            if (item == Items.NETHERITE_PICKAXE || item == Items.DIAMOND_PICKAXE) return i;
        }
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem() instanceof net.minecraft.item.PickaxeItem) return i;
        }
        return -1;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || targetBlock == null) return;
        RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), targetBlock, renderColor.get(), 1f);
        RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), new Box(targetBlock), 0xFFFF8800);
    }
}
