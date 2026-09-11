package com.ares.core.mixin;

import com.ares.core.util.Wrapper;
import com.ares.core.util.render.FovModifier;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Podmienia FOV w locie (Zoom / Custom FOV).
 * Dzieki temu nie zapisujemy niedozwolonych wartosci do opcji Minecrafta
 * (vanilla przyjmuje tylko 30-110 i spamowala "Illegal option value").
 */
@Mixin(GameRenderer.class)
public final class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov,
                          CallbackInfoReturnable<Float> info) {
        try {
            if (!Wrapper.nullCheck()) return;
            float base = info.getReturnValue();
            float result = FovModifier.apply(base);
            if (result > 0.01f && Math.abs(result - base) > 0.01f) {
                info.setReturnValue(result);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
