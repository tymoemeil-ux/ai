package com.ares.core.mixin;

import com.ares.Ares;
import com.ares.modules.render.NoRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No Render: ukrywa pasek bossa. */
@Mixin(BossBarHud.class)
public final class BossBarHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, CallbackInfo info) {
        NoRender noRender = Ares.get().modules().get(NoRender.class);
        if (noRender != null && noRender.bossBar()) info.cancel();
    }
}
