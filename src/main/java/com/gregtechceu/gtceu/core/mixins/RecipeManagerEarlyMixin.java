package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.common.data.GTRecipes;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.SortedMap;

@Mixin(value = RecipeManager.class, priority = 500)
public abstract class RecipeManagerEarlyMixin {

    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/SortedMap;forEach(Ljava/util/function/BiConsumer;)V",
                     shift = At.Shift.BEFORE))
    private void gtceu$removeRecipes(ResourceManager pResourceManager, ProfilerFiller pProfiler,
                                     CallbackInfoReturnable<RecipeMap> cir,
                                     @Local(ordinal = 0) SortedMap<Identifier, Recipe<?>> recipes) {
        GTRecipes.recipeRemoval(recipes::remove);
    }
}
