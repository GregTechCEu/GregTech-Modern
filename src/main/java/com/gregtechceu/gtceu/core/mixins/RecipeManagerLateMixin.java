package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.data.dynamic.DynamicRecipeHandler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 1500)
public abstract class RecipeManagerLateMixin extends SimpleJsonResourceReloadListener {

    public RecipeManagerLateMixin(Gson gson, String directory) {
        super(gson, directory);
    }

    @Shadow
    public abstract void replaceRecipes(Iterable<RecipeHolder<?>> recipes);

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void gtceu$handleDynamicRecipesLate(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                                ProfilerFiller profiler, CallbackInfo ci) {
        DynamicRecipeHandler.handleRecipesLate((RecipeManager) (Object) this);
    }
}
