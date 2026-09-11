package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;
import net.minecraft.util.math.Vec3d;

/** Safe Walk - nie pozwala zleciec z krawedzi bloku. */
public final class SafeWalk extends Module {

    private final BoolSetting onlyWhenSneaking = add(new BoolSetting("Only Sneaking", "Tylko podczas kucania", false).group("General"));

    public SafeWalk() {
        super("Safe Walk", "Blokuje spadanie z krawedzi", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (!Wrapper.player().isOnGround()) return;
        if (onlyWhenSneaking.get() && !Wrapper.player().isSneaking()) return;

        Wrapper.player().setVelocity(Wrapper.player().getVelocity().x, Math.min(Wrapper.player().getVelocity().y, 0),
                Wrapper.player().getVelocity().z);

        Vec3d pos = Wrapper.player().getPos();
        net.minecraft.util.math.BlockPos feet = Wrapper.player().getBlockPos();
        if (com.ares.core.util.world.BlockUtil.isAir(feet.down())
                && Wrapper.player().getVelocity().horizontalLengthSquared() > 0) {
            Wrapper.player().setVelocity(new Vec3d(0, Wrapper.player().getVelocity().y, 0));
        }
    }
}
