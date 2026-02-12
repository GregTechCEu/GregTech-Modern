package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class NetworkUtils {

    public static final Consumer<FriendlyByteBuf> EMPTY_PACKET = buffer -> {};

    public static boolean isClient(Player player) {
        if (player == null) return GTCEu.isClientThread();
        return player.level().isClientSide;
    }

    public static void writeByteBuf(FriendlyByteBuf writeTo, ByteBuf writeFrom) {
        writeTo.writeVarInt(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom.slice());
    }

    public static ByteBuf readByteBuf(FriendlyByteBuf buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        return Unpooled.copiedBuffer(directSliceBuffer);
    }

    public static FriendlyByteBuf readFriendlyByteBuf(FriendlyByteBuf buf) {
        return new FriendlyByteBuf(readByteBuf(buf));
    }

    public static void writeStringSafe(FriendlyByteBuf buffer, String string) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, false);
    }

    public static void writeStringSafe(FriendlyByteBuf buffer, @Nullable String string, boolean crash) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, crash);
    }

    public static void writeStringSafe(FriendlyByteBuf buffer, @Nullable String string, int maxBytes) {
        writeStringSafe(buffer, string, maxBytes, false);
    }

    private static final int MAX_ENCODED = getMaxEncodedUtfLength(Short.MAX_VALUE);

    public static void writeStringSafe(FriendlyByteBuf buffer, @Nullable String string, int maxBytes, boolean crash) {
        if (string == null) {
            buffer.writeVarInt(MAX_ENCODED + 1);
            return;
        }
        if (string.isEmpty()) {
            buffer.writeVarInt(0);
            return;
        }
        maxBytes = Math.min(maxBytes, Short.MAX_VALUE);
        byte[] bytesTest = string.getBytes(StandardCharsets.UTF_8);
        byte[] bytes;

        int maxEncoded = getMaxEncodedUtfLength(maxBytes);
        if (bytesTest.length > maxEncoded) {
            if (crash) {
                throw new IllegalArgumentException("Max String size is " + maxEncoded + ", but found " +
                        bytesTest.length + " bytes for '" + string + "'!");
            }
            bytes = new byte[maxEncoded];
            System.arraycopy(bytesTest, 0, bytes, 0, maxEncoded);
            GTCEu.LOGGER.warn("Warning! Synced string exceeds max length!");
        } else {
            bytes = bytesTest;
        }
        buffer.writeVarInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    public static String readStringSafe(FriendlyByteBuf buffer) {
        int length = buffer.readVarInt();
        if (length > MAX_ENCODED) return null;
        if (length == 0) return "";
        String s = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        buffer.readerIndex(buffer.readerIndex() + length);
        return s;
    }

    private static int getMaxEncodedUtfLength(int maxLength) {
        return maxLength * 3;
    }
}
