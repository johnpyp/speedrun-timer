package com.johnpyp.speedruntimer;

import com.johnpyp.speedruntimer.datastorage.AbstractRun;
import com.johnpyp.speedruntimer.datastorage.DataStorage;
import com.johnpyp.speedruntimer.datastorage.SingleRun;
import com.johnpyp.speedruntimer.mixin.MinecraftClientAccessor;
import com.johnpyp.speedruntimer.mixin.ServerPlayerEntityAccessor;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TickHandler {

  private static final String ACHIEVEMENT_NETHER = "minecraft:nether/root";
  private static final String ACHIEVEMENT_STRONGHOLD = "minecraft:end/root";

  private static final long DEBOUNCE_PERSIST_MS = 2000L;
  private static final long DEBOUNCE_SERVER_QUERY_MS = 500L;

  private final MinecraftClient minecraftClient;
  private final Debounce persistDebounce;
  private final Debounce serverQueryDebounce;
  private final DataStorage store;
  private final Config config;
  private final Executor executor;
  private final Runnable lambda;

  public TickHandler(MinecraftClient client, DataStorage store, Config config) {
    this.config = config;
    this.store = store;
    persistDebounce = new Debounce(DEBOUNCE_PERSIST_MS);
    serverQueryDebounce = new Debounce(DEBOUNCE_SERVER_QUERY_MS);
    minecraftClient = client;
    executor = Executors.newSingleThreadExecutor();
    lambda = store::persist;
  }

  private static long getGameTicks(PlayerEntity player) {
    return player.world.getTime();
  }

  private static String timeLabel(long ms) {return ms < 0 ? "--" : timeString(ms);}

  private static String timeString(long ms) {
    if (ms < 1000 * 60 * 60) {
      return DurationFormatUtils.formatDuration(ms, "mm:ss.SSS");
    }
    return DurationFormatUtils.formatDuration(ms, "HH:mm:ss.SSS");
  }

  public void tick() {
    if (minecraftClient == null) return;
    PlayerEntity player = minecraftClient.player;
    if (player == null) return;
    MinecraftServer server = minecraftClient.getServer();
    if (server == null) return;
    ServerPlayerEntity serverPlayer = getServerPlayer(server);
    if (serverPlayer == null || getGameTicks(player) <= 0) return;

    SingleRun run = updateRunData(player, server, serverPlayer);
    render(minecraftClient, server, run);
  }

  private boolean advancementDone(
      String advancementId, ServerPlayerEntity serverPlayer, MinecraftServer server) {
    PlayerAdvancementTracker tracker = serverPlayer.getAdvancementTracker();
    Advancement advancement = server.getAdvancementLoader().get(new Identifier(advancementId));
    AdvancementProgress advancementProgress = tracker.getProgress(advancement);
    return advancementProgress.isDone();
  }

  @Nullable
  private ServerPlayerEntity getServerPlayer(MinecraftServer server) {
    List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
    return playerList.size() == 1 ? playerList.get(0) : null;
  }

  private SingleRun updateRunData(
      PlayerEntity player, MinecraftServer server, ServerPlayerEntity serverPlayer) {
    long ticks = TickHandler.getGameTicks(player);
    long currentTime = System.currentTimeMillis();

    if (persistDebounce.boing()) executor.execute(lambda);

    SingleRun run = store.getRuns().solveItem(server, ticks);

    run.ticks = ticks;
    run.startTimestamp = Math.min(currentTime - (ticks * 50), run.startTimestamp);

    if (!serverQueryDebounce.boing()) return run;

    boolean seenCredits = false;
    if (serverPlayer instanceof ServerPlayerEntityAccessor) {
      seenCredits = ((ServerPlayerEntityAccessor) serverPlayer).seenCredits();
    }
    if (!run.isFinished() && seenCredits) {
      run.finishedSplitTicks = ticks;
      run.finishedRealTime = run.getRealTimeDuration();
      store.getBestSplits().tryRun(run);
      store.getPersonalBest().tryRun(run);
    }
    if (!run.hasOverworldSplit() && advancementDone(ACHIEVEMENT_NETHER, serverPlayer, server)) {
      run.overworldSplitTicks = ticks;
      store.getBestSplits().tryRun(run);
    }

    if (!run.hasNetherSplit()
        && run.hasOverworldSplit()
        && serverPlayer.world.getRegistryKey() == World.OVERWORLD) {
      run.netherSplitTicks = ticks;
      store.getBestSplits().tryRun(run);
    }
    if (!run.hasStrongholdSplit()
        && advancementDone(ACHIEVEMENT_STRONGHOLD, serverPlayer, server)) {
      run.strongholdSplitTicks = ticks;
      store.getBestSplits().tryRun(run);
    }

    return run;
  }

  private String fullLabel(String prefix, long firstMs, long secondMs, boolean shouldShowSecond) {
    if (secondMs == -1|| !shouldShowSecond) return String.format("%s: %s", prefix, timeLabel(firstMs));
    return String.format("%s: %s vs %s", prefix, timeLabel(firstMs), timeLabel(secondMs));
  }
  private void render(MinecraftClient client, MinecraftServer server,SingleRun run) {

    boolean showCompare = config.data.showCompareSplits;
    final TextRenderer textRenderer = client.textRenderer;
    final AbstractRun comparedRun = config.data.useBestSplits ? store.getBestSplits() : store.getPersonalBest();
    Hud hud = new Hud(textRenderer, config.data.xOffset, config.data.yOffset);
    if (((MinecraftClientAccessor) client).getGameOptions().debugEnabled) return;
    String gameTimeLabel = fullLabel("Game Time", run.getGameTime(), -1, false);
    String realTimeLabel = fullLabel("Real Time", run.getRealTimeDuration(), -1, false);
    String overworldSplitLabel = fullLabel("Overworld", run.getOverworld(), comparedRun.getOverworld(), showCompare);
    String netherSplitLabel = fullLabel("Nether", run.getNether(), comparedRun.getNether(), showCompare);
    String strongholdSplitLabel = fullLabel("Stronghold", run.getStronghold(), comparedRun.getStronghold(), showCompare);
    String finishedSplitLabel = fullLabel("Finished", run.getFinished(), comparedRun.getFinished(), showCompare);
    String seedLabel = String.format("Seed: %s", server.getSaveProperties().getGeneratorOptions().getSeed());

    hud.print(gameTimeLabel, 0x29DB87)
        .println(realTimeLabel, 10, 0x59AB87)
        .insertSpace(10)
        .println(overworldSplitLabel, 10, 0x777CFF)
        .println(netherSplitLabel, 10, 0xFF5555)
        .println(strongholdSplitLabel, 10, 0x99AADF)
        .println(finishedSplitLabel, 10, 0xFFA500);
    if (config.data.showSeed && run.isFinished()) {
      hud.insertSpace(5).println(seedLabel, 10, 0x00FF22);
    }
    hud.render(4, 0x000011, config.data.backgroundTransparency);
  }
}
