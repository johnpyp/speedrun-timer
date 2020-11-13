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

import java.util.List;

public class TickHandler {
  private final MinecraftClient minecraftClient;
  private final Debounce persistDebounce;
  private final Debounce serverQueryDebounce;
  private final RunDataStore store;

  public TickHandler(MinecraftClient client, RunDataStore store) {
    this.store = store;
    persistDebounce = new Debounce(2000);
    serverQueryDebounce = new Debounce(500);
    minecraftClient = client;
  }

  private static String splitLabel(long ticks) {
    return ticks == -1 ? "--" : tickTime(ticks);
  }

  private static long getGameTicks(PlayerEntity player) {
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
    PlayerAdvancementTracker tracker = serverPlayer.getAdvancementTracker();
    Advancement advancement = server.getAdvancementLoader().get(new Identifier(advancementId));
    AdvancementProgress advancementProgress = tracker.getProgress(advancement);
    return advancementProgress.isDone();
  }

  private ServerPlayerEntity getServerPlayer(MinecraftServer server) {
    List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
    if (playerList.size() != 1) return null;
    return playerList.get(0);
  }

  private SingleRunData updateRunData(
      PlayerEntity player, MinecraftServer server, ServerPlayerEntity serverPlayer) {
    long ticks = TickHandler.getGameTicks(player);
    long currentTime = System.currentTimeMillis();

    String runKey = RunDataStore.getRunKey(server);
    SingleRunData run = store.solveItem(runKey, ticks);

    run.ticks = ticks;
    run.startTimestamp = Math.min(currentTime - (ticks * 50), run.startTimestamp);

    if (!serverQueryDebounce.boing()) return run;

    if (run.finishedSplit == -1 && ((ServerPlayerEntityAccessor) serverPlayer).seenCredits()) {
      run.finishedSplit = ticks;
      run.finishedTimestamp = currentTime;
    }
    if (run.overworldSplit == -1
        && advancementDone("minecraft:nether/root", serverPlayer, server)) {
      run.overworldSplit = ticks;
    }

    if (run.netherSplit == -1
        && run.overworldSplit != -1
        && serverPlayer.world.getRegistryKey() == World.OVERWORLD) {
      run.netherSplit = ticks;
    }
    if (run.strongholdSplit == -1 && advancementDone("minecraft:end/root", serverPlayer, server)) {
      run.strongholdSplit = ticks;
    }

    return run;
  }

  private void render(MinecraftClient client, SingleRunData run) {
    if (persistDebounce.boing()) store.persist();

    final TextRenderer textRenderer = client.textRenderer;
    Hud hud = new Hud(textRenderer, 5, 5);
    if (((MinecraftClientAccessor) client).getGameOptions().debugEnabled) return;
    String gameTimeLabel = "Game Time: " + tickTime(run.ticks);
    String realTimeLabel =
        "Real Time: " + timeFormat(System.currentTimeMillis() - run.startTimestamp);
    String overworldSplitLabel = "Overworld: " + splitLabel(run.overworldSplit);
    String netherSplitLabel = "Nether: " + splitLabel(run.netherSplit);
    String strongholdSplitLabel = "Stronghold: " + splitLabel(run.strongholdSplit);
    String finishedSplitLabel = "FINISH: " + splitLabel(run.finishedSplit);

    hud.draw(gameTimeLabel, 0x29DB87);
    hud.drawLine(realTimeLabel, 10, 0x59AB87);
    hud.insertSpace(10);
    hud.drawLine(overworldSplitLabel, 10, 0x9988FF);
    hud.drawLine(netherSplitLabel, 10, 0xFF5555);
    hud.drawLine(strongholdSplitLabel, 10, 0x99AADF);
    hud.drawLine(finishedSplitLabel, 10, 0xFFA500);
    hud.drawBackground(4, 0x99000011);
    hud.renderText();
  }
}
