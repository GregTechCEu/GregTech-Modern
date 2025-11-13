package com.gregtechceu.gtceu.utils.serialization.network;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.utils.EqualityTest;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ByteBufAdapters {

    // spotless:off
    public static final IByteBufAdapter<ItemStack> ITEM_STACK = makeAdapter(FriendlyByteBuf::readItem, FriendlyByteBuf::writeItem, ItemStack::matches);
    public static final IByteBufAdapter<FluidStack> FLUID_STACK = makeMemberAdapter(FluidStack::readFromPacket, FluidStack::writeToPacket, FluidStack::isFluidStackIdentical);
    public static final IByteBufAdapter<CompoundTag> NBT = makeAdapter(FriendlyByteBuf::readNbt, FriendlyByteBuf::writeNbt, null);
    public static final IByteBufAdapter<String> STRING = makeAdapter(NetworkUtils::readStringSafe, NetworkUtils::writeStringSafe, null);
    public static final IByteBufAdapter<ByteBuf> BYTE_BUF = makeAdapter(NetworkUtils::readByteBuf, NetworkUtils::writeByteBuf, null);
    public static final IByteBufAdapter<FriendlyByteBuf> FRIENDLY_BYTE_BUF = makeAdapter(NetworkUtils::readFriendlyByteBuf, NetworkUtils::writeByteBuf, null);
    public static final IByteBufAdapter<List<MonitorGroup>> MONITOR_GROUPS = makeAdapter(MonitorGroup.CODEC.listOf());
    // spotless:on

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                     @NotNull IByteBufSerializer<T> serializer,
                                                     @Nullable EqualityTest<T> comparator) {
        final EqualityTest<T> tester = comparator != null ? comparator : EqualityTest.defaultTester();
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
                return tester.areEqual(t1, t2);
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
