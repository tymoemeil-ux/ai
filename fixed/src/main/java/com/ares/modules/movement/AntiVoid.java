package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import net.minecraft.util.math.Vec3d;

/** Anti Void - zatrzymuje spadanie w pustke. */
public final class AntiVoid extends Module {

    private final IntSetting voidLevel = add(new IntSetting("Void Y", "Ponizej jakiego Y to pustka", -60, -128, 0).group("General"));
    private final BoolSetting bounce = add(new BoolSetting("Bounce", "Odbijaj sie od pustki", true).group("General"));

    public AntiVoid() {
        super("Anti Void", "Zabezpiecza przed wpadnieciem w pustke", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (Wrapper.player().getY() > voidLevel.get()) return;

        if (Wrapper.player().isCreative()) return;

        Vec3d velocity = Wrapper.player().getVelocity();
        if (bounce.get()) {
            Wrapper.player().setVelocity(new Vec3d(velocity.x, 0.5, velocity.z));
        } else {
            Wrapper.player().setVelocity(new Vec3d(velocity.x, 0, velocity.z));
        }
    }
}
