package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.ModelBakingUtil;
import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;
import com.gregtechceu.gtceu.client.model.compat.ModelState;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartBakedModel;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartUnbakedModel;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

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
    private final Map<String, Identifier> textureOverrides;

    public UnbakedMachineModel(MachineDefinition definition,
                               Map<MachineRenderState, UnbakedModel> models,
                               @Nullable MultiPartUnbakedModel multiPart,
                               List<DynamicRender<?, ?>> dynamicRenders,

                               Set<String> replaceableTextures,
                               Map<String, Identifier> textureOverrides) {
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
                           ItemOverrides overrides) {
        Map<String, TextureAtlasSprite> textureOverrides = new HashMap<>();
        for (var entry : this.textureOverrides.entrySet()) {
            Material material = new Material(entry.getValue());
            textureOverrides.put(entry.getKey(), spriteGetter.apply(material));
        }

        Map<MachineRenderState, BakedModel> baseModels = new IdentityHashMap<>();
        this.models.forEach((state, unbakedModel) -> baseModels.put(state,
                ModelBakingUtil.bake(unbakedModel, context, baker, spriteGetter, modelState, overrides)));

        MultiPartBakedModel multiPart = null;
        if (this.multiPart != null) {
            MultiPartBakedModel.Builder builder = new MultiPartBakedModel.Builder();
            this.multiPart.selectors().forEach(selector -> builder.add(
                    selector.getPredicate(this.multiPart.definition()),
                    ModelBakingUtil.bakeMultiVariant(selector.getVariant(), context, baker, spriteGetter, overrides)));
            multiPart = builder.build();
        }

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
    public void resolveParents(Function<Identifier, UnbakedModel> resolver, IGeometryBakingContext context) {
        MachineModelLoader.resolveStateModels(this, resolver);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.models.values().forEach(model -> {
            Identifier parent = model.parent();
            if (parent != null) {
                resolver.markDependency(parent);
            }
            model.resolveDependencies(resolver);
        });
        if (this.multiPart != null) {
            this.multiPart.resolveDependencies(resolver);
        }
    }
}
