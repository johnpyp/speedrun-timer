package com.johnpyp.speedruntimer;

public class Debounce {
  private final long intervalMs;
  private long lastCalled;

  Debounce(long intervalMs) {
    this.intervalMs = intervalMs;
    this.lastCalled = System.currentTimeMillis();
  }

  public boolean boing() {
    long currentTime = System.currentTimeMillis();
    if (lastCalled + intervalMs < currentTime) {
      lastCalled = currentTime;
      return true;
    }
    return false;
  }
}
