package com.herobrot.scalingdifficulty.data;

import com.google.gson.JsonObject;
import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;

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
        this.increasingDistance = data.has("increasingDistance") ? data.get("increasingDistance").getAsInt() : c.increasingDistance;
        this.distanceFactor = data.has("distanceFactor") ? data.get("distanceFactor").getAsDouble() : c.distanceFactor;

        this.increasingTime = data.has("increasingTime") ? data.get("increasingTime").getAsInt() : c.increasingTime;
        this.timeFactor = data.has("timeFactor") ? data.get("timeFactor").getAsDouble() : c.timeFactor;

        this.heightDistance = data.has("heightDistance") ? data.get("heightDistance").getAsInt() : c.heightDistance;
        this.heightFactor = data.has("heightFactor") ? data.get("heightFactor").getAsDouble() : c.heightFactor;

        this.maxFactorHealth = data.has("maxFactorHealth") ? data.get("maxFactorHealth").getAsDouble() : c.maxFactorHealth;
        this.maxFactorDamage = data.has("maxFactorDamage") ? data.get("maxFactorDamage").getAsDouble() : c.maxFactorDamage;
        this.maxFactorProtection = data.has("maxFactorProtection") ? data.get("maxFactorProtection").getAsDouble() : c.maxFactorProtection;
        this.maxFactorSpeed = data.has("maxFactorSpeed") ? data.get("maxFactorSpeed").getAsDouble() : c.maxFactorSpeed;

        this.startingFactor = data.has("startingFactor") ? data.get("startingFactor").getAsDouble() : c.startingFactor;
        this.startingDistance = data.has("startingDistance") ? data.get("startingDistance").getAsInt() : c.startingDistance;
        this.startingTime = data.has("startingTime") ? data.get("startingTime").getAsInt() : c.startingTime;
        this.startingHeight = data.has("startingHeight") ? data.get("startingHeight").getAsInt() : c.startingHeight;

        this.positiveHeightIncrement = data.has("positiveHeightIncrement") ? data.get("positiveHeightIncrement").getAsBoolean() : c.positiveHeightIncrement;
        this.negativeHeightIncrement = data.has("negativeHeightIncrement") ? data.get("negativeHeightIncrement").getAsBoolean() : c.negativeHeightIncrement;
    }
}