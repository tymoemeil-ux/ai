package com.ares.core.mixin;

import com.ares.Ares;
import com.ares.modules.render.Freecam;
import com.ares.core.util.Wrapper;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freecam: po normalnym wyliczeniu kamery podmienia ja na pozycje freecama. */
@Mixin(Camera.class)
public final class CameraMixin {

    /** setPos / setRotation sa protected w Camera - Mixin podmieni te deklaracje na prawdziwe metody. */
    @Shadow
    protected void setPos(Vec3d pos) {
        throw new AssertionError();
    }

    @Shadow
    protected void setRotation(float yaw, float pitch) {
        throw new AssertionError();
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson,
                          boolean inverseView, float tickDelta, CallbackInfo info) {
        try {
            applyViewClip(focusedEntity, thirdPerson);

            Freecam freecam = Ares.get().modules().get(Freecam.class);
            if (freecam == null || !freecam.active() || !Wrapper.nullCheck()) return;

            Vec3d pos = freecam.position();
            if (pos == null) return;
            setPos(pos);
            setRotation(Wrapper.player().getYaw(), Wrapper.player().getPitch());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * ViewClip: kamera ignoruje kolizje ze scianami.
     * Vanilla przesuwa kamere do przodu gdy cos zaslania widok - my cofamy ja z powrotem
     * na pelna odleglosc (4 bloki w trzeciej osobie, 0 w pierwszej).
     */
    private void applyViewClip(Entity focused, boolean thirdPerson) {
        com.ares.modules.render.ViewClip viewClip =
                Ares.get().modules().get(com.ares.modules.render.ViewClip.class);
        if (viewClip == null || !viewClip.isEnabled()) return;
        if (focused == null) return;

        double distance = (thirdPerson || viewClip.thirdPerson()) ? 4.0 : 0.0;
        Vec3d eye = focused.getEyePos();
        Vec3d direction = Vec3d.fromPolar(focused.getPitch(), focused.getYaw());
        setPos(eye.subtract(direction.multiply(distance)));
    }
}
