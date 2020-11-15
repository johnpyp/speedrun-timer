package com.johnpyp.speedruntimer;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class TimerModMenu implements ModMenuApi {
  public static final Config config = SpeedrunTimer.config;

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return this::getConfigScreenByCloth;
  }

  public Screen getConfigScreenByCloth(Screen parent) {
    ConfigBuilder builder =
        ConfigBuilder.create().setParentScreen(parent).setTitle(new LiteralText("Speedrun Timer"));

    ConfigEntryBuilder eb = builder.entryBuilder();
    ConfigCategory general = builder.getOrCreateCategory(new LiteralText("General"));

    general.addEntry(
        eb.startIntField(new LiteralText("X Offset"), config.data.xOffset)
            .setDefaultValue(5)
            .setSaveConsumer(integer -> config.data.xOffset = integer)
            .build());

    general.addEntry(
        eb.startIntField(new LiteralText("Y Offset"), config.data.yOffset)
            .setDefaultValue(5)
            .setSaveConsumer(val -> config.data.yOffset = val)
            .build());

    general.addEntry(
        eb.startDoubleField(
                new LiteralText("Background transparency"), config.data.backgroundTransparency)
            .setDefaultValue(0.5)
            .setMin(0.0)
            .setMax(1.0)
            .setSaveConsumer(val -> config.data.backgroundTransparency = val)
            .build());

    general.addEntry(
        eb.startBooleanToggle(
                new LiteralText("Display seed on hud after run is finished?"), config.data.showSeed)
            .setDefaultValue(true)
            .setSaveConsumer(val -> config.data.showSeed = val)
            .build());

    general.addEntry(
        eb.startBooleanToggle(
                new LiteralText("Show comparison best times?"), config.data.showCompareSplits)
            .setDefaultValue(true)
            .setSaveConsumer(val -> config.data.showCompareSplits = val)
            .build());
    general.addEntry(
        eb.startBooleanToggle(
                new LiteralText(
                    "Compare using individual best splits instead of personal best run?"),
                config.data.useBestSplits)
            .setDefaultValue(false)
            .setSaveConsumer(val -> config.data.useBestSplits = val)
            .build());

    return builder
        .setSavingRunnable(
            () -> {
              try {
                config.saveConfig();
              } catch (Exception e) {
                e.printStackTrace();
              }
              config.loadConfig();
            })
        .build();
  }
}
