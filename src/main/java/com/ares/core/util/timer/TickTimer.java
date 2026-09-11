package com.ares.core.util.timer;

/** Licznik odmierzany w tickach (1 tick = 50 ms). */
public final class TickTimer {
    private int ticks;

    public TickTimer() {
        this(0);
    }

    public TickTimer(int ticks) {
        this.ticks = ticks;
    }

    public void increment() {
        ticks++;
    }

    /** Zwraca true gdy minelo przynajmniej {@code delay} tickow i resetuje licznik. */
    public boolean passed(int delay) {
        if (ticks >= delay) {
            ticks = 0;
            return true;
        }
        return false;
    }

    /** Jak wyzej, ale bez resetu (gdy {@code reset} = false). */
    public boolean passed(int delay, boolean reset) {
        if (ticks >= delay) {
            if (reset) ticks = 0;
            return true;
        }
        return false;
    }

    public void reset() {
        ticks = 0;
    }

    public int ticks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }
}
