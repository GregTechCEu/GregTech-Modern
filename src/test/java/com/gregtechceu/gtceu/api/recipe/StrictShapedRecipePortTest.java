package com.gregtechceu.gtceu.api.recipe;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StrictShapedRecipePortTest {
    @org.junit.jupiter.api.BeforeAll
    static void bindDefaultComponents() {
        // Isolated fixture: the unit loader does not perform a server's component reload.
        // These tests only need ordinary stackable items, not their data-pack properties.
        var components = net.minecraft.core.component.DataComponentMap.builder()
                .set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 64).build();
        for (var item : List.of(Items.STONE, Items.DIRT, Items.STICK, Items.DIAMOND)) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(components);
            }
        }
    }

    private static StrictShapedRecipe recipe(boolean matchSize, String... rows) {
        return new StrictShapedRecipe(new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "test"),
                new ShapedRecipePattern.Data(Map.of('A', Ingredient.of(Items.STONE),
                        'B', Ingredient.of(Items.DIRT)), List.of(rows)),
                new ItemStackTemplate(Items.DIAMOND, 2), matchSize);
    }

    private static CraftingInput grid(int width, int height, int stoneSlot, int dirtSlot) {
        var items = new ArrayList<ItemStack>();
        for (int i = 0; i < width * height; i++) items.add(ItemStack.EMPTY);
        items.set(stoneSlot, new ItemStack(Items.STONE));
        items.set(dirtSlot, new ItemStack(Items.DIRT));
        return CraftingInput.of(width, height, items);
    }

    @Test void originalGridSurvivesTrimming() {
        var input = grid(3, 3, 7, 8);
        assertEquals(2, input.width());
        assertEquals(1, input.height());
        var access = assertInstanceOf(CraftingGridOrigin.Access.class, input);
        assertEquals(new CraftingGridOrigin(3, 3, 1, 2), access.gtceu$getGridOrigin());
        assertNull(((CraftingGridOrigin.Access) CraftingInput.EMPTY).gtceu$getGridOrigin());
    }

    @Test void matchesOffsetsButNeverMirrors() {
        var recipe = recipe(false, "AB");
        assertTrue(recipe.matches(grid(3, 3, 7, 8), null));
        assertFalse(recipe.matches(grid(3, 3, 8, 7), null));
    }

    @Test void explicitEmptyBordersAndExactSizeArePreserved() {
        var padded = recipe(false, "   ", " AB", "   ");
        assertEquals(3, padded.getWidth());
        assertEquals(3, padded.getHeight());
        assertTrue(padded.matches(grid(3, 3, 4, 5), null));
        assertFalse(padded.matches(grid(3, 3, 1, 2), null));
        assertFalse(padded.matches(grid(2, 2, 2, 3), null));
        var exact = recipe(true, "AB");
        assertTrue(exact.matches(grid(2, 1, 0, 1), null));
        assertFalse(exact.matches(grid(3, 3, 7, 8), null));
    }

    @Test void extraItemsAreRejectedAndResultsAreIndependent() {
        var recipe = recipe(false, "AB");
        var input = grid(3, 3, 0, 1);
        var items = new ArrayList<>(java.util.Collections.nCopies(9, ItemStack.EMPTY));
        items.set(0, new ItemStack(Items.STONE));
        items.set(1, new ItemStack(Items.DIRT));
        items.set(8, new ItemStack(Items.STICK));
        assertFalse(recipe.matches(CraftingInput.of(3, 3, items), null));
        recipe.assemble(input).setCount(1);
        assertEquals(2, recipe.assemble(input).getCount());
        assertSame(StrictShapedRecipe.SERIALIZER, recipe.getSerializer());
    }

    @Test void jsonAndNetworkPreservePaddingAndFlags() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        var original = recipe(true, "   ", " AB", "   ");
        var json = StrictShapedRecipe.MAP_CODEC.codec().encodeStart(ops, original).getOrThrow();
        var decoded = StrictShapedRecipe.MAP_CODEC.codec().parse(ops, json).getOrThrow();
        assertTrue(decoded.isMatchSize());
        assertTrue(decoded.matches(grid(3, 3, 4, 5), null));
        assertFalse(decoded.matches(grid(3, 3, 1, 2), null));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            StrictShapedRecipe.STREAM_CODEC.encode(buffer, original);
            var network = StrictShapedRecipe.STREAM_CODEC.decode(buffer);
            assertEquals(json, StrictShapedRecipe.MAP_CODEC.codec().encodeStart(ops, network).getOrThrow());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
        var invalid = JsonParser.parseString("{\"key\":{},\"pattern\":[\"   \"],\"result\":\"minecraft:stone\"}");
        assertTrue(StrictShapedRecipe.MAP_CODEC.codec().parse(ops, invalid).error().isPresent());
    }
}
