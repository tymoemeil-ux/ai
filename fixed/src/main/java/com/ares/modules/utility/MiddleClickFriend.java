package com.ares.modules.utility;

import com.ares.Ares;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Middle Click Friend - srodkowy przycisk dodaje / usuwa znajomego. */
public final class MiddleClickFriend extends Module {

    private final BoolSetting notify = add(new BoolSetting("Notify", "Powiadom na czacie", true).group("General"));

    private boolean previous;

    public MiddleClickFriend() {
        super("Middle Click Friend", "Srodkowy przycisk = znajomy", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;

        boolean pressed = Wrapper.mc().options.pickItemKey.isPressed();
        if (pressed && !previous) {
            handle();
        }
        previous = pressed;
    }

    private void handle() {
        if (Wrapper.mc().crosshairTarget == null) return;
        if (Wrapper.mc().crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult hit = (EntityHitResult) Wrapper.mc().crosshairTarget;
        Entity entity = hit.getEntity();
        if (!(entity instanceof PlayerEntity player)) return;

        String name = player.getName().getString();
        if (Ares.get().friends().isFriend(name)) {
            Ares.get().friends().remove(name);
            if (notify.get()) Ares.get().mc().player.sendMessage(
                    net.minecraft.text.Text.literal("§c[Friends] §fUsunieto: " + name), false);
        } else {
            Ares.get().friends().add(name);
            if (notify.get()) Ares.get().mc().player.sendMessage(
                    net.minecraft.text.Text.literal("§a[Friends] §fDodano: " + name), false);
        }
    }
}
