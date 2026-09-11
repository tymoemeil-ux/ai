package com.ares.core.event.events;

import com.ares.core.event.Event;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * Wywolywany po wyrenderowaniu bytow w swiecie (3D). Wspolrzedne sa relatywne do kamery.
 */
public class Render3DEvent extends Event {
    private final MatrixStack matrices;
    private final VertexConsumerProvider consumers;
    private final Camera camera;
    private final float tickDelta;
    private final Vec3d cameraPos;

    public Render3DEvent(MatrixStack matrices, VertexConsumerProvider consumers, Camera camera, float tickDelta, Vec3d cameraPos) {
        this.matrices = matrices;
        this.consumers = consumers;
        this.camera = camera;
        this.tickDelta = tickDelta;
        this.cameraPos = cameraPos;
    }

    public MatrixStack matrices() {
        return matrices;
    }

    public VertexConsumerProvider consumers() {
        return consumers;
    }

    public Camera camera() {
        return camera;
    }

    public float tickDelta() {
        return tickDelta;
    }

    public Vec3d cameraPos() {
        return cameraPos;
    }
}
