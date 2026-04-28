package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.model.machine.variant.MultiVariantModel;

import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.StateDefinition;

import com.google.gson.*;

import java.util.*;
import java.util.function.Function;

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

    public Collection<Identifier> getDependencies() {
        return Collections.emptyList();
    }

    public void resolveParents(Function<Identifier, UnbakedModel> resolver) {
        this.selectors().forEach((selector) -> selector.getVariant().resolveParents(resolver));
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.selectors().forEach(selector -> selector.getVariant().resolveDependencies(resolver));
    }

    public static MultiPartUnbakedModel deserialize(MachineDefinition definition, JsonArray elements,
                                                    JsonDeserializationContext context) {
        return new MultiPartUnbakedModel(definition.getStateDefinition(), getSelectors(elements, context));
    }

    private static List<MultiPartSelector> getSelectors(JsonArray elements, JsonDeserializationContext context) {
        List<MultiPartSelector> list = new ArrayList<>();

        for (JsonElement e : elements) {
            list.add(MultiPartSelector.Deserializer.fromJson(e, context));
        }
        return list;
    }
}
