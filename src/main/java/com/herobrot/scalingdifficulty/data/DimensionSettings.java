package com.herobrot.scalingdifficulty.data;

import com.google.gson.JsonObject;
import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import net.minecraft.util.GsonHelper;

public class DimensionSettings {
    public final Integer distanceCoordinatesX;
    public final Integer distanceCoordinatesZ;
    public final int increasingDistance;
    public final double distanceFactor;
    public final int increasingTime;
    public final double timeFactor;
    public final int heightDistance;
    public final double heightFactor;
    public final double maxFactorHealth;
    public final double maxFactorDamage;
    public final double maxFactorProtection;
    public final double maxFactorSpeed;
    public final double startingFactor;
    public final int startingDistance;
    public final int startingTime;
    public final int startingHeight;
    public final boolean positiveHeightIncrement;
    public final boolean negativeHeightIncrement;

    public DimensionSettings(JsonObject data) {
        ScalingDifficultyConfig c = ScalingDifficulty.CONFIG;
        this.distanceCoordinatesX = data.has("distanceCoordinatesX") ? data.get("distanceCoordinatesX").getAsInt() : null;
        this.distanceCoordinatesZ = data.has("distanceCoordinatesZ") ? data.get("distanceCoordinatesZ").getAsInt() : null;
        this.increasingDistance = GsonHelper.getAsInt(data, "increasingDistance", c.increasingDistance);
        this.distanceFactor = GsonHelper.getAsDouble(data, "distanceFactor", c.distanceFactor);
        this.increasingTime = GsonHelper.getAsInt(data, "increasingTime", c.increasingTime);
        this.timeFactor = GsonHelper.getAsDouble(data, "timeFactor", c.timeFactor);
        this.heightDistance = GsonHelper.getAsInt(data, "heightDistance", c.heightDistance);
        this.heightFactor = GsonHelper.getAsDouble(data, "heightFactor", c.heightFactor);
        this.maxFactorHealth = GsonHelper.getAsDouble(data, "maxFactorHealth", c.maxFactorHealth);
        this.maxFactorDamage = GsonHelper.getAsDouble(data, "maxFactorDamage", c.maxFactorDamage);
        this.maxFactorProtection = GsonHelper.getAsDouble(data, "maxFactorProtection", c.maxFactorProtection);
        this.maxFactorSpeed = GsonHelper.getAsDouble(data, "maxFactorSpeed", c.maxFactorSpeed);
        this.startingFactor = GsonHelper.getAsDouble(data, "startingFactor", c.startingFactor);
        this.startingDistance = GsonHelper.getAsInt(data, "startingDistance", c.startingDistance);
        this.startingTime = GsonHelper.getAsInt(data, "startingTime", c.startingTime);
        this.startingHeight = GsonHelper.getAsInt(data, "startingHeight", c.startingHeight);
        this.positiveHeightIncrement = GsonHelper.getAsBoolean(data, "positiveHeightIncrement", c.positiveHeightIncrement);
        this.negativeHeightIncrement = GsonHelper.getAsBoolean(data, "negativeHeightIncrement", c.negativeHeightIncrement);
    }
}