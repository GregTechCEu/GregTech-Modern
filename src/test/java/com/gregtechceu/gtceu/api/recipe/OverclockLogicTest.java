package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.LARGE_CHEMICAL_RECIPES;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class OverclockLogicTest {

    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CR_RECIPE_TYPE;

    @BeforeBatch(batch = "OverclockLogic")
    public static void prepare(ServerLevel level) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("overclock_logic_lcr_tests");
        CR_RECIPE_TYPE = TestUtils.createRecipeType("overclock_logic_cr_tests");

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic"))
                .inputItems(new ItemStack(Items.RED_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(20)
                // NBT has a schematic in it with an HV energy input hatch
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_2"))
                .inputItems(new ItemStack(Items.STICK))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.LV])
                .duration(1)
                // NBT has a schematic in it with an HV energy input hatch
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_3"))
                .inputItems(new ItemStack(Items.BROWN_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.EV])
                .duration(1)
                // NBT has a schematic in it with an HV energy input hatch
                .buildRawRecipe());
        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_4"))
                .inputItems(new ItemStack(Items.RED_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(16)
                // NBT has a schematic in it with an HV charged singleblock CR in it
                .buildRawRecipe());
        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_overclock_logic_5"))
                .inputItems(new ItemStack(Items.BROWN_BED))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.MV])
                .duration(16)
                // NBT has a schematic in it with an HV charged singleblock CR in it
                .buildRawRecipe());
    }

    private static MetaMachine getMetaMachine(BlockEntity entity) {
        return ((MetaMachineBlockEntity) entity).getMetaMachine();
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             FluidHatchPartMachine outputHatch1, WorkableMultiblockMachine controller) {}

    /**
     * Retrieves the busses for this specific template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(LCR_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        FluidHatchPartMachine outputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 2, 0)));
        return new BusHolder(inputBus1, inputBus2, outputBus1, outputHatch1, controller);
    }

    private static final Codec<HolderSet<Fluid>> FLUID_SET_CODEC = net.minecraft.core.RegistryCodecs
            .homogeneousList(Registries.FLUID);

    @GameTest(template = "empty_5x5")
    public static void GPT_tests(GameTestHelper helper) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());

        // Create a HolderSet with a direct fluid (water)
        HolderSet<Fluid> directSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder());

        // Serialize to JSON
        JsonElement json = FLUID_SET_CODEC.encodeStart(ops, directSet)
                .getOrThrow(false, System.err::println);

        System.out.println("Serialized direct fluid: " + json);

        // Deserialize back
        HolderSet<Fluid> decoded = FLUID_SET_CODEC.parse(ops, json)
                .getOrThrow(false, System.err::println);

        // Assert round-trip works
        helper.assertTrue(decoded.contains(Fluids.WATER.builtInRegistryHolder()), "a");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5")
    public static void GPT_tests_2(GameTestHelper helper) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());

        // Create a HolderSet with a tag (forge:water)
        TagKey<Fluid> waterTag = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "water"));
        HolderSet<Fluid> tagSet = GTRegistries.builtinRegistry().registryOrThrow(Registries.FLUID)
                .getOrCreateTag(waterTag);

        // Serialize to JSON
        JsonElement json = FLUID_SET_CODEC.encodeStart(ops, tagSet)
                .getOrThrow(false, System.err::println);

        System.out.println("Serialized tag fluid: " + json);

        // Deserialize back
        HolderSet<Fluid> decoded = FLUID_SET_CODEC.parse(ops, json)
                .getOrThrow(false, System.err::println);

        // Assert round-trip works
        helper.assertTrue(decoded.unwrapKey().isPresent() && decoded.unwrapKey().get().equals(waterTag), "b");
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

        System.out.println("Serialized AdjacentFluidCondition: " + json);

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
        System.out.println("Direct fluid JSON: " + json);

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
        System.out.println("Tag fluid JSON: " + json);

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
        System.out.println("Mixed fluid JSON: " + json);

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
        System.out.println("Condition.serialize() JSON: " + json);

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

    // Test for running HV recipe at HV
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic", setupTicks = 40, timeoutTicks = 200)
    public static void overclockLogicOnTierNothingChanges(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.RED_BED));
        // One tick to start, 20 for the recipe to run
        helper.succeedOnTickWhen(21, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    // Test for running LV 1t recipe at HV
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic", setupTicks = 40, timeoutTicks = 200)
    public static void overclockLogicTwoTiersAbove16Parallels(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.STICK, 64));
        // One tick to start, 4 for the recipe to run (16/t from ULV recipe to HV)
        helper.succeedOnTickWhen(5, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE, 64)),
                    "Item didn't craft at the right tick with an on-tier recipe" +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    // Test for running EV recipe at HV
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic", setupTicks = 40, timeoutTicks = 200)
    public static void overclockLogicOverTierNothingHappens(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        helper.failIfEver(() -> {
            helper.assertFalse(
                    busHolder.outputBus1.getInventory().getStackInSlot(0).getItem().equals(Blocks.STONE.asItem()),
                    "Item crafted at one tier over when it shouldn't have");
        });
        helper.succeed();
    }

    // Test for code wise calculating perfect OC
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyPerfectOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-input-separation"))
                .id(GTCEu.id("test-multiblock-input-separation"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.ACACIA_WOOD))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(100)
                .buildRawRecipe();

        GTRecipe newRecipe = OC_PERFECT.applyModifier(busHolder.controller, recipeBeforeModifiers);
        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.duration == (recipeBeforeModifiers.duration / PERFECT_DURATION_FACTOR_INV),
                "Perfect perfect overclock didn't cut recipe time by 4");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() ==
                        (recipeBeforeModifiers.getInputEUt().getTotalEU() * STD_VOLTAGE_FACTOR),
                "Non perfect overclock didn't multiply EU by 4");
        helper.succeed();
    }

    // Test for code wise calculating non-perfect OC
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyNonPerfectOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-npo"))
                .id(GTCEu.id("test-multiblock-overclock-test-npo"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.ACACIA_WOOD))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(100)
                .buildRawRecipe();

        GTRecipe newRecipe = OC_NON_PERFECT.applyModifier(busHolder.controller, recipeBeforeModifiers);
        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.duration == (recipeBeforeModifiers.duration / STD_DURATION_FACTOR_INV),
                "Non perfect overclock didn't cut recipe time by 2");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() ==
                        (recipeBeforeModifiers.getInputEUt().getTotalEU() * STD_VOLTAGE_FACTOR),
                "Non perfect overclock didn't multiply EU by 4");
        helper.succeed();
    }

    // Test for code wise calculating subtick perfect OC
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyPerfectParallelOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-psto"))
                .id(GTCEu.id("test-multiblock-overclock-test-psto"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(1)
                .buildRawRecipe();
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64));

        GTRecipe newRecipe = OC_PERFECT_SUBTICK.applyModifier(busHolder.controller, recipeBeforeModifiers);

        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.parallels == PERFECT_DURATION_FACTOR_INV,
                "Perfect subtick overclock didn't multiply parallels by 4");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() ==
                        (recipeBeforeModifiers.getInputEUt().getTotalEU() * STD_VOLTAGE_FACTOR),
                "Perfect subtick overclock didn't multiply EU by 4");
        helper.succeed();
    }

    // Test for code wise calculating subtick non-perfect OC
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyNonPerfectParallelOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-npsto"))
                .id(GTCEu.id("test-multiblock-overclock-test-npsto"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(1)
                .buildRawRecipe();
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64));

        GTRecipe newRecipe = OC_NON_PERFECT_SUBTICK.applyModifier(busHolder.controller, recipeBeforeModifiers);

        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.parallels == STD_DURATION_FACTOR_INV,
                "Non-Perfect subtick overclock didn't multiply parallels by 2");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() ==
                        (recipeBeforeModifiers.getInputEUt().getTotalEU() * STD_VOLTAGE_FACTOR),
                "Non-Perfect subtick overclock didn't multiply EU by 4");
        helper.succeed();
    }

    // Test for code wise calculating non-subtick non-perfect OC on a 1t recipe
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicApplyNonPerfectNonParallel1tOverclockTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-npsto"))
                .id(GTCEu.id("test-multiblock-overclock-test-npsto"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.MV]).duration(1)
                .buildRawRecipe();
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64));

        GTRecipe newRecipe = OC_NON_PERFECT.applyModifier(busHolder.controller, recipeBeforeModifiers);

        helper.assertTrue(newRecipe != null, "Could not apply overclock to recipe");
        helper.assertTrue(newRecipe.parallels == 1,
                "Non-Perfect Non-subtick overclock overclocked when it shouldn't have");
        helper.assertTrue(
                newRecipe.getInputEUt().getTotalEU() == recipeBeforeModifiers.getInputEUt().getTotalEU(),
                "Non-Perfect Non-subtick overclock at 1t changed EU");
        helper.succeed();
    }

    // Test for code wise calculating an overclock on a recipe that can't be run
    @GameTest(template = "lcr_input_separation", batch = "OverclockLogic")
    public static void overclockLogicEVRecipeHVMachineTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        // An HV LCR can overclock an MV recipe once
        // We pass the controller because it is used to fetch .getMaxVoltageTier() and check input ingredients for
        // parallel
        GTRecipe recipeBeforeModifiers = LARGE_CHEMICAL_RECIPES
                .recipeBuilder(GTCEu.id("test-multiblock-overclock-test-ev-hv"))
                .id(GTCEu.id("test-multiblock-overclock-test-ev-hv"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.EV]).duration(1)
                .buildRawRecipe();
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64));

        GTRecipe newRecipe = OC_NON_PERFECT.applyModifier(busHolder.controller, recipeBeforeModifiers);

        helper.assertTrue(newRecipe == null, "Applied EV overclock to HV recipe when it shouldn't have");

        helper.succeed();
    }

    // Test for charge usage of a singleblock HV chemical reactor running an HV recipe
    @GameTest(template = "singleblock_charged_cr", batch = "OverclockLogic")
    public static void overclockLogicHVPowerTest(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableEnergyContainer energyContainer = (NotifiableEnergyContainer) machine
                .getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        long originalCharge = GTValues.V[GTValues.HV] * 64L;
        helper.assertTrue(energyContainer.getEnergyStored() == originalCharge,
                "Singleblock charged CR NBT changed, machine not fully charged anymore");

        itemIn.setStackInSlot(0, new ItemStack(Items.RED_BED));
        // 1t to turn on, 16t to run the recipe
        helper.succeedOnTickWhen(17, () -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                    itemOut.getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 1)),
                    "Singleblock CR didn't run recipe in correct time");
            long chargeUsed = originalCharge - energyContainer.getEnergyStored();
            long chargeNeeded = GTValues.V[GTValues.HV] * 16L;
            helper.assertTrue(chargeUsed == chargeNeeded,
                    "Recipe didn't consume right amount, instead of " + chargeNeeded + " it used " + chargeUsed);
        });
    }

    // Test for charge usage of a singleblock HV chemical reactor running an MV recipe
    @GameTest(template = "singleblock_charged_cr", batch = "OverclockLogic")
    public static void overclockLogicMVPowerTest(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableEnergyContainer energyContainer = (NotifiableEnergyContainer) machine
                .getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        long originalCharge = GTValues.V[GTValues.HV] * 64L;
        helper.assertTrue(energyContainer.getEnergyStored() == originalCharge,
                "Singleblock charged CR NBT changed, machine not fully charged anymore");

        itemIn.setStackInSlot(0, new ItemStack(Items.BROWN_BED));
        // 1t to turn on, 8t to run the recipe (single overclock)
        helper.succeedOnTickWhen(9, () -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                    itemOut.getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 1)),
                    "Singleblock CR didn't run recipe in correct time");
            long chargeUsed = originalCharge - energyContainer.getEnergyStored();
            // One overclock ups EU/t by STD_VOLTAGE_FACTOR, decreases time by STD_DURATION_FACTOR_INV
            long chargeNeeded = (long) ((GTValues.V[GTValues.MV] * STD_VOLTAGE_FACTOR) *
                    (16L / STD_DURATION_FACTOR_INV));
            helper.assertTrue(chargeUsed == chargeNeeded,
                    "Recipe didn't consume right amount, instead of " + chargeNeeded + " it used " + chargeUsed);
        });
    }
}
