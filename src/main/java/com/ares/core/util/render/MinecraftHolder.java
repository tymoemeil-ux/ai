package com.ares.core.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Cienka warstwa nad TextRendererem, zeby nie powielac null-checkow. */
public final class MinecraftHolder {
    private static MinecraftClient mc;

    private MinecraftHolder() {
    }

    public static MinecraftHolder get() {
        return mc == null ? null : INSTANCE;
    }

    private static final MinecraftHolder INSTANCE = new MinecraftHolder();

    static {
        mc = MinecraftClient.getInstance();
    }

    public int width(String text) {
        TextRenderer renderer = mc.textRenderer;
        return renderer == null ? 0 : renderer.getWidth(text);
    }

    public void draw(DrawContext context, String text, int x, int y, int color, boolean shadow) {
        TextRenderer renderer = mc.textRenderer;
        if (renderer == null) return;
        context.drawText(renderer, text, x, y, color, shadow);
    }

    public void draw(DrawContext context, Text text, int x, int y, int color, boolean shadow) {
        TextRenderer renderer = mc.textRenderer;
        if (renderer == null) return;
        context.drawText(renderer, text, x, y, color, shadow);
    }

    public int fontHeight() {
        TextRenderer renderer = mc.textRenderer;
        return renderer == null ? 9 : renderer.fontHeight;
    }

    public TextRenderer renderer() {
        return mc.textRenderer;
    }
}
