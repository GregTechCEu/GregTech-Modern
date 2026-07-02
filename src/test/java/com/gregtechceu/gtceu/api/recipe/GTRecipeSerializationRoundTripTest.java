package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeSerializationRoundTripTest {

    @GameTest(template = "empty", batch = "GTRecipeSerializationRoundTrip")
    public static void contentListMapJsonRoundTrip(GameTestHelper helper) {
        ContentListMap map = createContentListMap();
        ContentListMap roundTripped = ContentListMap.CODEC.parse(JsonOps.INSTANCE, contentJson(map))
                .getOrThrow(false, GTCEu.LOGGER::error);

        assertContentJsonEquals(helper, map, roundTripped, "ContentListMap JSON round-trip changed content");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "GTRecipeSerializationRoundTrip")
    public static void contentListMapNbtRoundTrip(GameTestHelper helper) {
        ContentListMap map = createContentListMap();
        ContentListMap roundTripped = ContentListMap.CODEC.parse(NbtOps.INSTANCE, contentNbt(map))
                .getOrThrow(false, GTCEu.LOGGER::error);

        assertContentNbtEquals(helper, map, roundTripped, "ContentListMap NBT round-trip changed content");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "GTRecipeSerializationRoundTrip")
    public static void contentListMapNetworkRoundTrip(GameTestHelper helper) {
        ContentListMap map = createContentListMap();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        map.toNetwork(buf);
        ContentListMap roundTripped = ContentListMap.fromNetwork(buf);

        assertContentJsonEquals(helper, map, roundTripped, "ContentListMap network round-trip changed content");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "GTRecipeSerializationRoundTrip")
    public static void recipeDefinitionJsonRoundTrip(GameTestHelper helper) {
        GTRecipeDefinition recipe = createRecipeDefinition();
        JsonObject json = GTRecipeSerializer.SERIALIZER.toJson(recipe);

        GTRecipeDefinition roundTripped = GTRecipeSerializer.SERIALIZER.fromJson(recipe.id, json);

        assertRecipeDefinitionJsonEquals(helper, recipe, roundTripped,
                "GTRecipeDefinition JSON round-trip changed recipe");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "GTRecipeSerializationRoundTrip")
    public static void recipeDefinitionNetworkRoundTrip(GameTestHelper helper) {
        GTRecipeDefinition recipe = createRecipeDefinition();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        GTRecipeSerializer.SERIALIZER.toNetwork(buf, recipe);
        GTRecipeDefinition roundTripped = GTRecipeSerializer.SERIALIZER.fromNetwork(recipe.id, buf);

        assertRecipeDefinitionJsonEquals(helper, recipe, roundTripped,
                "GTRecipeDefinition network round-trip changed recipe");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "GTRecipeSerializationRoundTrip")
    public static void runtimeRecipeNbtRoundTrip(GameTestHelper helper) {
        GTRecipe recipe = createRuntimeRecipe();

        GTRecipe roundTripped = GTRecipe.fromNBT(recipe.toNBT());

        assertRuntimeRecipeNbtEquals(helper, recipe, roundTripped, "GTRecipe NBT round-trip changed recipe");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "GTRecipeSerializationRoundTrip")
    public static void runtimeRecipeNetworkRoundTrip(GameTestHelper helper) {
        GTRecipe recipe = createRuntimeRecipe();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        recipe.toNetwork(buf);
        GTRecipe roundTripped = GTRecipe.fromNetwork(buf);

        assertRuntimeRecipeNbtEquals(helper, recipe, roundTripped, "GTRecipe network round-trip changed recipe");
        helper.succeed();
    }

    private static GTRecipeDefinition createRecipeDefinition() {
        CompoundTag data = new CompoundTag();
        data.putString("string", "recipe serialization");
        data.putInt("integer", 42);
        CompoundTag nested = new CompoundTag();
        nested.putBoolean("flag", true);
        data.put("nested", nested);

        return new GTRecipeDefinition(
                GTCEu.id("dummy/serialization_round_trip"),
                GTRecipeTypes.DUMMY_RECIPES,
                GTRecipeTypes.DUMMY_RECIPES.getCategory(),
                createContentListMap(),
                createOutputs(),
                createTickInputs(),
                createTickOutputs(),
                123,
                new ArrayList<>(List.of(AdjacentFluidCondition.fromTags(FluidTags.WATER))),
                data,
                4);
    }

    private static GTRecipe createRuntimeRecipe() {
        GTRecipe recipe = createRecipeDefinition().toRuntime();
        recipe.parallels = 3;
        recipe.subtickParallels = 2;
        recipe.batchParallels = 5;
        recipe.ocLevel = 1;
        return recipe;
    }

    private static ContentListMap createContentListMap() {
        ContentListMap map = new ContentListMap();
        map.put(ItemRecipeCapability.CAP, createItemIngredients());
        map.put(FluidRecipeCapability.CAP, createFluidIngredients());
        map.put(EURecipeCapability.CAP, List.of(120L * 2));
        map.put(CWURecipeCapability.CAP, List.of(48));
        return map;
    }

    private static ContentListMap createOutputs() {
        ContentListMap map = new ContentListMap();
        map.put(ItemRecipeCapability.CAP, List.of(
                ItemIngredient.of(Items.NETHERITE_SCRAP, 2),
                ItemIngredient.of(Items.EMERALD, 1).copyWithChance(6500)));
        map.put(FluidRecipeCapability.CAP, createFluidIngredients());
        return map;
    }

    private static ContentListMap createTickInputs() {
        ContentListMap map = new ContentListMap();
        map.put(EURecipeCapability.CAP, List.of(32L * 4));
        map.put(CWURecipeCapability.CAP, List.of(7));
        return map;
    }

    private static ContentListMap createTickOutputs() {
        ContentListMap map = new ContentListMap();
        map.put(ItemRecipeCapability.CAP, List.of(ItemIngredient.ranged(Items.REDSTONE, 1, 4)));
        map.put(FluidRecipeCapability.CAP, List.of(FluidIngredient.ranged(Fluids.WATER, 25, 75)));
        map.put(EURecipeCapability.CAP, List.of(16L));
        map.put(CWURecipeCapability.CAP, List.of(3));
        return map;
    }

    private static List<ItemIngredient> createItemIngredients() {
        CompoundTag strictNbt = new CompoundTag();
        strictNbt.putString("title", "Serialization");
        strictNbt.putString("author", "GTCEu");
        ItemStack strictStack = new ItemStack(Items.WRITTEN_BOOK, 2);
        strictStack.setTag(strictNbt);

        CompoundTag partialNbt = new CompoundTag();
        partialNbt.putInt("charge", 7);

        ItemStack predicateStack = new ItemStack(Items.CLOCK, 1);

        return List.of(
                ItemIngredient.of(Items.IRON_INGOT, 3),
                ItemIngredient.of(ItemTags.PLANKS, 2),
                ItemIngredient.of(Ingredient.of(Items.COPPER_INGOT, Items.GOLD_INGOT), 4),
                ItemIngredient.of(strictStack),
                ItemIngredient.of(PartialNBTIngredient.of(Items.CHEST, partialNbt), 1),
                ItemIngredient.of(predicateStack, NBTPredicates.eqString("mode", "charged")),
                ItemIngredient.circuit(7),
                ItemIngredient.ranged(Items.REDSTONE, 2, 5),
                ItemIngredient.of(Items.DIAMOND, 1).copyWithChance(2500),
                ItemIngredient.ranged(Items.EMERALD, 1, 3).copyWithChance(5000));
    }

    private static List<FluidIngredient> createFluidIngredients() {
        CompoundTag fluidNbt = new CompoundTag();
        fluidNbt.putString("grade", "test");

        FluidStack rangedNbtFluid = new FluidStack(Fluids.WATER, 1);
        rangedNbtFluid.setTag(fluidNbt.copy());

        return List.of(
                FluidIngredient.of(Fluids.WATER, 1000),
                FluidIngredient.of(Fluids.LAVA, 500, fluidNbt),
                FluidIngredient.of(FluidTags.WATER, 250),
                FluidIngredient.of(FluidTags.LAVA, 125, fluidNbt),
                FluidIngredient.ranged(Fluids.WATER, 100, 300),
                FluidIngredient.of(Fluids.LAVA, 144).copyWithChance(4000),
                FluidIngredient.ranged(rangedNbtFluid, 50, 75).copyWithChance(9000));
    }

    private static JsonElement contentJson(ContentListMap map) {
        return ContentListMap.CODEC.encodeStart(JsonOps.INSTANCE, map)
                .getOrThrow(false, GTCEu.LOGGER::error);
    }

    private static Tag contentNbt(ContentListMap map) {
        return ContentListMap.CODEC.encodeStart(NbtOps.INSTANCE, map)
                .getOrThrow(false, GTCEu.LOGGER::error);
    }

    private static void assertContentJsonEquals(GameTestHelper helper, ContentListMap expected, ContentListMap actual,
                                                String message) {
        JsonElement expectedJson = contentJson(expected);
        JsonElement actualJson = contentJson(actual);
        helper.assertTrue(expectedJson.equals(actualJson),
                "%s. Expected: %s Actual: %s".formatted(message, expectedJson, actualJson));
    }

    private static void assertContentNbtEquals(GameTestHelper helper, ContentListMap expected, ContentListMap actual,
                                               String message) {
        Tag expectedNbt = contentNbt(expected);
        Tag actualNbt = contentNbt(actual);
        helper.assertTrue(expectedNbt.equals(actualNbt),
                "%s. Expected: %s Actual: %s".formatted(message, expectedNbt, actualNbt));
    }

    private static void assertRecipeDefinitionJsonEquals(GameTestHelper helper, GTRecipeDefinition expected,
                                                         GTRecipeDefinition actual, String message) {
        JsonObject expectedJson = GTRecipeSerializer.SERIALIZER.toJson(expected);
        JsonObject actualJson = GTRecipeSerializer.SERIALIZER.toJson(actual);
        helper.assertTrue(expectedJson.equals(actualJson),
                "%s. Expected: %s Actual: %s".formatted(message, expectedJson, actualJson));
    }

    private static void assertRuntimeRecipeNbtEquals(GameTestHelper helper, GTRecipe expected, GTRecipe actual,
                                                     String message) {
        CompoundTag expectedNbt = expected.toNBT();
        CompoundTag actualNbt = actual.toNBT();
        helper.assertTrue(expectedNbt.equals(actualNbt),
                "%s. Expected: %s Actual: %s".formatted(message, expectedNbt, actualNbt));
    }
}
