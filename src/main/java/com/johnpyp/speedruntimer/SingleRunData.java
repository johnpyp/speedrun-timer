package com.johnpyp.speedruntimer;

public class SingleRunData {
    public long startTimestamp = -1;
    public long finishedTimestamp = -1;
    public long ticks = -1;
    public long overworldSplit = -1;
    public long netherSplit = -1;
    public long strongholdSplit = -1;
    public long finishedSplit = -1;

    public boolean isFinished() {
        return this.finishedSplit != -1;
    }
}
