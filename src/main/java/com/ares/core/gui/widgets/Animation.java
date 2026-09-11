package com.ares.core.gui.widgets;

/** Prosta animacja plynnego przejscia (0..1). */
public final class Animation {
    private double value;
    private long lastTime = System.currentTimeMillis();

    public Animation(double value) {
        this.value = value;
    }

    public void update(boolean target, double speed) {
        long now = System.currentTimeMillis();
        double delta = Math.min(1, (now - lastTime) / 16.0);
        lastTime = now;
        double goal = target ? 1 : 0;
        value += (goal - value) * speed * delta;
        value = Math.max(0, Math.min(1, value));
    }

    public double value() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
