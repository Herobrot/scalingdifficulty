package com.herobrot.scalingdifficulty;

import com.herobrot.scalingdifficulty.config.ConfigInit;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ScalingDifficulty.MOD_ID)
public class ScalingDifficulty {
    public static final String MOD_ID = "scalingdifficulty";
    public static final Logger LOGGER = LogManager.getLogger();

    // Puntero para mantener compatibilidad con las llamadas en la API
    public static ScalingDifficultyConfig CONFIG;

    public ScalingDifficulty(IEventBus modEventBus, ModContainer modContainer) {
        // 1. Inicializar Configuración y Datos
        ConfigInit.init();
        CONFIG = ConfigInit.CONFIG;
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // 2. Registrar interfaz gráfica (Solo Cliente)
        if (FMLEnvironment.dist.isClient()) {
            com.herobrot.scalingdifficulty.events.ClientEvents.registerConfigScreen(modContainer);
        }

        LOGGER.info("ScalingDifficulty Core Initialized!");
    }
}