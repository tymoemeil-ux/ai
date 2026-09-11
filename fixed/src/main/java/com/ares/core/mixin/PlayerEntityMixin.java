package com.ares.core.mixin;

import com.ares.Ares;
import com.ares.modules.utility.Reach;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zwieksza zasieg interakcji i ataku (modul Reach). */
@Mixin(PlayerEntity.class)
public final class PlayerEntityMixin {

    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void onEntityInteractionRange(CallbackInfoReturnable<Double> info) {
        try {
            Reach reach = Ares.get().modules().get(Reach.class);
            if (reach != null && reach.isEnabled()) {
                info.setReturnValue(info.getReturnValue() + reach.distance());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Inject(method = "getBlockInteractionRange", at = @At("RETURN"), cancellable = true)
    private void onBlockInteractionRange(CallbackInfoReturnable<Double> info) {
        try {
            Reach reach = Ares.get().modules().get(Reach.class);
            if (reach != null && reach.isEnabled()) {
                info.setReturnValue(info.getReturnValue() + reach.distance());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
