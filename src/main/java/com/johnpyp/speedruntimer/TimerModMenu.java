package com.johnpyp.speedruntimer;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

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
        eb.startIntField(new TranslatableText("X Offset"), config.data.xOffset)
            .setDefaultValue(5)
            .setSaveConsumer(integer -> config.data.xOffset = integer)
            .build());
    general.addEntry(
        eb.startIntField(new TranslatableText("Y Offset"), config.data.yOffset)
            .setDefaultValue(5)
            .setSaveConsumer(integer -> config.data.yOffset = integer)
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
