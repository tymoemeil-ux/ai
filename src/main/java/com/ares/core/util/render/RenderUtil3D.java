package com.ares.core.util.render;

import com.ares.core.util.math.ColorUtil;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Rysowanie geometrii 3D (ESP, target, crystal render itd.). */
public final class RenderUtil3D {

    private RenderUtil3D() {
    }

    public static void drawFilledBox(MatrixStack matrices, VertexConsumerProvider consumers,
                                     BlockPos pos, int color, float height) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + height, pos.getZ() + 1);
        drawFilledBox(matrices, consumers, box, color);
    }

    public static void drawFilledBox(MatrixStack matrices, VertexConsumerProvider consumers, Box box, int color) {
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getDebugFilledBox());
        float r = ColorUtil.red(color) / 255f;
        float g = ColorUtil.green(color) / 255f;
        float b = ColorUtil.blue(color) / 255f;
        float a = ColorUtil.alpha(color) / 255f;
        VertexRendering.drawFilledBox(matrices, consumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
    }

    public static void drawBoxOutline(MatrixStack matrices, VertexConsumerProvider consumers, Box box, int color) {
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLines());
        float r = ColorUtil.red(color) / 255f;
        float g = ColorUtil.green(color) / 255f;
        float b = ColorUtil.blue(color) / 255f;
        float a = ColorUtil.alpha(color) / 255f;
        VertexRendering.drawBox(matrices, consumer, box, r, g, b, a);
    }

    public static void drawBlockBox(MatrixStack matrices, VertexConsumerProvider consumers,
                                    BlockPos pos, int fillColor, int lineColor) {
        Box box = new Box(pos);
        if ((fillColor & 0xFF000000) != 0) drawFilledBox(matrices, consumers, box, fillColor);
        if ((lineColor & 0xFF000000) != 0) drawBoxOutline(matrices, consumers, box, lineColor);
    }

    /** Linia (np. tracer) miedzy dwoma punktami. */
    public static void drawLine(MatrixStack matrices, VertexConsumerProvider consumers,
                                Vec3d start, Vec3d end, int color) {
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = matrices.peek();
        float r = ColorUtil.red(color) / 255f;
        float g = ColorUtil.green(color) / 255f;
        float b = ColorUtil.blue(color) / 255f;
        float a = ColorUtil.alpha(color) / 255f;
        consumer.vertex(entry, (float) start.x, (float) start.y, (float) start.z).color(r, g, b, a)
                .normal(entry, 1f, 1f, 1f);
        consumer.vertex(entry, (float) end.x, (float) end.y, (float) end.z).color(r, g, b, a)
                .normal(entry, 1f, 1f, 1f);
    }

    /** Prostokat w 3D uzywany przez TargetHUD / nametagi. */
    public static void drawSide(MatrixStack matrices, VertexConsumerProvider consumers,
                                Box box, int color, net.minecraft.util.math.Direction side) {
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getDebugFilledBox());
        float r = ColorUtil.red(color) / 255f;
        float g = ColorUtil.green(color) / 255f;
        float b = ColorUtil.blue(color) / 255f;
        float a = ColorUtil.alpha(color) / 255f;
        VertexRendering.drawSide(matrices, consumer, side,
                (float) box.minX, (float) box.minY, (float) box.minZ,
                (float) box.maxX, (float) box.maxY, (float) box.maxZ, r, g, b, a);
    }
}
