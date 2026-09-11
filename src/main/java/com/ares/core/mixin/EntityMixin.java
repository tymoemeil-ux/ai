package com.ares.core.mixin;

import com.ares.Ares;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Powieksza hitboxy bytow (modul Hitboxes). */
@Mixin(Entity.class)
public final class EntityMixin {

    @Inject(method = "getTargetingMargin", at = @At("RETURN"), cancellable = true)
    private void onTargetingMargin(CallbackInfoReturnable<Float> info) {
        com.ares.modules.utility.Hitboxes hitboxes = Ares.get().modules().get(com.ares.modules.utility.Hitboxes.class);
        if (hitboxes != null && hitboxes.isEnabled()) {
            info.setReturnValue(info.getReturnValue() + hitboxes.expansion());
        }
    }
}
