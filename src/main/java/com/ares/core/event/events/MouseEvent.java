package com.ares.core.event.events;

import com.ares.core.event.Event;

public class MouseEvent extends Event {
    private final int button;
    private final int action;
    private final double x;
    private final double y;

    public MouseEvent(int button, int action, double x, double y) {
        this.button = button;
        this.action = action;
        this.x = x;
        this.y = y;
    }

    public int button() {
        return button;
    }

    public int action() {
        return action;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }
}
