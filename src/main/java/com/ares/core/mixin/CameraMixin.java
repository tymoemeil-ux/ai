package com.ares.core.mixin;

import com.ares.Ares;
import com.ares.modules.render.Freecam;
import com.ares.core.util.Wrapper;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freecam: po normalnym wyliczeniu kamery podmienia ja na pozycje freecama. */
@Mixin(Camera.class)
public final class CameraMixin {

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson,
                          boolean inverseView, float tickDelta, CallbackInfo info) {
        Freecam freecam = Ares.get().modules().get(Freecam.class);
        if (freecam == null || !freecam.active() || !Wrapper.nullCheck()) return;

        Camera self = (Camera) (Object) this;
        Vec3d pos = freecam.position();
        if (pos == null) return;
        self.setPos(pos);
        self.setRotation(Wrapper.player().getYaw(), Wrapper.player().getPitch());
    }
}
