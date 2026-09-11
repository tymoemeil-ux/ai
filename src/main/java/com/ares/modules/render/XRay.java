package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.render.RenderUtil3D;
import com.ares.core.util.world.BlockUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/** XRay - podswietla rudy przez sciany (skanowanie okolicy). */
public final class XRay extends Module {

    private final BoolSetting diamond = add(new BoolSetting("Diamond", "Diamenty", true).group("Ores"));
    private final BoolSetting netherite = add(new BoolSetting("Netherite", "Ancient debris", true).group("Ores"));
    private final BoolSetting gold = add(new BoolSetting("Gold", "Zloto", false).group("Ores"));
    private final BoolSetting iron = add(new BoolSetting("Iron", "Zelazo", false).group("Ores"));
    private final BoolSetting emerald = add(new BoolSetting("Emerald", "Szmaragdy", false).group("Ores"));
    private final BoolSetting coal = add(new BoolSetting("Coal", "Wegiel", false).group("Ores"));
    private final BoolSetting redstone = add(new BoolSetting("Redstone", "Redstone", false).group("Ores"));

    private final IntSetting radius = add(new IntSetting("Radius", "Promien skanowania", 16, 4, 32).group("General"));
    private final IntSetting interval = add(new IntSetting("Interval", "Co ile tickow skanowac", 20, 1, 100).group("General"));
    private final ColorSetting color = add(new ColorSetting("Color", "Kolor rud", 0x667C5CFF).group("General"));

    private final List<BlockPos> ores = new ArrayList<>();
    private int ticks;

    public XRay() {
        super("XRay", "Podswietla rudy przez sciany", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        if (ticks++ % interval.get() != 0) return;

        ores.clear();
        BlockPos center = Wrapper.player().getBlockPos();
        int r = radius.get();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (y + center.getY() < Wrapper.world().getBottomY()) continue;
                    if (y + center.getY() > Wrapper.world().getTopY()) continue;
                    if (isOre(BlockUtil.block(pos))) ores.add(pos);
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (ores.isEmpty()) return;
        for (BlockPos pos : ores) {
            Box box = new Box(pos).offset(-event.cameraPos().x, -event.cameraPos().y, -event.cameraPos().z);
            RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, color.get());
        }
    }

    private boolean isOre(Block block) {
        if (diamond.get() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return true;
        if (netherite.get() && block == Blocks.ANCIENT_DEBRIS) return true;
        if (gold.get() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE)) return true;
        if (iron.get() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) return true;
        if (emerald.get() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return true;
        if (coal.get() && (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)) return true;
        return redstone.get() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE);
    }

    @Override
    public void onDisable() {
        ores.clear();
        ticks = 0;
    }

    @Override
    public String info() {
        return String.valueOf(ores.size());
    }
}
