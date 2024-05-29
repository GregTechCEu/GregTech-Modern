package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.commands.arguments.MaterialArgument;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

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
    private static final SimpleCommandExceptionType ERROR_INVALID_MATERIAL = new SimpleCommandExceptionType(
            Component.translatable("commands.gtceu.hazard.invalid.material"));
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
                        .then(Commands.literal("hazard")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("clear")
                                        .executes(context -> {
                                            ServerPlayer target = context.getSource().getPlayerOrException();
                                            IHazardEffectTracker tracker = GTCapabilityHelper
                                                    .getHazardEffectTracker(target);
                                            if (tracker == null) {
                                                throw EntityArgument.NO_PLAYERS_FOUND.create();
                                            }
                                            int count = tracker.getCurrentHazardEffects().keySet().size();
                                            tracker.getCurrentHazardEffects().clear();
                                            return count;
                                        })
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> {
                                                    Collection<ServerPlayer> targets = EntityArgument
                                                            .getPlayers(context, "targets");
                                                    int count = 0;
                                                    for (ServerPlayer target : targets) {
                                                        IHazardEffectTracker tracker = GTCapabilityHelper
                                                                .getHazardEffectTracker(target);
                                                        if (tracker == null) {
                                                            continue;
                                                        }
                                                        count += tracker.getCurrentHazardEffects().keySet().size();
                                                        tracker.getCurrentHazardEffects().clear();
                                                    }
                                                    if (count == 0) {
                                                        throw ERROR_CLEAR_EVERYTHING_FAILED.create();
                                                    }
                                                    return count;
                                                })))
                                .then(Commands.literal("apply")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("material", MaterialArgument.material())
                                                        .executes(context -> {
                                                            Material material = MaterialArgument.getMaterial(context,
                                                                    "material");
                                                            Collection<ServerPlayer> players = EntityArgument
                                                                    .getPlayers(context, "targets");
                                                            int success = 0;
                                                            HazardProperty property = material
                                                                    .getProperty(PropertyKey.HAZARD);
                                                            if (property == null) {
                                                                throw ERROR_INVALID_MATERIAL.create();
                                                            }
                                                            for (ServerPlayer player : players) {
                                                                IHazardEffectTracker tracker = GTCapabilityHelper
                                                                        .getHazardEffectTracker(player);
                                                                if (tracker == null) {
                                                                    continue;
                                                                }
                                                                tracker.addHazardItem(
                                                                        new UnificationEntry(TagPrefix.dust, material));
                                                                success++;
                                                            }
                                                            if (success == 0) {
                                                                throw ERROR_GIVE_FAILED.create();
                                                            }
                                                            return success;
                                                        }))))));
    }
}
