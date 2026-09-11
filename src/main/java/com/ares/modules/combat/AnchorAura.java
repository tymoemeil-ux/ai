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
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.InteractionUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.render.RenderUtil3D;
import com.ares.core.util.timer.TickTimer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Anchor Aura - agresywny wariant Auto Anchor: stawia kilka anchorow naraz,
 * laduje je i detonuje w momencie gdy wybuch zada zadane obrazenia.
 */
public final class AnchorAura extends Module {

    private final FloatSetting targetRange = add(new FloatSetting("Target Range", "Zasieg szukania celu", 10f, 2f, 20f).group("Targeting"));
    private final BoolSetting ignoreFriends = add(new BoolSetting("Ignore Friends", "Pomijaj znajomych", true).group("Targeting"));

    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg dzialania", 5f, 1f, 8f).group("Aura"));
    private final IntSetting maxAnchors = add(new IntSetting("Max Anchors", "Ile anchorow trzymac naraz", 2, 1, 6).group("Aura"));
    private final IntSetting placePerTick = add(new IntSetting("Place / Tick", "Ile stawiac na tick", 1, 1, 5).group("Aura"));
    private final IntSetting chargePerTick = add(new IntSetting("Charge / Tick", "Ile klikniec ladujacych na tick", 1, 1, 5).group("Aura"));
    private final IntSetting chargesNeeded = add(new IntSetting("Charges", "Ladunki przed wybuchem", 4, 1, 4).group("Aura"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 1, 0, 20).group("Aura"));

    private final FloatSetting minDamage = add(new FloatSetting("Min Damage", "Minimalne obrazenia", 6f, 0f, 36f).group("Damage"));
    private final FloatSetting maxSelfDamage = add(new FloatSetting("Max Self Damage", "Maksymalne obrazenia wlasne", 9f, 0f, 36f).group("Damage"));
    private final BoolSetting antiSuicide = add(new BoolSetting("Anti Suicide", "Nie wysadzaj sie", true).group("Damage"));
    private final BoolSetting lethalOverride = add(new BoolSetting("Lethal Override", "Wybuch gdy zabije cel", true).group("Damage"));

    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj w strone anchora", true).group("Rotations"));
    private final BoolSetting silentSwitch = add(new BoolSetting("Silent Switch", "Ciche przelaczanie itemow", true).group("Rotations"));

    private final BoolSetting render = add(new BoolSetting("Render", "Podswietlaj anchory", true).group("Render"));
    private final ColorSetting renderColor = add(new ColorSetting("Render Color", "Kolor", 0x44FFCC00).group("Render"));

    private final TickTimer timer = new TickTimer();
    private final List<BlockPos> anchors = new ArrayList<>();
    private LivingEntity target;

    public AnchorAura() {
        super("Anchor Aura", "Aura anchorow: stawiaj, laduj, wysadzaj w kolko", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (!AnchorUtil.canExplodeHere()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        target = TargetUtil.best(targetRange.get(), TargetUtil.SortMode.CLOSEST, TargetUtil.Filter.PLAYERS,
                ignoreFriends.get(), Ares.get().friends());
        if (target == null) return;

        anchors.removeIf(pos -> !AnchorUtil.isAnchor(pos));

        // 1. Laduj wszystkie postawione anchory
        int charged = 0;
        for (BlockPos pos : new ArrayList<>(anchors)) {
            if (charged >= chargePerTick.get()) break;
            if (!AnchorUtil.canCharge(pos)) continue;
            if (AnchorUtil.charges(pos) >= chargesNeeded.get()) continue;
            if (charge(pos)) charged++;
        }

        // 2. Detonuj te, ktore spelniaja warunki obrazen
        for (BlockPos pos : new ArrayList<>(anchors)) {
            if (AnchorUtil.charges(pos) < chargesNeeded.get()) continue;
            if (shouldDetonate(pos)) {
                detonate(pos);
                anchors.remove(pos);
                break;
            }
        }

        // 3. Stawiaj kolejne, jesli jest miejsce
        if (anchors.size() < maxAnchors.get()) {
            int placed = 0;
            for (BlockPos pos : AnchorUtil.placementsAround(target.getBlockPos(), range, 1)) {
                if (placed >= placePerTick.get()) break;
                if (anchors.contains(pos) || AnchorUtil.isAnchor(pos)) continue;
                if (place(pos)) {
                    anchors.add(pos);
                    placed++;
                }
            }
        }
        timer.reset();
    }

    private boolean place(BlockPos pos) {
        int previous = Wrapper.player().getInventory().getSelectedSlot();
        int slot = InventoryUtil.findHotbarItem(Items.RESPAWN_ANCHOR);
        if (slot == -1) return false;

        if (silentSwitch.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        if (rotate.get()) {
            com.ares.core.rotation.RotationUtil.setRotation(
                    com.ares.core.rotation.RotationUtil.toPoint(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
        }

        boolean result = InteractionUtil.place(pos, false, Hand.MAIN_HAND, true);
        if (silentSwitch.get()) InventoryUtil.selectSilently(previous);
        return result;
    }

    private boolean charge(BlockPos pos) {
        int previous = Wrapper.player().getInventory().getSelectedSlot();
        int slot = InventoryUtil.findHotbarItem(Items.GLOWSTONE);
        if (slot == -1) return false;

        if (silentSwitch.get()) InventoryUtil.selectSilently(slot);
        else InventoryUtil.selectSlot(slot);

        if (rotate.get()) {
            com.ares.core.rotation.RotationUtil.setRotation(
                    com.ares.core.rotation.RotationUtil.toPoint(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
        }

        boolean result = InteractionUtil.rightClickBlock(pos, Hand.MAIN_HAND, false);
        if (silentSwitch.get()) InventoryUtil.selectSilently(previous);
        return result;
    }

    private boolean shouldDetonate(BlockPos pos) {
        float damage = AnchorUtil.damage(target, pos);
        float self = AnchorUtil.damage(Wrapper.player(), pos);
        if (antiSuicide.get() && DamageUtil.isLethal(self, Wrapper.player())) return false;
        if (self > maxSelfDamage.get()) return false;
        boolean lethal = lethalOverride.get() && DamageUtil.isLethal(damage, target);
        return lethal || damage >= minDamage.get();
    }

    private void detonate(BlockPos pos) {
        int previous = Wrapper.player().getInventory().getSelectedSlot();
        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv != null) {
            for (int i = 0; i < 9; i++) {
                if (!inv.getStack(i).isEmpty() && inv.getStack(i).getItem() != Items.GLOWSTONE) {
                    if (silentSwitch.get()) InventoryUtil.selectSilently(i);
                    else InventoryUtil.selectSlot(i);
                    break;
                }
            }
        }
        if (rotate.get()) {
            com.ares.core.rotation.RotationUtil.setRotation(
                    com.ares.core.rotation.RotationUtil.toPoint(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
        }
        InteractionUtil.rightClickBlock(pos, Hand.MAIN_HAND, false);
        if (silentSwitch.get()) InventoryUtil.selectSilently(previous);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get()) return;
        for (BlockPos pos : anchors) {
            RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), pos, renderColor.get(), 1f);
            RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), new Box(pos), 0xFFFFCC00);
        }
    }

    @Override
    public void onDisable() {
        anchors.clear();
        target = null;
    }

    @Override
    public String info() {
        return String.valueOf(anchors.size());
    }
}
