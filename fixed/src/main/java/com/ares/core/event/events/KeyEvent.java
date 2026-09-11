package com.ares.core.event.events;

import com.ares.core.event.Event;

public class KeyEvent extends Event {
    private final int key;
    private final int scanCode;
    private final int modifiers;

    public KeyEvent(int key, int scanCode, int modifiers) {
        this.key = key;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
    }

    public int key() {
        return key;
    }

    public int scanCode() {
        return scanCode;
    }

    public int modifiers() {
        return modifiers;
    }
}
