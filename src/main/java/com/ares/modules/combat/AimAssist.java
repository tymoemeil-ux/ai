package com.ares.modules.combat;

import com.ares.Ares;
import com.ares.core.combat.TargetUtil;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.rotation.Rotation;
import com.ares.core.rotation.RotationUtil;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.math.MathUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.world.BlockUtil;
import net.minecraft.entity.LivingEntity;

/**
 * AimAssist - plynnie dociaga celownik do najblizszego celu.
 * Nie teleportuje kamery: za kazdym tickiem przybliza kat o czesc roznicy,
 * dzieki czemu wyglada to naturalnie.
 */
public final class AimAssist extends Module {

    public enum Mode { LINEAR, DISTANCE, STEP }

    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg dzialania", 4.5f, 1f, 8f).group("Targeting"));
    private final FloatSetting fov = add(new FloatSetting("FOV", "Pole widzenia w ktorym dziala", 90f, 10f, 180f).group("Targeting"));
    private final BoolSetting playersOnly = add(new BoolSetting("Players Only", "Tylko gracze", true).group("Targeting"));
    private final BoolSetting ignoreFriends = add(new BoolSetting("Ignore Friends", "Pomijaj znajomych", true).group("Targeting"));
    private final BoolSetting throughWalls = add(new BoolSetting("Through Walls", "Dzialaj przez sciany", false).group("Targeting"));

    private final ModeSetting<Mode> mode = add(new ModeSetting<>("Mode", "Sposob dociagania", Mode.DISTANCE).group("Aim"));
    private final FloatSetting speed = add(new FloatSetting("Speed", "Szybkosc dociagania", 0.35f, 0.05f, 1f).group("Aim"));
    private final FloatSetting maxStep = add(new FloatSetting("Max Step", "Maksymalna zmiana kąta na tick (stopnie)", 25f, 1f, 90f).group("Aim"));
    private final BoolSetting onlyOnClick = add(new BoolSetting("Only On Click", "Dzialaj tylko przy wcisnietym ataku", true).group("Aim"));
    private final BoolSetting predict = add(new BoolSetting("Predict", "Przewiduj ruch celu", false).group("Aim"));

    private LivingEntity target;

    public AimAssist() {
        super("AimAssist", "Plynnie dociaga celownik do celu", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (onlyOnClick.get() && !Wrapper.mc().options.attackKey.isPressed()) {
            target = null;
            return;
        }

        target = TargetUtil.best(range.get(), TargetUtil.SortMode.CROSSHAIR,
                playersOnly.get() ? TargetUtil.Filter.PLAYERS : TargetUtil.Filter.ALL,
                ignoreFriends.get(), Ares.get().friends());
        if (target == null || !target.isAlive()) return;

        Rotation wanted = RotationUtil.toEntity(target, predict.get());
        net.minecraft.client.network.ClientPlayerEntity player = Wrapper.player();

        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float deltaYaw = MathUtil.wrapDegrees(wanted.yaw() - yaw);
        float deltaPitch = MathUtil.wrapDegrees(wanted.pitch() - pitch);

        // poza polem widzenia - nie ruszamy kamery
        if (Math.abs(deltaYaw) > fov.get() / 2f) return;

        if (!throughWalls.get() && !BlockUtil.canSee(target.getPos().add(0, target.getBoundingBox().getLengthY() * 0.5, 0))) {
            return;
        }

        double factor = switch (mode.get()) {
            case LINEAR -> speed.get();
            // im dalej od celu tym szybciej dociagamy (naturalniej przy duzych skretach)
            case DISTANCE -> Math.min(1f, speed.get() * (0.5 + Math.abs(deltaYaw) / 90f));
            case STEP -> 1f;
        };

        double stepYaw = deltaYaw * factor;
        double stepPitch = deltaPitch * factor;

        // ograniczamy pojedynczy skok, zeby nie bylo "teleportu" kamery
        stepYaw = MathUtil.clamp(stepYaw, -maxStep.get(), maxStep.get());
        stepPitch = MathUtil.clamp(stepPitch, -maxStep.get(), maxStep.get());

        RotationUtil.setRotation((float) (yaw + stepYaw), (float) (pitch + stepPitch));
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
