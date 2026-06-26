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
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("deprecation")
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeSerializerTest {

    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void serializeTest(GameTestHelper helper) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());

        // Create Fluid Condition based on fluidSetIn
        TagKey<Fluid> lavaTag = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "lava"));
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder(),
                Fluids.FLOWING_WATER.builtInRegistryHolder());
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(lavaTag);
        List<HolderSet<Fluid>> fluidSetIn = List.of(waterSet, lavaSet);
        AdjacentFluidCondition fluidCondition = new AdjacentFluidCondition(fluidSetIn);

        // Create Block Condition based on blockSetIn
        TagKey<Block> oreTag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));
        HolderSet<Block> blockSet = HolderSet.direct(Blocks.DIAMOND_BLOCK.builtInRegistryHolder(),
                Blocks.GOLD_BLOCK.builtInRegistryHolder());
        HolderSet<Block> oreSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.BLOCK)
                .getOrCreateTag(oreTag);
        List<HolderSet<Block>> blockSetIn = List.of(blockSet, oreSet);
        AdjacentBlockCondition blockCondition = new AdjacentBlockCondition(blockSetIn);

        // Serialize and back
        GTRecipe originalRecipe = GTRecipeBuilder.ofRaw()
                .addCondition(fluidCondition)
                .addCondition(blockCondition)
                .build();
        JsonElement recipeJson = Recipe.CODEC.encodeStart(ops, originalRecipe)
                .getOrThrow(GameTestAssertException::new);

        Recipe<?> parsedRecipe = Recipe.CODEC.parse(ops, recipeJson)
                .getOrThrow(GameTestAssertException::new);
        if (!(parsedRecipe instanceof GTRecipe recipe)) {
            helper.fail("Expected recipe to deserialize back to itself, but it didn't. Got %s instead"
                    .formatted(parsedRecipe));
            return;
        }

        // Validate
        boolean foundFluid = false, foundBlock = false;
        for (var condition : recipe.conditions) {
            if (condition instanceof AdjacentBlockCondition recipeBlockCondition) {
                foundBlock = true;
                helper.assertTrue(equalHolderSetLists(recipeBlockCondition.getOrInitBlocks(null), blockSetIn),
                        "AdjacentBlockCondition did not deserialize properly");
            } else if (condition instanceof AdjacentFluidCondition recipeFluidCondition) {
                foundFluid = true;
                helper.assertTrue(equalHolderSetLists(recipeFluidCondition.getOrInitFluids(null), fluidSetIn),
                        "AdjacentFluidCondition did not deserialize properly");
            } else {
                helper.fail("Found condition that should not be present: " + condition);
            }
        }
        if (!foundBlock) {
            helper.fail("AdjacentBlockCondition did not deserialize properly");
        }
        if (!foundFluid) {
            helper.fail("AdjacentFluidCondition did not deserialize properly");
        }
        helper.succeed();
    }

    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void testSerializingFluidCondition(GameTestHelper helper) {
        TagKey<Fluid> lavaTag = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "lava"));
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder(),
                Fluids.FLOWING_WATER.builtInRegistryHolder());
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(lavaTag);
        List<HolderSet<Fluid>> fluidSetIn = List.of(waterSet, lavaSet);
        AdjacentFluidCondition condition = new AdjacentFluidCondition(fluidSetIn);

        helper.assertTrue(equalHolderSetLists(condition.getOrInitFluids(null), fluidSetIn),
                "AdjacentFluidCondition did not deserialize properly");

        JsonObject jsonConfig = condition.serialize();
        AdjacentFluidCondition newCondition = (AdjacentFluidCondition) AdjacentFluidCondition.deserialize(jsonConfig);

        helper.assertTrue(equalHolderSetLists(newCondition.getOrInitFluids(null), fluidSetIn),
                "AdjacentFluidCondition did not deserialize properly");

        helper.succeed();
    }

    @TestHolder()
    // TODO this should use JUnit
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void testSerializingBlockCondition(GameTestHelper helper) {
        TagKey<Block> oreTag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));
        HolderSet<Block> blockSet = HolderSet.direct(Blocks.DIAMOND_BLOCK.builtInRegistryHolder(),
                Blocks.GOLD_BLOCK.builtInRegistryHolder());
        HolderSet<Block> oreSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.BLOCK)
                .getOrCreateTag(oreTag);
        List<HolderSet<Block>> blockSetIn = List.of(blockSet, oreSet);
        AdjacentBlockCondition condition = new AdjacentBlockCondition(blockSetIn);

        helper.assertTrue(equalHolderSetLists(condition.getOrInitBlocks(null), blockSetIn),
                "AdjacentBlockCondition did not deserialize properly");

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
            TagKey<T> tagA = a.unwrapKey().get();
            TagKey<T> tagB = b.unwrapKey().get();
            return Objects.equals(tagA, tagB);
        }

        // Case 2: both are Direct
        if (a.unwrapKey().isEmpty() && b.unwrapKey().isEmpty()) {
            Set<Holder<T>> setA = new HashSet<>(a.stream().toList());
            Set<Holder<T>> setB = new HashSet<>(b.stream().toList());
            return setA.equals(setB);
        }

        // One is Named, the other is Direct → not equal
        return false;
    }
}
