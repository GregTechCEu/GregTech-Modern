package com.gregtechceu.gtceu.client.model.machine.variant;

import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record MultiVariantModel(List<VariantState> variants) implements UnbakedModel {

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof MultiVariantModel model) {
            return this.variants.equals(model.variants);
        } else {
            return false;
        }
    }

    public List<Identifier> getDependencies() {
        return Collections.emptyList();
    }

    public void resolveParents(Function<Identifier, UnbakedModel> resolver) {
        this.variants.forEach((variant) -> {
            UnbakedModel model = variant.getModel().map(resolver, Function.identity());
            variant.setResolvedModel(model);
        });
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.variants.forEach(variant -> variant.getModel()
                .ifLeft(resolver::markDependency)
                .ifRight(model -> {
                    Identifier parent = model.parent();
                    if (parent != null) {
                        resolver.markDependency(parent);
                    }
                    model.resolveDependencies(resolver);
                }));
    }

    public static MultiVariantModel deserialize(JsonElement json,
                                                JsonDeserializationContext context) throws JsonParseException {
        List<VariantState> variants = new ArrayList<>();
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.isEmpty()) {
                throw new JsonParseException("Empty variant array");
            }

            for (JsonElement v : array) {
                variants.add(VariantState.deserialize(v, context));
            }
        } else {
            variants.add(VariantState.deserialize(json, context));
        }

        return new MultiVariantModel(variants);
    }

    public static class Deserializer implements JsonDeserializer<MultiVariantModel> {

        public MultiVariantModel deserialize(JsonElement json, Type type,
                                             JsonDeserializationContext context) throws JsonParseException {
            return MultiVariantModel.deserialize(json, context);
        }
    }
}
