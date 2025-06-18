package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import lombok.Getter;

import java.util.*;
import java.util.function.Function;

public class UnbakedMachineModel implements IUnbakedGeometry<UnbakedMachineModel> {

    @Getter
    private final MachineDefinition definition;
    @Getter
    private final Map<String, Either<ResourceLocation, UnbakedModel>> unresolvedModels;
    @Getter
    private final List<DynamicRender<?, ?>> dynamicRenders;
    @Getter
    private final Map<MachineRenderState, UnbakedModel> resolvedModels = new HashMap<>();

    public UnbakedMachineModel(ResourceLocation machineId,
                               Map<String, Either<ResourceLocation, UnbakedModel>> unresolvedModels,
                               List<DynamicRender<?, ?>> dynamicRenders) {
        this.definition = GTRegistries.MACHINES.get(machineId);
        this.unresolvedModels = unresolvedModels;
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

        MachineModel model = new MachineModel(this.getDefinition(), baseModels, dynamicRenders);
        model.setParticleIcon(spriteGetter.apply(context.getMaterial("particle")));
        return model;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        this.resolvedModels.clear();
        this.resolvedModels.putAll(MachineModelLoader.resolveStateModels(this, modelGetter));

        for (UnbakedModel resolved : this.resolvedModels.values()) {
            resolved.resolveParents(modelGetter);
        }
    }
}
