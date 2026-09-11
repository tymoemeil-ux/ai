package com.ares.modules.combat;

import com.ares.Ares;
import com.ares.core.combat.TargetUtil;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.rotation.RotationUtil;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.InteractionUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.render.RenderUtil3D;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** KillAura / Aura - automatyczne atakowanie celow w zasiegu. */
public final class KillAura extends Module {

    public enum Weapon { ANY, SWORD, AXE }

    private final ModeSetting<TargetUtil.SortMode> sort = add(new ModeSetting<>("Sort", "Sposob wyboru celu",
            TargetUtil.SortMode.CLOSEST).group("Targeting"));
    private final ModeSetting<TargetUtil.Filter> filter = add(new ModeSetting<>("Filter", "Kogo atakowac",
            TargetUtil.Filter.PLAYERS).group("Targeting"));
    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg ataku", 3.6f, 1f, 8f).group("Targeting"));
    private final BoolSetting ignoreFriends = add(new BoolSetting("Ignore Friends", "Pomijaj znajomych", true).group("Targeting"));
    private final BoolSetting ignoreNaked = add(new BoolSetting("Ignore Naked", "Pomijaj bez pancerza", false).group("Targeting"));

    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie miedzy atakami (ticki)", 10, 0, 20).group("Attack"));
    private final BoolSetting onlyWeapon = add(new BoolSetting("Only Weapon", "Atakuj tylko bronmieczem/tporem", true).group("Attack"));
    private final ModeSetting<Weapon> weaponMode = add(new ModeSetting<>("Weapon", "Preferowana bron", Weapon.ANY).group("Attack"));
    private final BoolSetting autoSwitch = add(new BoolSetting("Auto Switch", "Sam przelaczaj na bron", false).group("Attack"));
    private final BoolSetting shieldBreaker = add(new BoolSetting("Shield Breaker", "Uzywaj topora na tarcze", true).group("Attack"));

    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Obracaj sie na cel", true).group("Rotations"));
    private final BoolSetting render = add(new BoolSetting("Render", "Podswietlaj cel", true).group("Render"));

    private final TickTimer timer = new TickTimer();
    private LivingEntity target;

    public KillAura() {
        super("KillAura", "Automatycznie atakuje pobliskie cele", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        target = TargetUtil.best(range.get(), sort.get(), filter.get(), ignoreFriends.get(), Ares.get().friends());
        if (target == null) return;
        if (ignoreNaked.get() && target.getArmor() == 0) return;
        if (target.squaredDistanceTo(Wrapper.player()) > range.get() * range.get()) return;
        if (onlyWeapon.get() && !PlayerUtil.holdingWeapon() && !autoSwitch.get()) return;

        if (autoSwitch.get() && !PlayerUtil.holdingWeapon()) {
            int slot = findWeaponSlot();
            if (slot == -1) return;
            InventoryUtil.selectSlot(slot);
            return;
        }

        if (shieldBreaker.get() && target instanceof PlayerEntity player && player.isBlocking()) {
            int axe = findAxeSlot();
            if (axe != -1 && InventoryUtil.mainHand().getItem() != Items.NETHERITE_AXE
                    && InventoryUtil.mainHand().getItem() != Items.DIAMOND_AXE) {
                InventoryUtil.selectSlot(axe);
                return;
            }
        }

        // czekaj na cooldown ataku (1.9+)
        if (Wrapper.player().getAttackCooldownProgress(0.5f) < 0.9f) return;

        if (rotate.get()) {
            RotationUtil.setRotation(RotationUtil.toEntity(target, false));
        }

        InteractionUtil.attack(target, true);
        Wrapper.player().resetLastAttackedTicks();
        timer.reset();
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

    private int findAxeSlot() {
        net.minecraft.entity.player.PlayerInventory inv = InventoryUtil.inventory();
        if (inv == null) return -1;
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.Item item = inv.getStack(i).getItem();
            if (item == Items.NETHERITE_AXE || item == Items.DIAMOND_AXE || item == Items.IRON_AXE) return i;
        }
        return -1;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || target == null || !target.isAlive()) return;
        RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(),
                target.getBoundingBox().expand(0.05, 0.05, 0.05), 0xFFFF3366);
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public String info() {
        return target == null ? null : target.getName().getString();
    }
}
