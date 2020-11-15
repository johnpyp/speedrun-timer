package com.johnpyp.speedruntimer.datastorage;

public final class PersonalBest extends AbstractRun {
  public void tryRun(AbstractRun otherRun) {
    if (otherRun.finishedSplitTicks == UNINITIALIZED
        || (finishedSplitTicks != UNINITIALIZED
            && finishedSplitTicks <= otherRun.finishedSplitTicks)) return;
    overworldSplitTicks = otherRun.overworldSplitTicks;
    netherSplitTicks = otherRun.netherSplitTicks;
    strongholdSplitTicks = otherRun.strongholdSplitTicks;
    finishedSplitTicks = otherRun.finishedSplitTicks;
    finishedRealTime = otherRun.finishedRealTime;
  }
}
