package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.commands.Commands.*;

public class MedicalConditionCommands {

    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(
            Component.translatable("command.gtceu.medical_condition.clear.everything.failed"));
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("command.gtceu.medical_condition.give.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(
            Component.translatable("command.gtceu.medical_condition.clear.specific.failed"));

    // spotless:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("medical_condition")
                        .then(literal("query")
                                .executes(ctx -> {
                                    return queryMedicalConditions(ctx.getSource(), ctx.getSource().getPlayerOrException());
                                })
                                .then(argument("target", EntityArgument.player())
                                        .requires(source -> source.hasPermission(LEVEL_GAMEMASTERS))
                                        .executes(context -> {
                                            return queryMedicalConditions(context.getSource(), EntityArgument.getPlayer(context, "target"));
                                        }))
                                .then(literal("symptoms")
                                        .executes(ctx -> {
                                            return querySymptoms(ctx.getSource(), ctx.getSource().getPlayerOrException());
                                        })
                                        .then(argument("target", EntityArgument.player())
                                                .requires(source -> source.hasPermission(LEVEL_GAMEMASTERS))
                                                .executes(context -> {
                                                    return querySymptoms(context.getSource(), EntityArgument.getPlayer(context, "target"));
                                                }))))
                        .then(literal("clear")
                                .requires(ctx -> ctx.hasPermission(LEVEL_GAMEMASTERS))
                                .executes(ctx -> {
                                    return clearMedicalConditions(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayerOrException()), null);
                                })
                                .then(argument("targets", EntityArgument.players())
                                        .executes(ctx -> {
                                            return clearMedicalConditions(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), null);
                                        })
                                        .then(argument("condition", MedicalConditionArgument.medicalCondition())
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
                                                    MedicalCondition condition = MedicalConditionArgument.getCondition(ctx, "condition");
                                                    return clearMedicalConditions(ctx.getSource(), targets, condition);
                                                }))))
                        .then(literal("apply")
                                .requires(ctx -> ctx.hasPermission(LEVEL_GAMEMASTERS))
                                .then(argument("targets", EntityArgument.players())
                                        .then(argument("condition", MedicalConditionArgument.medicalCondition())
                                                .executes(ctx -> {
                                                    MedicalCondition condition = MedicalConditionArgument.getCondition(ctx, "condition");
                                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                    return applyMedicalConditions(ctx.getSource(), players, condition, 20);
                                                })
                                                .then(argument("progression", FloatArgumentType.floatArg())
                                                        .executes(ctx -> {
                                                            MedicalCondition condition = MedicalConditionArgument.getCondition(ctx, "condition");
                                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                            float progression = FloatArgumentType.getFloat(ctx, "progression");
                                                            return applyMedicalConditions(ctx.getSource(), players, condition, progression);
                                                        }))))));
    }
    // spotless:on

    private static int queryMedicalConditions(CommandSourceStack source,
                                              ServerPlayer target) throws CommandSyntaxException {
        MedicalConditionTracker tracker = getMedicalConditionTracker(target);

        int conditions = tracker.getMedicalConditions().size();
        if (conditions == 0) {
            source.sendSuccess(() -> {
                return Component.translatable("command.gtceu.medical_condition.get.empty", target.getName());
            }, false);
            return 0;
        } else {
            source.sendSuccess(() -> {
                return Component.translatable("command.gtceu.medical_condition.get", target.getName());
            }, false);
        }
        for (var entry : tracker.getMedicalConditions().reference2FloatEntrySet()) {
            String langKey;
            if (!entry.getKey().canBePermanent || entry.getFloatValue() < entry.getKey().maxProgression * 2) {
                langKey = "command.gtceu.medical_condition.get.element";
            } else {
                langKey = "command.gtceu.medical_condition.get.element.permanent";
            }
            float time = entry.getFloatValue();
            source.sendSuccess(() -> {
                return Component.translatable(langKey,
                        entry.getKey().getAffectedName(), (int) (time / 60), (int) (time % 60));
            }, false);
        }
        return conditions;
    }

    private static int querySymptoms(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        MedicalConditionTracker tracker = getMedicalConditionTracker(target);

        int symptoms = tracker.getActiveSymptoms().size();
        if (symptoms == 0) {
            source.sendSuccess(() -> {
                return Component.translatable("command.gtceu.medical_condition.get.symptoms.empty", target.getName());
            }, false);
            return 0;
        } else {
            source.sendSuccess(() -> {
                return Component.translatable("command.gtceu.medical_condition.get.symptoms", target.getName());
            }, false);
        }
        for (Symptom.ConfiguredSymptom symptom : tracker.getActiveSymptoms().keySet()) {
            source.sendSuccess(() -> {
                return Component.translatable("command.gtceu.medical_condition.get.symptoms.element",
                        Component.translatable(symptom.getSymptom().name));
            }, false);
        }
        return symptoms;
    }

    private static int clearMedicalConditions(CommandSourceStack source, Collection<ServerPlayer> targets,
                                              @Nullable MedicalCondition condition) throws CommandSyntaxException {
        int removed = 0;
        for (ServerPlayer target : targets) {
            MedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(target);
            if (tracker == null) {
                continue;
            }
            if (condition == null) {
                removed += tracker.getMedicalConditions().size();
                for (MedicalCondition medicalCondition : tracker.getMedicalConditions().keySet()) {
                    tracker.removeMedicalCondition(medicalCondition);
                }
            } else {
                removed++;
                tracker.removeMedicalCondition(condition);
            }
        }

        if (removed == 0) {
            if (condition == null) {
                throw ERROR_CLEAR_EVERYTHING_FAILED.create();
            } else {
                throw ERROR_CLEAR_SPECIFIC_FAILED.create();
            }
        }
        if (targets.size() == 1) {
            if (condition == null) {
                source.sendSuccess(() -> {
                    return Component.translatable("command.gtceu.medical_condition.clear.everything.success.single",
                            targets.iterator().next().getDisplayName());
                }, true);
            } else {
                source.sendSuccess(() -> {
                    return Component.translatable("command.gtceu.medical_condition.clear.specific.success.single",
                            condition.getAffectedName(), targets.iterator().next().getDisplayName());
                }, true);
            }
        } else {
            if (condition == null) {
                source.sendSuccess(() -> {
                    return Component.translatable("command.gtceu.medical_condition.clear.everything.success.multiple",
                            targets.size());
                }, true);
            } else {
                source.sendSuccess(() -> {
                    return Component.translatable("command.gtceu.medical_condition.clear.specific.success.multiple",
                            condition.getAffectedName(), targets.size());
                }, true);
            }
        }

        return removed;
    }

    private static int applyMedicalConditions(CommandSourceStack source, Collection<ServerPlayer> targets,
                                              MedicalCondition condition,
                                              float progression) throws CommandSyntaxException {
        int applied = 0;
        for (ServerPlayer player : targets) {
            MedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
            if (tracker == null) {
                continue;
            }
            tracker.progressCondition(condition, progression);
            applied++;
        }
        if (applied == 0) {
            throw ERROR_GIVE_FAILED.create();
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> {
                return Component.translatable("command.gtceu.medical_condition.give.success.single",
                        condition.getAffectedName(), targets.iterator().next().getDisplayName());
            }, true);
        } else {
            source.sendSuccess(() -> {
                return Component.translatable("command.gtceu.medical_condition.give.success.multiple",
                        condition.getAffectedName(), targets.size());
            }, true);
        }
        return applied;
    }

    private static @NotNull MedicalConditionTracker getMedicalConditionTracker(@Nullable ServerPlayer target) throws CommandSyntaxException {
        if (target == null) throw EntityArgument.NO_PLAYERS_FOUND.create();
        MedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(target);
        if (tracker == null) throw EntityArgument.NO_PLAYERS_FOUND.create();

        return tracker;
    }
}
