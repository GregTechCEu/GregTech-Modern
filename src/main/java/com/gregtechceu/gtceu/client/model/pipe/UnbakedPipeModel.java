package com.gregtechceu.gtceu.client.model.pipe;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class UnbakedPipeModel implements IUnbakedGeometry<UnbakedPipeModel> {

    private final Map<@Nullable Direction, UnbakedModel> parts;
    private final Map<@NotNull Direction, UnbakedModel> blockers;

    public UnbakedPipeModel(Map<@Nullable Direction, UnbakedModel> parts, Map<@NotNull Direction, UnbakedModel> blockers) {
        this.parts = parts;
        this.blockers = blockers;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        Map<Direction, BakedModel> bakedParts = new IdentityHashMap<>();
        this.parts.forEach((direction, unbaked) -> {
            bakedParts.put(direction, unbaked.bake(baker, spriteGetter, modelState, modelLocation));
        });
        Map<Direction, BakedModel> bakedBlockers = new IdentityHashMap<>();
        this.blockers.forEach((direction, unbaked) -> {
            bakedBlockers.put(direction, unbaked.bake(baker, spriteGetter, modelState, modelLocation));
        });
        return new BakedPipeModel(bakedParts, bakedBlockers);
    }
}
