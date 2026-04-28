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
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Mixin(value = ReloadableServerResources.class, priority = 2000)
public abstract class ReloadableServerResourcesMixin {

    @Inject(method = "lambda$loadResources$2",
            at = @At(value = "INVOKE",
                     target = "Lnet/neoforged/neoforge/event/EventHooks;onResourceReload(Lnet/minecraft/server/ReloadableServerResources;Lnet/minecraft/core/RegistryAccess;)Ljava/util/List;",
                     shift = At.Shift.BEFORE))
    private static void gtceu$init(ReloadableServerRegistries.LoadResult fullRegistries,
                                   FeatureFlagSet featureFlags, Commands.CommandSelection commands,
                                   List<Registry.PendingTags<?>> pendingTags, PermissionSet permissions,
                                   ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor,
                                   List<DataComponentInitializers.PendingComponents<?>> pendingComponents,
                                   CallbackInfoReturnable<CompletionStage<ReloadableServerResources>> cir) {
        // load and loot tables recipes *before* other data so that we have the registries loaded
        // before saving recipes to JSON.
        // because it breaks if we don't do that.

        // this doesn't have dynamic registries available, by the way.
        pendingTags.forEach(Registry.PendingTags::apply);
        pendingComponents.forEach(DataComponentInitializers.PendingComponents::apply);
        RegistryAccess.Frozen frozen = fullRegistries.layers().compositeAccess();

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
            public void accept(@NotNull ResourceKey<Recipe<?>> id, @NotNull Recipe<?> recipe,
                               @Nullable AdvancementHolder advancement, ICondition @NotNull... conditions) {
                GTDynamicDataPack.addRecipe(id.identifier(), recipe, advancement, frozen);
            }

            @Override
            public void includeRootAdvancement() {}
        });
        MixinHelpers.generateGTDynamicLoot(GTDynamicDataPack::addLootTable, frozen);
        // Initialize dungeon loot additions
        DungeonLootLoader.init();

        GTCEu.LOGGER.info("GregTech Data loading took {}ms", System.currentTimeMillis() - startTime);
    }
}
