package com.gregtechceu.gtceu.test.api.machine.trait;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ParallelLogicTest {

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
        GTRecipe recipe = GTRecipeBuilder.ofRaw()
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .inputFluids(GTMaterials.Acetone.getFluid(4000))
                .outputItems(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .buildRawRecipe();

        ((IItemTransfer) rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0,
                new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidHandler) rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP))
                .fill(GTMaterials.Acetone.getFluid(8000), IFluidHandler.FluidAction.EXECUTE);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled.getSecond() == 2,
                "Expected Parallel amount to be 2, is %s.".formatted(paralleled.getSecond()));

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
        GTRecipe recipe = GTRecipeBuilder.ofRaw()
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .inputFluids(GTMaterials.Acetone.getFluid(1000))
                .outputItems(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .buildRawRecipe();

        ((IItemTransfer) rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0,
                new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidHandler) rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP))
                .fill(GTMaterials.Acetone.getFluid(8000), IFluidHandler.FluidAction.EXECUTE);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getSecond() == 0,
                "Parallel is too high, should be 0, is %s.".formatted(paralleled.getSecond()));

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
        GTRecipe recipe = GTRecipeBuilder.ofRaw()
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .inputFluids(GTMaterials.Acetone.getFluid(100))
                .outputItems(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .buildRawRecipe();

        ((IItemTransfer) rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP)).insertItem(0,
                new ItemStack(Blocks.COBBLESTONE, 16), false);
        ((IFluidHandler) rlm.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP))
                .fill(GTMaterials.Naphtha.getFluid(8000), IFluidHandler.FluidAction.EXECUTE);

        var paralleled = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);

        helper.assertTrue(paralleled == null || paralleled.getSecond() == 0,
                "Parallel is too high, should be 0, is %s.".formatted(paralleled.getSecond()));

        helper.succeed();
    }

    // TODO add the rest of
    // https://github.com/GregTechCEu/GregTech/blob/master/src/test/java/gregtech/api/recipes/logic/ParallelLogicTest.java.
}
