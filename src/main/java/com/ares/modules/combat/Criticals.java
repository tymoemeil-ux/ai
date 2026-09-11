package com.ares.modules.combat;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.IntSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InteractionUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/** Criticals - krytyczne trafienia (skok przed atakiem / pakiet). */
public final class Criticals extends Module {

    public enum Mode { JUMP, PACKET, MINI_JUMP }

    private final ModeSetting<Mode> mode = add(new ModeSetting<>("Mode", "Sposob robienia krytykow", Mode.PACKET).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 4, 1, 20).group("General"));
    private final IntSetting hits = add(new IntSetting("Hits", "Ile ciosow na krytyka", 1, 1, 5).group("General"));

    private final TickTimer timer = new TickTimer();
    private int hitCount;

    public Criticals() {
        super("Criticals", "Automatyczne trafienia krytyczne", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (Wrapper.player().isOnGround() && mode.get() == Mode.JUMP) return;

        LivingEntity target = nearestTarget();
        if (target == null) return;

        timer.increment();
        if (!timer.passed(delay.get())) return;

        switch (mode.get()) {
            case JUMP -> {
                if (Wrapper.player().isOnGround()) {
                    Wrapper.player().jump();
                }
            }
            case MINI_JUMP -> {
                if (Wrapper.player().isOnGround()) {
                    Wrapper.player().setVelocity(Wrapper.player().getVelocity().x, 0.12, Wrapper.player().getVelocity().z);
                }
            }
            case PACKET -> {
                double x = Wrapper.player().getX();
                double y = Wrapper.player().getY();
                double z = Wrapper.player().getZ();
                Wrapper.player().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
                Wrapper.player().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
            }
        }

        InteractionUtil.attack(target, true);
        hitCount++;
        if (hitCount >= hits.get()) {
            hitCount = 0;
            timer.reset();
        }
    }

    private LivingEntity nearestTarget() {
        double best = Double.MAX_VALUE;
        LivingEntity found = null;
        for (net.minecraft.entity.Entity entity : Wrapper.world().getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == Wrapper.player() || !living.isAlive()) continue;
            double distance = living.squaredDistanceTo(Wrapper.player());
            if (distance < best && distance < 9) {
                best = distance;
                found = living;
            }
        }
        return found;
    }
}
