package com.johnpyp.speedruntimer;

public class Debounce {
    private final long intervalMs;
    private long lastCalled;
    Debounce(long intervalMs) {
        this.intervalMs = intervalMs;
        this.lastCalled = System.currentTimeMillis();
    }
    public boolean boing() {
        if (lastCalled + intervalMs < System.currentTimeMillis()) {
            lastCalled = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
