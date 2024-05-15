package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote ServerCommands
 */
public class ServerCommands {

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
                                    for (RecipeHolder<?> recipe : context.getSource().getServer().getRecipeManager()
                                            .getRecipes()) {
                                        if (recipe.value() instanceof GTRecipe gtRecipe &&
                                                !gtRecipe.checkRecipeValid()) {
                                            context.getSource().sendSuccess(
                                                    () -> Component
                                                            .literal("recipe %s is invalid".formatted(recipe.id())),
                                                    false);
                                        }
                                    }
                                    return 1;
                                })));
    }
}
