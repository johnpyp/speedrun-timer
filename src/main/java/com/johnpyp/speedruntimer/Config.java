package com.johnpyp.speedruntimer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class ConfigData {
    public int xOffset;
    public int yOffset;
    ConfigData(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }
}
public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public ConfigData data;
    public File configFile;

    Config(File configFile) {
        this.configFile = configFile;
        data = new ConfigData(5, 5);
    }

    public static Config of(File configFile) {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Config(configFile);
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
            data = GSON.fromJson(reader, ConfigData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
