package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.utils.EqualityTest;
import brachy.modularui.utils.serialization.network.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GTByteBufAdapters {

    // spotless:off
    public static final IByteBufAdapter<MonitorGroup> MONITOR_GROUPS = makeAdapter(MonitorGroup.CODEC);
    public static final IByteBufAdapter<Component> COMPONENT = ByteBufAdapters.makeAdapter(FriendlyByteBuf::readComponent, FriendlyByteBuf::writeComponent,
            (a, b) -> Objects.equals(a.toString(), b.toString()));

    // spotless:on

    public static final IByteBufAdapter<GTRecipe> GTRECIPE = new IByteBufAdapter<>() {

        @Override
        public @Nullable GTRecipe deserialize(FriendlyByteBuf buffer) {
            if (!buffer.readBoolean()) {
                return null;
            }
            ResourceLocation id = buffer.readResourceLocation();
            return GTRecipeSerializer.SERIALIZER.fromNetwork(id, buffer);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, @Nullable GTRecipe u) {
            if (u == null) {
                buffer.writeBoolean(false);
                return;
            }
            buffer.writeBoolean(true);
            buffer.writeResourceLocation(u.getId());
            GTRecipeSerializer.SERIALIZER.toNetwork(buffer, u);
        }

        @Override
        public boolean areEqual(GTRecipe t1, GTRecipe t2) {
            return EqualityTest.wrapNullSafe(GTRecipe::equals).areEqual(t1, t2);
        }
    };

    public static <T> IByteBufAdapter<T> makeAdapter(Codec<T> codec) {
        return new IByteBufAdapter<>() {

            @Override
            public T deserialize(FriendlyByteBuf buffer) {
                return buffer.readJsonWithCodec(codec);
            }

            @Override
            public void serialize(FriendlyByteBuf buffer, T u) {
                buffer.writeJsonWithCodec(codec, u);
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
