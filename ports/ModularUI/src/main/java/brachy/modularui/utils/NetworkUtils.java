package brachy.modularui.utils;

import brachy.modularui.ModularUI;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.world.entity.player.Player;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class NetworkUtils {

    public static final Consumer<ByteBuf> EMPTY_PACKET = buffer -> {};
    private static final int MAX_ENCODED = getMaxEncodedUtfLength(Short.MAX_VALUE);

    public static boolean isClient(Player player) {
        if (player == null) return ModularUI.isClientThread();
        return player.level().isClientSide();
    }

    public static void writeByteBuf(ByteBuf writeTo, ByteBuf writeFrom) {
        writeFrom.readerIndex(0);
        VarInt.write(writeTo, writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static void writeRemainingByteBuf(ByteBuf writeTo, ByteBuf writeFrom) {
        VarInt.write(writeTo, writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom.slice());
    }

    public static ByteBuf readByteBuf(ByteBuf buf) {
        return buf.readBytes(VarInt.read(buf));
    }

    public static FriendlyByteBuf readFriendlyByteBuf(ByteBuf buf) {
        return new FriendlyByteBuf(readByteBuf(buf));
    }

    public static void writeStringSafe(ByteBuf buffer, String string) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, false);
    }

    public static void writeStringSafe(ByteBuf buffer, @Nullable String string, boolean crash) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, crash);
    }

    public static void writeStringSafe(ByteBuf buffer, @Nullable String string, int maxBytes) {
        writeStringSafe(buffer, string, maxBytes, false);
    }

    public static void writeStringSafe(ByteBuf buffer, @Nullable String string, int maxBytes, boolean crash) {
        if (string == null) {
            VarInt.write(buffer, MAX_ENCODED + 1);
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
            ModularUI.LOGGER.warn("Warning! Synced string exceeds max length!");
        } else {
            bytes = bytesTest;
        }
        VarInt.write(buffer, bytes.length);
        buffer.writeBytes(bytes);
    }

    public static String readStringSafe(ByteBuf buffer) {
        int length = VarInt.read(buffer);
        if (length > MAX_ENCODED) {
            return null;
        }
        String s = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        buffer.readerIndex(buffer.readerIndex() + length);
        return s;
    }

    private static int getMaxEncodedUtfLength(int maxLength) {
        return maxLength * 3;
    }
}
