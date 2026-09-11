package com.ares.core.mixin;

import com.ares.Ares;
import com.ares.modules.utility.Hitboxes;
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
        try {
            Hitboxes hitboxes = Ares.get().modules().get(Hitboxes.class);
            if (hitboxes != null && hitboxes.isEnabled()) {
                info.setReturnValue(info.getReturnValue() + hitboxes.expansion());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
