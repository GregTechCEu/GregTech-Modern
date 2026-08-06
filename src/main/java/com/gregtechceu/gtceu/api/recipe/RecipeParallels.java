package com.gregtechceu.gtceu.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RecipeParallels(
                              int parallels,
                              int subtickParallels,
                              int batchParallels) {

    public static final Codec<RecipeParallels> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("parallels").forGetter(RecipeParallels::parallels),
            Codec.INT.fieldOf("subtickParallels").forGetter(RecipeParallels::subtickParallels),
            Codec.INT.fieldOf("batchParallels").forGetter(RecipeParallels::batchParallels))
            .apply(instance, RecipeParallels::new));
}
