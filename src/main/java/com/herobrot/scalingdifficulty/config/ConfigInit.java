package com.herobrot.scalingdifficulty.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class ConfigInit {
    public static ScalingDifficultyConfig CONFIG = new ScalingDifficultyConfig();

    public static void init() {
        AutoConfig.register(ScalingDifficultyConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ScalingDifficultyConfig.class).getConfig();
    }
}