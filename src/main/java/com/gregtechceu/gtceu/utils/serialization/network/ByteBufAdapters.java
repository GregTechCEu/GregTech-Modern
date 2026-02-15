package com.gregtechceu.gtceu.utils.serialization.network;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.utils.EqualityTest;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class ByteBufAdapters {

    // spotless:off
    public static final IByteBufAdapter<ItemStack> ITEM_STACK = makeAdapter(FriendlyByteBuf::readItem, FriendlyByteBuf::writeItem, ItemStack::matches);
    public static final IByteBufAdapter<FluidStack> FLUID_STACK = makeMemberAdapter(FluidStack::readFromPacket, FluidStack::writeToPacket, FluidStack::isFluidStackIdentical);
    public static final IByteBufAdapter<CompoundTag> NBT = makeAdapter(FriendlyByteBuf::readNbt, FriendlyByteBuf::writeNbt, null);
    public static final IByteBufAdapter<String> STRING = makeAdapter(NetworkUtils::readStringSafe, NetworkUtils::writeStringSafe, null);
    public static final IByteBufAdapter<ByteBuf> BYTE_BUF = makeAdapter(NetworkUtils::readByteBuf, NetworkUtils::writeByteBuf, null);
    public static final IByteBufAdapter<FriendlyByteBuf> FRIENDLY_BYTE_BUF = makeAdapter(NetworkUtils::readFriendlyByteBuf, NetworkUtils::writeByteBuf, null);
    public static final IByteBufAdapter<Component> COMPONENT = makeAdapter(FriendlyByteBuf::readComponent, FriendlyByteBuf::writeComponent, Objects::equals);
    // spotless:on

    public static final IByteBufAdapter<byte[]> BYTE_ARR = new IByteBufAdapter<>() {

        @Override
        public byte[] deserialize(FriendlyByteBuf buffer) {
            return buffer.readByteArray();
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, byte[] u) {
            buffer.writeByteArray(u);
        }

        @Override
        public boolean areEqual(byte @NotNull [] t1, byte @NotNull [] t2) {
            if (t1.length != t2.length) return false;
            for (int i = 0; i < t1.length; i++) {
                if (t1[i] != t2[i]) return false;
            }
            return true;
        }
    };

    public static final IByteBufAdapter<long[]> LONG_ARR = new IByteBufAdapter<>() {

        @Override
        public long[] deserialize(FriendlyByteBuf buffer) {
            return buffer.readLongArray();
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, long[] u) {
            buffer.writeLongArray(u);
        }

        @Override
        public boolean areEqual(long @NotNull [] t1, long @NotNull [] t2) {
            if (t1.length != t2.length) return false;
            for (int i = 0; i < t1.length; i++) {
                if (t1[i] != t2[i]) return false;
            }
            return true;
        }
    };

    public static final IByteBufAdapter<BigInteger> BIG_INT = new IByteBufAdapter<>() {

        @Override
        public BigInteger deserialize(FriendlyByteBuf buffer) {
            return new BigInteger(buffer.readByteArray());
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, BigInteger u) {
            buffer.writeByteArray(u.toByteArray());
        }

        @Override
        public boolean areEqual(@NotNull BigInteger t1, @NotNull BigInteger t2) {
            return t1.equals(t2);
        }
    };

    public static final IByteBufAdapter<BigDecimal> BIG_DECIMAL = new IByteBufAdapter<>() {

        @Override
        public BigDecimal deserialize(FriendlyByteBuf buffer) {
            return new BigDecimal(BIG_INT.deserialize(buffer), buffer.readVarInt());
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, BigDecimal u) {
            BIG_INT.serialize(buffer, u.unscaledValue());
            buffer.writeVarInt(u.scale());
        }

        @Override
        public boolean areEqual(@NotNull BigDecimal t1, @NotNull BigDecimal t2) {
            return t1.equals(t2);
        }
    };

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

    public static <T> IByteBufAdapter<T> makeMemberAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                           @NotNull IByteBufMemberSerializer<T> memberSerializer,
                                                           @Nullable EqualityTest<T> comparator) {
        return makeAdapter(deserializer, memberSerializer.asBasic(), comparator);
    }
}
