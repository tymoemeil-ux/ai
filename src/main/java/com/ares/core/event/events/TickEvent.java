package com.ares.core.event.events;

import com.ares.core.event.Event;

/**
 * Wywolywany raz na tick klienta.
 */
public class TickEvent extends Event {

    public static final class Client extends TickEvent {
    }

    public static final class Post extends TickEvent {
    }
}
