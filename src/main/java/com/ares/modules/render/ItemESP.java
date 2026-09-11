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
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;

/** Item ESP - podswietla lezace przedmioty. */
public final class ItemESP extends Module {

    private final ColorSetting color = add(new ColorSetting("Color", "Kolor", 0x66FFCC00).group("General"));
    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg", 48f, 8f, 128f).group("General"));
    private final BoolSetting outline = add(new BoolSetting("Outline", "Rysuj krawedzie", true).group("General"));

    public ItemESP() {
        super("Item ESP", "Podswietla przedmioty na ziemi", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;

        for (net.minecraft.entity.Entity entity : com.ares.core.util.world.EntityUtil.all()) {
            if (!(entity instanceof ItemEntity item)) continue;
            if (entity.squaredDistanceTo(Wrapper.player()) > range.get() * range.get()) continue;

            Box box = entity.getBoundingBox().offset(-event.cameraPos().x, -event.cameraPos().y, -event.cameraPos().z);
            RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, color.get());
            if (outline.get()) {
                RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), box, 0xFFFFCC00);
            }
        }
    }
}
