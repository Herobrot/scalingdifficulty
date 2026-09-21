package com.herobrot.scalingdifficulty;

import com.herobrot.scalingdifficulty.config.ConfigInit;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import com.herobrot.scalingdifficulty.events.ClientEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ScalingDifficulty.MOD_ID)
public class ScalingDifficulty {
    public static final String MOD_ID = "scalingdifficulty";
    public static final Logger LOGGER = LogManager.getLogger();
    public static ScalingDifficultyConfig CONFIG;
    public static boolean isLevelplateLoaded = false;
    public static boolean isHerosLevelsLoaded = false;

    public ScalingDifficulty(IEventBus modEventBus, ModContainer modContainer) {
        ConfigInit.init();
        CONFIG = ConfigInit.CONFIG;
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        verifyingMods();
        if (FMLEnvironment.dist.isClient()) ClientEvents.registerConfigScreen(modContainer);
    }

    private void verifyingMods() {
        isLevelplateLoaded = isModLoaded("levelplate");
        isHerosLevelsLoaded = isModLoaded("heroslevels");
    }

    private static boolean isModLoaded(String modId) { return ModList.get().isLoaded(modId); }

    public static String getModVersion() {
        return ModList.get().getModContainerById(ScalingDifficulty.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("1.1.0");
    }
}