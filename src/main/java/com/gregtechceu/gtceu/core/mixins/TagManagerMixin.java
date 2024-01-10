package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IGTTagLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TagManager.class)
public class TagManagerMixin {

    @Inject(method = "createLoader", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tags/TagLoader;<init>(Ljava/util/function/Function;Ljava/lang/String;)V",
            shift = At.Shift.BY,
            by = 2
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private <T> void gtceu$saveRegistryToTagLoader(ResourceManager rm, Executor executor, RegistryAccess.RegistryEntry<T> reg,
                                                   CallbackInfoReturnable<CompletableFuture<TagManager.LoadResult<T>>> cir,
                                                   ResourceKey<? extends Registry<T>> key, Registry<T> registry, TagLoader<Holder<T>> loader) {
        ((IGTTagLoader<T>) loader).gtceu$setRegistry(registry);
    }
}
