package com.ares;

import com.ares.core.event.events.TickEvent;
import com.ares.core.util.Wrapper;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

/** Punkt wejscia moda Fabric. */
public final class AresMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Path directory = FabricLoader.getInstance().getConfigDir().resolve("ares");

        Ares.get().init(directory);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!Wrapper.nullCheck()) return;
            Ares.get().eventBus().post(new TickEvent.Client());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(Ares.get()::save));

        System.out.println("[" + Ares.NAME + "] Zaladowano " + Ares.get().modules().size() + " modulow.");
    }
}
