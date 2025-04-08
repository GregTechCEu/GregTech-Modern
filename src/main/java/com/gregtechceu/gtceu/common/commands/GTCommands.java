package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OrePlacer;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.common.commands.arguments.GTRegistryArgument;
import com.gregtechceu.gtceu.data.loader.BedrockFluidLoader;
import com.gregtechceu.gtceu.data.loader.BedrockOreLoader;
import com.gregtechceu.gtceu.data.loader.GTOreLoader;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import static net.minecraft.commands.Commands.*;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote GTCommands
 */
public class GTCommands {

    public static final SuggestionProvider<CommandSourceStack> ALL_CAPES = SuggestionProviders.register(
            GTCEu.id("all_capes"),
            (ctx, builder) -> {
                return SharedSuggestionProvider.suggestResource(CapeRegistry.ALL_CAPES.keySet(), builder);
            });
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("command.gtceu.cape.give.failed"));
    private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("command.gtceu.cape.take.failed"));

    // spotless:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("gtceu")
                        .requires(ctx -> ctx.hasPermission(3))
                        .then(literal("ui_editor")
                                .executes(context -> {
                                    GTUIEditorFactory.INSTANCE.openUI(GTUIEditorFactory.INSTANCE, context.getSource().getPlayerOrException());
                                    return 1;
                                }))
                        .then(literal("dump_data")
                                .requires(ctx -> ctx.hasPermission(4))
                                .then(literal("bedrock_fluid_veins")
                                        .executes(context -> dumpDataRegistry(context,
                                                GTRegistries.BEDROCK_FLUID_DEFINITIONS,
                                                BedrockFluidDefinition.FULL_CODEC,
                                                BedrockFluidLoader.FOLDER)))
                                .then(literal("bedrock_ore_veins")
                                        .executes(context -> dumpDataRegistry(context,
                                                GTRegistries.BEDROCK_ORE_DEFINITIONS,
                                                BedrockOreDefinition.FULL_CODEC,
                                                BedrockOreLoader.FOLDER)))
                                .then(literal("ore_veins")
                                        .executes(context -> dumpDataRegistry(context,
                                                GTRegistries.ORE_VEINS,
                                                GTOreDefinition.FULL_CODEC,
                                                GTOreLoader.FOLDER))))
                        .then(literal("place_vein")
                                .then(argument("vein", GTRegistryArgument.registry(GTRegistries.ORE_VEINS, ResourceLocation.class))
                                        .executes(context -> {
                                            return GTCommands.placeVein(context, BlockPos.containing(context.getSource().getPosition()));
                                        })
                                        .then(argument("position", BlockPosArgument.blockPos())
                                                .executes(context -> {
                                                    return GTCommands.placeVein(context, BlockPosArgument.getBlockPos(context, "position"));
                                                }))))
                        .then(literal("cape")
                                .then(literal("give")
                                        .requires(ctx -> ctx.hasPermission(2))
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("cape", ResourceLocationArgument.id())
                                                        .suggests(ALL_CAPES)
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
                                        .requires(ctx -> ctx.hasPermission(3))
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("cape", ResourceLocationArgument.id())
                                                        .suggests(ALL_CAPES)
                                                        .executes(ctx -> {
                                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                            Collection<ResourceLocation> cape = Collections.singleton(ResourceLocationArgument.getId(ctx, "cape"));
                                                            return takeCapes(ctx.getSource(), players, cape);
                                                        }))
                                                .then(literal("*")
                                                        .executes(ctx -> {
                                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                            return takeCapes(ctx.getSource(), players, CapeRegistry.ALL_CAPES.keySet());
                                                        }))))));
    }
    // spotless:on

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

    private static <T> int dumpDataRegistry(CommandContext<CommandSourceStack> context,
                                            GTRegistry<ResourceLocation, T> registry, Codec<T> codec, String folder) {
        Path parent = GTCEu.getGameDir().resolve("gtceu/dumped/data");
        var ops = RegistryOps.create(JsonOps.INSTANCE, context.getSource().registryAccess());
        int dumpedCount = 0;
        for (ResourceLocation id : registry.keys()) {
            T entry = registry.get(id);
            JsonElement json = codec.encodeStart(ops, entry).getOrThrow(false, GTCEu.LOGGER::error);
            GTDynamicDataPack.writeJson(id, folder, parent, json);
            dumpedCount++;
        }
        final int result = dumpedCount;
        context.getSource().sendSuccess(
                () -> Component.translatable("command.gtceu.dump_data.success", result,
                        registry.getRegistryName().toString(), parent.toString()),
                true);
        return result;
    }

    private static int placeVein(CommandContext<CommandSourceStack> context, BlockPos sourcePos) {
        GTOreDefinition vein = context.getArgument("vein", GTOreDefinition.class);
        ResourceLocation id = GTRegistries.ORE_VEINS.getKey(vein);

        ChunkPos chunkPos = new ChunkPos(sourcePos);
        ServerLevel level = context.getSource().getLevel();

        GeneratedVeinMetadata metadata = new GeneratedVeinMetadata(id, chunkPos, sourcePos, vein);
        RandomSource random = level.random;

        OrePlacer placer = new OrePlacer();
        OreGenerator generator = placer.getOreGenCache().getOreGenerator();

        try (BulkSectionAccess access = new BulkSectionAccess(level)) {
            var generated = generator.generateOres(new OreGenerator.VeinConfiguration(metadata, random), level,
                    chunkPos);
            if (generated.isEmpty()) {
                throw new CommandRuntimeException(Component.translatable("command.gtceu.place_vein.failure",
                        id.toString(), sourcePos.toString()));
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
