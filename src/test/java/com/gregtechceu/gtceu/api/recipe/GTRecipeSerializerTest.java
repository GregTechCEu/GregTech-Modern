package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentBlockCondition;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeSerializerTest {

    @GameTest(template = "empty")
    public static void testSerializeAdjacentFluid(GameTestHelper helper) {
        // Create Fluid Condition based on fluidSetIn
        @SuppressWarnings("deprecation")
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder(),
                Fluids.FLOWING_WATER.builtInRegistryHolder());
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(FluidTags.LAVA);
        List<HolderSet<Fluid>> fluidSetIn = List.of(waterSet, lavaSet);
        AdjacentFluidCondition fluidCondition = new AdjacentFluidCondition(fluidSetIn);

        // Serialize and immediately deserialize
        JsonObject json = new JsonObject();
        GTRecipeBuilder.ofRaw().addCondition(fluidCondition).toJson(json);
        GTRecipe recipe = GTRecipeSerializer.SERIALIZER.fromJson(GTCEu.id("test"), json);

        // Validate
        boolean foundFluid = false;
        for (var condition : recipe.conditions) {
            if (condition instanceof AdjacentFluidCondition recipeFluidCondition) {
                foundFluid = true;
                helper.assertTrue(equalHolderSetLists(recipeFluidCondition.getOrInitFluids(null), fluidSetIn),
                        "AdjacentFluidCondition did not deserialize properly");
            } else {
                helper.fail("Found condition that should not be present: " + condition);
            }
        }
        if (!foundFluid) {
            helper.fail("AdjacentFluidCondition did not deserialize properly");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void testSerializeAdjacentBlock(GameTestHelper helper) {
        // Create Block Condition based on blockSetIn
        @SuppressWarnings("deprecation")
        HolderSet<Block> blockSet = HolderSet.direct(Blocks.DIAMOND_BLOCK.builtInRegistryHolder(),
                Blocks.GOLD_BLOCK.builtInRegistryHolder());
        HolderSet<Block> oreSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.BLOCK)
                .getOrCreateTag(Tags.Blocks.ORES);
        List<HolderSet<Block>> blockSetIn = List.of(blockSet, oreSet);
        AdjacentBlockCondition blockCondition = new AdjacentBlockCondition(blockSetIn);

        // Serialize and back
        JsonObject json = new JsonObject();
        GTRecipeBuilder.ofRaw().addCondition(blockCondition).toJson(json);
        GTRecipe recipe = GTRecipeSerializer.SERIALIZER.fromJson(GTCEu.id("test"), json);

        // Validate
        boolean foundBlock = false;
        for (var condition : recipe.conditions) {
            if (condition instanceof AdjacentBlockCondition recipeBlockCondition) {
                foundBlock = true;
                helper.assertTrue(equalHolderSetLists(recipeBlockCondition.getOrInitBlocks(null), blockSetIn),
                        "AdjacentBlockCondition did not deserialize properly");
            } else {
                helper.fail("Found condition that should not be present: " + condition);
            }
        }
        if (!foundBlock) {
            helper.fail("AdjacentBlockCondition did not deserialize properly");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void testSerializingFluidCondition(GameTestHelper helper) {
        @SuppressWarnings("deprecation")
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder(),
                Fluids.FLOWING_WATER.builtInRegistryHolder());
        TagKey<Fluid> lavaTag = FluidTags.LAVA;
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(FluidTags.LAVA);

        List<HolderSet<Fluid>> fluidSetIn = List.of(waterSet, lavaSet);
        AdjacentFluidCondition condition = new AdjacentFluidCondition(fluidSetIn);

        helper.assertTrue(equalHolderSetLists(condition.getOrInitFluids(null), fluidSetIn),
                "AdjacentFluidCondition did not store its data properly");

        JsonObject jsonConfig = condition.serialize();
        AdjacentFluidCondition newCondition = (AdjacentFluidCondition) AdjacentFluidCondition.deserialize(jsonConfig);

        helper.assertTrue(equalHolderSetLists(newCondition.getOrInitFluids(null), fluidSetIn),
                "AdjacentFluidCondition did not deserialize properly");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void testSerializingBlockCondition(GameTestHelper helper) {
        @SuppressWarnings("deprecation")
        HolderSet<Block> blockSet = HolderSet.direct(Blocks.DIAMOND_BLOCK.builtInRegistryHolder(),
                Blocks.GOLD_BLOCK.builtInRegistryHolder());
        HolderSet<Block> oreSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.BLOCK)
                .getOrCreateTag(Tags.Blocks.ORES);
        List<HolderSet<Block>> blockSetIn = List.of(blockSet, oreSet);
        AdjacentBlockCondition condition = new AdjacentBlockCondition(blockSetIn);

        helper.assertTrue(equalHolderSetLists(condition.getOrInitBlocks(null), blockSetIn),
                "AdjacentBlockCondition did not store its data properly");

        JsonObject jsonConfig = condition.serialize();
        AdjacentBlockCondition newCondition = (AdjacentBlockCondition) AdjacentBlockCondition.deserialize(jsonConfig);

        helper.assertTrue(equalHolderSetLists(newCondition.getOrInitBlocks(null), blockSetIn),
                "AdjacentBlockCondition did not deserialize properly");

        helper.succeed();
    }

    public static <T> boolean equalHolderSetLists(List<HolderSet<T>> a, List<HolderSet<T>> b) {
        if (a.size() != b.size()) return false;

        // Convert list B into a mutable set for matching
        Set<HolderSet<T>> unmatched = new HashSet<>(b);

        outer:
        for (HolderSet<T> setA : a) {
            for (HolderSet<T> setB : unmatched) {
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

    private static <T> boolean holderSetEquals(HolderSet<T> a, HolderSet<T> b) {
        // Case 1: both are Named (tags)
        if (a.unwrapKey().isPresent() && b.unwrapKey().isPresent()) {
            // tag keys are interned so they can be compared by reference
            return a.unwrapKey().get() == b.unwrapKey().get();
        }

        // Case 2: both are Direct
        if (a.unwrapKey().isEmpty() && b.unwrapKey().isEmpty()) {
            Set<Holder<T>> setA = a.stream().collect(Collectors.toSet());
            Set<Holder<T>> setB = b.stream().collect(Collectors.toSet());
            return setA.containsAll(setB) && setB.containsAll(setA);
        }

        // One is Named, the other is Direct -> not equal
        // (or they don't have the same elements)
        return false;
    }
}
