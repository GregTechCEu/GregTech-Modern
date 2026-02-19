package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.integration.kjs.GTKubeJSPlugin;
import com.gregtechceu.gtceu.integration.modernfix.GTModernFixIntegration;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ModelManager.class)
public abstract class ModelManagerMixin {

    @Inject(method = "reload", at = @At(value = "HEAD"))
    private void gtceu$loadDynamicModels(PreparableReloadListener.PreparationBarrier preparationBarrier,
                                         ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
                                         ProfilerFiller reloadProfiler, Executor backgroundExecutor,
                                         Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (ModLoader.hasErrors()) {
            GTCEu.LOGGER.warn("GregTech Model loading CANCELLED because loading errors have been encountered");
            return;
        }

        long startTime = System.currentTimeMillis();
        // turns out these do have to be init in here after all, as they check for asset existence. whoops.
        ModLoader.postEventWrapContainerInModOrder(new RegisterDynamicResourcesEvent());

        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTKubeJSPlugin.generateMachineBlockModels();
        }
        if (GTCEu.Mods.isModernFixLoaded()) {
            GTModernFixIntegration.setAsLast();
        }
        GTCEu.LOGGER.info("GregTech Model loading took {}ms", System.currentTimeMillis() - startTime);
    }
}
