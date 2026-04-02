package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.utils.EqualityTest;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.utils.serialization.network.IByteBufAdapter;
import brachy.modularui.utils.serialization.network.IByteBufDeserializer;
import brachy.modularui.utils.serialization.network.IByteBufMemberSerializer;
import brachy.modularui.utils.serialization.network.IByteBufSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GTByteBufAdapters {

    // spotless:off
    public static final IByteBufAdapter<MonitorGroup> MONITOR_GROUPS = makeAdapter(MonitorGroup.CODEC);
    public static final IByteBufAdapter<Component> COMPONENT = makeAdapter(FriendlyByteBuf::readComponent, FriendlyByteBuf::writeComponent, (a, b) -> Objects.equals(a.toString(), b.toString()));

    // spotless:on

    public static final IByteBufAdapter<GTRecipe> GTRECIPE = new IByteBufAdapter<>() {

        @Override
        public GTRecipe deserialize(FriendlyByteBuf buffer) {
            if (!buffer.readBoolean()) {
                return null;
            }
            ResourceLocation id = buffer.readResourceLocation();
            return GTRecipeSerializer.SERIALIZER.fromNetwork(id, buffer);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, GTRecipe u) {
            if (u == null) {
                buffer.writeBoolean(false);
                return;
            }
            buffer.writeBoolean(true);
            buffer.writeResourceLocation(u.getId());
            GTRecipeSerializer.SERIALIZER.toNetwork(buffer, u);
        }

        @Override
        public boolean areEqual(@NotNull GTRecipe t1, @NotNull GTRecipe t2) {
            return EqualityTest.wrapNullSafe(GTRecipe::equals).areEqual(t1, t2);
        }
    };

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                     @NotNull IByteBufSerializer<T> serializer,
                                                     @Nullable EqualityTest<T> tester) {
        return new IByteBufAdapter<>() {

            @Override
            public T deserialize(FriendlyByteBuf buffer) {
                return deserializer.deserialize(buffer);
            }

            @Override
            public void serialize(FriendlyByteBuf buffer, T u) {
                serializer.serialize(buffer, u);
            }

            @Override
            public boolean areEqual(@NotNull T t1, @NotNull T t2) {
                return tester != null ? tester.areEqual(t1, t2) : Objects.equals(t1, t2);
            }
        };
    }

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull Codec<T> codec) {
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
            public boolean areEqual(@NotNull T a, @NotNull T b) {
                String encoded1 = codec.encodeStart(JsonOps.INSTANCE, a).result().orElseThrow().toString();
                String encoded2 = codec.encodeStart(JsonOps.INSTANCE, b).result().orElseThrow().toString();
                return Objects.equals(encoded1, encoded2);
            }
        };
    }

    public static <T> IByteBufAdapter<T> makeMemberAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                           @NotNull IByteBufMemberSerializer<T> memberSerializer,
                                                           @Nullable EqualityTest<T> comparator) {
        return makeAdapter(deserializer, memberSerializer.asBasic(), comparator);
    }
}
