package com.gregtechceu.gtceu.client.shader.post;

import com.gregtechceu.gtceu.config.ConfigHolder;

public class BloomEffect {

    public static float strength = ConfigHolder.INSTANCE.client.shader.strength;
    public static float baseBrightness = ConfigHolder.INSTANCE.client.shader.baseBrightness;
    public static float highBrightnessThreshold = ConfigHolder.INSTANCE.client.shader.highBrightnessThreshold;
    public static float lowBrightnessThreshold = ConfigHolder.INSTANCE.client.shader.lowBrightnessThreshold;
    public static float step = ConfigHolder.INSTANCE.client.shader.step;
}
