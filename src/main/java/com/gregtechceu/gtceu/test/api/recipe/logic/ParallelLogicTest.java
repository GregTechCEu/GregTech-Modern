package com.gregtechceu.gtceu.test.api.recipe.logic;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

@SuppressWarnings({"DataFlowIssue", "unused"})
public class ParallelLogicTest {

    @GameTest(template = "gtceu:ebf")
    public void getMaxRecipeMultiplier_ItemLimitTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }

        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
            .inputItems(new ItemStack(Blocks.COBBLESTONE))
            .inputFluids(GTMaterials.Acetone.getFluid(100))
            .outputItems(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 3), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Acetone.getFluid(8000), false);

        int itemRatio = Objects.requireNonNull(GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false)).getB();

        helper.assertTrue(itemRatio == 3, "itemRatio is %s, expected 3".formatted(itemRatio));

        helper.succeed();
    }

    @GameTest(template = "gtceu:ebf")
    public void getMaxRecipeMultiplier_FluidLimitTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }

        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .inputFluids(GTMaterials.Acetone.getFluid(4000))
                .outputItems(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Acetone.getFluid(8000), false);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled.getB() == 2,"Expected Parallel amount to be 2, is %s.".formatted(paralleled.getB()));

        helper.succeed();
    }

    @GameTest(template = "gtceu:ebf")
    public void getMaxRecipeMultiplier_LimitFailureTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }

        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
            .inputItems(new ItemStack(Blocks.COBBLESTONE))
            .inputFluids(GTMaterials.Acetone.getFluid(1000))
            .outputItems(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Acetone.getFluid(8000), false);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getB() == 0, "Parallel is too high, should be 0, is %s.".formatted(paralleled.getB()));

        helper.succeed();
    }

    @GameTest(template = "gtceu:ebf")
    public void getMaxRecipeMultiplier_ItemFailureTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }

        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
            .inputItems(new ItemStack(Blocks.COBBLESTONE))
            .inputFluids(GTMaterials.Acetone.getFluid(100))
            .outputItems(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Naphtha.getFluid(8000), false);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getB() == 0, "Parallel is too high, should be 0, is %s.".formatted(paralleled.getB()));

        helper.succeed();
    }

    @GameTest(template = "gtceu:ebf")
    public void getMaxRecipeMultiplier_FluidFailureTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
            .inputItems(new ItemStack(Blocks.COBBLESTONE))
            .inputFluids(GTMaterials.Acetone.getFluid(100))
            .outputItems(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Naphtha.getFluid(8000), false);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getB() == 0, "Parallel is too high, should be 0, is %s.".formatted(paralleled.getB()));

        helper.succeed();
    }

    @GameTest(template = "gtceu:ebf")
    public void limitParallelByItems_MaxParallelTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
            .inputItems(new ItemStack(Blocks.COBBLESTONE))
            .inputFluids(GTMaterials.Acetone.getFluid(100))
            .outputItems(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Acetone.getFluid(8000), false);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getB() == 4, "Wrong parallel value, should be 4, is %s.".formatted(paralleled.getB()));

        helper.succeed();
    }

    @GameTest(template = "gtceu:ebf")
    public void limitParallelByItems_LessThanMaxParallelsTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
            .inputItems(new ItemStack(Blocks.COBBLESTONE))
            .inputFluids(GTMaterials.Acetone.getFluid(100))
            .outputItems(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Acetone.getFluid(8000), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(2, new ItemStack(Blocks.BONE_BLOCK), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(3, new ItemStack(Blocks.STONE, 62), false);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getB() == 2, "Wrong parallel value, should be 2, is %s.".formatted(paralleled.getB()));

        helper.succeed();
    }

    @GameTest(template = "gtceu:ebf")
    public void limitParallelByItems_SplitAcrossStacksTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
            .inputItems(new ItemStack(Blocks.COBBLESTONE))
            .inputFluids(GTMaterials.Acetone.getFluid(100))
            .outputItems(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Acetone.getFluid(8000), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(2, new ItemStack(Blocks.STONE, 62), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(3, new ItemStack(Blocks.STONE, 62), false);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getB() == 4, "Wrong parallel value, should be 4, is %s.".formatted(paralleled.getB()));

        helper.succeed();
    }

    @GameTest(template = "gtceu:ebf")
    public void limitParallelByItems_ItemOutputFullTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        int parallelLimit = 4;

        // Create a simple recipe to be used for testing
        GTRecipe recipe = GTRecipeTypes.BLAST_RECIPES.recipeBuilder("test")
            .inputItems(new ItemStack(Blocks.COBBLESTONE))
            .inputFluids(GTMaterials.Acetone.getFluid(100))
            .outputItems(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .buildRawRecipe();

        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidTransfer)rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)).fill(GTMaterials.Acetone.getFluid(8000), false);

        // Fill the export bus
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(2, new ItemStack(Blocks.BONE_BLOCK), false);
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP)).insertItem(3, new ItemStack(Blocks.BONE_BLOCK), false);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getB() == 0, "Wrong parallel value, should be 0, is %s.".formatted(paralleled.getB()));

        helper.succeed();
    }

    /*
    @Test
    public void limitParallelByFluids_MaxParallelTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);
        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .inputs(new ItemStack(Blocks.COBBLESTONE))
            .fluidOutputs(Materials.Acetone.getFluid(100))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe,
            new OverlayedFluidHandler(exportFluidBus.getExportFluids()), parallelLimit);

        assertThat(itemRatio, is(4));
    }

    @Test
    public void limitParallelByFluids_PartialParallelsTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .inputs(new ItemStack(Blocks.COBBLESTONE))
            .fluidOutputs(Materials.Acetone.getFluid(100))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        exportFluidBus.getExportFluids().fill(Materials.Acetone.getFluid(15800), true);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe,
            new OverlayedFluidHandler(exportFluidBus.getExportFluids()), parallelLimit);

        assertThat(itemRatio, is(2));
    }

    @Test
    public void limitParallelByFluids_FluidOutputFullTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .inputs(new ItemStack(Blocks.COBBLESTONE))
            .fluidOutputs(Materials.Acetone.getFluid(100))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        exportFluidBus.getExportFluids().fill(Materials.Acetone.getFluid(16000), true);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe,
            new OverlayedFluidHandler(exportFluidBus.getExportFluids()), parallelLimit);

        assertThat(itemRatio, is(0));
    }

    @Test
    public void getMaxRatioItem_SameNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .inputs(new ItemStack(Blocks.COBBLESTONE))
            .notConsumable(new ItemStack(Blocks.COBBLESTONE))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        // Test less than maximum limit
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 3), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
            recipe, parallelLimit);

        assertThat(itemRatio, is(2));

        // Test = max limit
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 2), false);

        int secondItemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
            recipe, parallelLimit);

        assertThat(secondItemRatio, is(parallelLimit));

        // Test > max limit
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 2), false);

        int thirdItemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
            recipe, parallelLimit);

        assertThat(thirdItemRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioItem_DifferentNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .inputs(new ItemStack(Blocks.COBBLESTONE))
            .notConsumable(new ItemStack(Blocks.STONE))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 4), false);
        importItemBus.getImportItems().insertItem(1, new ItemStack(Blocks.STONE, 1), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
            recipe, parallelLimit);

        assertThat(itemRatio, is(4));
    }

    @Test
    public void getMaxRatioItem_OnlyNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .notConsumable(new ItemStack(Blocks.STONE))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 1), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
            recipe, parallelLimit);

        assertThat(itemRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioItem_OnlyNonConsumedWithStacksizeTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .notConsumable(new ItemStack(Blocks.STONE, 2))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        // Test Parallel Limit with not enough Non-consumed items
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 1), false);

        int itemRatioFailure = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
            recipe, parallelLimit);

        assertThat(itemRatioFailure, is(0));

        // Test Parallel Limit by Non-consumed item amounts
        // Add one more stone to meet the recipe NC amount
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 1), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
            recipe, parallelLimit);

        assertThat(itemRatio, is(parallelLimit));

        // Test Parallel Limit for > max
        importItemBus.getImportItems().insertItem(1, new ItemStack(Blocks.STONE, 6), false);

        int secondItemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
            recipe, parallelLimit);

        assertThat(secondItemRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_RegularFluidInputsTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .fluidInputs(Materials.Water.getFluid(1000))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(500), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit with > min, < max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2500), true);

        int fluidRatio = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatio, is(3));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_SameNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            1,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .fluidInputs(Materials.Water.getFluid(1000))
            .notConsumable(Materials.Water.getFluid())
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(500), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit with > min, < max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(1501), true);

        int fluidRatio = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatio, is(2));

        // Test Parallel Limit Exactly equal inputs
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2000), true);

        int fluidRatioExact = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatioExact, is(parallelLimit));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_DifferentNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            2,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .fluidInputs(Materials.Water.getFluid(1000))
            .notConsumable(Materials.Acetone.getFluid())
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(1000), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit with > min, < max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(1000), true);
        secondImportFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(1), true);

        IMultipleTankHandler tankHandler = new FluidTankList(false, importFluidBus.getImportFluids().getTankAt(0),
            secondImportFluidBus.getImportFluids().getTankAt(0));

        int fluidRatio = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(tankHandler),
            recipe, parallelLimit);

        assertThat(fluidRatio, is(2));

        // Test Parallel Limit Exactly equal inputs
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2000), true);

        int fluidRatioExact = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(tankHandler),
            recipe, parallelLimit);

        assertThat(fluidRatioExact, is(parallelLimit));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(tankHandler),
            recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_OnlyNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            2,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .notConsumable(Materials.Acetone.getFluid())
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(0), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit Exactly equal inputs
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(4), true);

        int fluidRatioExact = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatioExact, is(parallelLimit));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_OnlyNonConsumedWithStacksizeTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
            3,
            2,
            2,
            1,
            new BlastRecipeBuilder(),
            false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
            .notConsumable(Materials.Acetone.getFluid(1000))
            .outputs(new ItemStack(Blocks.STONE))
            .blastFurnaceTemp(1000)
            .EUt(30).duration(100)
            .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(500), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit Exactly equal inputs
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(500), true);

        int fluidRatioExact = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(fluidRatioExact, is(parallelLimit));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(
            GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
            recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void doParallelRecipes_ExistingEUValueTest() {
        int parallelAmount = 4;

        // Do not specify the EUt or duration to test how they are taken into account
        Recipe maceratorRecipe = RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .input(Blocks.STONE)
            .output(Items.CARROT)
            .build().getResult();

        SimpleMachineMetaTileEntityResizable macerator = MetaTileEntities.registerMetaTileEntity(1,
            new SimpleMachineMetaTileEntityResizable(
                gregtechId("macerator"),
                RecipeMaps.MACERATOR_RECIPES,
                -1,
                4,
                null,
                GTValues.EV));

        macerator.getImportItems().setStackInSlot(0, new ItemStack(Blocks.STONE, 10));

        RecipeBuilder<?> testMaceratorRecipe = doParallelRecipes(maceratorRecipe, RecipeMaps.MACERATOR_RECIPES,
            macerator.getImportItems(),
            macerator.getImportFluids(), macerator.getExportItems(), macerator.getExportFluids(), parallelAmount,
            GTValues.V[GTValues.EV], macerator);

        assertThat(testMaceratorRecipe, notNullValue());

        // 2 is the default EUt value assigned to macerator recipes when not specified
        assertThat(testMaceratorRecipe.getEUt(), is(2 * parallelAmount));

        // 150 is the default duration value assigned to macerator recipes when not specified
        assertThat(testMaceratorRecipe.getDuration(), is(150));
    }
    */
}
