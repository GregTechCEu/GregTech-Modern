package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.content.RecipeContentCodec;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeContentCodecPortTest {
    private static <T> RecipeContentCodec<T> transport(Codec<T> codec) {
        return () -> codec;
    }

    @Test void primitiveJsonAndNbtRoundTrips() {
        var transport = transport(Codec.INT);
        for (int value : new int[] {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE}) {
            assertEquals(value, transport.fromJson(transport.toJson(value)));
            assertEquals(value, transport.fromNbt(transport.toNbt(value)));
        }
        assertThrows(IllegalStateException.class, () -> transport.fromNbt(new CompoundTag()));
    }

    @Test void plainNetworkRetainsUtfJsonFraming() {
        var transport = transport(Codec.STRING);
        String value = "Research: \"研究\" \\ payload";
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            transport.toNetwork(buffer, value);
            assertEquals(value, JsonParser.parseString(buffer.readUtf()).getAsString());
            buffer.clear();
            transport.toNetwork(buffer, value);
            assertEquals(value, transport.fromNetwork(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test void registryContextReachesJsonNbtAndNetworkCodecs() {
        RecipeContentCodec<Holder<Item>> transport = transport(RegistryFixedCodec.create(Registries.ITEM));
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var item = Items.STONE.builtInRegistryHolder();
        assertSame(Items.STONE, transport.fromJson(transport.toJson(item, registries), registries).value());
        assertSame(Items.STONE, transport.fromNbt(transport.toNbt(item, registries), registries).value());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            transport.toNetwork(buffer, item);
            assertEquals("minecraft:stone", JsonParser.parseString(buffer.readUtf()).getAsString());
            buffer.clear();
            transport.toNetwork(buffer, item);
            assertSame(Items.STONE, transport.fromNetwork(buffer).value());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test void absentRegistryContextAndUnknownIdsFailClearly() {
        RecipeContentCodec<Holder<Item>> transport = transport(RegistryFixedCodec.create(Registries.ITEM));
        var plain = new FriendlyByteBuf(Unpooled.buffer());
        try {
            assertThrows(EncoderException.class, () -> transport.toNetwork(plain, Items.STONE.builtInRegistryHolder()));
        } finally {
            plain.release();
        }
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var registryBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            registryBuffer.writeUtf("\"minecraft:nonexistent_research_item\"");
            assertThrows(DecoderException.class, () -> transport.fromNetwork(registryBuffer));
        } finally {
            registryBuffer.release();
        }
    }
}
