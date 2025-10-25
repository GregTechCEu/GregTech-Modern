package com.gregtechceu.gtceu.gametest.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTRIC;

public class TestUtils {

    /**
     * Compares two itemstacks' items and amounts
     * DOES NOT CHECK TAGS OR NBT ETC!
     *
     * @return {@code true} if items and amounts are equal
     */
    public static boolean isItemStackEqual(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItem(stack1, stack2) && stack1.getCount() == stack2.getCount();
    }

    /**
     * Compares two itemstacks and a range.
     *
     * @return {@code true} if items are equal, and if stack2's amount is within range.
     */
    public static boolean isItemStackWithinRange(ItemStack stack1, ItemStack stack2, int min, int max) {
        return ItemStack.isSameItem(stack1, stack2) && isItemWithinRange(stack2, min, max);
    }

    /**
     * Compares an int representing an itemstack's size with a number of batches, parallels, and runs.
     * Intended to test if an IntProvider is being rolled correctly for a batch, or if it is returning a single value
     * multiplied.
     * This test can trigger false positives from bad luck and should be run more than once to reduce the odds of bad
     * luck.
     *
     * @return {@code true} if the size is an exact multiple of the total run count. TRUE INDICATES FAILURE.
     */
    public static boolean isStackSizeExactlyEvenMultiple(int size, int batches, int parallels, int runs) {
        return size % (batches * parallels * runs) == 0;
    }

    /**
     * Compares two itemstack[]s' items and amounts
     * Necessary because itemStack does not implement .equals()
     */
    public static boolean areItemStacksEqual(ItemStack[] stack1, ItemStack[] stack2) {
        if (stack1.length != stack2.length)
            return false;

        for (int i = 0; i < stack1.length; i++) {
            if (!isItemStackEqual(stack1[i], stack2[i]))
                return false;
        }
        return true;
    }

    /**
     * Compares two fluidstacks' fluids and amounts
     * DOES NOT CHECK TAGS OR NBT ETC!
     *
     * @return {@code true} if fluids and amounts are equal
     */
    public static boolean isFluidStackEqual(FluidStack stack1, FluidStack stack2) {
        return stack1.isFluidEqual(stack2) && stack1.getAmount() == stack2.getAmount();
    }

    /**
     * Compares two fluidstacks and a range.
     *
     * @return {@code true} if items are equal, and if stack2's amount is within range.
     */
    public static boolean isFluidStackWithinRange(FluidStack stack1, FluidStack stack2, int min, int max) {
        return stack1.isFluidEqual(stack2) && isFluidWithinRange(stack2, min, max);
    }

    /**
     * Compares two fluidstack[]s' fluids and amounts
     * Necessary because fluidStack's implementation of .equals() does not check amounts
     */
    public static boolean areFluidStacksEqual(FluidStack[] stack1, FluidStack[] stack2) {
        if (stack1.length != stack2.length)
            return false;

        for (int i = 0; i < stack1.length; i++) {
            if (!isFluidStackEqual(stack1[i], stack2[i]))
                return false;
        }
        return true;
    }

    /**
     * Compares an ItemStack with a range
     *
     * @return {@code true} if the ItemStack's count is within range
     */
    public static boolean isItemWithinRange(ItemStack stack, int min, int max) {
        return stack.getCount() <= max && stack.getCount() >= min;
    }

    /**
     * Compares a FluidStack with a range
     *
     * @return {@code true} if the FluidStack's amount is within range
     */
    public static boolean isFluidWithinRange(FluidStack stack, int min, int max) {
        return stack.getAmount() <= max && stack.getAmount() >= min;
    }

    /**
     * compares an integer with a range
     *
     * @return {@code true} if the integer count is within range
     */
    public static boolean isCountWithinRange(int stack, int min, int max) {
        return stack <= max && stack >= min;
    }

    /**
     * Forces a structure check on multiblocks after being placed, to avoid having to wait ticks.
     * Ideally this doesn't need to happen, but it seems not doing this makes the multiblock tests flakey
     */
    public static void formMultiblock(MultiblockControllerMachine controller) {
        controller.getPattern().checkPatternAt(controller.getMultiblockState(), false);
        controller.onStructureFormed();
    }

