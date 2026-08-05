package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.data.dynamic.DynamicRecipeHandler;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 500)
public abstract class RecipeManagerEarlyMixin extends SimpleJsonResourceReloadListener {

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    private RecipeManagerEarlyMixin(Gson gson, String directory) {
        super(gson, directory);
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    private void gtceu$handleDynamicRecipesEarly(Map<ResourceLocation, JsonElement> map,
                                                 ResourceManager resourceManager, ProfilerFiller profiler,
                                                 CallbackInfo ci) {
        DynamicRecipeHandler.handleRecipesEarly(map, this.registries, this.makeConditionalOps());
    }
}
