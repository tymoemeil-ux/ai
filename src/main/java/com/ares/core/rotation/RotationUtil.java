package com.ares.core.rotation;

import com.ares.core.util.Wrapper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Licznie katow i nakladanie rotacji na gracza. */
public final class RotationUtil {

    private RotationUtil() {
    }

    public static Rotation to(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distance));
        return new Rotation(MathHelper.wrapDegrees(yaw), MathHelper.clamp(MathHelper.wrapDegrees(pitch), -90f, 90f));
    }

    public static Rotation toBlock(BlockPos pos) {
        PlayerEntity player = Wrapper.player();
        if (player == null) return new Rotation(0, 0);
        return to(player.getEyePos(), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public static Rotation toPoint(Vec3d point) {
        PlayerEntity player = Wrapper.player();
        if (player == null) return new Rotation(0, 0);
        return to(player.getEyePos(), point);
    }

    public static Rotation toEntity(Entity entity, boolean predict) {
        PlayerEntity player = Wrapper.player();
        if (player == null || entity == null) return new Rotation(0, 0);
        Vec3d target = entity.getPos();
        if (predict) target = target.add(entity.getVelocity().multiply(2));
        target = target.add(0, entity.getBoundingBox().getLengthY() * 0.5, 0);
        return to(player.getEyePos(), target);
    }

    public static void setRotation(Rotation rotation) {
        setRotation(rotation.yaw(), rotation.pitch());
    }

    public static void setRotation(float yaw, float pitch) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null) return;
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.headYaw = yaw;
        player.bodyYaw = yaw;
    }

    /** Ustawia rotacje, wykonuje akcje i przywraca poprzednie katy. */
    public static void setRotationSilently(Rotation rotation, Runnable action) {
        ClientPlayerEntity player = Wrapper.player();
        if (player == null) return;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        setRotation(rotation);
        try {
            action.run();
        } finally {
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = yaw;
            player.bodyYaw = yaw;
        }
    }

    public static float angleDifference(Rotation a, Rotation b) {
        return Math.abs(MathHelper.wrapDegrees(a.yaw() - b.yaw())) + Math.abs(MathHelper.wrapDegrees(a.pitch() - b.pitch()));
    }
}
