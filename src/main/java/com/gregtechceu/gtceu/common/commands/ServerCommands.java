package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.common.commands.arguments.MedicalConditionArgument;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static net.minecraft.commands.Commands.*;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote ServerCommands
 */
public class ServerCommands {

    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(
            Component.translatable("effect.clear.everything.failed"));
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("effect.give.failed"));

    public static List<LiteralArgumentBuilder<CommandSourceStack>> createServerCommands() {
        return List.of(
                literal("gtceu")
                        .then(literal("ui_editor")
                                .executes(context -> {
                                    GTUIEditorFactory.INSTANCE.openUI(GTUIEditorFactory.INSTANCE,
                                            context.getSource().getPlayerOrException());
                                    return 1;
                                }))
                        .then(literal("check_recipes_valid")
                                .requires(cs -> cs.hasPermission(0))
                                .executes(context -> {
                                    for (Recipe<?> recipe : context.getSource().getServer().getRecipeManager()
                                            .getRecipes()) {
                                        if (recipe instanceof GTRecipe gtRecipe && !gtRecipe.checkRecipeValid()) {
                                            context.getSource().sendSuccess(
                                                    () -> Component
                                                            .literal("recipe %s is invalid".formatted(gtRecipe.id)),
                                                    false);
                                        }
                                    }
                                    return 1;
                                }))
                        .then(literal("medical_condition")
                                .requires(source -> source.hasPermission(2))
                                .then(literal("query")
                                        .executes(context -> queryMedicalConditions(
                                                context.getSource().getPlayerOrException()))
                                        .then(argument("target", EntityArgument.player())
                                                .executes(context -> queryMedicalConditions(
                                                        EntityArgument.getPlayer(context, "target")))))
                                .then(literal("clear")
                                        .executes(context -> clearMedicalConditions(
                                                Collections.singleton(context.getSource().getPlayerOrException()),
                                                null))
                                        .then(argument("targets", EntityArgument.players())
                                                .executes(context -> clearMedicalConditions(
                                                        EntityArgument.getPlayers(context, "targets"), null))
                                                .then(argument("condition",
                                                        MedicalConditionArgument.medicalCondition())
                                                        .executes(context -> {
                                                            Collection<ServerPlayer> targets = EntityArgument
                                                                    .getPlayers(context, "targets");
                                                            MedicalCondition condition = MedicalConditionArgument
                                                                    .getCondition(context, "condition");
                                                            return clearMedicalConditions(targets, condition);
                                                        }))))
                                .then(literal("apply")
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("condition",
                                                        MedicalConditionArgument.medicalCondition())
                                                        .executes(context -> {
                                                            MedicalCondition condition = MedicalConditionArgument
                                                                    .getCondition(context, "condition");
                                                            Collection<ServerPlayer> players = EntityArgument
                                                                    .getPlayers(context, "targets");
                                                            return applyMedicalConditions(players, condition, 1);
                                                        }).then(argument("progression_multiplier",
                                                                FloatArgumentType.floatArg(0))
                                                                .executes(context -> {
                                                                    MedicalCondition condition = MedicalConditionArgument
                                                                            .getCondition(context, "condition");
                                                                    Collection<ServerPlayer> players = EntityArgument
                                                                            .getPlayers(context, "targets");
                                                                    float strength = FloatArgumentType.getFloat(
                                                                            context,
                                                                            "progression_multiplier");
                                                                    return applyMedicalConditions(players,
                                                                            condition, strength);
                                                                }))))))
                        .then(literal("environmental_hazard")
                                .then(argument("condition", MedicalConditionArgument.medicalCondition())
                                        .then(argument("can_spread", BoolArgumentType.bool())
                                                .then(argument("source", BlockPosArgument.blockPos())
                                                        .then(literal("chunk")
                                                                .then(Commands
                                                                        .argument("strength",
                                                                                IntegerArgumentType.integer(1))
                                                                        .executes(
                                                                                ServerCommands::spawnChunkEnvironmentalHazard)))
                                                        .then(literal("local")
                                                                .then(Commands
                                                                        .argument("from", BlockPosArgument.blockPos())
                                                                        .then(Commands
                                                                                .argument("to",
                                                                                        BlockPosArgument.blockPos())
                                                                                .executes(
                                                                                        ServerCommands::spawnLocalEnvironmentalHazard)))))))
                                .then(literal("clear")
                                        .then(argument("source", BlockPosArgument.blockPos())
                                                .executes(context -> {
                                                    BlockPos source = BlockPosArgument.getBlockPos(context, "source");
                                                    return clearEnvironmentalHazard(context, source, null);
                                                })
                                                .then(argument("condition", MedicalConditionArgument.medicalCondition())
                                                        .executes(context -> {
                                                            BlockPos source = BlockPosArgument.getBlockPos(context,
                                                                    "source");
                                                            MedicalCondition condition = MedicalConditionArgument
                                                                    .getCondition(context, "condition");
                                                            return clearEnvironmentalHazard(context, source, condition);
                                                        }))))));
    }

    private static int queryMedicalConditions(ServerPlayer target) throws CommandSyntaxException {
        IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(target);
        if (tracker == null) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }
        int count = tracker.getMedicalConditions().size();
        if (count == 0) {
            target.sendSystemMessage(
                    Component.translatable("command.gtceu.medical_condition.get.empty", target.getName()));
        } else {
            target.sendSystemMessage(
                    Component.translatable("command.gtceu.medical_condition.get", target.getName()));
        }
        for (var entry : tracker.getMedicalConditions().object2FloatEntrySet()) {
            String langKey = "command.gtceu.medical_condition.get.element";
            if (entry.getKey().maxProgression * 2 <= entry.getFloatValue() &&
                    entry.getKey().canBePermanent) {
                langKey = "command.gtceu.medical_condition.get.element.permanent";
            }
            target.sendSystemMessage(
                    Component.translatable(langKey,
                            Component.translatable("gtceu.medical_condition." + entry.getKey().name),
                            entry.getFloatValue() / 20f));
        }
        return count;
    }

    private static int clearMedicalConditions(Collection<ServerPlayer> targets,
                                              @Nullable MedicalCondition condition) throws CommandSyntaxException {
        int count = 0;
        for (ServerPlayer target : targets) {
            IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(target);
            if (tracker == null) {
                continue;
            }
            if (condition == null) {
                count += tracker.getMedicalConditions().keySet().size();
                for (MedicalCondition medicalCondition : tracker.getMedicalConditions().keySet()) {
                    tracker.removeMedicalCondition(medicalCondition);
                }
            } else {
                count++;
                tracker.removeMedicalCondition(condition);
            }
        }
        if (count == 0) {
            throw ERROR_CLEAR_EVERYTHING_FAILED.create();
        }
        return count;
    }

    private static int applyMedicalConditions(Collection<ServerPlayer> targets, MedicalCondition condition,
                                              float strength) throws CommandSyntaxException {
        int success = 0;
        for (ServerPlayer player : targets) {
            IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
            if (tracker == null) {
                continue;
            }
            tracker.progressCondition(condition, strength);
            success++;
        }
        if (success == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        return success;
    }

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
