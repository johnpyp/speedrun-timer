package com.johnpyp.speedruntimer;

final class Debounce {
  private final long intervalMs;
  private long lastCalled;

  Debounce(long intervalMs) {
    this.intervalMs = intervalMs;
    lastCalled = System.currentTimeMillis();
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