    /**
     * Creates a dummy recipe type that also includes a basic, HV, 1 tick, cobblestone -> stone recipe
     * Requires a {@link GTRecipeType} to inherit I/O counts from
     */
    public static GTRecipeType createRecipeTypeAndInsertRecipe(String name, GTRecipeType original) {
        GTRecipeType type = createRecipeType(name, original);
        type.getLookup().addRecipe(type
                .recipeBuilder(GTCEu.id("test_recipe"))
                .inputItems(new ItemStack(Items.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(1).buildRawRecipe());
        return type;
    }

    /**
     * Creates a dummy recipe type. Safe for use in recipe lookup.
     * DO NOT USE THIS FOR MACHINE RECIPES. Use {@link #createRecipeType(String, GTRecipeType)} for that.
     */
    @Deprecated
    public static GTRecipeType createRecipeType(String name) {
        return createRecipeType(name, 2, 2, 2, 2);
    }

    /**
     * Creates a recipe type for writing test cases.
     * Requires a {@link GTRecipeType} to inherit I/O counts from.
     */
    public static GTRecipeType createRecipeType(String name, GTRecipeType original) {
        return createRecipeType(name,
                original.getMaxInputs(ItemRecipeCapability.CAP),
                original.getMaxOutputs(ItemRecipeCapability.CAP),
                original.getMaxInputs(FluidRecipeCapability.CAP),
                original.getMaxOutputs(FluidRecipeCapability.CAP));
    }

    /**
     * Creates a recipe type for writing test cases.
     * Requires setting I/O counts manually.
     * You probably want to be using {@link #createRecipeType(String, GTRecipeType)}
     */
    public static GTRecipeType createRecipeType(String name, int maxInputs, int maxOutputs, int maxFluidInputs,
                                                int maxFluidOutputs) {
        GTRegistries.RECIPE_TYPES.unfreeze();
        GTRegistries.RECIPE_CATEGORIES.unfreeze();
        GTRecipeType type = new GTRecipeType(GTCEu.id(name), ELECTRIC, RecipeType.SMELTING)
                .setEUIO(IO.IN)
                .setMaxIOSize(maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs);

        GTRegistries.RECIPE_CATEGORIES.freeze();
        GTRegistries.RECIPE_TYPES.freeze();
        return type;
    }

    public static CoverBehavior placeCover(GameTestHelper helper, MetaMachine machine, ItemStack stack,
                                           Direction direction) {
        return placeCover(helper, machine, stack, direction, false);
    }

    public static CoverBehavior placeCover(GameTestHelper helper, MetaMachine machine, ItemStack stack,
                                           Direction direction, boolean shouldFail) {
        CoverDefinition coverDefinition = null;
        if (stack.getItem() instanceof IComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof CoverPlaceBehavior coverPlaceBehavior) {
                    helper.assertTrue(coverDefinition == null, "stack has multiple coverPlaceBehaviours");
                    coverDefinition = coverPlaceBehavior.coverDefinition();
                }
            }
        }
        helper.assertTrue(coverDefinition != null, "attempted to place cover with item that is not a cover");
        assert coverDefinition != null;
        helper.assertTrue(shouldFail ^ machine.getCoverContainer().placeCoverOnSide(
                direction, stack, coverDefinition, null), "failed to place cover");
        return machine.getCoverContainer().getCoverAtSide(direction);
    }

    public static MetaMachine setMachine(GameTestHelper helper, BlockPos pos, MachineDefinition machineDefinition) {
        helper.setBlock(pos, machineDefinition.getBlock());
        return ((IMachineBlockEntity) Objects.requireNonNull(helper.getBlockEntity(pos))).getMetaMachine();
    }

    public static void assertEqual(GameTestHelper helper, List<MutableComponent> text, String s) {
        MultiLineComponent component = new MultiLineComponent(text);
        helper.assertTrue(component.equalsString(s),
                "strings not equal: \"%s\" != \"%s\"".formatted(component.toString(), s));
    }

    public static void assertEqual(GameTestHelper helper, ItemStack stack1, ItemStack stack2) {
        helper.assertTrue(isItemStackEqual(stack1, stack2),
                "Item stacks not equal: \"%s\" != \"%s\"".formatted(stack1.toString(), stack2.toString()));
    }

    public static void assertEqual(GameTestHelper helper, FluidStack stack1, FluidStack stack2) {
        helper.assertTrue(stack1.isFluidStackIdentical(stack2),
                "Fluid stacks not equal: \"%s %d\" != \"%s %d\"".formatted(
                        stack1.getDisplayName().getString(), stack1.getAmount(),
                        stack2.getDisplayName().getString(), stack2.getAmount()));
    }

    public static void assertLampOn(GameTestHelper helper, BlockPos pos) {
        helper.assertBlockProperty(pos, RedstoneLampBlock.LIT, true);
    }

    public static void assertLampOff(GameTestHelper helper, BlockPos pos) {
        helper.assertBlockProperty(pos, RedstoneLampBlock.LIT, false);
    }

    /**
     * Shortcut function to retrieve a metamachine from a blockentity's
     *
     * @param entity The MetaMachineBlockEntity
     * @return the machine held, if any
     */
    public static MetaMachine getMetaMachine(BlockEntity entity) {
        return ((MetaMachineBlockEntity) entity).getMetaMachine();
    }

    /**
     * Helper function to succeed after the test is over
     *
     * @param helper GameTestHelper
     */
    public static void succeedAfterTest(GameTestHelper helper) {
        succeedAfterTest(helper, 100);
    }

    /**
     * Helper function to succeed after the test is over
     *
     * @param helper  GameTestHelper
     * @param timeout Ticks to wait until succeeding
     */
    public static void succeedAfterTest(GameTestHelper helper, long timeout) {
        helper.runAtTickTime(timeout, helper::succeed);
    }

    public static void assertEqual(GameTestHelper helper, @Nullable BlockPos pos1, @Nullable BlockPos pos2) {
        helper.assertTrue(pos1 != null && pos1.equals(pos2), "Expected %s to equal to %s".formatted(pos1, pos2));
    }
}
