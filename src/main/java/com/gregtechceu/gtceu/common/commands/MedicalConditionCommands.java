package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.commands.arguments.MedicalConditionArgument;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MedicalConditionCommands {

    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(
            Component.translatable("commands.effect.clear.everything.failed"));
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("commands.effect.give.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("medical_condition")
                        .then(literal("query")
                                .executes(context -> queryMedicalConditions(
                                        context.getSource().getPlayerOrException()))
                                .then(argument("target", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> queryMedicalConditions(
                                                EntityArgument.getPlayer(context, "target")))))
                        .then(literal("clear")
                                .requires(source -> source.hasPermission(2))
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
                                .requires(source -> source.hasPermission(2))
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
            float time = entry.getFloatValue();
            target.sendSystemMessage(
                    Component.translatable(langKey,
                            Component.translatable("gtceu.medical_condition." + entry.getKey().name),
                            (int) (time / 60), (int) (time % 60)));
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
}
