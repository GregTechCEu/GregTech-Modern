package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.core.mixins.RecipeManagerAccessor;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RecipeLogicTest {

    @GameTest(setupTicks = 20L)
    public static void recipeLogicTest(GameTestHelper helper) {
        Level world = helper.getLevel();

        BlockEntity holder = helper.getBlockEntity(helper.relativePos(new BlockPos(0, 1, 0)));
        if (!(holder instanceof MetaMachineBlockEntity atte)) return;
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) return;

        GTRecipe recipe = GTRecipeBuilder.ofRaw()
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildRawRecipe();
        // force insert the recipe into the manager.
        ((RecipeManagerAccessor) Platform.getMinecraftServer().getRecipeManager()).getRawRecipes().get(GTRecipeTypes.CHEMICAL_RECIPES).put(GTCEu.id("test"), recipe);

        RecipeLogic arl = rlm.getRecipeLogic();

        arl.searchRecipe();

        // no recipe found
        helper.assertFalse(arl.isActive(), "Recipe logic is active");
        helper.assertTrue(arl.lastRecipe == null, "Recipe logic found a recipe");

        // put an item in the inventory that will trigger recipe recheck
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP).get(0)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        // Inputs change. did we detect it ?
        helper.assertTrue(arl.recipeDirty, "Recipe is not dirty");
        arl.searchRecipe();
        helper.assertFalse(arl.lastRecipe == null, "Last recipe is empty");
        helper.assertFalse(arl.isActive(), "Recipelogic is active.");
        helper.assertTrue(((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP).get(0)).getStackInSlot(0).getCount() == 15, "Count is wrong");

        // Save a reference to the old recipe so we can make sure it's getting reused
        GTRecipe prev = arl.lastRecipe;

        // Finish the recipe, the output should generate, and the next iteration should begin
        arl.serverTick();
        helper.assertTrue(arl.lastRecipe == prev, "lastRecipe is wrong");
        helper.assertTrue(ItemStack.isSameItem(((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), "wrong output stack.");
        helper.assertTrue(arl.isActive(), "RecipeLogic is not active.");

        // Complete the second iteration, but the machine stops because its output is now full
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        arl.serverTick();
        helper.assertFalse(arl.isActive(), "RecipeLogic is active.");

        // Try to process again and get failed out because of full buffer.
        arl.serverTick();
        helper.assertFalse(arl.isActive(), "Recipelogic is active.");

        // Some room is freed in the output bus, so we can continue now.
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).setStackInSlot(1, ItemStack.EMPTY);
        arl.serverTick();
        helper.assertTrue(arl.isActive(), "Recipelogic is inactive.");
        helper.assertTrue(ItemStack.isSameItem(((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).getStackInSlot(0), new ItemStack(Blocks.STONE, 1)), "Wrong stack.");
    }
}
