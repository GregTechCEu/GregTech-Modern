package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IGTTagLoader;
import com.gregtechceu.gtceu.core.MixinHelpers;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;

import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(value = TagLoader.class, priority = 500)
public class TagLoaderMixin implements IGTTagLoader {

    @Nullable
    @Unique
    private Registry<?> gtceu$storedRegistry;

    @Inject(method = "load", at = @At(value = "RETURN"))
    public void gtceu$load(ResourceManager resourceManager,
                           CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir) {
        if (gtceu$storedRegistry == null) return;
        MixinHelpers.generateGTDynamicTags(cir.getReturnValue(), gtceu$storedRegistry);
    }

    @Inject(method = "loadPendingTags",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/tags/TagLoader;load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;"))
    private static <T> void gtceu$setRegistry(ResourceManager resourceManager, Registry<T> registry,
                                              CallbackInfoReturnable<Optional<Registry.PendingTags<T>>> cir,
                                              @Local(ordinal = 0) TagLoader<T> tagLoader) {
        ((IGTTagLoader) tagLoader).gtceu$setRegistry(registry);
    }

    @Override
    public void gtceu$setRegistry(Registry<?> registry) {
        this.gtceu$storedRegistry = registry;
    }
}
