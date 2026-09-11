package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.render.RenderUtil3D;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

/** Chams - kolorowe sylwetki graczy widoczne przez sciany. */
public final class Chams extends Module {

    private final ColorSetting color = add(new ColorSetting("Color", "Kolor sylwetki", 0x66FF3366).group("General"));
    private final ColorSetting friendColor = add(new ColorSetting("Friend Color", "Kolor znajomych", 0x6622C55E).group("General"));
    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg", 64f, 8f, 256f).group("General"));
    private final BoolSetting playersOnly = add(new BoolSetting("Players Only", "Tylko gracze", true).group("General"));
    private final FloatSetting expand = add(new FloatSetting("Expand", "Powiększenie sylwetki", 0.05f, 0f, 0.5f).group("General"));

    public Chams() {
        super("Chams", "Kolorowe sylwetki przez sciany", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;

        for (net.minecraft.entity.Entity entity : Wrapper.world().getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == Wrapper.player() || !living.isAlive()) continue;
            if (playersOnly.get() && !(living instanceof net.minecraft.entity.player.PlayerEntity)) continue;
            if (living.squaredDistanceTo(Wrapper.player()) > range.get() * range.get()) continue;

            boolean friend = living instanceof net.minecraft.entity.player.PlayerEntity player
                    && com.ares.Ares.get().friends().isFriend(player);

            Box box = living.getBoundingBox().expand(expand.get())
                    .offset(-event.cameraPos().x, -event.cameraPos().y, -event.cameraPos().z);
            RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, friend ? friendColor.get() : color.get());
        }
    }
}
