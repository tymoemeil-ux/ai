package com.ares.modules.combat;

import com.ares.Ares;
import com.ares.core.combat.CrystalUtil;
import com.ares.core.combat.DamageUtil;
import com.ares.core.combat.HoleUtil;
import com.ares.core.combat.TargetUtil;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.rotation.Rotation;
import com.ares.core.rotation.RotationUtil;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Auto Crystal - serce klienta Ares.
 *
 * Pelny cykl: cel -> kalkulacja obrazen -> wybor najlepszej pozycji ->
 * rotacja -> przelaczenie na krysztal -> postawienie -> zniszczenie.
 */
public final class AutoCrystal extends Module {

    public enum RotateMode { NONE, SILENT, FULL, YAW_STEP }

    public enum SwitchMode { NONE, SILENT, NORMAL }

    private final ModeSetting<TargetUtil.SortMode> sort = add(new ModeSetting<>("Sort", "Sposob wyboru celu",
            TargetUtil.SortMode.CLOSEST).group("Targeting"));
    private final ModeSetting<TargetUtil.Filter> filter = add(new ModeSetting<>("Filter", "Kogo brac pod uwage",
            TargetUtil.Filter.PLAYERS).group("Targeting"));
    private final FloatSetting targetRange = add(new FloatSetting("Target Range", "Zasieg szukania celu", 12f, 2f, 20f).group("Targeting"));
    private final BoolSetting ignoreFriends = add(new BoolSetting("Ignore Friends", "Pomijaj znajomych", true).group("Targeting"));

    private final IntSetting placeDelay = add(new IntSetting("Place Delay", "Opuznienie stawiania (ticki)", 1, 0, 20).group("Timing"));
    private final IntSetting breakDelay = add(new IntSetting("Break Delay", "Opuznienie niszczenia (ticki)", 1, 0, 20).group("Timing"));
    private final BoolSetting sequential = add(new BoolSetting("Sequential", "Najpierw postaw, potem zniszcz (nie w tym samym ticku)", true).group("Timing"));
    private final BoolSetting breakFirst = add(new BoolSetting("Break First", "Zniszcz krysztal przed postawieniem", true).group("Timing"));
    private final IntSetting actionsPerTick = add(new IntSetting("Actions / Tick", "Ile akcji na tick", 1, 1, 5).group("Timing"));
    private final IntSetting maxPositions = add(new IntSetting("Max Positions", "Ile pozycji sprawdzac na tick (mniej = plynniej)", 8, 2, 24).group("Timing"));
    private final IntSetting calcEvery = add(new IntSetting("Calc Every", "Liczenie co ile tickow (2 = dwa razy rzadziej, plynniej)", 1, 1, 4).group("Timing"));

    private final BoolSetting place = add(new BoolSetting("Place", "Stawiaj krysztaly", true).group("Place"));
    private final FloatSetting placeRange = add(new FloatSetting("Place Range", "Zasieg stawiania", 5f, 1f, 8f).group("Place"));
    private final FloatSetting placeWallRange = add(new FloatSetting("Place Wall Range", "Zasieg przez sciany", 4f, 0f, 8f).group("Place"));
    private final FloatSetting minPlaceDamage = add(new FloatSetting("Min Place Damage", "Minimalne obrazenia przy stawianiu", 5f, 0f, 36f).group("Place"));
    private final FloatSetting maxSelfPlace = add(new FloatSetting("Max Self Place", "Maksymalne obrazenia wlasne", 8f, 0f, 36f).group("Place"));
    private final BoolSetting placeRaytrace = add(new BoolSetting("Place Raytrace", "Sprawdzaj widocznosc przy stawianiu", false).group("Place"));
    private final BoolSetting facePlace = add(new BoolSetting("Face Place", "Stawiaj na wysokosci glowy", true).group("Place"));
    private final BoolSetting antiSuicide = add(new BoolSetting("Anti Suicide", "Nie zabijaj sie wlasnym krysztalem", true).group("Place"));
    private final ModeSetting<SwitchMode> switchMode = add(new ModeSetting<>("Switch", "Przelaczanie na krysztal", SwitchMode.SILENT).group("Place"));
    private final BoolSetting onlyHoldingCrystal = add(new BoolSetting("Only Holding Crystal", "Dzialaj tylko z krysztalem w rece", false).group("Place"));

