package com.johnpyp.speedruntimer.datastorage;

public class SingleRun extends AbstractRun {
  public long ticks;
  public long startTimestamp;

  SingleRun(long ticks) {
    this.ticks = ticks;
    startTimestamp = System.currentTimeMillis();
  }

  public long getGameTime() {
    return ticks * 50;
  }
  public long getRealTimeDuration() {
    return System.currentTimeMillis() - startTimestamp;
  }
}
