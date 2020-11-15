package com.johnpyp.speedruntimer.datastorage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class DataStorage {

  static final class Data {
    public String version = "2.0";
    public Runs runs = new Runs();
    public PersonalBest personalBest = new PersonalBest();
    public BestSplits bestSplits = new BestSplits();
  }

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final Data data;
  private final File file;

  private DataStorage(File file, Data data) {
    this.file = file;
    this.data = data;
  }

  public static DataStorage of(File file) {
    Data initData = new Data();
    if (file.isFile()) {
      try (FileReader reader = new FileReader(file)) {
        initData = GSON.fromJson(reader, Data.class);
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
    return new DataStorage(file, initData);
  }

  public void persist() {
    if (data == null) return;
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(GSON.toJson(data));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Runs getRuns() {
    return this.data.runs;
  }

  public BestSplits getBestSplits() {
    return this.data.bestSplits;
  }

  public PersonalBest getPersonalBest() {
    return this.data.personalBest;
  }

  public void refreshBests(String ignoreKey) {
    getRuns().forEach((k, v) -> {
      if (k.equals(ignoreKey)) return;
      data.bestSplits.tryRun(v);
      data.personalBest.tryRun(v);
    });
  }
}
