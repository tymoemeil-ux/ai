package com.ares.core.rotation;

/** Para katow (yaw, pitch) uzywana przy obracaniu gracza. */
public final class Rotation {
    private final float yaw;
    private final float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    @Override
    public String toString() {
        return String.format("%.1f / %.1f", yaw, pitch);
    }
}
