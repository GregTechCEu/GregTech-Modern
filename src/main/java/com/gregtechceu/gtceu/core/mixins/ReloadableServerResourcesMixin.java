package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.loot.DungeonLootLoader;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ReloadableServerResources.class, priority = 2000)
public abstract class ReloadableServerResourcesMixin {

    @Inject(method = "loadResources", at = @At("HEAD"))
    private static void gtceu$init(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> access,
                                   FeatureFlagSet featureFlags, Commands.CommandSelection commands,
                                   int functionCompilationLevel, Executor backgroundExecutor, Executor gameExecutor,
                                   CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        // load and loot tables recipes *before* other data so that we have the registries loaded
        // before saving recipes to JSON.
        // because it breaks if we don't do that.

        // this doesn't have dynamic registries available, by the way.
        RegistryAccess.Frozen frozen = access.compositeAccess();

        // Register recipes & unification data again
        long startTime = System.currentTimeMillis();
        GTCraftingComponents.init();
        GTRecipes.recipeAddition(new RecipeOutput() {

            @Override
            public Advancement.@NotNull Builder advancement() {
                // noinspection removal
                return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
            }

            @Override
            public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe,
                               @Nullable AdvancementHolder advancement, ICondition @NotNull... conditions) {
                GTDynamicDataPack.addRecipe(id, recipe, advancement, frozen);
            }
        });
        MixinHelpers.generateGTDynamicLoot(GTDynamicDataPack::addLootTable, frozen);
        // Initialize dungeon loot additions
        DungeonLootLoader.init();

        GTCEu.LOGGER.info("GregTech Data loading took {}ms", System.currentTimeMillis() - startTime);
    }
}
