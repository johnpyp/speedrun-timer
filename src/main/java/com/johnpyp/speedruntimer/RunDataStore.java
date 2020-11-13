package com.johnpyp.speedruntimer;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

  private static final Type TYPE_TOKEN = new TypeToken<Map<String, SingleRunData>>() {}.getType();
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final Map<String, SingleRunData> data;
  private final File file;

  private RunDataStore(File file, Map<String, SingleRunData> data) {
    this.file = file;
    this.data = data;
  }

  public static RunDataStore of(File file) {
    Map<String, SingleRunData> data = new HashMap<>();
    if (file.isFile()) {
      try (FileReader reader = new FileReader(file)) {
        data.putAll(GSON.fromJson(reader, TYPE_TOKEN));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return new RunDataStore(file, data);
  }

  private static String getRunKey(MinecraftServer server) {
    String levelPath = server.getSavePath(WorldSavePath.ROOT).normalize().toString();
    long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
    return levelPath + ":" + seed;
  }

  public SingleRunData solveItem(MinecraftServer server, long ticks) {
    String key = getRunKey(server);
    SingleRunData run = data.get(key);
    if (run == null) {
      run = new SingleRunData(ticks);
      data.put(key, run);
      return run;
    }

    return data.get(key);
  }

  public void persist() {
    if (data == null || data.isEmpty()) return;
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(GSON.toJson(data));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
