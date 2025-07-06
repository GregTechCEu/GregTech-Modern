package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class BasicUnbakedModel implements UnbakedModel {

    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> function) {
        for (ResourceLocation dependency : getDependencies()) {
            function.apply(dependency).resolveParents(function);
        }
    }

    @Override
    public @Nullable BakedModel bake(@NotNull ModelBaker baker,
                                     @NotNull Function<Material, TextureAtlasSprite> spriteGetter,
                                     @NotNull ModelState state, @NotNull ResourceLocation location) {
        return null;
    }
}
