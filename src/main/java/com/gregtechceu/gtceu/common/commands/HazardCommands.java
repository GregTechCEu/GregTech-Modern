package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;

import com.gregtechceu.gtceu.core.mixins.ResourceKeyArgumentAccessor;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import static net.minecraft.commands.Commands.*;

public class HazardCommands {

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
            id -> Component.translatable("argument.item.id.invalid", id));

    // spotless:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("environmental_hazard")
                        .requires(source -> source.hasPermission(LEVEL_ADMINS))
                        .then(argument("condition", ResourceKeyArgument.key(GTRegistries.Keys.MEDICAL_CONDITION))
                                .then(argument("can_spread", BoolArgumentType.bool())
                                        .then(argument("source", BlockPosArgument.blockPos())
                                                .then(literal("chunk")
                                                        .then(Commands.argument("strength", IntegerArgumentType.integer(1))
                                                                .executes(HazardCommands::spawnChunkEnvironmentalHazard)))
                                                .then(literal("local")
                                                        .then(Commands.argument("from", BlockPosArgument.blockPos())
                                                                .then(Commands.argument("to", BlockPosArgument.blockPos())
                                                                        .executes(HazardCommands::spawnLocalEnvironmentalHazard)))))))
                        .then(literal("clear")
                                .then(argument("source", BlockPosArgument.blockPos())
                                        .executes(context -> {
                                            BlockPos source = BlockPosArgument.getBlockPos(context, "source");
                                            return clearEnvironmentalHazard(context, source, null);
                                        })
                                        .then(argument("condition", ResourceKeyArgument.key(GTRegistries.Keys.MEDICAL_CONDITION))
                                                .executes(context -> {
                                                    BlockPos source = BlockPosArgument.getBlockPos(context, "source");
                                                    MedicalCondition condition = ResourceKeyArgumentAccessor.callResolveKey(context, "condition",
                                                            GTRegistries.Keys.MEDICAL_CONDITION, ERROR_UNKNOWN_ITEM).value();
                                                    return clearEnvironmentalHazard(context, source, condition);
                                                })))));
    }
    // spotless:on

    private static int spawnChunkEnvironmentalHazard(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel serverLevel = context.getSource().getLevel();

        BlockPos source = BlockPosArgument.getBlockPos(context, "source");
        int strength = IntegerArgumentType.getInteger(context, "strength");
        MedicalCondition condition = ResourceKeyArgumentAccessor.callResolveKey(context, "condition",
                GTRegistries.Keys.MEDICAL_CONDITION, ERROR_UNKNOWN_ITEM).value();
        boolean canSpread = BoolArgumentType.getBool(context, "can_spread");

        EnvironmentalHazardSavedData.getOrCreate(serverLevel)
                .addZone(source, strength, canSpread, HazardProperty.HazardTrigger.INHALATION, condition);

        return 1;
    }

    private static int spawnLocalEnvironmentalHazard(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel serverLevel = context.getSource().getLevel();
        BlockPos source = BlockPosArgument.getBlockPos(context, "source");
        BlockPos from = BlockPosArgument.getBlockPos(context, "from");
        BlockPos to = BlockPosArgument.getBlockPos(context, "to");

        MedicalCondition condition = ResourceKeyArgumentAccessor.callResolveKey(context, "condition",
                GTRegistries.Keys.MEDICAL_CONDITION, ERROR_UNKNOWN_ITEM).value();
        boolean canSpread = BoolArgumentType.getBool(context, "can_spread");

        LocalizedHazardSavedData.getOrCreate(serverLevel)
                .addCuboidZone(source, from, to, canSpread, HazardProperty.HazardTrigger.INHALATION, condition);

        return 1;
    }

    private static int clearEnvironmentalHazard(CommandContext<CommandSourceStack> context,
                                                BlockPos clearAt, MedicalCondition condition) {
        ServerLevel serverLevel = context.getSource().getLevel();
        if (condition == null) {
            EnvironmentalHazardSavedData.getOrCreate(serverLevel).removeZone(clearAt);
            LocalizedHazardSavedData.getOrCreate(serverLevel).removeZoneByPosition(clearAt);
        } else {
            EnvironmentalHazardSavedData.getOrCreate(serverLevel).removeZone(clearAt, condition);
            LocalizedHazardSavedData.getOrCreate(serverLevel).removeZoneByPosition(clearAt, condition);
        }
        return 1;
    }
}
