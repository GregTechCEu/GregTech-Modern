package com.gregtechceu.gtceu.test.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.core.mixins.RecipeManagerAccessor;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;

public class RecipeLogicTest {

    private static boolean hasInjectedRecipe = false;

    @BeforeBatch(batch = GTCEu.MOD_ID)
    public static void replaceRecipeManagerEntries(ServerLevel level) {
        if (hasInjectedRecipe) return;
        var recipes = new HashMap<>(((RecipeManagerAccessor) level.getRecipeManager()).getRawRecipes());
        ((RecipeManagerAccessor)level.getRecipeManager()).setRawRecipes(recipes);
        recipes.replaceAll((k, v) -> new HashMap<>(v));
    }

    @GameTest(template = "gtceu:recipelogic")
    public static void recipeLogicTest(GameTestHelper helper) {
        // oops the BeforeBatch isn't registered.
        RecipeLogicTest.replaceRecipeManagerEntries(helper.getLevel());

        BlockEntity holder = helper.getBlockEntity(new BlockPos(0, 2, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [0,1,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }

        GTRecipe recipe = GTRecipeBuilder.ofRaw()
                .id(GTCEu.id("test"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildRawRecipe();
        // force insert the recipe into the manager.

        if (!hasInjectedRecipe) {
            ((RecipeManagerAccessor) helper.getLevel().getRecipeManager()).getRawRecipes().get(GTRecipeTypes.CHEMICAL_RECIPES).put(GTCEu.id("test"), recipe);
            hasInjectedRecipe = true;
        }

        RecipeLogic arl = rlm.getRecipeLogic();

        arl.findAndHandleRecipe();

        // no recipe found
        helper.assertFalse(arl.isActive(), "Recipe logic is active, even when it shouldn't be");
        helper.assertTrue(arl.getLastRecipe() == null, "Recipe logic has somehow found a recipe, when there should be none");

        // put an item in the inventory that will trigger recipe recheck
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP).get(0)).insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        // Inputs change. did we detect it ?
//        helper.assertTrue(arl.isRecipeDirty(), "Recipe is not dirty");
        arl.findAndHandleRecipe();
        helper.assertFalse(arl.getLastRecipe() == null, "Last recipe is empty, even though recipe logic should've found a recipe.");
        helper.assertTrue(arl.isActive(), "Recipelogic is inactive, when it should be active.");
        int stackCount = ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP).get(0)).getStackInSlot(0).getCount();
        helper.assertTrue(stackCount == 15, "Count is wrong (should be 15, when it's %s".formatted(stackCount));

        // Save a reference to the old recipe so we can make sure it's getting reused
        GTRecipe prev = arl.getLastRecipe();

        // Finish the recipe, the output should generate, and the next iteration should begin
        arl.serverTick();
        helper.assertTrue(arl.getLastRecipe() == prev, "lastRecipe is wrong");
        helper.assertTrue(ItemStack.isSameItem(((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), "wrong output stack.");
        helper.assertTrue(arl.isActive(), "RecipeLogic is not active, when it should be.");

        // Complete the second iteration, but the machine stops because its output is now full
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        arl.serverTick();
        helper.assertFalse(arl.isActive(), "RecipeLogic is active, when it shouldn't be.");

        // Try to process again and get failed out because of full buffer.
        arl.serverTick();
        helper.assertFalse(arl.isActive(), "Recipelogic is active, when it shouldn't be.");

        // Some room is freed in the output bus, so we can continue now.
        ((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).setStackInSlot(1, ItemStack.EMPTY);
        arl.serverTick();
//        helper.assertTrue(arl.isActive(), "Recipelogic is inactive.");
        helper.assertTrue(ItemStack.isSameItem(((IItemTransfer)rlm.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0)).getStackInSlot(0), new ItemStack(Blocks.STONE, 1)), "Wrong stack.");

        // Finish.
        helper.succeed();
    }
}
