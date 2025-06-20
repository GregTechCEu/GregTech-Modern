package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartBakedModel;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartUnbakedModel;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import com.mojang.datafixers.util.Either;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class UnbakedMachineModel implements IUnbakedGeometry<UnbakedMachineModel> {

    @Getter
    private final MachineDefinition definition;
    @Getter
    private final Map<String, Either<ResourceLocation, UnbakedModel>> unresolvedModels;
    @Nullable
    @Getter
    private final MultiPartUnbakedModel multiPart;
    @Getter
    private final List<DynamicRender<?, ?>> dynamicRenders;
    @Getter
    private final Map<MachineRenderState, UnbakedModel> resolvedModels = new HashMap<>();

    public UnbakedMachineModel(MachineDefinition definition,
                               Map<String, Either<ResourceLocation, UnbakedModel>> unresolvedModels,
                               @Nullable MultiPartUnbakedModel multiPart,
                               List<DynamicRender<?, ?>> dynamicRenders) {
        this.definition = definition;
        this.unresolvedModels = unresolvedModels;
        this.multiPart = multiPart;
        this.dynamicRenders = dynamicRenders;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState state,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        Map<MachineRenderState, BakedModel> baseModels = new IdentityHashMap<>();
        resolvedModels.forEach((machineState, unbaked) -> {
            baseModels.put(machineState, unbaked.bake(baker, spriteGetter, state, modelLocation));
        });
        MultiPartBakedModel multiPart = this.multiPart.bake(baker, spriteGetter, state, modelLocation);

        MachineModel model = new MachineModel(this.getDefinition(), baseModels, multiPart, dynamicRenders);
        model.setParticleIcon(spriteGetter.apply(context.getMaterial("particle")));
        return model;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext context) {
        this.resolvedModels.clear();
        this.resolvedModels.putAll(MachineModelLoader.resolveStateModels(this, resolver));

        for (UnbakedModel resolved : this.resolvedModels.values()) {
            resolved.resolveParents(resolver);
        }
    }
}
