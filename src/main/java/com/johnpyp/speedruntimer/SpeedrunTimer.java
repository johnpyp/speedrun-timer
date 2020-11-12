package com.johnpyp.speedruntimer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class SpeedrunTimer implements ModInitializer {

  @Override
  public void onInitialize() {
    MinecraftClient client = MinecraftClient.getInstance();
    RunDataStore store = new RunDataStore("speedrun-timer.data.json");
    try {
      store.load();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    TickHandler tickHandler = new TickHandler(client, store);
    HudRenderCallback.EVENT.register((__, ___) -> tickHandler.tick());
  }
}
