package com.ares.core.util.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class MathUtil {

    private MathUtil() {
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static double roundToStep(double value, double step) {
        return Math.round(value / step) * step;
    }

    public static float wrapDegrees(float degrees) {
        return (float) MathHelper.wrapDegrees(degrees);
    }

    public static double distance2D(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distance2D(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double squaredDistance(Vec3d a, Vec3d b) {
        return a.squaredDistanceTo(b);
    }

    public static Vec3d center(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static Vec3d interpolate(Vec3d from, Vec3d to, double delta) {
        return new Vec3d(
                from.x + (to.x - from.x) * delta,
                from.y + (to.y - from.y) * delta,
                from.z + (to.z - from.z) * delta);
    }

    public static double easeOutCubic(double t) {
        return 1 - Math.pow(1 - t, 3);
    }

    public static double easeOutExpo(double t) {
        return t >= 1 ? 1 : 1 - Math.pow(2, -10 * t);
    }

    public static int lerpColor(int from, int to, double t) {
        int a = (from >> 24) & 0xFF, r = (from >> 16) & 0xFF, g = (from >> 8) & 0xFF, b = from & 0xFF;
        int a2 = (to >> 24) & 0xFF, r2 = (to >> 16) & 0xFF, g2 = (to >> 8) & 0xFF, b2 = to & 0xFF;
        return ((int) (a + (a2 - a) * t) << 24)
                | ((int) (r + (r2 - r) * t) << 16)
                | ((int) (g + (g2 - g) * t) << 8)
                | (int) (b + (b2 - b) * t);
    }
}
