package com.ares.core.util.math;

import java.awt.Color;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int darker(int color, float amount) {
        return rgb((int) Math.max(0, red(color) * (1 - amount)),
                (int) Math.max(0, green(color) * (1 - amount)),
                (int) Math.max(0, blue(color) * (1 - amount)));
    }

    public static int brighter(int color, float amount) {
        return rgb(Math.min(255, (int) (red(color) * (1 + amount))),
                Math.min(255, (int) (green(color) * (1 + amount))),
                Math.min(255, (int) (blue(color) * (1 + amount))));
    }

    /** Tecza przesuwajaca sie w czasie. */
    public static int rainbow(long offset, float saturation, float brightness) {
        float hue = ((System.currentTimeMillis() + offset) % 8000L) / 8000.0f;
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    /** Gradient pionowy Ares (fiolet -> cyjan). */
    public static int gradient(double t) {
        return MathUtil.lerpColor(0xFF7C5CFF, 0xFF22D3EE, MathUtil.clamp(t, 0, 1));
    }

    /** Kolor zalezny od zdrowia (czerwony -> zolty -> zielony). */
    public static int healthColor(float health, float max) {
        float t = MathUtil.clamp(health / Math.max(1, max), 0, 1);
        if (t > 0.5) {
            return MathUtil.lerpColor(0xFFEAB308, 0xFF22C55E, (t - 0.5) * 2);
        }
        return MathUtil.lerpColor(0xFFEF4444, 0xFFEAB308, t * 2);
    }
}
