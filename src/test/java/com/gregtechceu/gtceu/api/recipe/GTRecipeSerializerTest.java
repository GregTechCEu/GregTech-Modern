package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeSerializerTest {

    @GameTest(template = "empty_5x5")
    public static void serializeTest(GameTestHelper helper) {
        // Direct: water
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder());

        // Tag: forge:lava
        TagKey<Fluid> lavaTag = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "lava"));
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(lavaTag);

        List<HolderSet<Fluid>> list = List.of(waterSet, lavaSet);

        var condition = new AdjacentFluidCondition();
        condition.setFluids(list);

        var jsonObject = condition.serialize();

        var back_to_condition = (AdjacentFluidCondition) RecipeCondition.deserialize(jsonObject);

        helper.assertTrue(equalFluidSets(condition.getFluids(), (back_to_condition).getFluids()),
                "Condition did not deserialize properly");
        JsonObject AFConditionJSON = new JsonObject();
        GTRecipeBuilder.ofRaw().addCondition(condition).toJson(AFConditionJSON);

        GTRecipe recipe = GTRecipeSerializer.SERIALIZER.fromJson(GTCEu.id("test"), AFConditionJSON);

        AFConditionJSON.get("config");

        helper.assertTrue(recipe.conditions.stream().anyMatch(x -> x instanceof AdjacentFluidCondition),
                "Fluid recipe condition did not deserialize properly");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5")
    public static void testAdjacentFluidConditionRoundTrip(GameTestHelper helper) {
        // RegistryOps with builtin registry
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());

        // Build a condition with one direct fluid and one tag
        TagKey<Fluid> lavaTag = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "lava"));
        HolderSet<Fluid> waterDirect = HolderSet.direct(Fluids.WATER.builtInRegistryHolder());
        HolderSet<Fluid> lavaTagSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(lavaTag);

        AdjacentFluidCondition original = new AdjacentFluidCondition(List.of(waterDirect, lavaTagSet));

        // Serialize to JSON
        JsonElement json = AdjacentFluidCondition.CODEC.encodeStart(ops, original)
                .getOrThrow(false, System.err::println);

        // Deserialize back
        AdjacentFluidCondition decoded = AdjacentFluidCondition.CODEC.parse(ops, json)
                .getOrThrow(false, System.err::println);

        // Assertions
        helper.assertTrue(decoded.getFluids().size() == 2, "Expected 2 fluid sets");
        helper.assertTrue(
                decoded.getFluids().get(0).contains(Fluids.WATER.builtInRegistryHolder()),
                "First set should contain water");
        helper.assertTrue(
                decoded.getFluids().get(1).unwrapKey().isPresent() &&
                        decoded.getFluids().get(1).unwrapKey().get().equals(lavaTag),
                "Second set should be the forge:lava tag");

        helper.succeed();
    }

    @GameTest(template = "empty_5x5")
    public static void testFluidCodecDirect(GameTestHelper helper) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());

        // Direct fluid: water
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder());
        List<HolderSet<Fluid>> list = List.of(waterSet);

        // Serialize
        JsonElement json = AdjacentFluidCondition.FLUID_CODEC.encodeStart(ops, list)
                .getOrThrow(false, System.err::println);

        // Deserialize
        List<HolderSet<Fluid>> decoded = AdjacentFluidCondition.FLUID_CODEC.parse(ops, json)
                .getOrThrow(false, System.err::println);

        helper.assertTrue(decoded.size() == 1, "Expected 1 fluid set");
        helper.assertTrue(decoded.get(0).contains(Fluids.WATER.builtInRegistryHolder()), "Should contain water");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5")
    public static void testFluidCodecTag(GameTestHelper helper) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());

        // Tag: forge:lava
        TagKey<Fluid> lavaTag = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "lava"));
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(lavaTag);
        List<HolderSet<Fluid>> list = List.of(lavaSet);

        // Serialize
        JsonElement json = AdjacentFluidCondition.FLUID_CODEC.encodeStart(ops, list)
                .getOrThrow(false, System.err::println);

        // Deserialize
        List<HolderSet<Fluid>> decoded = AdjacentFluidCondition.FLUID_CODEC.parse(ops, json)
                .getOrThrow(false, System.err::println);

        helper.assertTrue(decoded.size() == 1, "Expected 1 fluid set");
        helper.assertTrue(decoded.get(0).unwrapKey().isPresent() && decoded.get(0).unwrapKey().get().equals(lavaTag),
                "Should be forge:lava tag");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5")
    public static void testFluidCodecMixed(GameTestHelper helper) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());

        // Direct: water
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder());

        // Tag: forge:lava
        TagKey<Fluid> lavaTag = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "lava"));
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(lavaTag);

        List<HolderSet<Fluid>> list = List.of(waterSet, lavaSet);

        // Serialize
        JsonElement json = AdjacentFluidCondition.FLUID_CODEC.encodeStart(ops, list)
                .getOrThrow(false, System.err::println);

        // Deserialize
        List<HolderSet<Fluid>> decoded = AdjacentFluidCondition.FLUID_CODEC.parse(ops, json)
                .getOrThrow(false, System.err::println);

        helper.assertTrue(decoded.size() == 2, "Expected 2 fluid sets");
        helper.assertTrue(decoded.get(0).contains(Fluids.WATER.builtInRegistryHolder()), "First should be water");
        helper.assertTrue(decoded.get(1).unwrapKey().isPresent() && decoded.get(1).unwrapKey().get().equals(lavaTag),
                "Second should be forge:lava tag");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5")
    public static void testConditionSerializeThenCodecDeserialize(GameTestHelper helper) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());

        // Build a condition with water + forge:lava
        TagKey<Fluid> lavaTag = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "lava"));
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder());
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(lavaTag);

        AdjacentFluidCondition original = new AdjacentFluidCondition(List.of(waterSet, lavaSet));
        original.setReverse(true); // test reverse flag too

        // Serialize using the condition's custom serialize()
        JsonObject json = original.serialize();

        // Now parse the entire object with the CODEC
        AdjacentFluidCondition decoded = AdjacentFluidCondition.CODEC.parse(ops, json)
                .getOrThrow(false, System.err::println);

        // Assertions
        helper.assertTrue(decoded.isReverse(), "Reverse flag should be true");
        helper.assertTrue(decoded.getFluids().size() == 2, "Expected 2 fluid sets");
        helper.assertTrue(decoded.getFluids().get(0).contains(Fluids.WATER.builtInRegistryHolder()),
                "First should be water");
        helper.assertTrue(
                decoded.getFluids().get(1).unwrapKey().isPresent() &&
                        decoded.getFluids().get(1).unwrapKey().get().equals(lavaTag),
                "Second should be forge:lava tag");

        helper.succeed();
    }

    public static boolean equalFluidSets(List<HolderSet<Fluid>> a, List<HolderSet<Fluid>> b) {
        if (a.size() != b.size()) return false;

        // Convert list B into a mutable set for matching
        Set<HolderSet<Fluid>> unmatched = new HashSet<>(b);

        outer:
        for (HolderSet<Fluid> setA : a) {
            for (HolderSet<Fluid> setB : unmatched) {
                if (holderSetEquals(setA, setB)) {
                    unmatched.remove(setB);
                    continue outer;
                }
            }
            // No match found for setA
            return false;
        }

        // All matched
        return unmatched.isEmpty();
    }

    private static boolean holderSetEquals(HolderSet<Fluid> a, HolderSet<Fluid> b) {
        // Case 1: both are Named (tags)
        if (a.unwrapKey().isPresent() && b.unwrapKey().isPresent()) {
            TagKey<Fluid> tagA = a.unwrapKey().get();
            TagKey<Fluid> tagB = b.unwrapKey().get();
            return Objects.equals(tagA, tagB);
        }

        // Case 2: both are Direct
        if (!a.unwrapKey().isPresent() && !b.unwrapKey().isPresent()) {
            Set<Holder<Fluid>> setA = new HashSet<>(a.stream().toList());
            Set<Holder<Fluid>> setB = new HashSet<>(b.stream().toList());
            return setA.equals(setB);
        }

        // One is Named, the other is Direct → not equal
        return false;
    }
}
