package com.ares.core.util.render;

import com.ares.core.util.math.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

/** Pomocnicze funkcje rysowania 2D (HUD i GUI). */
public final class RenderUtil2D {

    private RenderUtil2D() {
    }

    public static void rect(DrawContext context, double x, double y, double width, double height, int color) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
    }

    public static void rectOutline(DrawContext context, double x, double y, double width, double height, int color) {
        context.drawBorder((int) x, (int) y, (int) width + 1, (int) height + 1, color);
    }

    /** Prostokat z zaokraglonymi rogami (rysowany jako warstwy prostokatow). */
    public static void roundedRect(DrawContext context, double x, double y, double width, double height, double radius, int color) {
        double r = Math.min(radius, Math.min(width, height) / 2.0);
        int xi = (int) x, yi = (int) y, wi = (int) width, hi = (int) height, ri = (int) Math.round(r);

        context.fill(xi + ri, yi, xi + wi - ri, yi + hi, color);
        context.fill(xi, yi + ri, xi + wi, yi + hi - ri, color);

        // rogi
        context.fill(xi + ri / 2, yi + ri / 4, xi + wi - ri / 2, yi + ri - ri / 4, color);
        context.fill(xi + ri / 2, yi + hi - ri + ri / 4, xi + wi - ri / 2, yi + hi - ri / 4, color);
        context.fill(xi + ri / 4, yi + ri / 2, xi + ri - ri / 4, yi + hi - ri / 2, color);
        context.fill(xi + wi - ri + ri / 4, yi + ri / 2, xi + wi - ri / 4, yi + hi - ri / 2, color);
    }

    public static void roundedOutline(DrawContext context, double x, double y, double width, double height, double radius, int color) {
        double r = Math.min(radius, Math.min(width, height) / 2.0);
        int xi = (int) x, yi = (int) y, wi = (int) width, hi = (int) height, ri = (int) Math.round(r);
        context.fill(xi + ri, yi, xi + wi - ri, yi + 1, color);
        context.fill(xi + ri, yi + hi - 1, xi + wi - ri, yi + hi, color);
        context.fill(xi, yi + ri, xi + 1, yi + hi - ri, color);
        context.fill(xi + wi - 1, yi + ri, xi + wi, yi + hi - ri, color);
        context.fill(xi + ri / 2, yi + ri / 4, xi + wi - ri / 2, yi + ri / 4 + 1, color);
        context.fill(xi + ri / 2, yi + hi - ri / 4 - 1, xi + wi - ri / 2, yi + hi - ri / 4, color);
    }

    /** Pionowy gradient. */
    public static void gradient(DrawContext context, double x, double y, double width, double height, int from, int to) {
        int steps = Math.max(1, (int) height);
        for (int i = 0; i < steps; i++) {
            double t = steps <= 1 ? 0 : (double) i / (steps - 1);
            int color = com.ares.core.util.math.MathUtil.lerpColor(from, to, t);
            context.fill((int) x, (int) (y + i), (int) (x + width), (int) (y + i + 1), color);
        }
    }

    public static void horizontalGradient(DrawContext context, double x, double y, double width, double height, int from, int to) {
        int steps = Math.max(1, (int) width);
        for (int i = 0; i < steps; i++) {
            double t = steps <= 1 ? 0 : (double) i / (steps - 1);
            int color = com.ares.core.util.math.MathUtil.lerpColor(from, to, t);
            context.fill((int) (x + i), (int) y, (int) (x + i + 1), (int) (y + height), color);
        }
    }

    /** Cien pod panelem (kilka warstw z malejacym alpha). */
    public static void shadow(DrawContext context, double x, double y, double width, double height, int strength) {
        for (int i = 0; i < strength; i++) {
            int alpha = (int) (18 * (1 - (double) i / strength));
            roundedRect(context, x - i * 0.5 + 1, y - i * 0.5 + 1, width + i, height + i, 6 + i,
                    (alpha << 24));
        }
    }

    public static int textWidth(String text) {
        MinecraftHolder holder = MinecraftHolder.get();
        return holder == null ? text.length() * 6 : holder.width(text);
    }

    public static void text(DrawContext context, String text, double x, double y, int color, boolean shadow) {
        if (context == null) return;
        MinecraftHolder holder = MinecraftHolder.get();
        if (holder == null) return;
        holder.draw(context, text, (int) x, (int) y, color, shadow);
    }

    public static void text(DrawContext context, Text text, double x, double y, int color, boolean shadow) {
        if (context == null) return;
        MinecraftHolder holder = MinecraftHolder.get();
        if (holder == null) return;
        holder.draw(context, text, (int) x, (int) y, color, shadow);
    }

    public static void centeredText(DrawContext context, String text, double x, double y, int color, boolean shadow) {
        double width = textWidth(text);
        text(context, text, x - width / 2.0, y, color, shadow);
    }

    /** Skaluje wszystko narysowane wewnatrz {@code runnable}. */
    public static void scaled(DrawContext context, double x, double y, double scale, Runnable runnable) {
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate((float) x, (float) y);
        matrices.scale((float) scale, (float) scale);
        matrices.translate((float) -x, (float) -y);
        try {
            runnable.run();
        } finally {
            matrices.popMatrix();
        }
    }

    public static void scissor(DrawContext context, double x, double y, double width, double height, Runnable runnable) {
        context.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));
        try {
            runnable.run();
        } finally {
            context.disableScissor();
        }
    }

    public static int fade(int color, double progress) {
        return ColorUtil.withAlpha(color, (int) (ColorUtil.alpha(color) * com.ares.core.util.math.MathUtil.clamp(progress, 0, 1)));
    }
}
