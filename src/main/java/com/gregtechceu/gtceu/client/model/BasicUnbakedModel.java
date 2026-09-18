package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class BasicUnbakedModel implements UnbakedModel {

    @Override
    public @NotNull Collection<Identifier> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(@NotNull Function<Identifier, UnbakedModel> function) {
        for (Identifier dependency : getDependencies()) {
            function.apply(dependency).resolveParents(function);
        }
    }

    @Override
    public @Nullable BakedModel bake(@NotNull ModelBaker baker,
                                     @NotNull Function<Material, TextureAtlasSprite> spriteGetter,
                                     @NotNull ModelState state, @NotNull Identifier location) {
        return null;
    }
}
