package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.timer.TickTimer;

/** Auto Respawn - automatyczne odradzanie sie po smierci. */
public final class AutoRespawn extends Module {

    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie (ticki)", 10, 0, 60).group("General"));

    private final TickTimer timer = new TickTimer();

    public AutoRespawn() {
        super("Auto Respawn", "Automatycznie odradza po smierci", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;
        if (Wrapper.player().isAlive()) {
            timer.reset();
            return;
        }

        timer.increment();
        if (!timer.passed(delay.get())) return;

        // singleplayer: odrodzenie przez zintegrowany serwer
        net.minecraft.server.integrated.IntegratedServer server = Wrapper.mc().getServer();
        if (server != null) {
            net.minecraft.server.network.ServerPlayerEntity player =
                    server.getPlayerManager().getPlayer(Wrapper.player().getUuid());
            if (player != null) {
                server.getPlayerManager().respawnPlayer(player, true,
                        net.minecraft.entity.Entity.RemovalReason.KILLED);
            }
        }
        timer.reset();
    }
}
