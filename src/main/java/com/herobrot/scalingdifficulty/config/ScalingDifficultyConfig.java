package com.herobrot.scalingdifficulty.config;

import java.util.ArrayList;
import java.util.List;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "scalingdifficulty")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class ScalingDifficultyConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public int increasingDistance = 300;
    @ConfigEntry.Gui.Tooltip
    public double distanceFactor = 0.1D;

    @ConfigEntry.Gui.Tooltip
    public int increasingTime = 60;
    @ConfigEntry.Gui.Tooltip
    public double timeFactor = 0.05D;

    @ConfigEntry.Gui.Tooltip
    public int heightDistance = 30;
    @ConfigEntry.Gui.Tooltip
    public double heightFactor = 0.1D;
    
    
    

    @ConfigEntry.Gui.Tooltip
    public double maxFactorHealth = 3.0D;
    public double maxFactorDamage = 3.0D;
    public double maxFactorProtection = 1.5D;
    public double maxFactorSpeed = 2.0D;

    public boolean allowRandomValues = false;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int randomChance = 10;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int randomFactor = 10;

    @ConfigEntry.Gui.Tooltip
    public boolean extraXp = true;
    public float maxXPFactor = 2.0f;

    public double startingFactor = 1.0D;
    @ConfigEntry.Gui.Tooltip
    public int startingDistance = 0;
    @ConfigEntry.Gui.Tooltip
    public int startingTime = 0;
    @ConfigEntry.Gui.Tooltip
    public int startingHeight = 62;

    public boolean positiveHeightIncrement = true;
    public boolean negativeHeightIncrement = true;

    public boolean affectBosses = true;
    @ConfigEntry.Gui.Tooltip
    public boolean excludeDistanceInOtherDimension = false;
    @ConfigEntry.Gui.Tooltip
    public boolean excludeTimeInOtherDimension = false;
    @ConfigEntry.Gui.Tooltip
    public boolean excludeHeightInOtherDimension = false;
    @ConfigEntry.Gui.Tooltip
    public boolean dropMoreLoot = true;
    @ConfigEntry.Gui.Tooltip
    public float moreLootChance = 0.02F;
    public float maxLootChance = 0.7F;
    @ConfigEntry.Gui.Tooltip
    public float chanceForEachItem = 0.5F;

    //TODO until LevelZ is ported
    @ConfigEntry.Gui.Tooltip
    public double levelFactor = 0.0D;
    @ConfigEntry.Gui.Tooltip
    public double playerRadius = 100.0D;

    @ConfigEntry.Gui.Tooltip
    public boolean hudTesting = false;

    @ConfigEntry.Gui.Tooltip
    public ArrayList<String> excludedEntity = new ArrayList<>(List.of("the_bumblezone:cosmic_crystal_entity"));

    @ConfigEntry.Category("monster_setting")
    public boolean allowSpecialZombie = true;
    @ConfigEntry.Category("monster_setting")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int bigZombieChance = 10;
    @ConfigEntry.Category("monster_setting")
    public float bigZombieSlownessFactor = 0.7F;
    @ConfigEntry.Category("monster_setting")
    public int bigZombieBonusLifePoints = 10;
    @ConfigEntry.Category("monster_setting")
    public int bigZombieBonusDamage = 2;
    @ConfigEntry.Category("monster_setting")
    public float bigZombieSize = 1.3F;
    @ConfigEntry.Category("monster_setting")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int speedZombieChance = 10;
    @ConfigEntry.Category("monster_setting")
    public double speedZombieSpeedFactor = 1.3D;
    @ConfigEntry.Category("monster_setting")
    public int speedZombieMalusLifePoints = 10;
    @ConfigEntry.Category("monster_setting")
    @ConfigEntry.Gui.Tooltip
    public boolean dynamicBossModification = true;
    @ConfigEntry.Category("monster_setting")
    @ConfigEntry.Gui.Tooltip
    public double dynamicBossModificator = 0.3D;
    @ConfigEntry.Category("monster_setting")
    public double bossMaxFactor = 3.0D;
    @ConfigEntry.Category("monster_setting")
    public double bossDistanceFactor = 0.0D;
    @ConfigEntry.Category("monster_setting")
    public double bossTimeFactor = 0.1D;
    @ConfigEntry.Category("monster_setting")
    @ConfigEntry.Gui.Tooltip
    public double bossDistance = 256.0D;
    @ConfigEntry.Category("monster_setting")
    @ConfigEntry.Gui.Tooltip
    public double creeperExplosionFactor = 1.0D;
    @ConfigEntry.Category("monster_setting")
    public boolean affectAnimalBabies = false;
}