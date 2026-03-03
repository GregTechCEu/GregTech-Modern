package com.gregtechceu.gtceu.gametest.stresstest;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class RecipeIteratorStressTest {

    private static final boolean DO_RUN_RECIPE_ITERATOR_STRESSTEST = false;

    private static GTRecipeType LCR_RECIPE_TYPE;

    @BeforeBatch(batch = "StressTests")
    public static void prepare(ServerLevel level) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("stress_tests", 3, 3, 3, 3);
        // Force insert the recipe into the manager.
        LCR_RECIPE_TYPE.getAdditionHandler().beginStaging();
        LCR_RECIPE_TYPE.getAdditionHandler().addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_stress_tests"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.ACACIA_WOOD))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                // NBT has a schematic in it with an HV energy input hatch
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getAdditionHandler().completeStaging();
    }

    private static BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) helper.getBlockEntity(new BlockPos(1, 2, 0));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(LCR_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) helper.getBlockEntity(new BlockPos(2, 1, 0));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) helper.getBlockEntity(new BlockPos(2, 2, 0));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) helper.getBlockEntity(new BlockPos(0, 1, 0));
        FluidHatchPartMachine outputHatch1 = (FluidHatchPartMachine) helper.getBlockEntity(new BlockPos(0, 2, 0));
        return new BusHolder(inputBus1, inputBus2, outputBus1, outputHatch1, controller);
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             FluidHatchPartMachine outputHatch1, WorkableMultiblockMachine controller) {}

    @GameTest(template = "empty", batch = "StressTests")
    public static void iteratorStressTest(GameTestHelper helper) {
        if (!DO_RUN_RECIPE_ITERATOR_STRESSTEST) {
            helper.succeed();
            return;
        }
        List<List<AbstractMapIngredient>> list = new ArrayList();
        for (var item : BuiltInRegistries.ITEM) {
            list.add(MapIngredientTypeManager.getFrom(Ingredient.of(item), ItemRecipeCapability.CAP));
        }
        for (var block : BuiltInRegistries.BLOCK) {
            list.add(MapIngredientTypeManager.getFrom(Ingredient.of(block), ItemRecipeCapability.CAP));
        }

        long start = System.nanoTime();

        long currentIterator = 0;
        for (int i = 0; i < 20; i++) {
            RecipeDB.RecipeIterator iterator = new RecipeDB.RecipeIterator(GTRecipeTypes.ASSEMBLER_RECIPES.db(), list,
                    (ignored) -> true);
            while (iterator.hasNext()) {
                var recipe = iterator.next();
                currentIterator++;
            }
        }
        long end = System.nanoTime();
        GTCEu.LOGGER.info("current iterator recipes: " + currentIterator / 100);
        GTCEu.LOGGER.info("Lookup in big tree Took " + (end - start) / 1_000_000.0 + " ms");
        helper.succeed();
    }

    @GameTest(template = "lcr_input_separation", batch = "StressTests")
    public static void iteratorOnMachineStressTest(GameTestHelper helper) {
        if (!DO_RUN_RECIPE_ITERATOR_STRESSTEST) {
            helper.succeed();
            return;
        }

        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        busHolder.inputBus1.getInventory().setStackInSlot(1, new ItemStack(Blocks.ACACIA_WOOD));
        busHolder.controller.recipeLogic.searchRecipe();
        long start = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            busHolder.controller.recipeLogic.findAndHandleRecipe();
            busHolder.controller.recipeLogic.markLastRecipeDirty();
        }
        long end = System.nanoTime();
        GTCEu.LOGGER.info("On machine took " + (end - start) / 1_000_000.0 + " ms");
        helper.succeed();
    }
}
