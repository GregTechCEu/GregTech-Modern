package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.common.commands.arguments.MedicalConditionArgument;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import static net.minecraft.commands.Commands.*;

public class HazardCommands {

    // spotless:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("environmental_hazard")
                        .requires(source -> source.hasPermission(LEVEL_ADMINS))
                        .then(argument("condition", MedicalConditionArgument.medicalCondition())
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
                                        .then(argument("condition", MedicalConditionArgument.medicalCondition())
                                                .executes(context -> {
                                                    BlockPos source = BlockPosArgument.getBlockPos(context, "source");
                                                    MedicalCondition condition = MedicalConditionArgument.getCondition(context, "condition");
                                                    return clearEnvironmentalHazard(context, source, condition);
                                                })))));
    }
    // spotless:on

    private static int spawnChunkEnvironmentalHazard(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();

        BlockPos source = BlockPosArgument.getBlockPos(context, "source");
        int strength = IntegerArgumentType.getInteger(context, "strength");
        MedicalCondition condition = MedicalConditionArgument.getCondition(context, "condition");
        boolean canSpread = BoolArgumentType.getBool(context, "can_spread");

        EnvironmentalHazardSavedData.getOrCreate(serverLevel)
                .addZone(source, strength, canSpread, HazardProperty.HazardTrigger.INHALATION, condition);

        return 1;
    }

    private static int spawnLocalEnvironmentalHazard(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        BlockPos source = BlockPosArgument.getBlockPos(context, "source");
        BlockPos from = BlockPosArgument.getBlockPos(context, "from");
        BlockPos to = BlockPosArgument.getBlockPos(context, "to");

        MedicalCondition condition = MedicalConditionArgument.getCondition(context, "condition");
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
