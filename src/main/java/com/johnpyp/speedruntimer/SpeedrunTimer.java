package com.johnpyp.speedruntimer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class SpeedrunTimer implements ModInitializer {

  @Override
  public void onInitialize() {
    RunDataStore store = new RunDataStore("speedrun-timer.data.json");
    try {
      store.load();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    TickHandler tickHandler = new TickHandler(store);
    HudRenderCallback.EVENT.register((__, ___) -> tickHandler.tick());
  }
}