    private final BoolSetting doBreak = add(new BoolSetting("Break", "Niszcz krysztaly", true).group("Break"));
    private final FloatSetting breakRange = add(new FloatSetting("Break Range", "Zasieg niszczenia", 5f, 1f, 8f).group("Break"));
    private final FloatSetting breakWallRange = add(new FloatSetting("Break Wall Range", "Zasieg niszczenia przez sciany", 4f, 0f, 8f).group("Break"));
    private final FloatSetting minBreakDamage = add(new FloatSetting("Min Break Damage", "Minimalne obrazenia przy niszczeniu", 4f, 0f, 36f).group("Break"));
    private final FloatSetting maxSelfBreak = add(new FloatSetting("Max Self Break", "Maksymalne obrazenia wlasne", 8f, 0f, 36f).group("Break"));
    private final BoolSetting breakRaytrace = add(new BoolSetting("Break Raytrace", "Sprawdzaj widocznosc krysztalu", false).group("Break"));
    private final IntSetting breakAttempts = add(new IntSetting("Break Attempts", "Ile krysztalow na tick", 1, 1, 5).group("Break"));

    private final FloatSetting minRatio = add(new FloatSetting("Min Ratio", "Stosunek obrazen celu do wlasnych", 1.2f, 0f, 10f).group("Damage"));
    private final BoolSetting lethalOverride = add(new BoolSetting("Lethal Override", "Ignoruj limity gdy wybuch zabije cel", true).group("Damage"));
    private final BoolSetting forcePlace = add(new BoolSetting("Force Place", "Stawiaj agresywnie gdy cel ma malo HP", true).group("Damage"));
    private final FloatSetting forcePlaceHealth = add(new FloatSetting("Force HP", "Ponizej ilu HP forsowac", 10f, 1f, 36f).group("Damage"));
    private final BoolSetting predict = add(new BoolSetting("Predict", "Przewiduj ruch celu", true).group("Damage"));
    private final BoolSetting antiWeakness = add(new BoolSetting("Anti Weakness", "Przelacz na miecz gdy weakness", true).group("Damage"));

    private final ModeSetting<RotateMode> rotate = add(new ModeSetting<>("Rotate", "Sposob obracania", RotateMode.FULL).group("Rotations"));
    private final IntSetting yawStep = add(new IntSetting("Yaw Step", "Maksymalna zmiana yawa na tick", 180, 5, 180).group("Rotations"));

    private final BoolSetting renderPlace = add(new BoolSetting("Render Place", "Pokazuj gdzie stawia", true).group("Render"));
    private final BoolSetting renderBreak = add(new BoolSetting("Render Break", "Pokazuj niszczony krysztal", true).group("Render"));
    private final ColorSetting placeFill = add(new ColorSetting("Place Fill", "Kolor wypelnienia", 0x407C5CFF).group("Render"));
    private final ColorSetting placeLine = add(new ColorSetting("Place Line", "Kolor krawedzi", 0xFF7C5CFF).group("Render"));
    private final BoolSetting renderTarget = add(new BoolSetting("Render Target", "Obrysuj cel", true).group("Render"));
    private final ColorSetting targetLine = add(new ColorSetting("Target Line", "Kolor obrysu celu", 0xFF22D3EE).group("Render"));

    private final TickTimer placeTimer = new TickTimer();
    private final TickTimer breakTimer = new TickTimer();
    private final TickTimer targetTimer = new TickTimer();

    private LivingEntity target;
    private BlockPos renderPos;
    private EndCrystalEntity renderCrystal;
    private long renderStartTime;
    private float lastDamage;
    private float lastSelfDamage;
    private int swingHandTicks;
    private int calcTick;

