package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.model.machine.variant.MultiVariantModel;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.StateDefinition;

import com.google.gson.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record MultiPartUnbakedModel(StateDefinition<MachineDefinition, MachineRenderState> definition,
                                    List<MultiPartSelector> selectors)
        implements UnbakedModel {

    public Set<MultiVariantModel> getModels() {
        Set<MultiVariantModel> set = new HashSet<>();

        for (MultiPartSelector selector : this.selectors()) {
            set.add(selector.getVariant());
        }
        return set;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.selectors().stream()
                .flatMap((selector) -> selector.getVariant().getDependencies().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        this.selectors().forEach((selector) -> selector.getVariant().resolveParents(resolver));
    }

    @Override
    public MultiPartBakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
                                    ModelState state, ResourceLocation location) {
        MultiPartBakedModel.Builder builder = new MultiPartBakedModel.Builder();

        for (MultiPartSelector selector : this.selectors()) {
            BakedModel bakedmodel = selector.getVariant().bake(baker, spriteGetter, state, location);
            if (bakedmodel != null) {
                builder.add(selector.getPredicate(this.definition), bakedmodel);
            }
        }
        return builder.build();
    }

    public static MultiPartUnbakedModel deserialize(MachineDefinition definition, JsonArray elements) {
        return new MultiPartUnbakedModel(definition.getStateDefinition(), getSelectors(elements));
    }

    private static List<MultiPartSelector> getSelectors(JsonArray elements) {
        List<MultiPartSelector> list = new ArrayList<>();

        for (JsonElement e : elements) {
            list.add(MachineModelLoader.GSON.fromJson(e, MultiPartSelector.class));
        }
        return list;
    }
}
