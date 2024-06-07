package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.commands.arguments.MedicalConditionArgument;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.util.Collection;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote ServerCommands
 */
public class ServerCommands {

    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(
            Component.translatable("commands.effect.clear.everything.failed"));
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("commands.effect.give.failed"));

    public static List<LiteralArgumentBuilder<CommandSourceStack>> createServerCommands() {
        return List.of(
                Commands.literal("gtceu")
                        .then(Commands.literal("ui_editor")
                                .executes(context -> {
                                    GTUIEditorFactory.INSTANCE.openUI(GTUIEditorFactory.INSTANCE,
                                            context.getSource().getPlayerOrException());
                                    return 1;
                                }))
                        .then(Commands.literal("check_recipes_valid")
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
                        .then(Commands.literal("medical_condition")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("query")
                                        .executes(context -> {
                                            ServerPlayer target = context.getSource().getPlayerOrException();
                                            IMedicalConditionTracker tracker = GTCapabilityHelper
                                                    .getMedicalConditionTracker(target);
                                            if (tracker == null) {
                                                throw EntityArgument.NO_PLAYERS_FOUND.create();
                                            }
                                            int count = tracker.getMedicalConditions().size();
                                            if (count == 0) {

                                                target.sendSystemMessage(
                                                        Component.translatable("command.gtceu.medical_condition.get.empty",
                                                                target.getName()));
                                            } else {
                                                target.sendSystemMessage(
                                                        Component.translatable("command.gtceu.medical_condition.get",
                                                                target.getName()));
                                            }
                                            for (var entry : tracker.getMedicalConditions().object2FloatEntrySet()) {
                                                String langKey = "command.gtceu.medical_condition.get.element";
                                                if (entry.getKey().maxProgression * 2 <= entry.getFloatValue() &&
                                                        entry.getKey().canBePermanent) {
                                                    langKey = "command.gtceu.medical_condition.get.element.permanent";
                                                }
                                                target.sendSystemMessage(
                                                        Component.translatable(
                                                                langKey,
                                                                Component.translatable("gtceu.medical_condition." +
                                                                        entry.getKey().name),
                                                                entry.getFloatValue() / 20f));
                                            }
                                            return count;
                                        })
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                                    IMedicalConditionTracker tracker = GTCapabilityHelper
                                                            .getMedicalConditionTracker(target);
                                                    if (tracker == null) {
                                                        throw EntityArgument.NO_PLAYERS_FOUND.create();
                                                    }
                                                    target.sendSystemMessage(
                                                            Component.translatable(
                                                                    "command.gtceu.medical_condition.get",
                                                                    target.getName()));
                                                    int count = tracker.getMedicalConditions().size();
                                                    for (var entry : tracker.getMedicalConditions()
                                                            .object2FloatEntrySet()) {
                                                        target.sendSystemMessage(
                                                                Component.translatable(
                                                                        "command.gtceu.medical_condition.get.element",
                                                                        Component.translatable(
                                                                                "gtceu.medical_condition." +
                                                                                        entry.getKey().name),
                                                                        entry.getFloatValue() / 20f));
                                                    }
                                                    return count;
                                                })))
                                .then(Commands.literal("clear")
                                        .executes(context -> {
                                            ServerPlayer target = context.getSource().getPlayerOrException();
                                            IMedicalConditionTracker tracker = GTCapabilityHelper
                                                    .getMedicalConditionTracker(target);
                                            if (tracker == null) {
                                                throw EntityArgument.NO_PLAYERS_FOUND.create();
                                            }
                                            int count = tracker.getMedicalConditions().keySet().size();
                                            for (MedicalCondition condition : tracker.getMedicalConditions().keySet()) {
                                                tracker.removeMedicalCondition(condition);
                                            }
                                            return count;
                                        })
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> {
                                                    Collection<ServerPlayer> targets = EntityArgument
                                                            .getPlayers(context, "targets");
                                                    int count = 0;
                                                    for (ServerPlayer target : targets) {
                                                        IMedicalConditionTracker tracker = GTCapabilityHelper
                                                                .getMedicalConditionTracker(target);
                                                        if (tracker == null) {
                                                            continue;
                                                        }
                                                        count += tracker.getMedicalConditions().keySet().size();
                                                        for (MedicalCondition condition : tracker.getMedicalConditions()
                                                                .keySet()) {
                                                            tracker.removeMedicalCondition(condition);
                                                        }
                                                    }
                                                    if (count == 0) {
                                                        throw ERROR_CLEAR_EVERYTHING_FAILED.create();
                                                    }
                                                    return count;
                                                }).then(Commands.argument("condition",
                                                        MedicalConditionArgument.medicalCondition())
                                                        .executes(context -> {
                                                            Collection<ServerPlayer> targets = EntityArgument
                                                                    .getPlayers(context, "targets");
                                                            MedicalCondition condition = MedicalConditionArgument
                                                                    .getMaterial(context, "condition");
                                                            int count = 0;
                                                            for (ServerPlayer target : targets) {
                                                                IMedicalConditionTracker tracker = GTCapabilityHelper
                                                                        .getMedicalConditionTracker(target);
                                                                if (tracker == null) {
                                                                    continue;
                                                                }
                                                                tracker.removeMedicalCondition(condition);
                                                                count++;
                                                            }
                                                            if (count == 0) {
                                                                throw ERROR_CLEAR_EVERYTHING_FAILED.create();
                                                            }
                                                            return count;
                                                        }))))
                                .then(Commands.literal("apply")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("condition",
                                                        MedicalConditionArgument.medicalCondition())
                                                        .executes(context -> {
                                                            MedicalCondition condition = MedicalConditionArgument
                                                                    .getMaterial(context, "condition");
                                                            Collection<ServerPlayer> players = EntityArgument
                                                                    .getPlayers(context, "targets");
                                                            int success = 0;
                                                            for (ServerPlayer player : players) {
                                                                IMedicalConditionTracker tracker = GTCapabilityHelper
                                                                        .getMedicalConditionTracker(player);
                                                                if (tracker == null) {
                                                                    continue;
                                                                }
                                                                tracker.progressCondition(condition, 1);
                                                                success++;
                                                            }
                                                            if (success == 0) {
                                                                throw ERROR_GIVE_FAILED.create();
                                                            }
                                                            return success;
                                                        }).then(Commands.argument("progression_multiplier",
                                                                IntegerArgumentType.integer(0))
                                                                .executes(context -> {
                                                                    MedicalCondition condition = MedicalConditionArgument
                                                                            .getMaterial(context, "condition");
                                                                    Collection<ServerPlayer> players = EntityArgument
                                                                            .getPlayers(context, "targets");
                                                                    int multiplier = IntegerArgumentType.getInteger(
                                                                            context,
                                                                            "progression_multiplier");
                                                                    int success = 0;
                                                                    for (ServerPlayer player : players) {
                                                                        IMedicalConditionTracker tracker = GTCapabilityHelper
                                                                                .getMedicalConditionTracker(player);
                                                                        if (tracker == null) {
                                                                            continue;
                                                                        }
                                                                        tracker.progressCondition(condition, multiplier);
                                                                        success++;
                                                                    }
                                                                    if (success == 0) {
                                                                        throw ERROR_GIVE_FAILED.create();
                                                                    }
                                                                    return success;
                                                                })))))));
    }
}
