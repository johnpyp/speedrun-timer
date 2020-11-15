package com.johnpyp.speedruntimer.datastorage;

public abstract class AbstractRun {
  public static long UNINITIALIZED = -1;
  public long overworldSplitTicks = UNINITIALIZED;
  public long netherSplitTicks = UNINITIALIZED;
  public long strongholdSplitTicks = UNINITIALIZED;
  public long finishedSplitTicks = UNINITIALIZED;
  public long finishedRealTime = UNINITIALIZED;

  public boolean isFinished() {
    return finishedSplitTicks != UNINITIALIZED;
  }

  public boolean hasStrongholdSplit() {
    return strongholdSplitTicks != UNINITIALIZED;
  }

  public boolean hasNetherSplit() {
    return netherSplitTicks != UNINITIALIZED;
  }

  public boolean hasOverworldSplit() {
    return overworldSplitTicks != UNINITIALIZED;
  }

  public long getOverworld() {
    return overworldSplitTicks * 50;
  }

  public long getNether() {
    return netherSplitTicks * 50;
  }

  public long getStronghold() {
    return strongholdSplitTicks * 50;
  }

  public long getFinished() {
    return finishedSplitTicks * 50;
  }
}
