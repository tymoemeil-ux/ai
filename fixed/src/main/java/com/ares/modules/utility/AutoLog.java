package com.ares.modules.utility;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;

/** Auto Log - rozlacza przy niskim zdrowiu. */
public final class AutoLog extends Module {

    private final FloatSetting health = add(new FloatSetting("Health", "Ponizej ilu HP", 6f, 1f, 36f).group("General"));
    private final BoolSetting onlyWhenEnemies = add(new BoolSetting("Enemies Near", "Tylko gdy wrog jest blisko", true).group("General"));
    private final FloatSetting enemyRange = add(new FloatSetting("Enemy Range", "Zasieg wroga", 16f, 2f, 64f).group("General"));

    public AutoLog() {
        super("Auto Log", "Rozlacza przy niskim zdrowiu", ModuleCategory.UTILITY);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (PlayerUtil.health() > health.get()) return;

        if (onlyWhenEnemies.get()) {
            boolean enemy = false;
            for (net.minecraft.entity.player.PlayerEntity player : com.ares.core.util.world.EntityUtil.otherPlayers()) {
                if (player.squaredDistanceTo(Wrapper.player()) <= enemyRange.get() * enemyRange.get()) {
                    enemy = true;
                    break;
                }
            }
            if (!enemy) return;
        }

        Wrapper.mc().setScreen(null);
        PlayerUtil.sendMessage("AutoLog: rozlaczanie przy " + String.format("%.1f", PlayerUtil.health()) + " HP");
        setEnabled(false);
    }
}
