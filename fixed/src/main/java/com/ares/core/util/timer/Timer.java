package com.ares.core.util.timer;

public final class Timer {
    private long time;

    public Timer() {
        time = System.currentTimeMillis();
    }

    public boolean passed(long ms) {
        return System.currentTimeMillis() - time >= ms;
    }

    public boolean passed(long ms, boolean reset) {
        if (passed(ms)) {
            if (reset) reset();
            return true;
        }
        return false;
    }

    public void reset() {
        time = System.currentTimeMillis();
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long elapsed() {
        return System.currentTimeMillis() - time;
    }
}
