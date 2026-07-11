package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

import brachy.modularui.utils.EqualityTest;
import brachy.modularui.utils.serialization.network.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.Objects;

public class GTByteBufAdapters {

    // spotless:off
    public static final IByteBufAdapter<FriendlyByteBuf, MonitorGroup> MONITOR_GROUPS = makeAdapter(MonitorGroup.CODEC);
    public static final IByteBufAdapter<FriendlyByteBuf, PatternError> PATTERN_ERRORS = makeAdapter(PatternError.CODEC);

    // spotless:on

    public static final IByteBufAdapter<RegistryFriendlyByteBuf, GTRecipe> GTRECIPE = new IByteBufAdapter<>() {

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, GTRecipe u) {
            GTRecipeSerializer.toNetwork(buffer, u);
        }

        @Override
        public GTRecipe decode(RegistryFriendlyByteBuf buffer) {
            return GTRecipeSerializer.fromNetwork(buffer);
        }

        @Override
        public boolean areEqual(GTRecipe t1, GTRecipe t2) {
            return EqualityTest.wrapNullSafe(GTRecipe::equals).areEqual(t1, t2);
        }
    };

    public static <T> IByteBufAdapter<FriendlyByteBuf, T> makeAdapter(Codec<T> codec) {
        return new IByteBufAdapter<>() {

            @Override
            public void encode(FriendlyByteBuf buffer, T u) {
                buffer.writeJsonWithCodec(codec, u);
            }

            @Override
            public T decode(FriendlyByteBuf buffer) {
                return buffer.readJsonWithCodec(codec);
            }

            @Override
            public boolean areEqual(T a, T b) {
                String encoded1 = codec.encodeStart(JsonOps.INSTANCE, a).result().orElseThrow().toString();
                String encoded2 = codec.encodeStart(JsonOps.INSTANCE, b).result().orElseThrow().toString();
                return Objects.equals(encoded1, encoded2);
            }
        };
    }
}
