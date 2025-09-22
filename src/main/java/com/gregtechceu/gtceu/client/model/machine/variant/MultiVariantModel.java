package com.gregtechceu.gtceu.client.model.machine.variant;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.SimpleModelState;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public @NotNull Collection<ResourceLocation> getDependencies() {
        return this.variants.stream()
                .map(VariantState::getModel)
                .flatMap(either -> either.map(Stream::of, model -> model.getDependencies().stream()))
                .collect(Collectors.toSet());
    }

    public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> resolver) {
        this.variants.forEach((variant) -> {
            UnbakedModel model = variant.getModel().map(resolver, Function.identity());
            variant.setResolvedModel(model);
            model.resolveParents(resolver);
        });
    }

    public @Nullable BakedModel bake(@NotNull ModelBaker baker,
                                     @NotNull Function<Material, TextureAtlasSprite> spriteGetter,
                                     @NotNull ModelState state, @NotNull ResourceLocation location) {
        if (this.variants.isEmpty()) {
            return null;
        } else {
            WeightedBakedModel.Builder weightedBuilder = new WeightedBakedModel.Builder();

            for (VariantState variant : this.variants) {
                // rotate the transform by both the variant and the original blockstate rotation
                var actualRotation = state.getRotation().compose(variant.getRotation());
                var actualState = new SimpleModelState(actualRotation, variant.isUvLocked());

                BakedModel baked = variant.getResolvedModel().bake(baker, spriteGetter, actualState,
                        variant.getModel().map(Function.identity(), model -> location));
                weightedBuilder.add(baked, variant.getWeight());
            }
            return weightedBuilder.build();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<MultiVariantModel> {

        public MultiVariantModel deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                                                                                                              throws JsonParseException {
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
