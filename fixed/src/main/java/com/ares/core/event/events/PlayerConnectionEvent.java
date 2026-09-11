package com.ares.core.event.events;

import com.ares.core.event.Event;
import net.minecraft.client.network.PlayerListEntry;

public class PlayerConnectionEvent extends Event {
    private final PlayerListEntry entry;
    private final boolean joining;

    public PlayerConnectionEvent(PlayerListEntry entry, boolean joining) {
        this.entry = entry;
        this.joining = joining;
    }

    public PlayerListEntry entry() {
        return entry;
    }

    public boolean isJoining() {
        return joining;
    }
}
