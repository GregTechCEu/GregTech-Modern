package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IGTTagLoader;
import com.gregtechceu.gtceu.core.MixinHelpers;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(value = TagLoader.class, priority = 500)
public class TagLoaderMixin implements IGTTagLoader {

    @Nullable
    @Unique
    private Registry<?> gtceu$storedRegistry;

    @Inject(method = "load", at = @At(value = "RETURN"))
    public void gtceu$load(ResourceManager resourceManager,
                           CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        if (gtceu$storedRegistry == null) return;
        MixinHelpers.generateGTDynamicTags(cir.getReturnValue(), gtceu$storedRegistry);
    }

    @Override
    public void gtceu$setRegistry(Registry<?> registry) {
        this.gtceu$storedRegistry = registry;
    }
}
