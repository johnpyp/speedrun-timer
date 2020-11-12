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
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.List;


public class TickHandler {
    private final MinecraftClient client;
    private final Debounce persistDebounce;
    private final Debounce serverQueryDebounce;
    private PlayerEntity player = null;
    private World world = null;
    private MinecraftServer server = null;
    private ServerPlayerEntity serverPlayer = null;
    private final RunDataStore store;

    public TickHandler(RunDataStore store) {
        this.store = store;
        this.persistDebounce = new Debounce(2000);
        this.serverQueryDebounce = new Debounce(500);
        client = MinecraftClient.getInstance();
    }

    public void tick() {
        if (client == null)
            return;
        player = client.player;
        world = client.world;
        server = client.getServer();

        if (server == null) return;

        serverPlayer = getServerPlayer(server);

        if (world == null || player == null || serverPlayer == null || getGameTicks() <= 0)
            return;

        render();
    }

    private boolean advancementDone(String advancementId, String criterionId) {
        PlayerAdvancementTracker tracker = serverPlayer.getAdvancementTracker();
        Advancement advancement = server.getAdvancementLoader().get(new Identifier(advancementId));
        AdvancementProgress advancementProgress = tracker.getProgress(advancement);
        return advancementProgress.isDone();
    }

    private ServerPlayerEntity getServerPlayer(MinecraftServer server) {
        List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
        if (playerList.size() != 1)
            return null;
        return playerList.get(0);

    }

    private SingleRunData updateRunData() {
        String levelPath = server.getSavePath(WorldSavePath.ROOT).normalize().toString();
        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
        long ticks = this.getGameTicks();
        SingleRunData run = store.solveItem(levelPath + ":" + seed, ticks);
        if (!serverQueryDebounce.boing()) return run;
        run.startTimestamp = Math.min(System.currentTimeMillis() - (ticks * 50), run.startTimestamp);
        if (run.finishedSplit == -1 && ((ServerPlayerEntityAccessor) serverPlayer).seenCredits()) {
            run.finishedSplit = this.getGameTicks();
            run.finishedTimestamp = System.currentTimeMillis();
        }
        if (run.overworldSplit == -1 && advancementDone("minecraft:nether/root", "entered_nether"))
            run.overworldSplit = this.getGameTicks();

        if (run.netherSplit == -1 && run.overworldSplit != -1 && serverPlayer.world.getRegistryKey() == World.OVERWORLD) {
            run.netherSplit = this.getGameTicks();
        }
        if (run.strongholdSplit == -1 && advancementDone("minecraft:end/root", "entered_end"))
            run.strongholdSplit = this.getGameTicks();

        return run;
    }

    private void render() {
        // final MatrixStack matrixStack = new MatrixStack();

        SingleRunData run = updateRunData();
        if (this.persistDebounce.boing()) store.persist();

        final TextRenderer textRenderer = client.textRenderer;
        Hud hud = new Hud(textRenderer, 5, 5, false, 0);
        if (((MinecraftClientAccessor) client).getGameOptions().debugEnabled) return;
        String gameTimeLabel = "Game Time: " + getGameTime();
        String realTimeLabel = "Real Time: " + timeFormat(System.currentTimeMillis() - run.startTimestamp);
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
        hud.drawBackground(4,0x99000011);
        hud.renderText();
    }

    private String splitLabel(long ticks) {
        return ticks == -1 ? "--" : tickTime(ticks);
    }

    private long getGameTicks() {
        return player.world.getTime();
    }

    private String tickTime(long ticks) {
        return timeFormat(ticks * 50);
    }

    private String getGameTime() {
        return tickTime(getGameTicks());
    }

    private String timeFormat(long ms) {
        return DurationFormatUtils.formatDurationHMS(ms);
    }

}
