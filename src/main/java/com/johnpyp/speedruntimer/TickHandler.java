package com.johnpyp.speedruntimer;

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
import java.util.concurrent.ThreadPoolExecutor;

public class TickHandler {

  private static final String ACHIEVEMENT_NETHER = "minecraft:nether/root";
  private static final String ACHIEVEMENT_STRONGHOLD = "minecraft:end/root";

  private static final long DEBOUNCE_PERSIST_MS = 2000L;
  private static final long DEBOUNCE_SERVER_QUERY_MS = 500L;

  private final MinecraftClient minecraftClient;
  private final Debounce persistDebounce;
  private final Debounce serverQueryDebounce;
  private final RunDataStore store;
  private final Config config;
  private final Executor executor;
  private final Runnable lambda;

  public TickHandler(MinecraftClient client, RunDataStore store, Config config) {
    this.config = config;
    this.store = store;
    persistDebounce = new Debounce(DEBOUNCE_PERSIST_MS);
    serverQueryDebounce = new Debounce(DEBOUNCE_SERVER_QUERY_MS);
    minecraftClient = client;
    executor = Executors.newSingleThreadExecutor();
    lambda = store::persist;
  }

  private static String splitLabel(long ticks) {
    return ticks == -1 ? "--" : tickTime(ticks);
  }

  private static long getGameTicks(PlayerEntity player) {
    if (player == null) return 0;
    return player.world.getTime();
  }

  private static String tickTime(long ticks) {
    return timeFormat(ticks * 50);
  }

  private static String timeFormat(long ms) {
    return DurationFormatUtils.formatDurationHMS(ms);
  }

  public void tick() {
    if (minecraftClient == null) return;
    PlayerEntity player = minecraftClient.player;
    if (player == null) return;
    MinecraftServer server = minecraftClient.getServer();
    if (server == null) return;
    ServerPlayerEntity serverPlayer = getServerPlayer(server);
    if (serverPlayer == null || getGameTicks(player) <= 0) return;

    SingleRunData run = updateRunData(player, server, serverPlayer);
    render(minecraftClient, run);
  }

  private boolean advancementDone(
      String advancementId, ServerPlayerEntity serverPlayer, MinecraftServer server) {
    if (serverPlayer == null) return false;
    PlayerAdvancementTracker tracker = serverPlayer.getAdvancementTracker();
    if (tracker == null) return false;
    Advancement advancement = server.getAdvancementLoader().get(new Identifier(advancementId));
    if (advancement == null) return false;
    AdvancementProgress advancementProgress = tracker.getProgress(advancement);
    if (advancementProgress == null) return false;
    return advancementProgress.isDone();
  }

  @Nullable
  private ServerPlayerEntity getServerPlayer(MinecraftServer server) {
    List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
    return playerList.size() == 1 ? playerList.get(0) : null;
  }

  private SingleRunData updateRunData(
      PlayerEntity player, MinecraftServer server, ServerPlayerEntity serverPlayer) {
    long ticks = TickHandler.getGameTicks(player);
    long currentTime = System.currentTimeMillis();

    if (persistDebounce.boing()) executor.execute(lambda);

    SingleRunData run = store.solveItem(server, ticks);

    run.ticks = ticks;
    run.startTimestamp = Math.min(currentTime - (ticks * 50), run.startTimestamp);

    if (!serverQueryDebounce.boing()) return run;

    boolean seenCredits = false;
    if (serverPlayer == null || serverPlayer instanceof ServerPlayerEntityAccessor) {
      seenCredits = ((ServerPlayerEntityAccessor) serverPlayer).seenCredits();
    }
    if (!run.isFinished() && seenCredits) {
      run.finishedSplit = ticks;
      run.finishedTimestamp = currentTime;
    }
    if (!run.hasOverworldSplit() && advancementDone(ACHIEVEMENT_NETHER, serverPlayer, server)) {
      run.overworldSplit = ticks;
    }

    if (!run.hasNetherSplit()
        && run.hasOverworldSplit()
        && serverPlayer.world.getRegistryKey() == World.OVERWORLD) {
      run.netherSplit = ticks;
    }
    if (!run.hasStrongholdSplit()
        && advancementDone(ACHIEVEMENT_STRONGHOLD, serverPlayer, server)) {
      run.strongholdSplit = ticks;
    }

    return run;
  }

  private void render(MinecraftClient client, SingleRunData run) {

    final TextRenderer textRenderer = client.textRenderer;
    Hud hud = new Hud(textRenderer, config.data.xOffset, config.data.yOffset);
    if (((MinecraftClientAccessor) client).getGameOptions().debugEnabled) return;
    String gameTimeLabel = "Game Time: " + tickTime(run.ticks);
    String realTimeLabel =
        "Real Time: " + timeFormat(System.currentTimeMillis() - run.startTimestamp);
    String overworldSplitLabel = "Overworld: " + splitLabel(run.overworldSplit);
    String netherSplitLabel = "Nether: " + splitLabel(run.netherSplit);
    String strongholdSplitLabel = "Stronghold: " + splitLabel(run.strongholdSplit);
    String finishedSplitLabel = "Finish: " + splitLabel(run.finishedSplit);

    hud.print(gameTimeLabel, 0x29DB87)
        .println(realTimeLabel, 10, 0x59AB87)
        .insertSpace(10)
        .println(overworldSplitLabel, 10, 0x777CFF)
        .println(netherSplitLabel, 10, 0xFF5555)
        .println(strongholdSplitLabel, 10, 0x99AADF)
        .println(finishedSplitLabel, 10, 0xFFA500)
        .render(4, 0x99000011);
  }
}
