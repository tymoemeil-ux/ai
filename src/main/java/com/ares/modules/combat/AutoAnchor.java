package com.ares.modules.combat;

import com.ares.Ares;
import com.ares.core.combat.AnchorUtil;
import com.ares.core.combat.DamageUtil;
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
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.InteractionUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.render.RenderUtil3D;
import com.ares.core.util.timer.TickTimer;
import com.ares.core.util.world.BlockUtil;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * Auto Anchor - stawia anchory, laduje glowstonem i detonuje gdy oplaca sie to obrazeniami.
 */
public final class AutoAnchor extends Module {

    public enum RotateMode { NONE, FULL, SILENT }
    public enum SwitchMode { NONE, SILENT, NORMAL }

    private final ModeSetting<TargetUtil.SortMode> sort = add(new ModeSetting<>("Sort", "Sposob wyboru celu",
            TargetUtil.SortMode.CLOSEST).group("Targeting"));
    private final FloatSetting targetRange = add(new FloatSetting("Target Range", "Zasieg szukania celu", 10f, 2f, 20f).group("Targeting"));
    private final BoolSetting ignoreFriends = add(new BoolSetting("Ignore Friends", "Pomijaj znajomych", true).group("Targeting"));

    private final FloatSetting placeRange = add(new FloatSetting("Place Range", "Zasieg stawiania", 5f, 1f, 8f).group("Place"));
    private final IntSetting maxHeight = add(new IntSetting("Max Height", "Maksymalna wysokosc nad celem", 1, 0, 3).group("Place"));
    private final BoolSetting autoGlowstone = add(new BoolSetting("Auto Glowstone", "Sam laduj glowstonem", true).group("Place"));
    private final IntSetting charges = add(new IntSetting("Charges", "Ile ladunkow przed wybuchem", 4, 1, 4).group("Place"));
    private final FloatSetting minDamage = add(new FloatSetting("Min Damage", "Minimalne obrazenia przy wybuchu", 8f, 0f, 36f).group("Damage"));
    private final FloatSetting maxSelfDamage = add(new FloatSetting("Max Self Damage", "Maksymalne obrazenia wlasne", 8f, 0f, 36f).group("Damage"));
    private final BoolSetting antiSuicide = add(new BoolSetting("Anti Suicide", "Nie wysadzaj sie", true).group("Damage"));
    private final BoolSetting lethalOverride = add(new BoolSetting("Lethal Override", "Wysadzaj gdy zabije cel", true).group("Damage"));

    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie miedzy akcjami (ticki)", 2, 0, 20).group("Timing"));
    private final ModeSetting<RotateMode> rotate = add(new ModeSetting<>("Rotate", "Obracanie", RotateMode.FULL).group("Rotations"));
    private final ModeSetting<SwitchMode> switchMode = add(new ModeSetting<>("Switch", "Przelaczanie itemow", SwitchMode.SILENT).group("Rotations"));

    private final BoolSetting render = add(new BoolSetting("Render", "Pokazuj anchora", true).group("Render"));
    private final ColorSetting renderColor = add(new ColorSetting("Render Color", "Kolor podswietlenia", 0x55FF7A45).group("Render"));

    private final TickTimer timer = new TickTimer();
    private LivingEntity target;
    private BlockPos anchorPos;
    private Phase phase = Phase.PLACE;

    private enum Phase { PLACE, CHARGE, DETONATE }

