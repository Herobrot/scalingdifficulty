package com.herobrot.scalingdifficulty.commands;

import com.herobrot.scalingdifficulty.network.ZoneSyncManager;
import com.herobrot.scalingdifficulty.zone.DifficultyZone;
import com.herobrot.scalingdifficulty.zone.DifficultyZoneSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public class DifficultyZoneCommand {

    private static final int MIN_ID_PREFIX_LENGTH = 4;

    private static final SuggestionProvider<CommandSourceStack> ZONE_SUGGESTIONS = (context, builder) -> {
        for (DifficultyZone zone : DifficultyZoneSavedData.get(context.getSource().getServer()).getZones())
            builder.suggest(zone.getDisplayText());
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> ZONE_IDS_SUGGESTIONS = (context, builder) -> {
        for (DifficultyZone zone : DifficultyZoneSavedData.get(context.getSource().getServer()).getZones())
            builder.suggest(zone.getIdString());
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("scalingdifficulty")
                .then(Commands.literal("zone")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("create")
                                .then(Commands.literal("box")
                                        .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                                        .then(Commands.argument("factor", DoubleArgumentType.doubleArg(0.0))
                                                                .executes(ctx -> createBox(ctx, null))
                                                                .then(Commands.argument("name", StringArgumentType.string())
                                                                        .executes(ctx -> createBox(ctx, StringArgumentType.getString(ctx, "name"))))))))
                                .then(Commands.literal("sphere")
                                        .then(Commands.argument("center", BlockPosArgument.blockPos())
                                                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0))
                                                        .then(Commands.argument("factor", DoubleArgumentType.doubleArg(0.0))
                                                                .executes(ctx -> createSphere(ctx, null))
                                                                .then(Commands.argument("name", StringArgumentType.string())
                                                                        .executes(ctx -> createSphere(ctx, StringArgumentType.getString(ctx, "name")))))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .suggests(ZONE_SUGGESTIONS)
                                        .executes(DifficultyZoneCommand::remove))
                                .then(Commands.literal("id")
                                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                                .suggests(ZONE_IDS_SUGGESTIONS)
                                                .executes(DifficultyZoneCommand::removeById))))
                        .then(Commands.literal("list")
                                .executes(DifficultyZoneCommand::list))));
    }

    private static int createBox(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        BlockPos pos1 = BlockPosArgument.getBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgument.getBlockPos(context, "pos2");
        double factor = DoubleArgumentType.getDouble(context, "factor");
        if (name != null && nameExists(source.getServer(), name)) {
            source.sendFailure(plain(source, "command.scalingdifficulty.zone.name_taken",
                    "A zone named '%s' already exists. Zone names must be unique.", name));
            return 0;
        }

        String dimension = source.getLevel().dimension().location().toString();
        DifficultyZone zone = DifficultyZone.createBox(dimension, pos1, pos2, factor, name);
        DifficultyZoneSavedData.get(source.getServer()).addZone(zone);
        ZoneSyncManager.syncToAll(source.getServer());
        sendCreated(source, zone);
        return 1;
    }

    private static int createSphere(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        BlockPos center = BlockPosArgument.getBlockPos(context, "center");
        double radius = DoubleArgumentType.getDouble(context, "radius");
        double factor = DoubleArgumentType.getDouble(context, "factor");
        if (name != null && nameExists(source.getServer(), name)) {
            source.sendFailure(plain(source, "command.scalingdifficulty.zone.name_taken",
                    "A zone named '%s' already exists. Zone names must be unique.", name));
            return 0;
        }

        String dimension = source.getLevel().dimension().location().toString();
        DifficultyZone zone = DifficultyZone.createSphere(dimension, center, radius, factor, name);
        DifficultyZoneSavedData.get(source.getServer()).addZone(zone);
        ZoneSyncManager.syncToAll(source.getServer());
        sendCreated(source, zone);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String input = StringArgumentType.getString(context, "id");
        List<DifficultyZone> zones = DifficultyZoneSavedData.get(source.getServer()).getZones();

        DifficultyZone match = null;
        int matches = 0;
        for (DifficultyZone zone : zones)
            if (matchesZone(zone, input)) {
                match = zone;
                matches++;
            }

        if (matches == 0) {
            source.sendFailure(plain(source, "command.scalingdifficulty.zone.not_found", "No difficulty zone matching '%s' was found.", input));
            return 0;
        }
        if (matches > 1) {
            source.sendFailure(plain(source, "command.scalingdifficulty.zone.ambiguous", "Multiple difficulty zones match '%s'. Use a longer ID prefix or the exact name.", input));
            return 0;
        }

        DifficultyZoneSavedData.get(source.getServer()).removeZone(match.getId());
        ZoneSyncManager.syncToAll(source.getServer());
        sendRemoved(source, match);
        return 1;
    }

    private static int removeById(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        UUID uuid = UuidArgument.getUuid(context, "uuid");

        for (DifficultyZone zone : DifficultyZoneSavedData.get(source.getServer()).getZones())
            if (zone.getId().equals(uuid)) {
                DifficultyZoneSavedData.get(source.getServer()).removeZone(uuid);
                ZoneSyncManager.syncToAll(source.getServer());
                sendRemoved(source, zone);
                return 1;
            }

        source.sendFailure(plain(source, "command.scalingdifficulty.zone.not_found",
                "No difficulty zone matching '%s' was found.", uuid.toString()));
        return 0;
    }

    private static boolean nameExists(MinecraftServer server, String name) {
        for (DifficultyZone zone : DifficultyZoneSavedData.get(server).getZones())
            if (name.equals(zone.getName())) return true;
        return false;
    }

    private static boolean matchesZone(DifficultyZone zone, String input) {
        String id = zone.getIdString();
        if (id.equals(input)) return true;
        if (input.length() >= MIN_ID_PREFIX_LENGTH && id.startsWith(input)) return true;
        return input.equals(zone.getName());
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<DifficultyZone> zones = DifficultyZoneSavedData.get(source.getServer()).getZones();

        if (zones.isEmpty()) {
            source.sendSuccess(() -> plain(source, "command.scalingdifficulty.zone.list.empty", "No difficulty zones are defined.").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        source.sendSuccess(() -> plain(source, "command.scalingdifficulty.zone.list.header", "Active difficulty zones (%s):", String.valueOf(zones.size())).withStyle(ChatFormatting.YELLOW), false);
        boolean localized = isLocalized(source);
        for (DifficultyZone zone : zones) {
            if (localized)
                source.sendSuccess(() -> Component.translatable("command.scalingdifficulty.zone.list.entry",
                        zone.getDisplayIdentifier().withStyle(ChatFormatting.GOLD), zone.describeComponent(true)),
                        false);
            else source.sendSuccess(() -> Component.literal("- ")
                        .append(zone.getDisplayIdentifier().withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(": "))
                        .append(zone.describeComponent(false)), false);

        }
        return zones.size();
    }

    private static boolean isLocalized(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) return ZoneSyncManager.isModdedPlayer(player);
        return true;
    }

    private static MutableComponent plain(CommandSourceStack source, String key, String english, Object... args) {
        if (isLocalized(source)) return Component.translatable(key, args);
        return Component.literal(String.format(english, args));
    }

    private static void sendCreated(CommandSourceStack source, DifficultyZone zone) {
        MutableComponent identifier = zone.getDisplayIdentifier().withStyle(ChatFormatting.GOLD);
        Component localized = Component.translatable("command.scalingdifficulty.zone.created",
                identifier, zone.describeComponent(true));
        Component english = Component.literal("Difficulty zone ")
                .append(identifier)
                .append(Component.literal(" created: "))
                .append(zone.describeComponent(false));
        source.sendSuccess(() -> isLocalized(source) ? localized : english, false);
        broadcastToAdmins(source, localized, english);
    }

    private static void sendRemoved(CommandSourceStack source, DifficultyZone zone) {
        MutableComponent identifier = zone.getDisplayIdentifier().withStyle(ChatFormatting.GOLD);
        Component localized = Component.translatable("command.scalingdifficulty.zone.removed", identifier);
        Component english = Component.literal("Difficulty zone ")
                .append(identifier)
                .append(Component.literal(" was removed."));
        source.sendSuccess(() -> isLocalized(source) ? localized : english, false);
        broadcastToAdmins(source, localized, english);
    }

    private static void broadcastToAdmins(CommandSourceStack source, Component localized, Component english) {
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            if (player.equals(source.getEntity()) || !player.hasPermissions(2)) continue;
            player.sendSystemMessage(ZoneSyncManager.isModdedPlayer(player) ? localized : english);
        }
        source.getServer().sendSystemMessage(localized);
    }
}