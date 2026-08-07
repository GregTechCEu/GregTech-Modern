package com.gregtechceu.gtceu.client.model.pipe;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import static com.gregtechceu.gtceu.client.model.machine.MachineModelLoader.MISSING_MARKER;

public class UnbakedPipeModel implements IUnbakedGeometry<UnbakedPipeModel> {

    @Getter
    private final Map<@Nullable Direction, UnbakedModel> parts;
    @Getter
    private final Map<@NotNull Direction, UnbakedModel> restrictors;

    public UnbakedPipeModel(Map<@Nullable Direction, UnbakedModel> parts,
                            Map<@NotNull Direction, UnbakedModel> restrictors) {
        this.parts = parts;
        this.restrictors = restrictors;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        Map<Direction, BakedModel> bakedParts = new IdentityHashMap<>();
        this.parts.forEach((direction, unbaked) -> {
            bakedParts.put(direction, unbaked.bake(baker, spriteGetter, modelState));
        });
        Map<Direction, BakedModel> bakedRestrictors = new IdentityHashMap<>();
        this.restrictors.forEach((direction, unbaked) -> {
            bakedRestrictors.put(direction, unbaked.bake(baker, spriteGetter, modelState));
        });
        return new BakedPipeModel(bakedParts, bakedRestrictors);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext context) {
        UnbakedModel missingModel = resolver.apply(ModelBakery.MISSING_MODEL_LOCATION);

        Map<Direction, UnbakedModel> copy = new IdentityHashMap<>(this.parts);
        copy.forEach((side, variant) -> {
            if (variant == null || variant == MISSING_MARKER) {
                // replace null & markers with the actual missing model
                this.parts.put(side, missingModel);
            } else {
                variant.resolveParents(resolver);
                this.parts.put(side, variant);
            }
        });
        copy = new IdentityHashMap<>(this.restrictors);
        copy.forEach((side, variant) -> {
            if (variant == null || variant == MISSING_MARKER) {
                // replace null & markers with the actual missing model
                this.restrictors.put(side, missingModel);
            } else {
                variant.resolveParents(resolver);
                this.restrictors.put(side, variant);
            }
        });
    }
}