    public AutoAnchor() {
        super("Auto Anchor", "Automatyczny cykl anchor: postaw -> naladuj -> wysadz", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (!AnchorUtil.canExplodeHere()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        target = TargetUtil.best(targetRange.get(), sort.get(), TargetUtil.Filter.PLAYERS,
                ignoreFriends.get(), Ares.get().friends());
        if (target == null) return;

        switch (phase) {
            case PLACE -> {
                if (placeAnchor()) {
                    anchorPos = lastPlaced;
                    phase = Phase.CHARGE;
                    timer.reset();
                }
            }
            case CHARGE -> {
                if (anchorPos == null || !AnchorUtil.isAnchor(anchorPos)) {
                    phase = Phase.PLACE;
                    return;
                }
                int current = AnchorUtil.charges(anchorPos);
                if (current >= charges.get()) {
                    phase = Phase.DETONATE;
                    timer.reset();
                    return;
                }
                if (autoGlowstone.get()) {
                    chargeAnchor();
                    timer.reset();
                } else {
                    phase = Phase.DETONATE;
                }
            }
            case DETONATE -> {
                if (anchorPos == null || !AnchorUtil.isAnchor(anchorPos)) {
                    phase = Phase.PLACE;
                    return;
                }
                if (detonate()) {
                    anchorPos = null;
                    phase = Phase.PLACE;
                    timer.reset();
                }
            }
        }
    }

    private BlockPos lastPlaced;

    private boolean placeAnchor() {
        List<BlockPos> positions = AnchorUtil.placementsAround(target.getBlockPos(), placeRange.get(), maxHeight.get());
        BlockPos best = null;
        float bestDamage = 0;

        for (BlockPos pos : positions) {
            float damage = AnchorUtil.damage(target, pos);
            float self = AnchorUtil.damage(Wrapper.player(), pos);
            if (antiSuicide.get() && DamageUtil.isLethal(self, Wrapper.player())) continue;
            if (self > maxSelfDamage.get()) continue;
            if (damage > bestDamage) {
                bestDamage = damage;
                best = pos;
            }
        }

        if (best == null) return false;

        int previous = Wrapper.player().getInventory().getSelectedSlot();
        int anchorSlot = InventoryUtil.findHotbarItem(Items.RESPAWN_ANCHOR);
        if (anchorSlot == -1) return false;

        if (switchMode.get() == SwitchMode.NORMAL) InventoryUtil.selectSlot(anchorSlot);
        else InventoryUtil.selectSilently(anchorSlot);

        if (rotate.get() != RotateMode.NONE) {
            com.ares.core.rotation.RotationUtil.setRotation(
                    com.ares.core.rotation.RotationUtil.toPoint(
                            new net.minecraft.util.math.Vec3d(best.getX() + 0.5, best.getY() + 0.5, best.getZ() + 0.5)));
        }

        boolean placed = InteractionUtil.place(best, false, Hand.MAIN_HAND, true);
        if (switchMode.get() == SwitchMode.SILENT) InventoryUtil.selectSilently(previous);

        if (placed) lastPlaced = best;
        return placed;
    }

    private void chargeAnchor() {
        int previous = Wrapper.player().getInventory().getSelectedSlot();
        int glowSlot = InventoryUtil.findHotbarItem(Items.GLOWSTONE);
        if (glowSlot == -1) return;

        if (switchMode.get() == SwitchMode.NORMAL) InventoryUtil.selectSlot(glowSlot);
        else InventoryUtil.selectSilently(glowSlot);

        if (rotate.get() != RotateMode.NONE) {
            com.ares.core.rotation.RotationUtil.setRotation(
                    com.ares.core.rotation.RotationUtil.toPoint(
                            new net.minecraft.util.math.Vec3d(anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5)));
        }

        InteractionUtil.rightClickBlock(anchorPos, Hand.MAIN_HAND, false);

        if (switchMode.get() == SwitchMode.SILENT) InventoryUtil.selectSilently(previous);
    }

    private boolean detonate() {
        float damage = AnchorUtil.damage(target, anchorPos);
        float self = AnchorUtil.damage(Wrapper.player(), anchorPos);

        if (antiSuicide.get() && DamageUtil.isLethal(self, Wrapper.player())) return false;
        if (self > maxSelfDamage.get()) return false;

        boolean lethal = lethalOverride.get() && DamageUtil.isLethal(damage, target);
        if (!lethal && damage < minDamage.get()) return false;

        int previous = Wrapper.player().getInventory().getSelectedSlot();
        int nonGlowSlot = findNonGlowstoneSlot();
        if (nonGlowSlot != -1) {
            if (switchMode.get() == SwitchMode.NORMAL) InventoryUtil.selectSlot(nonGlowSlot);
            else InventoryUtil.selectSilently(nonGlowSlot);
        }

        if (rotate.get() != RotateMode.NONE) {
            com.ares.core.rotation.RotationUtil.setRotation(
                    com.ares.core.rotation.RotationUtil.toPoint(
                            new net.minecraft.util.math.Vec3d(anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5)));
        }

        boolean result = InteractionUtil.rightClickBlock(anchorPos, Hand.MAIN_HAND, false);
        if (switchMode.get() == SwitchMode.SILENT) InventoryUtil.selectSilently(previous);
        return result;
    }

    private int findNonGlowstoneSlot() {
        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem() != Items.GLOWSTONE && !inv.getStack(i).isEmpty()) return i;
        }
        return -1;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || anchorPos == null) return;
        if (!AnchorUtil.isAnchor(anchorPos)) return;
        RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), anchorPos, renderColor.get(), 1f);
        RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), new Box(anchorPos), 0xFFFF7A45);
    }

    @Override
    public void onDisable() {
        target = null;
        anchorPos = null;
        phase = Phase.PLACE;
    }

    @Override
    public String info() {
        return phase.name().toLowerCase();
    }
}
