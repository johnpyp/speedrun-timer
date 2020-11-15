package com.johnpyp.speedruntimer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
  public static final class ConfigData {
    public int xOffset;
    public int yOffset;
    public double backgroundTransparency;
    public boolean showSeed;
    public boolean showCompareSplits;
    public boolean useBestSplits;

    ConfigData(int xOffset, int yOffset, double backgroundTransparency, boolean showSeed, boolean showCompareSplits, boolean useBestSplits) {
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.backgroundTransparency = backgroundTransparency;
      this.showSeed = showSeed;
      this.showCompareSplits = showCompareSplits;
      this.useBestSplits = useBestSplits;
    }
  }

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  public final File configFile;
  public ConfigData data;

  Config(File configFile) {
    this.configFile = configFile;
    data = getDefaultConfigData();
  }

  public static ConfigData getDefaultConfigData() {
    return new ConfigData(5, 5, 0.5, true, true, false);
  }

  public static Config of(File configFile) {
    Config config = new Config(configFile);
    if (!configFile.exists()) {
      try {
        configFile.createNewFile();
        config.data = getDefaultConfigData();
        config.saveConfig();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return config;
  }

  public void saveConfig() {
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write(GSON.toJson(data));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void loadConfig() {
    try (FileReader reader = new FileReader(configFile)) {
      ConfigData nextData = GSON.fromJson(reader, ConfigData.class);
      if (nextData == null) {
        data = getDefaultConfigData();
        saveConfig();
        return;
      }
      data = nextData;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
