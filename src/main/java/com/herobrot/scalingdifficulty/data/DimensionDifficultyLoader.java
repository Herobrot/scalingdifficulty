package com.herobrot.scalingdifficulty.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herobrot.scalingdifficulty.ScalingDifficulty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DimensionDifficultyLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, DimensionSettings> DIMENSIONS = new HashMap<>();

    // Objeto de respaldo pre-calculado para cuando una dimensión no tiene JSON propio
    private static DimensionSettings FALLBACK_SETTINGS;

    public DimensionDifficultyLoader() {
        // Apunta al directorio data/*/difficulty/
        super(GSON, "difficulty");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        DIMENSIONS.clear();
        FALLBACK_SETTINGS = new DimensionSettings(new JsonObject()); // Inicializa solo con defaults

        jsonMap.forEach((location, element) -> {
            try {
                JsonObject data = element.getAsJsonObject();
                if (data.has("dimension")) {
                    String dimensionId = data.get("dimension").getAsString();
                    DIMENSIONS.put(dimensionId, new DimensionSettings(data));
                    ScalingDifficulty.LOGGER.info("Loaded custom difficulty rules for dimension: {}", dimensionId);
                }
            } catch (Exception e) {
                ScalingDifficulty.LOGGER.error("Failed to parse dimension difficulty datapack from {}: {}", location, e.getMessage());
            }
        });
    }

    public static DimensionSettings getSettings(String dimensionId) {
        // Retorna las reglas de la dimensión si existen, si no, retorna el fallback de Cloth Config
        if (FALLBACK_SETTINGS == null) {
            FALLBACK_SETTINGS = new DimensionSettings(new JsonObject());
        }
        return DIMENSIONS.getOrDefault(dimensionId, FALLBACK_SETTINGS);
    }
}