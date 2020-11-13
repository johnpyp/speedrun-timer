package com.johnpyp.speedruntimer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RunDataStore {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final File file;

  private Map<String, SingleRunData> data = null;

  RunDataStore(String filename) {
    file = new File(FabricLoader.getInstance().getConfigDir().toFile(), filename);
  }

  public void load() throws IOException {

    if (!file.exists()) {
      file.createNewFile();
    }
    FileReader reader = new FileReader(file);
    Type dataMapType = new TypeToken<Map<String, SingleRunData>>() {}.getType();
    data = GSON.fromJson(reader, dataMapType);
    System.out.println(data);
    if (data == null) data = new HashMap<>();
  }

  public static String getRunKey(MinecraftServer server) {
    String levelPath = server.getSavePath(WorldSavePath.ROOT).normalize().toString();
    long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
    return levelPath + ":" + seed;
  }

  public SingleRunData solveItem(String key, long ticks) {
    if (!data.containsKey(key)) {
      SingleRunData run = new SingleRunData();
      run.startTimestamp = System.currentTimeMillis();
      run.ticks = ticks;
      data.put(key, run);
      return run;
    }
    if (ticks > 20 && (ticks + 20) < data.get(key).ticks) {
      data.remove(key);
      return solveItem(key, ticks);
    }

    return data.get(key);
  }

  public void persist() {
    if (data != null) {
      try (FileWriter writer = new FileWriter(file)) {
        writer.write(GSON.toJson(data));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
