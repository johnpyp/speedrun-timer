package com.johnpyp.speedruntimer;

final class SingleRunData {
  private static final long UNINITIALIZED = -1;
  public long ticks = UNINITIALIZED;
  public long startTimestamp = UNINITIALIZED;
  public long finishedTimestamp = UNINITIALIZED;
  public long overworldSplit = UNINITIALIZED;
  public long netherSplit = UNINITIALIZED;
  public long strongholdSplit = UNINITIALIZED;
  public long finishedSplit = UNINITIALIZED;

  SingleRunData(long ticks) {
    this.ticks = ticks;
    startTimestamp = System.currentTimeMillis();
  }

  public boolean isFinished() {
    return finishedSplit != UNINITIALIZED;
  }

  public boolean hasStrongholdSplit() {
    return strongholdSplit != UNINITIALIZED;
  }

  public boolean hasNetherSplit() {
    return netherSplit != UNINITIALIZED;
  }

  public boolean hasOverworldSplit() {
    return overworldSplit != UNINITIALIZED;
  }
}
