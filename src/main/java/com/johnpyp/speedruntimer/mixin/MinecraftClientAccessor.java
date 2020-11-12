package com.johnpyp.speedruntimer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("options")
    public GameOptions getGameOptions();
}
