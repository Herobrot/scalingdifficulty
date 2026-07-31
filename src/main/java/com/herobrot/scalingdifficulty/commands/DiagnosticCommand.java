package com.herobrot.scalingdifficulty.commands;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.api.DifficultyCalculator;
import com.herobrot.scalingdifficulty.compat.LevelplateCompat;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
                        ctx.getSource().sendFailure(Component.literal("Apunta a un mob (máx. 10 bloques)")
                                .withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    float multiplier = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);
                    boolean isBigZombie = mob.getData(ModAttachments.BIG_ZOMBIE);
                    boolean isSpeedyZombie = mob.getData(ModAttachments.SPEEDY_ZOMBIE);
                    ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
                    float extraRolls = DifficultyCalculator.calculateExtraRolls(mob);
                    int level = ScalingDifficulty.isLevelplateLoaded ? LevelplateCompat.getMobLevel(mob) : 1;

                    MutableComponent message = Component.literal("=== ScalingDifficulty ===\n")
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);

                    message.append(formatLine("Mob: ", mobId.toString()));
                    message.append(formatLine("Multiplier (Data): ", String.valueOf(multiplier)));

                    if (isBigZombie)
                        message.append(formatLine("Big Zombie: "));
                    if (isSpeedyZombie)
                        message.append(formatLine("Speedy Zombie: "));

                    message.append(formatLineHealth(mob.getHealth(), mob.getMaxHealth()));
                    message.append(Component.literal("--- Loot Info ---\n").withStyle(ChatFormatting.YELLOW));

                    if (ScalingDifficulty.isLevelplateLoaded) {
                        message.append(formatLine("Mod Levelplate: "));
                        message.append(formatLine("Nivel (Levelplate): ", String.valueOf(level)));
                    } else {
                        int simulatedLevel = Math.max(1, (int) (10 * multiplier - 10));
                        message.append(formatLine("Nivel Simulado: ", String.valueOf(simulatedLevel)));
                    }
                    message.append(formatLine("Extra Loot Rolls: ", String.format("%.2fx", extraRolls), ChatFormatting.YELLOW));
                    ctx.getSource().sendSuccess(() -> message, false);
                    return 1;
                })
        );
    }

    private static Component formatLine(String label, String value) {
        return formatLine(label, value, ChatFormatting.WHITE);
    }

    private static Component formatLine(String label) {
        ChatFormatting color = ChatFormatting.GREEN;
        return Component.literal(label).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(true + "\n").withStyle(color));
    }

    private static Component formatLine(String label, String value, ChatFormatting color) {
        return Component.literal(label).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value + "\n").withStyle(color));
    }

    private static Component formatLineHealth(float health, float maxHealth) {
        return Component.literal("Vida actual: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(health)).withStyle(ChatFormatting.RED))
                .append(Component.literal("/").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(maxHealth + "\n").withStyle(ChatFormatting.DARK_RED));
    }
}