package com.ares.modules.movement;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.PlayerUtil;

/** No Push - blokuje odpychanie przez byty i wode. */
public final class NoPush extends Module {

    private final BoolSetting entities = add(new BoolSetting("Entities", "Byty nie odpychaja", true).group("General"));
    private final BoolSetting water = add(new BoolSetting("Water", "Woda nie odpycha", false).group("General"));
    private final BoolSetting blocks = add(new BoolSetting("Blocks", "Bloki nie odpychaja", true).group("General"));

    public NoPush() {
        super("No Push", "Blokuje odpychanie gracza", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        if (entities.get() && Wrapper.player().horizontalCollision) {
            Wrapper.player().setVelocity(Wrapper.player().getVelocity().multiply(1, 0.6, 1));
        }
    }
}
