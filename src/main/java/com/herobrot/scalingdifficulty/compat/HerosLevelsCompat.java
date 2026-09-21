package com.herobrot.scalingdifficulty.compat;

import com.herobrot.heroslevels.api.HerosLevelsAPI;
import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.data.DimensionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class HerosLevelsCompat {

    public static double getLevelFactor(ServerLevel level, double x, double y, double z, DimensionSettings settings) {
        return getAveragePlayerLevel(level, x, y, z, settings.getPlayerRadius()) * settings.getLevelFactor();
    }

    public static double getAveragePlayerLevel(ServerLevel level, double x, double y, double z, double radius) {
        int count = 0;
        long total = 0;
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative()) continue;
            if (player.distanceToSqr(x, y, z) <= radiusSqr) {
                count++;
                total += HerosLevelsAPI.getOverallLevel(player);
            }
        }
        return count > 0 ? (double) total / count : 0.0;
    }


    public static int getClientPlayerLevel(Player player) {
        return HerosLevelsAPI.getOverallLevel(player);
    }

    public static boolean shouldApplyLevelFactor(DimensionSettings settings){
        return ScalingDifficulty.isHerosLevelsLoaded && settings.getLevelFactor() > 0.001D;
    }
}