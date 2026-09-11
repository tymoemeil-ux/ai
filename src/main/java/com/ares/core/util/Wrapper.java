package com.ares.core.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;

/**
 * Szybki dostep do najczesciej uzywanych instancji klienta.
 */
public final class Wrapper {
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    private Wrapper() {
    }

    public static MinecraftClient mc() {
        return mc;
    }

    public static ClientPlayerEntity player() {
        return mc.player;
    }

    public static ClientWorld world() {
        return mc.world;
    }

    public static ClientPlayerInteractionManager interaction() {
        return mc.interactionManager;
    }

    public static boolean nullCheck() {
        return mc != null && mc.player != null && mc.world != null;
    }
}
