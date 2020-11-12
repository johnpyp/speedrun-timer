package com.johnpyp.speedruntimer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

class TextStuff {
  public String text;
  public int x;
  public int y;
  public int color;

  TextStuff(String text, int x, int y, int color) {
    this.text = text;
    this.x = x;
    this.y = y;
    this.color = color;
  }
}

public class Hud {

  public List<TextStuff> textList;
  private final TextRenderer textRenderer;
  private final MatrixStack matrixStack;
  private int maxLen = 0;
  private final int xOffset;
  private final int yOffset;
  private int lastY;
  private int lastX;

  Hud(TextRenderer textRenderer, int xOffset, int yOffset) {
    this.textRenderer = textRenderer;
    this.matrixStack = new MatrixStack();
    this.xOffset = this.lastX = xOffset;
    this.yOffset = this.lastY = yOffset;
    this.textList = new ArrayList<>();
  }

  public void draw(String text, int color) {
    maxLen = Math.max(maxLen, textRenderer.getWidth(text));
    textList.add(new TextStuff(text, lastX, lastY, color));
    lastX = lastX + textRenderer.getWidth(text);
  }

  public void drawLine(String text, int heightOffset, int color) {
    lastY = lastY + heightOffset;
    lastX = xOffset;
    textList.add(new TextStuff(text, lastX, lastY, color));
  }

  public void renderText() {
    for (TextStuff textStuff : textList) {
      textRenderer.drawWithShadow(
          matrixStack, textStuff.text, textStuff.x, textStuff.y, textStuff.color);
    }
  }

  public void insertSpace(int height) {
    lastY = lastY + height;
  }

  public void drawBackground(int padding, int color) {
    DrawableHelper.fill(
        matrixStack,
        xOffset - padding,
        yOffset - padding,
        maxLen + 5 + padding,
        lastY + textRenderer.fontHeight + padding,
        color);
  }
}
