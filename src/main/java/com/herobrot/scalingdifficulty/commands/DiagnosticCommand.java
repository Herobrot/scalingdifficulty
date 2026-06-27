package com.herobrot.scalingdifficulty.commands;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.compat.LevelplateCompat;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DiagnosticCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sddiag")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;

                    double range = 10.0;
                    Vec3 start = player.getEyePosition();
                    Vec3 direction = player.getViewVector(1.0f);
                    Vec3 end = start.add(direction.scale(range));

                    AABB searchBox = player.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0);
                    EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                            player.level(), player, start, end, searchBox,
                            entity -> entity instanceof Mob && !entity.isSpectator()
                    );

                    if (entityHit == null || !(entityHit.getEntity() instanceof Mob mob)) {
                        ctx.getSource().sendFailure(Component.literal("Apunta a un mob (máx. 10 bloques)"));
                        return 0;
                    }

                    float multiplier = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);
                    boolean isBigZombie = mob.getData(ModAttachments.BIG_ZOMBIE);
                    boolean isSpeedyZombie = mob.getData(ModAttachments.SPEEDY_ZOMBIE);

                    StringBuilder sb = new StringBuilder();
                    sb.append("=== ScalingDifficulty Diagnóstico ===\n");
                    sb.append("Mob: ").append(mob.getType().toShortString()).append("\n");
                    sb.append("DIFFICULTY_MULTIPLIER: ").append(multiplier).append("\n");
                    sb.append("Big Zombie: ").append(isBigZombie).append("\n");
                    sb.append("Speedy Zombie: ").append(isSpeedyZombie).append("\n");
                    sb.append("Vida actual: ").append(mob.getHealth())
                            .append(" / ").append(mob.getMaxHealth()).append("\n");

                    if (ScalingDifficulty.isLevelplateLoaded) {
                        int level = LevelplateCompat.getMobLevel(mob);
                        float dropChance = Math.min(level * ScalingDifficulty.CONFIG.moreLootChance,
                                ScalingDifficulty.CONFIG.maxLootChance);
                        sb.append("--- Levelplate ---\n");
                        sb.append("Nivel calculado: ").append(level).append("\n");
                        sb.append("Drop chance: ").append(String.format("%.2f%%", dropChance * 100)).append("\n");
                    } else {
                        float dropChance = Math.min(multiplier * ScalingDifficulty.CONFIG.moreLootChance,
                                ScalingDifficulty.CONFIG.maxLootChance);
                        sb.append("Drop chance (sin Levelplate): ")
                                .append(String.format("%.2f%%", dropChance * 100)).append("\n");
                    }

                    ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                    return 1;
                })
        );
    }
}