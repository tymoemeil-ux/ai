package com.ares;

import com.ares.core.event.events.Render3DEvent;
import com.ares.core.util.Wrapper;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.MatrixStack;

/** Punkt wejscia moda Fabric. */
public final class AresMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Path directory = FabricLoader.getInstance().getConfigDir().resolve("ares");

        Ares.get().init(directory);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                Ares.get().tick();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

        // Event renderu 3D (ESP, tracery, boxy) - Fabric API podaje nam kontekst swiata.
        WorldRenderEvents.LAST.register(context -> {
            try {
                if (!Wrapper.nullCheck()) return;
                MatrixStack matrices = context.matrixStack();
                VertexConsumerProvider consumers = context.consumers();
                Camera camera = context.camera();
                if (matrices == null || consumers == null || camera == null) return;
                float tickDelta = context.tickCounter().getTickProgress(true);
                Ares.get().postRender3D(new Render3DEvent(matrices, consumers, camera, tickDelta, camera.getPos()));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(Ares.get()::save));

        System.out.println("[" + Ares.NAME + "] Zaladowano " + Ares.get().modules().size() + " modulow.");
    }
}
