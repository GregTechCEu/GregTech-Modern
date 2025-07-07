package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IGTTagLoader;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.Executor;

@Mixin(TagManager.class)
public class TagManagerMixin {

    @ModifyExpressionValue(method = "createLoader", at = @At(value = "NEW", target = "net/minecraft/tags/TagLoader"))
    private <T> TagLoader<Holder<T>> gtceu$saveTagLoaderRegistry(TagLoader<Holder<T>> loader,
                                                                 ResourceManager rm, Executor executor,
                                                                 RegistryAccess.RegistryEntry<T> entry) {
        ((IGTTagLoader) loader).gtceu$setRegistry(entry.value());
        return loader;
    }
}
