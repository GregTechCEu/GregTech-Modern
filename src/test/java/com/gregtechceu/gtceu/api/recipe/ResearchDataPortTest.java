package com.gregtechceu.gtceu.api.recipe;

import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResearchDataPortTest {
    private static RegistryAccess registries;

    @BeforeAll
    static void setup() {
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        // Minimal stack fixtures; the unit loader does not load server data packs.
        var components = DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build();
        for (var item : List.of(Items.PAPER, Items.DIAMOND)) {
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder().bindComponents(components);
        }
    }

    private static ResearchData data() {
        var stack = new ItemStack(Items.PAPER, 3);
        var custom = new CompoundTag();
        custom.putString("research", "gtceu:example");
        custom.putInt("tier", 4);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
        return new ResearchData(List.of(new ResearchData.ResearchEntry("gtceu:example", stack),
                new ResearchData.ResearchEntry("unicode/研究", new ItemStack(Items.DIAMOND))));
    }

    private static void assertSameEntries(ResearchData expected, ResearchData actual) {
        var left = expected.iterator();
        var right = actual.iterator();
        while (left.hasNext()) {
            assertTrue(right.hasNext());
            var a = left.next();
            var b = right.next();
            assertEquals(a.getResearchId(), b.getResearchId());
            assertTrue(ItemStack.matches(a.getDataItem(), b.getDataItem()),
                    () -> a.getDataItem().getComponentsPatch() + " != " + b.getDataItem().getComponentsPatch());
        }
        assertFalse(right.hasNext());
    }

    @Test void jsonPreservesComponentValuesCountsAndIds() {
        var original = data();
        var encoded = original.toJson(registries);
        assertEquals("gtceu:example", encoded.get(0).getAsJsonObject().get("researchId").getAsString());
        assertTrue(encoded.get(0).getAsJsonObject().has("dataItem"));
        var decoded = ResearchData.fromJson(encoded, registries);
        // Vanilla's JSON/NBT conversion narrows numeric tags (e.g. int 4 becomes byte 4).
        // JSON must retain values; binary networking below must retain exact component types.
        assertEquals(encoded, decoded.toJson(registries));
        var decodedEntry = decoded.iterator().next();
        assertEquals("gtceu:example", decodedEntry.getResearchId());
        assertTrue(decodedEntry.getDataItem().is(Items.PAPER));
        assertEquals(3, decodedEntry.getDataItem().getCount());
        var custom = decodedEntry.getDataItem().get(DataComponents.CUSTOM_DATA).copyTag();
        assertEquals("gtceu:example", custom.getString("research").orElseThrow());
        assertEquals(4, custom.getInt("tier").orElseThrow());
        var entry = original.iterator().next();
        var copy = ResearchData.ResearchEntry.fromJson(entry.toJson(registries), registries);
        assertEquals(entry.toJson(registries), copy.toJson(registries));
        assertNotSame(entry.getDataItem(), copy.getDataItem());
    }

    @Test void networkPreservesComponentsCountsAndIds() {
        var original = data();
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            original.toNetwork(buffer);
            assertSameEntries(original, ResearchData.fromNetwork(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test void emptyListsAndDecodedListsRemainMutable() {
        var entries = new ArrayList<ResearchData.ResearchEntry>();
        entries.add(data().iterator().next());
        var snapshot = new ResearchData(entries);
        entries.clear();
        assertTrue(snapshot.iterator().hasNext());
        var empty = ResearchData.fromJson(new ResearchData().toJson(registries), registries);
        assertFalse(empty.iterator().hasNext());
        empty.add(data().iterator().next());
        assertTrue(empty.iterator().hasNext());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            new ResearchData().toNetwork(buffer);
            assertFalse(ResearchData.fromNetwork(buffer).iterator().hasNext());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test void malformedEntriesFailInsteadOfDroppingResearch() {
        assertThrows(IllegalStateException.class, () -> ResearchData.fromJson(
                JsonParser.parseString("[{\"researchId\":\"missing_item\"}]").getAsJsonArray(), registries));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            buffer.writeVarInt(1);
            buffer.writeUtf("empty_data_item");
            buffer.writeVarInt(0);
            assertThrows(DecoderException.class, () -> ResearchData.fromNetwork(buffer));
        } finally {
            buffer.release();
        }
    }
}
