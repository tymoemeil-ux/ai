package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.util.Wrapper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;

/** Block Highlight - podswietla wskazywany blok. */
public final class BlockHighlight extends Module {

    private final ColorSetting fill = add(new ColorSetting("Fill", "Kolor wypelnienia", 0x337C5CFF).group("General"));
    private final ColorSetting line = add(new ColorSetting("Line", "Kolor krawedzi", 0xFF7C5CFF).group("General"));
    private final BoolSetting outline = add(new BoolSetting("Outline", "Rysuj obramowke", true).group("General"));

    public BlockHighlight() {
        super("Block Highlight", "Podswietla wskazywany blok", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;
        if (Wrapper.mc().crosshairTarget == null) return;
        if (Wrapper.mc().crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) Wrapper.mc().crosshairTarget;
        Box box = new Box(hit.getBlockPos()).offset(-event.cameraPos().x, -event.cameraPos().y, -event.cameraPos().z);

        com.ares.core.util.render.RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, fill.get());
        if (outline.get()) {
            com.ares.core.util.render.RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), box, line.get());
        }
    }
}
