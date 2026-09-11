package com.ares.core.mixin;

import com.ares.Ares;
import com.ares.core.event.events.Render2DEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
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
}