    public AutoCrystal() {
        super("Auto Crystal", "Automatyczne stawianie i niszczenie krysztalow endu", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        targetTimer.increment();
        placeTimer.increment();
        breakTimer.increment();

        if (targetTimer.passed(10, false) || target == null || !target.isAlive() || target.isDead()
                || target.squaredDistanceTo(Wrapper.player()) > targetRange.get() * targetRange.get()) {
            target = TargetUtil.best(targetRange.get(), sort.get(), filter.get(), ignoreFriends.get(), Ares.get().friends());
            targetTimer.reset();
        }

        if (target == null) return;

        if (onlyHoldingCrystal.get() && !InventoryUtil.isCrystal(InventoryUtil.mainHand())) return;

        boolean breakNow = doBreak.get() && breakTimer.passed(breakDelay.get()) && (!sequential.get() || breakFirst.get());
        boolean placeNow = place.get() && placeTimer.passed(placeDelay.get()) && (!sequential.get() || !breakNow);

        if (breakNow) {
            int attempts = 0;
            for (int i = 0; i < breakAttempts.get() && attempts < actionsPerTick.get(); i++) {
                if (!breakCrystal()) break;
                attempts++;
            }
            if (sequential.get() && attempts > 0) breakTimer.reset();
        }

        if (placeNow) {
            int attempts = 0;
            for (int i = 0; i < actionsPerTick.get() && attempts < 1; i++) {
                if (!placeCrystal()) break;
                attempts++;
            }
            if (sequential.get() && attempts > 0) placeTimer.reset();
        }
    }

    /** Niszczenie najlepszego krysztalu. */
    private boolean breakCrystal() {
        Vec3d eyePos = Wrapper.player().getEyePos();
        List<EndCrystalEntity> crystals = CrystalUtil.crystalsInRange(eyePos, breakRange.get());

        EndCrystalEntity best = null;
        float bestDamage = 0;

        for (EndCrystalEntity crystal : crystals) {
            if (!crystal.isAlive()) continue;

            boolean visible = !breakRaytrace.get() || BlockUtil.canSee(eyePos, crystal.getPos().add(0, 1, 0));
            double distance = eyePos.distanceTo(crystal.getPos().add(0, 1, 0));
            if (!visible && distance > breakWallRange.get()) continue;

            Vec3d crystalPos = predict.get() ? crystal.getPos() : crystal.getPos().add(0, 1, 0);
            if (predict.get()) crystalPos = crystalPos.add(0, 1, 0);

            float damage = DamageUtil.crystal(target, crystalPos);
            float self = DamageUtil.crystal(Wrapper.player(), crystalPos);

            if (antiSuicide.get() && DamageUtil.isLethal(self, Wrapper.player())) continue;
            if (self > maxSelfBreak.get()) continue;

            boolean lethal = lethalOverride.get() && DamageUtil.isLethal(damage, target);
            if (!lethal && damage < minBreakDamage.get()) continue;
            if (!lethal && minRatio.get() > 0 && self > 0 && damage / self < minRatio.get()) continue;

            if (damage > bestDamage) {
                bestDamage = damage;
                best = crystal;
            }
        }

        if (best == null) return false;

        lastDamage = bestDamage;
        renderCrystal = best;
        renderStartTime = System.currentTimeMillis();

        int previousSlot = Wrapper.player().getInventory().getSelectedSlot();
        if (antiWeakness.get() && PlayerUtil.hasWeakness() && !PlayerUtil.holdingWeapon()) {
            int sword = findWeaponSlot();
            if (sword != -1) {
                if (switchMode.get() == SwitchMode.NORMAL) InventoryUtil.selectSlot(sword);
                else InventoryUtil.selectSilently(sword);
            }
        }

        Rotation rotation = RotationUtil.toPoint(best.getPos().add(0, 1, 0));
        applyRotation(rotation);
        InteractionUtil.attack(best, true, rotation);

        if (switchMode.get() == SwitchMode.SILENT) InventoryUtil.selectSilently(previousSlot);
        return true;
    }

    /** Stawianie krysztalu w najlepszej pozycji. */
    private boolean placeCrystal() {
        // ciezkie liczenie tylko co `calcEvery` tickow - dzieki temu gra nie dostaje zaciesiek
        if (calcTick > 0) {
            calcTick--;
            return false;
        }
        calcTick = Math.max(0, calcEvery.get() - 1);

        BlockPos targetPos = target.getBlockPos();
        List<BlockPos> positions = CrystalUtil.placementsAround(targetPos, placeRange.get(), 2);

        // bierzemy pod uwage tylko najblizsze pozycje (ogranicza liczbe raycastow)
        positions.sort(Comparator.comparingDouble(BlockUtil::distanceToEyes));
        int limit = Math.min(maxPositions.get(), positions.size());

        BlockPos best = null;
        float bestDamage = 0;
        float bestSelf = 0;

        for (int i = 0; i < limit; i++) {
            BlockPos pos = positions.get(i);
            if (BlockUtil.isCrystalAt(pos)) continue;
            if (CrystalUtil.intersectsPlayer(pos)) continue;

            Vec3d crystalPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            // wybuch krysztalu ma promien 12 - dalej nie zadziala, wiec nie liczymy obrazen
            if (target.squaredDistanceTo(crystalPos) > 144.0) continue;

            boolean visible = !placeRaytrace.get() || BlockUtil.canSeeBlock(pos, false);
            double distance = BlockUtil.distanceToEyes(pos);
            if (!visible && distance > placeWallRange.get()) continue;
            if (distance > placeRange.get()) continue;

            float damage = DamageUtil.crystal(target, crystalPos);
            float self = DamageUtil.crystal(Wrapper.player(), crystalPos);

            if (antiSuicide.get() && DamageUtil.isLethal(self, Wrapper.player())) continue;
            if (self > maxSelfPlace.get()) continue;

            boolean lethal = lethalOverride.get() && DamageUtil.isLethal(damage, target);
            boolean force = forcePlace.get() && DamageUtil.totalHealth(target) <= forcePlaceHealth.get();
            boolean weaklyArmored = forcePlace.get() && HoleUtil.isInHole(target);

            if (!lethal && !force && !weaklyArmored) {
                if (damage < minPlaceDamage.get()) continue;
                if (minRatio.get() > 0 && self > 0 && damage / self < minRatio.get()) continue;
            }

            if (facePlace.get() && pos.getY() >= targetPos.getY() + 2) continue;

            if (damage > bestDamage) {
                bestDamage = damage;
                bestSelf = self;
                best = pos;
            }
        }

        if (best == null) return false;

        lastDamage = bestDamage;
        lastSelfDamage = bestSelf;
        renderPos = best;
        renderStartTime = System.currentTimeMillis();

        int previousSlot = Wrapper.player().getInventory().getSelectedSlot();
        int crystalSlot = InventoryUtil.findHotbarItem(Items.END_CRYSTAL);
        if (crystalSlot == -1) return false;

        if (switchMode.get() == SwitchMode.NORMAL) InventoryUtil.selectSlot(crystalSlot);
        else InventoryUtil.selectSilently(crystalSlot);

        Rotation rotation = RotationUtil.toPoint(new Vec3d(best.getX() + 0.5, best.getY() + 1, best.getZ() + 0.5));
        applyRotation(rotation);

        boolean placed = InteractionUtil.place(best, false, Hand.MAIN_HAND, true);

        if (switchMode.get() == SwitchMode.SILENT) InventoryUtil.selectSilently(previousSlot);
        return placed;
    }

    private void applyRotation(Rotation wanted) {
        if (rotate.get() == RotateMode.NONE) return;

        float yaw = wanted.yaw();
        float pitch = wanted.pitch();

        if (rotate.get() == RotateMode.YAW_STEP) {
            float currentYaw = Wrapper.player().getYaw();
            float delta = Math.abs(com.ares.core.util.math.MathUtil.wrapDegrees(yaw - currentYaw));
            if (delta > yawStep.get()) {
                yaw = currentYaw + (yawStep.get() * (com.ares.core.util.math.MathUtil.wrapDegrees(yaw - currentYaw) > 0 ? 1 : -1));
            }
        }

        if (rotate.get() == RotateMode.SILENT) {
            RotationUtil.setRotationSilently(new Rotation(yaw, pitch), () -> {
            });
        } else {
            RotationUtil.setRotation(new Rotation(yaw, pitch));
        }
    }

    private int findWeaponSlot() {
        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.Item item = inv.getStack(i).getItem();
            if (item == Items.NETHERITE_SWORD || item == Items.DIAMOND_SWORD || item == Items.NETHERITE_AXE
                    || item == Items.DIAMOND_AXE || item == Items.MACE) return i;
        }
        return -1;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;

        if (renderPlace.get() && renderPos != null) {
            float progress = Math.min(1f, (System.currentTimeMillis() - renderStartTime) / 400f);
            int fill = com.ares.core.util.math.ColorUtil.withAlpha(placeFill.get(), (int) (110 * (1 - progress) + 40));
            RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), renderPos, fill, 1f);
            RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(),
                    new net.minecraft.util.math.Box(renderPos).expand(0.02, 0.02, 0.02), placeLine.get());
        }

        if (renderBreak.get() && renderCrystal != null && renderCrystal.isAlive()) {
            RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(),
                    renderCrystal.getBoundingBox().expand(0.05, 0.05, 0.05), 0xFFFF4D6D);
        }

        if (renderTarget.get() && target != null && target.isAlive()) {
            RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(),
                    target.getBoundingBox().expand(0.08, 0.08, 0.08), targetLine.get());
        }
    }

    @Override
    public void onDisable() {
        target = null;
        renderPos = null;
        renderCrystal = null;
    }

    @Override
    public String info() {
        if (target == null) return null;
        return String.format("%.1f", lastDamage);
    }

    public LivingEntity target() {
        return target;
    }

    public float lastDamage() {
        return lastDamage;
    }

    public float lastSelfDamage() {
        return lastSelfDamage;
    }

    public List<String> debugInfo() {
        List<String> lines = new ArrayList<>();
        lines.add("Target: " + (target == null ? "none" : target.getName().getString()));
        lines.add("Damage: " + String.format("%.1f", lastDamage));
        return lines;
    }
}
