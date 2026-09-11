package com.ares.modules.render;

import com.ares.core.combat.HoleUtil;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/** Hole ESP - podswietla dziury w ziemi. */
public final class HoleESP extends Module {

    private final IntSetting range = add(new IntSetting("Range", "Promien szukania", 8, 1, 32).group("General"));
    private final BoolSetting bedrock = add(new BoolSetting("Bedrock", "Pokazuj bedrock hole", true).group("General"));
    private final BoolSetting obsidian = add(new BoolSetting("Obsidian", "Pokazuj obsidian hole", true).group("General"));
    private final ColorSetting bedrockColor = add(new ColorSetting("Bedrock Color", "Kolor bedrock", 0x33FF2222).group("Colors"));
    private final ColorSetting obsidianColor = add(new ColorSetting("Obsidian Color", "Kolor obsydianu", 0x3322D3EE).group("Colors"));
    private final BoolSetting outline = add(new BoolSetting("Outline", "Rysuj krawedzie", true).group("General"));

    public HoleESP() {
        super("Hole ESP", "Podswietla dziury w poblizu", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;

        for (HoleUtil.Hole hole : HoleUtil.holesInRange(Wrapper.player().getBlockPos(), range.get())) {
            if (hole.type() == HoleUtil.HoleType.BEDROCK && !bedrock.get()) continue;
            if (hole.type() == HoleUtil.HoleType.OBSIDIAN && !obsidian.get()) continue;

            int color = hole.type() == HoleUtil.HoleType.BEDROCK ? bedrockColor.get() : obsidianColor.get();
            Box box = new Box(hole.pos()).offset(-event.cameraPos().x, -event.cameraPos().y, -event.cameraPos().z);

            com.ares.core.util.render.RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, color);
            if (outline.get()) {
                com.ares.core.util.render.RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), box,
                        com.ares.core.util.math.ColorUtil.withAlpha(color, 200));
            }
        }
    }
}
