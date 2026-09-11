package com.ares.core.event.events;

import com.ares.core.event.Event;
import net.minecraft.entity.player.PlayerEntity;

public class TotemPopEvent extends Event {
    private final PlayerEntity player;
    private final int pops;

    public TotemPopEvent(PlayerEntity player, int pops) {
        this.player = player;
        this.pops = pops;
    }

    public PlayerEntity player() {
        return player;
    }

    public int pops() {
        return pops;
    }
}
