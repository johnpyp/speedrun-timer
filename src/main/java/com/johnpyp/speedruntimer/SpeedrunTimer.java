package com.johnpyp.speedruntimer;

import com.johnpyp.speedruntimer.datastorage.DataStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.File;

@Environment(EnvType.CLIENT)
public class SpeedrunTimer implements ModInitializer {

  public static final Config config =
      Config.of(
          new File(
              FabricLoader.getInstance().getConfigDir().toFile(), "speedrun-timer.config.json"));

  @Override
  public void onInitialize() {
    config.loadConfig();
    MinecraftClient client = MinecraftClient.getInstance();
    File configDir = FabricLoader.getInstance().getConfigDir().toFile();
    DataStorage store = DataStorage.of(new File(configDir, "speedrun-timer.data.json"));
    store.refreshBests("");
    TickHandler tickHandler = new TickHandler(client, store, config);
    HudRenderCallback.EVENT.register((__, ___) -> tickHandler.tick());
  }



}
