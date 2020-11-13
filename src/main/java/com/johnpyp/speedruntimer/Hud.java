package com.johnpyp.speedruntimer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

final class Hud {

  private final List<TextStuff> textList;
  private final TextRenderer textRenderer;
  private final MatrixStack matrixStack;
  private final int xOffset;
  private final int yOffset;
  private int maxLen;
  private int lastY;
  private int lastX;

  Hud(TextRenderer textRenderer, int xOffset, int yOffset) {
    this.textRenderer = textRenderer;
    this.xOffset = lastX = xOffset;
    this.yOffset = lastY = yOffset;
    matrixStack = new MatrixStack();
    textList = new ArrayList<>();
  }

  public Hud print(String text, int color) {
    maxLen = Math.max(maxLen, textRenderer.getWidth(text));
    textList.add(new TextStuff(text, lastX, lastY, color));
    lastX = lastX + textRenderer.getWidth(text);
    return this;
  }

  public Hud println(String text, int heightOffset, int color) {
    maxLen = Math.max(maxLen, textRenderer.getWidth(text));
    lastY = lastY + heightOffset;
    lastX = xOffset;
    textList.add(new TextStuff(text, lastX, lastY, color));
    return this;
  }

  public void render(int backgroundPadding, int backgroundColor) {
    drawBackground(backgroundPadding, backgroundColor);
    for (TextStuff textStuff : textList) {
      textRenderer.drawWithShadow(
          matrixStack, textStuff.text, textStuff.x, textStuff.y, textStuff.color);
    }
  }

  public Hud insertSpace(int height) {
    lastY = lastY + height;
    return this;
  }

  private void drawBackground(int padding, int color) {
    DrawableHelper.fill(
        matrixStack,
        xOffset - padding,
        yOffset - padding,
        xOffset + maxLen + padding,
        lastY + textRenderer.fontHeight + padding,
        color);
  }

  static final class TextStuff {
    public final String text;
    public final int x;
    public final int y;
    public final int color;

    TextStuff(String text, int x, int y, int color) {
      this.text = text;
      this.x = x;
      this.y = y;
      this.color = color;
    }
  }
}
