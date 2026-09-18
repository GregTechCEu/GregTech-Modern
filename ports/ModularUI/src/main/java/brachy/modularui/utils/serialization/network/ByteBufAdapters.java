package brachy.modularui.utils.serialization.network;

import brachy.modularui.utils.EqualityTest;
import brachy.modularui.utils.NetworkUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public class ByteBufAdapters {

    // @formatter:off
    public static final IByteBufAdapter<RegistryFriendlyByteBuf, ItemStack> ITEM_STACK = makeAdapter(ItemStack.OPTIONAL_STREAM_CODEC, ItemStack::matches);
    public static final IByteBufAdapter<RegistryFriendlyByteBuf, FluidStack> FLUID_STACK = makeAdapter(FluidStack.OPTIONAL_STREAM_CODEC, FluidStack::matches);
    public static final IByteBufAdapter<ByteBuf, CompoundTag> NBT = makeAdapter(ByteBufCodecs.COMPOUND_TAG, null);
    public static final IByteBufAdapter<ByteBuf, String> STRING = makeAdapter(ByteBufCodecs.STRING_UTF8, null);
    public static final IByteBufAdapter<ByteBuf, ByteBuf> BYTE_BUF = makeAdapter(NetworkUtils::readByteBuf, NetworkUtils::writeByteBuf, null);
    public static final IByteBufAdapter<ByteBuf, FriendlyByteBuf> FRIENDLY_BYTE_BUF = makeAdapter(NetworkUtils::readFriendlyByteBuf, NetworkUtils::writeByteBuf, null);

    public static final IByteBufAdapter<ByteBuf, Integer> INT = makeAdapter(ByteBufCodecs.VAR_INT, null);
    public static final IByteBufAdapter<ByteBuf, Long> LONG = makeAdapter(ByteBufCodecs.VAR_LONG, null);
    public static final IByteBufAdapter<ByteBuf, Float> FLOAT = makeAdapter(ByteBufCodecs.FLOAT, null);
    public static final IByteBufAdapter<ByteBuf, Double> DOUBLE = makeAdapter(ByteBufCodecs.DOUBLE, null);
    public static final IByteBufAdapter<ByteBuf, Boolean> BOOL = makeAdapter(ByteBufCodecs.BOOL, null);
    public static final IByteBufAdapter<ByteBuf, Byte> BYTE = makeAdapter(ByteBufCodecs.BYTE, null);
    public static final IByteBufAdapter<ByteBuf, Short> SHORT = makeAdapter(ByteBufCodecs.SHORT, null);
    public static final IByteBufAdapter<ByteBuf, Character> CHAR = makeAdapter(ByteBuf::readChar, (buf, c) -> buf.writeChar(c), null);

    public static final IByteBufAdapter<ByteBuf, BlockState> BLOCKSTATE = makeAdapterFromCodec(BlockState.CODEC, BlockState::equals);
    public static final IByteBufAdapter<ByteBuf, BlockPos> BLOCKPOS = makeAdapter(BlockPos.STREAM_CODEC, BlockPos::equals);
    public static final IByteBufAdapter<ByteBuf, GlobalPos> GLOBAL_POS = makeAdapter(GlobalPos.STREAM_CODEC, GlobalPos::equals);
    public static final IByteBufAdapter<ByteBuf, Identifier> RESOURCE_LOCATION = makeAdapter(Identifier.STREAM_CODEC, Identifier::equals);
    public static final IByteBufAdapter<ByteBuf, UUID> UUID = makeAdapter(UUIDUtil.STREAM_CODEC, java.util.UUID::equals);
    public static final IByteBufAdapter<RegistryFriendlyByteBuf, Component> COMPONENT = makeAdapter(ComponentSerialization.STREAM_CODEC, Component::equals);
    // @formatter:on

    public static final IByteBufAdapter<ByteBuf, byte[]> BYTE_ARR = makeAdapter(ByteBufCodecs.BYTE_ARRAY, (t1, t2) -> {
        if (t1.length != t2.length) return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i]) return false;
        }
        return true;
    });

    public static final IByteBufAdapter<ByteBuf, long[]> LONG_ARR = new IByteBufAdapter<>() {

        @Override
        public long @NotNull [] decode(@NotNull ByteBuf buffer) {
            int length = VarInt.read(buffer);
            long[] array = new long[length];
            for (int i = 0; i < length; i++) {
                array[i] = buffer.readLong();
            }
            return array;
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, long @NotNull [] u) {
            VarInt.write(buffer, u.length);
            for (long i : u) {
                buffer.writeLong(i);
            }
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

    public static final IByteBufAdapter<ByteBuf, BigInteger> BIG_INT = new IByteBufAdapter<>() {

        @Override
        public @NotNull BigInteger decode(@NotNull ByteBuf buffer) {
            return new BigInteger(FriendlyByteBuf.readByteArray(buffer));
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull BigInteger u) {
            FriendlyByteBuf.writeByteArray(buffer, u.toByteArray());
        }

        @Override
        public boolean areEqual(@NotNull BigInteger t1, @NotNull BigInteger t2) {
            return t1.equals(t2);
        }
    };

    public static final IByteBufAdapter<ByteBuf, BigDecimal> BIG_DECIMAL = new IByteBufAdapter<>() {

        @Override
        public @NotNull BigDecimal decode(@NotNull ByteBuf buffer) {
            return new BigDecimal(BIG_INT.decode(buffer), VarInt.read(buffer));
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull BigDecimal u) {
            BIG_INT.encode(buffer, u.unscaledValue());
            VarInt.write(buffer, u.scale());
        }

        @Override
        public boolean areEqual(@NotNull BigDecimal t1, @NotNull BigDecimal t2) {
            return t1.equals(t2);
        }
    };

    public static <B, V> IByteBufAdapter<B, V> makeAdapter(@NotNull StreamDecoder<B, V> decoder,
                                                           @NotNull StreamEncoder<B, V> encoder,
                                                           @Nullable EqualityTest<V> comparator) {
        final EqualityTest<V> tester = comparator != null ? comparator : EqualityTest.defaultTester();
        return new IByteBufAdapter<>() {

            @Override
            public @NotNull V decode(@NotNull B buffer) {
                return decoder.decode(buffer);
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull V u) {
                encoder.encode(buffer, u);
            }

            @Override
            public boolean areEqual(@NotNull V v1, @NotNull V v2) {
                return tester.areEqual(v1, v2);
            }
        };
    }

    public static <B, V> IByteBufAdapter<B, V> makeAdapter(@NotNull StreamCodec<B, V> codec,
                                                           @Nullable EqualityTest<V> comparator) {
        return makeAdapter(codec, codec, comparator);
    }

    public static <B, V> IByteBufAdapter<B, V> makeMemberAdapter(@NotNull StreamDecoder<B, V> decoder,
                                                                 @NotNull StreamMemberEncoder<B, V> memberEncoder,
                                                                 @Nullable EqualityTest<V> comparator) {
        return makeAdapter(decoder, (buffer, value) -> memberEncoder.encode(value, buffer), comparator);
    }

    public static <B extends ByteBuf, V> IByteBufAdapter<B, V> makeAdapterFromCodec(@NotNull Codec<V> codec, @NotNull EqualityTest<V> equals) {
        return new IByteBufAdapter<>() {

            private static final Gson GSON = new Gson();

            @Override
            public V decode(B buffer) {
                JsonElement jsonelement = GsonHelper.fromJson(GSON, Utf8String.read(buffer, FriendlyByteBuf.MAX_STRING_LENGTH), JsonElement.class);
                DataResult<V> dataresult = codec.parse(JsonOps.COMPRESSED, jsonelement);
                return dataresult.getOrThrow(p_272382_ -> new DecoderException("Failed to decode json: " + p_272382_));
            }

            @Override
            public void encode(B buffer, V u) {
                DataResult<JsonElement> dataresult = codec.encodeStart(JsonOps.COMPRESSED, u);
                Utf8String.write(buffer, GSON.toJson(dataresult.getOrThrow(e -> new EncoderException("Failed to encode json: " + e + " " + u))), FriendlyByteBuf.MAX_STRING_LENGTH);
            }

            @Override
            public boolean areEqual(@NotNull V v1, @NotNull V v2) {
                return equals.areEqual(v1, v2);
            }
        };
    }
}
