package com.ares.core.mixin;

import com.ares.Ares;
import com.ares.core.event.events.Render2DEvent;
import com.ares.modules.render.NoRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Wywoluje event renderu HUDu (2D) po narysowaniu gry. */
@Mixin(InGameHud.class)
public final class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        if (context == null) return;
        Ares.get().postRender2D(new Render2DEvent(context, tickCounter,
                context.getScaledWindowWidth(), context.getScaledWindowHeight()));
    }

    // ---- No Render ----

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void onStatusEffects(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        NoRender noRender = Ares.get().modules().get(NoRender.class);
        if (noRender != null && noRender.potionIcons()) info.cancel();
    }

    /** Tekstury nakladek (dynia, ogien) - rozpoznajemy po sciezce tekstury. */
    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void onOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo info) {
        NoRender noRender = Ares.get().modules().get(NoRender.class);
        if (noRender == null || texture == null) return;
        String path = texture.getPath();
        if (noRender.fire() && path.contains("fire")) info.cancel();
        if (noRender.pumpkin() && path.contains("pumpkin")) info.cancel();
    }
}
