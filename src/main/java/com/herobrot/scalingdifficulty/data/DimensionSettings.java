package com.herobrot.scalingdifficulty.data;

import com.google.gson.JsonObject;
import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;

/**
 * Ajustes de dificultad para una dimension. Solo almacena los valores que el
 * datapack define explicitamente; los getters resuelven contra la config
 * global en vivo, permitiendo cambios en caliente de esta ultima.
 */
public class DimensionSettings {

    @Nullable private final Integer distanceCoordinatesX;
    @Nullable private final Integer distanceCoordinatesZ;
    @Nullable private final Integer increasingDistance;
    @Nullable private final Double distanceFactor;
    @Nullable private final Integer increasingTime;
    @Nullable private final Double timeFactor;
    @Nullable private final Integer heightDistance;
    @Nullable private final Double heightFactor;
    @Nullable private final Double maxFactorHealth;
    @Nullable private final Double maxFactorDamage;
    @Nullable private final Double maxFactorProtection;
    @Nullable private final Double maxFactorSpeed;
    @Nullable private final Double levelFactor;
    @Nullable private final Double playerRadius;
    @Nullable private final Double startingFactor;
    @Nullable private final Integer startingDistance;
    @Nullable private final Integer startingTime;
    @Nullable private final Integer startingHeight;
    @Nullable private final Boolean positiveHeightIncrement;
    @Nullable private final Boolean negativeHeightIncrement;

    public DimensionSettings(JsonObject data) {
        this.distanceCoordinatesX = getNullableInt(data, "distanceCoordinatesX");
        this.distanceCoordinatesZ = getNullableInt(data, "distanceCoordinatesZ");
        this.increasingDistance = getNullableInt(data, "increasingDistance");
        this.distanceFactor = getNullableDouble(data, "distanceFactor");
        this.increasingTime = getNullableInt(data, "increasingTime");
        this.timeFactor = getNullableDouble(data, "timeFactor");
        this.heightDistance = getNullableInt(data, "heightDistance");
        this.heightFactor = getNullableDouble(data, "heightFactor");
        this.maxFactorHealth = getNullableDouble(data, "maxFactorHealth");
        this.maxFactorDamage = getNullableDouble(data, "maxFactorDamage");
        this.maxFactorProtection = getNullableDouble(data, "maxFactorProtection");
        this.maxFactorSpeed = getNullableDouble(data, "maxFactorSpeed");
        this.levelFactor = getNullableDouble(data, "levelFactor");
        this.playerRadius = getNullableDouble(data, "playerRadius");
        this.startingFactor = getNullableDouble(data, "startingFactor");
        this.startingDistance = getNullableInt(data, "startingDistance");
        this.startingTime = getNullableInt(data, "startingTime");
        this.startingHeight = getNullableInt(data, "startingHeight");
        this.positiveHeightIncrement = getNullableBoolean(data, "positiveHeightIncrement");
        this.negativeHeightIncrement = getNullableBoolean(data, "negativeHeightIncrement");
    }

    @Nullable
    private static Integer getNullableInt(JsonObject data, String key) {
        return data.has(key) ? GsonHelper.getAsInt(data, key) : null;
    }

    @Nullable
    private static Double getNullableDouble(JsonObject data, String key) {
        return data.has(key) ? GsonHelper.getAsDouble(data, key) : null;
    }

    @Nullable
    private static Boolean getNullableBoolean(JsonObject data, String key) {
        return data.has(key) ? GsonHelper.getAsBoolean(data, key) : null;
    }

    private static ScalingDifficultyConfig config() { return ScalingDifficulty.CONFIG; }

    @Nullable
    public Integer getDistanceCoordinatesX() { return distanceCoordinatesX != null ? distanceCoordinatesX : null; }

    @Nullable
    public Integer getDistanceCoordinatesZ() { return distanceCoordinatesZ != null ? distanceCoordinatesZ : null; }

    public int getIncreasingDistance() { return increasingDistance != null ? increasingDistance : config().increasingDistance; }

    public double getDistanceFactor() { return distanceFactor != null ? distanceFactor : config().distanceFactor; }

    public int getIncreasingTime() { return increasingTime != null ? increasingTime : config().increasingTime; }

    public double getTimeFactor() { return timeFactor != null ? timeFactor : config().timeFactor; }

    public int getHeightDistance() { return heightDistance != null ? heightDistance : config().heightDistance; }

    public double getHeightFactor() { return heightFactor != null ? heightFactor : config().heightFactor; }

    public double getMaxFactorHealth() { return maxFactorHealth != null ? maxFactorHealth : config().maxFactorHealth; }

    public double getMaxFactorDamage() { return maxFactorDamage != null ? maxFactorDamage : config().maxFactorDamage; }

    public double getMaxFactorProtection() { return maxFactorProtection != null ? maxFactorProtection : config().maxFactorProtection; }

    public double getMaxFactorSpeed() { return maxFactorSpeed != null ? maxFactorSpeed : config().maxFactorSpeed; }

    public double getLevelFactor() { return levelFactor != null ? levelFactor : config().levelFactor; }

    public double getPlayerRadius() { return playerRadius != null ? playerRadius : config().playerRadius; }

    public double getStartingFactor() { return startingFactor != null ? startingFactor : config().startingFactor; }

    public int getStartingDistance() { return startingDistance != null ? startingDistance : config().startingDistance; }

    public int getStartingTime() { return startingTime != null ? startingTime : config().startingTime; }

    public int getStartingHeight() { return startingHeight != null ? startingHeight : config().startingHeight; }

    public boolean isPositiveHeightIncrement() { return positiveHeightIncrement != null ? positiveHeightIncrement : config().positiveHeightIncrement; }

    public boolean isNegativeHeightIncrement() { return negativeHeightIncrement != null ? negativeHeightIncrement : config().negativeHeightIncrement; }
}