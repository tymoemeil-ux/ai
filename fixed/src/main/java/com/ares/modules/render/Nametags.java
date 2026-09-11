package com.ares.modules.render;

import com.ares.Ares;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/** Nametags - rozbudowane nazwy nad graczami (HP, dystans, pancerz). */
public final class Nametags extends Module {

    private final BoolSetting showHealth = add(new BoolSetting("Health", "Pokazuj HP", true).group("General"));
    private final BoolSetting showDistance = add(new BoolSetting("Distance", "Pokazuj dystans", true).group("General"));
    private final BoolSetting showArmor = add(new BoolSetting("Armor", "Pokazuj pancerz", true).group("General"));
    private final BoolSetting showPops = add(new BoolSetting("Pops", "Pokazuj totemy", true).group("General"));
    private final FloatSetting scale = add(new FloatSetting("Scale", "Skala tekstu", 1f, 0.3f, 3f).group("General"));
    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg", 64f, 8f, 256f).group("General"));

    public Nametags() {
        super("Nametags", "Informacje nad glowami graczy", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;

        ClientPlayerEntity player = Wrapper.player();
        MatrixStack matrices = event.matrices();
        VertexConsumerProvider consumers = event.consumers();

        for (net.minecraft.entity.Entity entity : com.ares.core.util.world.EntityUtil.all()) {
            if (!(entity instanceof PlayerEntity target) || entity == player) continue;
            if (!target.isAlive()) continue;
            if (target.squaredDistanceTo(player) > range.get() * range.get()) continue;

            StringBuilder builder = new StringBuilder();
            builder.append(target.getName().getString());
            if (showHealth.get()) builder.append(" ").append(String.format("%.1f", target.getHealth() + target.getAbsorptionAmount()));
            if (showDistance.get()) builder.append(" ").append(String.format("%.0fm", player.distanceTo(target)));
            if (showPops.get()) {
                int pops = Ares.get().popCounter().pops(target.getUuid());
                if (pops > 0) builder.append(" (").append(pops).append(")");
            }

            Vec3d pos = target.getPos().add(0, target.getBoundingBox().getLengthY() + 0.4, 0).subtract(event.cameraPos());

            matrices.push();
            matrices.translate(pos.x, pos.y, pos.z);
            matrices.scale(-0.025f * scale.get(), -0.025f * scale.get(), 0.025f * scale.get());

            int color = Ares.get().friends().isFriend(target) ? 0xFF22C55E : 0xFFFFFFFF;
            Wrapper.mc().textRenderer.draw(Text.literal(builder.toString()), 0, 0, color, false,
                    matrices.peek().getPositionMatrix(), consumers,
                    net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
    }
}
