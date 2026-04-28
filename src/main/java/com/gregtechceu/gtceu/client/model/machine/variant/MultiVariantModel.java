package com.gregtechceu.gtceu.client.model.machine.variant;

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

    public static class Deserializer implements JsonDeserializer<MultiVariantModel> {

        public MultiVariantModel deserialize(JsonElement json, Type type,
                                             JsonDeserializationContext context) throws JsonParseException {
            List<VariantState> variants = new ArrayList<>();
            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                if (array.isEmpty()) {
                    throw new JsonParseException("Empty variant array");
                }

                for (JsonElement v : array) {
                    variants.add(context.deserialize(v, VariantState.class));
                }
            } else {
                variants.add(context.deserialize(json, VariantState.class));
            }

            return new MultiVariantModel(variants);
        }
    }
}
