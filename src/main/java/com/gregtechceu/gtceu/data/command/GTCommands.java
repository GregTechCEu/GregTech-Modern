package com.gregtechceu.gtceu.data.command;

import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.OreVeinDefinition;
import com.gregtechceu.gtceu.api.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.worldgen.ores.OreGenerator;
import com.gregtechceu.gtceu.api.worldgen.ores.OrePlacer;
import com.gregtechceu.gtceu.core.mixins.ResourceKeyArgumentAccessor;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.*;

import static net.minecraft.commands.Commands.*;

public class GTCommands {

    public static final SuggestionProvider<CommandSourceStack> OWNED_CAPES = (ctx, builder) -> {
        return SharedSuggestionProvider.suggestResource(findOwnedCapesFor(ctx), builder);
    };
    public static final SuggestionProvider<CommandSourceStack> NOT_OWNED_CAPES = (ctx, builder) -> {
        return SharedSuggestionProvider.suggestResource(findNotOwnedCapesFor(ctx), builder);
    };
    public static final DynamicCommandExceptionType ERROR_NO_SUCH_CAPE = new DynamicCommandExceptionType(
            id -> Component.translatable("command.gtceu.cape.failure.does_not_exist", id));

    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("command.gtceu.cape.give.failed"));
    private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("command.gtceu.cape.take.failed"));
    private static final Dynamic2CommandExceptionType ERROR_USE_FAILED = new Dynamic2CommandExceptionType(
            (player, cape) -> Component.translatable("command.gtceu.cape.use.failed", player, cape));

    private static final Dynamic2CommandExceptionType VEIN_PLACE_FAILURE = new Dynamic2CommandExceptionType(
            (id, sourcePos) -> Component.translatable("command.gtceu.place_vein.failure", id, sourcePos));
    private static final DynamicCommandExceptionType ERROR_INVALID_VEIN = new DynamicCommandExceptionType(
            id -> Component.translatableEscape("command.gtceu.place_vein.invalid", id));

    // spotless:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(literal("gtceu")
                .then(literal("ui_editor")
                        .requires(ctx -> ctx.hasPermission(LEVEL_ADMINS))
                        .executes(context -> {
                            GTUIEditorFactory.INSTANCE.openUI(GTUIEditorFactory.INSTANCE, context.getSource().getPlayerOrException());
                            return 1;
                        }))
                .then(literal("place_vein")
                        .requires(ctx -> ctx.hasPermission(LEVEL_ADMINS))
                        .then(argument("vein", ResourceKeyArgument.key(GTRegistries.ORE_VEIN_REGISTRY))
                                .executes(context -> {
                                    return GTCommands.placeVein(context, BlockPos.containing(context.getSource().getPosition()));
                                })
                                .then(argument("position", BlockPosArgument.blockPos())
                                        .executes(context -> {
                                            return GTCommands.placeVein(context, BlockPosArgument.getBlockPos(context, "position"));
                                        }))))
                .then(literal("cape")
                        .then(literal("give")
                                .requires(ctx -> ctx.hasPermission(LEVEL_GAMEMASTERS))
                                .then(argument("targets", EntityArgument.players())
                                        .then(argument("cape", ResourceLocationArgument.id())
                                                .suggests(NOT_OWNED_CAPES)
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                    Collection<ResourceLocation> cape = Collections.singleton(ResourceLocationArgument.getId(ctx, "cape"));
                                                    return giveCapes(ctx.getSource(), players, cape);
                                                }))
                                        .then(literal("*")
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                    return giveCapes(ctx.getSource(), players, CapeRegistry.ALL_CAPES.keySet());
                                                }))))
                        .then(literal("take")
                                .requires(ctx -> ctx.hasPermission(LEVEL_GAMEMASTERS))
                                .then(argument("targets", EntityArgument.players())
                                        .then(argument("cape", ResourceLocationArgument.id())
                                                .suggests(OWNED_CAPES)
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                    Collection<ResourceLocation> cape = Collections.singleton(ResourceLocationArgument.getId(ctx, "cape"));
                                                    return takeCapes(ctx.getSource(), players, cape);
                                                }))
                                        .then(literal("*")
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                    return takeCapes(ctx.getSource(), players, CapeRegistry.ALL_CAPES.keySet());
                                                }))))
                        .then(literal("use")
                                .then(argument("target", EntityArgument.player())
                                        .requires(ctx -> ctx.hasPermission(LEVEL_ADMINS))
                                        .then(argument("cape", ResourceLocationArgument.id())
                                                .suggests(OWNED_CAPES)
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
                                                    ResourceLocation cape = ResourceLocationArgument.getId(ctx, "cape");
                                                    return setActiveCape(ctx.getSource(), player, cape);
                                                }))
                                        .then(literal("none")
                                                .executes(ctx -> {
                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                    return setActiveCape(ctx.getSource(), player, null);
                                                })))
                                .then(argument("cape", ResourceLocationArgument.id())
                                        .suggests(OWNED_CAPES)
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            ResourceLocation cape = ResourceLocationArgument.getId(ctx, "cape");
                                            return setActiveCape(ctx.getSource(), player, cape);
                                        }))
                                .then(literal("none")
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            return setActiveCape(ctx.getSource(), player, null);
                                        })))));
    }
    // spotless:on

    public static Collection<ServerPlayer> findPlayersFrom(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        // go through all variants of the used player target selectors to find the targeted players
        try {
            return EntityArgument.getPlayers(ctx, "targets");
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            try {
                return EntityArgument.getPlayers(ctx, "target");
            } catch (IllegalArgumentException | CommandSyntaxException ignored) {
                return Collections.singleton(ctx.getSource().getPlayerOrException());
            }
        }
    }

    public static Collection<ResourceLocation> findOwnedCapesFor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = findPlayersFrom(ctx);
        if (players.isEmpty()) {
            return CapeRegistry.ALL_CAPES.keySet();
        }

        Set<ResourceLocation> validCapes = new HashSet<>();
        for (ServerPlayer player : players) {
            validCapes.addAll(CapeRegistry.getUnlockedCapes(player.getUUID()));
        }
        return validCapes;
    }

    public static Collection<ResourceLocation> findNotOwnedCapesFor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = findPlayersFrom(ctx);
        if (players.isEmpty()) {
            return CapeRegistry.ALL_CAPES.keySet();
        }

        Set<ResourceLocation> allCapes = CapeRegistry.ALL_CAPES.keySet();
        Set<ResourceLocation> validCapes = new HashSet<>();
        for (ServerPlayer player : players) {
            Set<ResourceLocation> unlockedCapes = new HashSet<>(CapeRegistry.getUnlockedCapes(player.getUUID()));
            // find all capes this player *doesn't* have
            validCapes.addAll(Sets.difference(allCapes, unlockedCapes));
        }
        return validCapes;
    }

    public static int giveCapes(CommandSourceStack source,
                                Collection<ServerPlayer> targets, Collection<ResourceLocation> capes)
                                                                                                      throws CommandSyntaxException {
        int successes = 0;

        for (var player : targets) {
            int playerSuccesses = 0;
            for (var cape : capes) {
                if (CapeRegistry.unlockCape(player.getUUID(), cape)) {
                    successes++;
                    playerSuccesses++;
                }
            }
            if (playerSuccesses > 0) {
                player.sendSystemMessage(Component.translatable("gtceu.chat.cape"));
            }
        }

        if (successes == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(
                    "command.gtceu.cape.give.success.single", capes.size(),
                    targets.iterator().next().getDisplayName()),
                    true);
        } else {
            source.sendSuccess(() -> Component.translatable(
                    "command.gtceu.cape.give.success.multiple", capes.size(), targets.size()),
                    true);
        }
        CapeRegistry.save();
        return successes;
    }

    private static int takeCapes(CommandSourceStack source,
                                 Collection<ServerPlayer> targets, Collection<ResourceLocation> capes)
                                                                                                       throws CommandSyntaxException {
        int successes = 0;

        for (var player : targets) {
            for (var cape : capes) {
                if (CapeRegistry.removeCape(player.getUUID(), cape)) {
                    successes++;
                }
            }
        }

        if (successes == 0) {
            throw ERROR_TAKE_FAILED.create();
        }
        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(
                    "command.gtceu.cape.take.success.single", capes.size(),
                    targets.iterator().next().getDisplayName()),
                    true);
        } else {
            source.sendSuccess(() -> Component.translatable(
                    "command.gtceu.cape.take.success.multiple", capes.size(), targets.size()),
                    true);
        }
        CapeRegistry.save();
        return successes;
    }

    private static int setActiveCape(CommandSourceStack source, ServerPlayer player, ResourceLocation cape)
                                                                                                            throws CommandSyntaxException {
        if (CapeRegistry.setActiveCape(player.getUUID(), cape)) {
            if (cape != null) {
                source.sendSuccess(() -> Component.translatable(
                        "command.gtceu.cape.use.success", player.getDisplayName(), cape.toString()),
                        true);
            } else {
                source.sendSuccess(() -> Component.translatable(
                        "command.gtceu.cape.use.success.none", player.getDisplayName()),
                        true);
            }
            return 1;
        } else {
            throw ERROR_USE_FAILED.create(player.getDisplayName(), cape);
        }
    }

    private static int placeVein(CommandContext<CommandSourceStack> context,
                                 BlockPos sourcePos) throws CommandSyntaxException {
        Holder.Reference<OreVeinDefinition> vein = ResourceKeyArgumentAccessor.callResolveKey(context, "vein",
                GTRegistries.ORE_VEIN_REGISTRY, ERROR_INVALID_VEIN);
        ResourceLocation id = vein.key().location();

        ChunkPos chunkPos = new ChunkPos(sourcePos);
        ServerLevel level = context.getSource().getLevel();

        GeneratedVeinMetadata metadata = new GeneratedVeinMetadata(chunkPos, sourcePos, vein);
        RandomSource random = level.random;

        OrePlacer placer = new OrePlacer();
        OreGenerator generator = placer.getOreGenCache().getOreGenerator();

        try (BulkSectionAccess access = new BulkSectionAccess(level)) {
            var generated = generator.generateOres(new OreGenerator.VeinConfiguration(metadata, random), level,
                    chunkPos);
            if (generated.isEmpty()) {
                throw VEIN_PLACE_FAILURE.create(id.toString(), sourcePos.toString());
            }
            for (ChunkPos pos : generated.get().getGeneratedChunks()) {
                placer.placeVein(pos, random, access, generated.get(), AlwaysTrueTest.INSTANCE);
                level.getChunk(pos.x, pos.z).setUnsaved(true);
            }
            context.getSource().sendSuccess(() -> Component.translatable("command.gtceu.place_vein.success",
                    id.toString(), sourcePos.toString()), true);
        }

        return 1;
    }
}
