package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartBakedModel;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartUnbakedModel;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class UnbakedMachineModel implements IUnbakedGeometry<UnbakedMachineModel> {

    @Getter
    private final MachineDefinition definition;
    @Getter
    private final Map<MachineRenderState, UnbakedModel> models;
    @Nullable
    @Getter
    private final MultiPartUnbakedModel multiPart;
    @Getter
    private final List<DynamicRender<?, ?>> dynamicRenders;
    private final Set<String> replaceableTextures;
    private final Map<String, ResourceLocation> textureOverrides;

    public UnbakedMachineModel(MachineDefinition definition,
                               Map<MachineRenderState, UnbakedModel> models,
                               @Nullable MultiPartUnbakedModel multiPart,
                               List<DynamicRender<?, ?>> dynamicRenders,

                               Set<String> replaceableTextures,
                               Map<String, ResourceLocation> textureOverrides) {
        this.definition = definition;
        this.models = models;
        this.multiPart = multiPart;
        this.dynamicRenders = dynamicRenders;
        this.replaceableTextures = replaceableTextures;
        this.textureOverrides = textureOverrides;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        Map<String, TextureAtlasSprite> textureOverrides = new HashMap<>();
        for (var entry : this.textureOverrides.entrySet()) {
            Material material = new Material(TextureAtlas.LOCATION_BLOCKS, entry.getValue());
            textureOverrides.put(entry.getKey(), spriteGetter.apply(material));
        }

        Map<MachineRenderState, BakedModel> baseModels = new IdentityHashMap<>();
        models.forEach((machineState, unbaked) -> {
            baseModels.put(machineState, unbaked.bake(baker, spriteGetter, modelState, modelLocation));
        });
        MultiPartBakedModel multiPart = this.multiPart == null ? null :
                this.multiPart.bake(baker, spriteGetter, modelState, modelLocation);

        MachineModel model = new MachineModel(this.getDefinition(), baseModels, multiPart, this.dynamicRenders,
                context.getTransforms(), context.getRootTransform(), modelState,
                context.isGui3d(), context.useBlockLight(), context.useAmbientOcclusion());

        if (context.hasMaterial("particle")) {
            model.setParticleIcon(spriteGetter.apply(context.getMaterial("particle")));
        }
        model.setReplaceableTextures(this.replaceableTextures);
        model.setTextureOverrides(textureOverrides);
        return model;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext context) {
        MachineModelLoader.resolveStateModels(this, resolver);
    }
}
