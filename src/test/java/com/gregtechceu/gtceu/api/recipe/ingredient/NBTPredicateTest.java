package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_RECIPES;
import static com.gregtechceu.gtceu.gametest.util.TestUtils.getMetaMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class NBTPredicateTest {

    private static GTRecipeType CR_RECIPE_TYPE;

    @BeforeBatch(batch = "NBTPredicateTest")
    public static void prepare(ServerLevel level) {
        CR_RECIPE_TYPE = TestUtils.createRecipeType("nbt_predicate_ingredient_cr_tests", CHEMICAL_RECIPES);
        CR_RECIPE_TYPE.getLookup().addRecipe(
                CR_RECIPE_TYPE.recipeBuilder("nbt_predicate_test")
                        .inputItemNbtPredicate(new ItemStack(Items.FEATHER), eq("foo", "bar"))
                        .outputItems(new ItemStack(Items.COAL))
                        .EUt(GTValues.V[GTValues.HV])
                        .duration(5)
                        .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(
                CR_RECIPE_TYPE.recipeBuilder("nbt_predicate_test_chanced")
                        .chance(4000)
                        .inputItemNbtPredicate(new ItemStack(Items.FEATHER), eq("bin", "bar"))
                        .chance(10000)
                        .outputItems(new ItemStack(Items.CHARCOAL))
                        .EUt(GTValues.V[GTValues.HV])
                        .duration(4)
                        .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(
                CR_RECIPE_TYPE.recipeBuilder("nbt_predicate_test_ranged")
                        .inputItemRanged(new IntProviderIngredient(new NBTPredicateIngredient(
                                new ItemStack(Items.FEATHER), eq("bash", "bar")), UniformInt.of(0, 4)))
                        .outputItems(new ItemStack(Items.COBBLESTONE))
                        .EUt(GTValues.V[GTValues.HV])
                        .duration(4)
                        .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(
                CR_RECIPE_TYPE.recipeBuilder("nbt_predicate_test_chanced_ranged")
                        .chance(4000)
                        .inputItemRanged(new IntProviderIngredient(new NBTPredicateIngredient(
                                new ItemStack(Items.FEATHER), eq("bash", "botch")), UniformInt.of(0, 4)))
                        .chance(10000)
                        .outputItems(new ItemStack(Items.DEEPSLATE))
                        .EUt(GTValues.V[GTValues.HV])
                        .duration(4)
                        .buildRawRecipe());
    }

    @GameTest(template = "empty", batch = "NBTPredicateTest")
    public static void NBTPredicateEqualsTest(GameTestHelper helper) {
        CompoundTag tag = new CompoundTag();
        tag.putString("foo", "bar");
        helper.assertTrue(eq("foo", "bar").test(tag), "String equality NBTPredicate failed when it shouldn't have");
        helper.assertFalse(eq("foo", "baz").test(tag), "String equality NBTPredicate succeeded when it shouldn't have");
        helper.assertFalse(eq("foo", 1).test(tag), "String equality NBTPredicate succeeded when it shouldn't have");
        helper.assertFalse(eq("foo", new CompoundTag()).test(tag),
                "String equality NBTPredicate succeeded when it shouldn't have");

        helper.assertFalse(neq("foo", "bar").test(tag),
                "String inequality NBTPredicate succeeded when it shouldn't have");
        helper.assertTrue(neq("foo", "baz").test(tag), "String inequality NBTPredicate failed when it shouldn't have");
        helper.assertTrue(neq("foo", 1).test(tag), "String inequality NBTPredicate failed when it shouldn't have");
        helper.assertTrue(neq("foo", new CompoundTag()).test(tag),
                "String inequality NBTPredicate failed when it shouldn't have");

        CompoundTag numberTag = new CompoundTag();
        numberTag.putFloat("foo", 7f);
        helper.assertTrue(eqDouble("foo", 7d).test(numberTag),
                "Double equality failed when it shouldn't have (number conversion)");
        helper.assertTrue(eqInt("foo", 7).test(numberTag),
                "Int equality failed when it shouldn't have (number conversion)");
        helper.assertTrue(eqFloat("foo", 7).test(numberTag),
                "Float equality failed when it shouldn't have (number conversion)");
        helper.assertTrue(eqByte("foo", (byte) 7).test(numberTag),
                "Float equality failed when it shouldn't have (number conversion)");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "NBTPredicateTest")
    public static void NBTPredicateComparisonTest(GameTestHelper helper) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("num", 10);

        helper.assertTrue(gt("num", 5).test(tag),
                "Greater-than NBTPredicate failed when it shouldn't have");
        helper.assertFalse(gt("num", 10).test(tag),
                "Greater-than NBTPredicate succeeded when it shouldn't have");
        helper.assertFalse(gt("num", 11).test(tag),
                "Greater-than NBTPredicate succeeded when it shouldn't have");

        helper.assertTrue(gte("num", 5).test(tag),
                "Greater-or-equal NBTPredicate failed when it shouldn't have");
        helper.assertTrue(gte("num", 10).test(tag),
                "Greater-or-equal NBTPredicate failed when it shouldn't have");
        helper.assertFalse(gte("num", 11).test(tag),
                "Greater-or-equal NBTPredicate succeeded when it shouldn't have");

        helper.assertTrue(lt("num", 15).test(tag),
                "Less-than NBTPredicate failed when it shouldn't have");
        helper.assertFalse(lt("num", 10).test(tag),
                "Less-than NBTPredicate succeeded when it shouldn't have");
        helper.assertFalse(lt("num", 9).test(tag),
                "Less-than NBTPredicate succeeded when it shouldn't have");

        helper.assertTrue(lte("num", 15).test(tag),
                "Less-or-equal NBTPredicate failed when it shouldn't have");
        helper.assertTrue(lte("num", 10).test(tag),
                "Less-or-equal NBTPredicate failed when it shouldn't have");
        helper.assertFalse(lte("num", 9).test(tag),
                "Less-or-equal NBTPredicate succeeded when it shouldn't have");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "NBTPredicateTest")
    public static void NBTPredicateAnyTest(GameTestHelper helper) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("a", 5);
        tag.putInt("b", 10);

        NBTPredicate predicate = any(
                eq("a", 7),
                eq("b", 10),
                eq("c", 99));

        helper.assertTrue(predicate.test(tag),
                "AnyNBTPredicate failed when one child was true");

        NBTPredicate predicateFail = any(
                eq("a", 1),
                eq("b", 2));

        helper.assertFalse(predicateFail.test(tag),
                "AnyNBTPredicate succeeded though all children were false");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "NBTPredicateTest")
    public static void NBTPredicateAllTest(GameTestHelper helper) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", 3);
        tag.putInt("y", 9);

        NBTPredicate predicate = all(
                lt("x", 5),
                gt("y", 5));

        helper.assertTrue(predicate.test(tag),
                "AllNBTPredicate failed when all children were true");

        NBTPredicate predicateFail = all(
                lt("x", 1),
                gt("y", 5));

        helper.assertFalse(predicateFail.test(tag),
                "AllNBTPredicate succeeded though one child was false");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "NBTPredicateTest")
    public static void NBTPredicateNotTest(GameTestHelper helper) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("enabled", true);

        NBTPredicate base = eq("enabled", true);
        NBTPredicate inverse = not(base);

        helper.assertTrue(base.test(tag),
                "Base NBTPredicate should return true for enabled=true");
        helper.assertFalse(inverse.test(tag),
                "NotNBTPredicate did not invert result correctly");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "NBTPredicateTest")
    public static void NBTPredicateEmptyTest(GameTestHelper helper) {
        CompoundTag tag = new CompoundTag();

        helper.assertFalse(eq("foo", "bar").test(tag), "String equality NBTPredicate succeeded with empty tag");
        helper.assertFalse(eq("foo", "baz").test(tag), "String equality NBTPredicate succeeded with empty tag");
        helper.assertFalse(eq("foo", 1).test(tag), "String equality NBTPredicate succeeded with empty tag");
        helper.assertFalse(eq("foo", new CompoundTag()).test(tag),
                "String equality NBTPredicate succeeded with empty tag");

        helper.assertFalse(neq("foo", "bar").test(tag), "String inequality NBTPredicate succeeded with empty tag");
        helper.assertFalse(neq("foo", "baz").test(tag), "String inequality NBTPredicate succeeded with empty tag");
        helper.assertFalse(neq("foo", 1).test(tag), "String inequality NBTPredicate succeeded with empty tag");
        helper.assertFalse(neq("foo", new CompoundTag()).test(tag),
                "String inequality NBTPredicate succeeded with empty tag");

        helper.assertFalse(gt("num", 5).test(tag),
                "Greater-than NBTPredicate succeeded with empty tag");
        helper.assertFalse(gte("num", 5).test(tag),
                "Greater-or-equal NBTPredicate succeeded with empty tag");
        helper.assertFalse(lt("num", 15).test(tag),
                "Less-than NBTPredicate succeeded with empty tag");
        helper.assertFalse(lte("num", 9).test(tag),
                "Less-or-equal NBTPredicate  succeeded with empty tag");
        helper.succeed();
    }

    @GameTest(template = "singleblock_chem_reactor", batch = "NBTPredicateTest")
    public static void NBTPredicateMachineCRTestSucceeds(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        var inputStack = new ItemStack(Items.FEATHER);
        inputStack.getOrCreateTag().putString("foo", "bar");
        itemIn.setStackInSlot(0, inputStack);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(ItemStack.isSameItemSameTags(itemOut.getStackInSlot(0), new ItemStack(Items.COAL)),
                    "NBT Predicate test didn't run when it should have.");
            helper.succeed();
        });
    }

    @GameTest(template = "singleblock_chem_reactor", batch = "NBTPredicateTest")
    public static void NBTPredicateMachineCRTestDoesntSucceed(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        var inputStack = new ItemStack(Items.FEATHER);
        inputStack.getOrCreateTag().putString("foo", "baz");
        itemIn.setStackInSlot(0, inputStack);
        helper.runAfterDelay(10, () -> {
            helper.assertFalse(ItemStack.isSameItemSameTags(itemOut.getStackInSlot(0), new ItemStack(Items.COAL)),
                    "NBT Predicate test ran when it shouldn't have.");
            helper.succeed();
        });
    }

    @GameTest(template = "singleblock_chem_reactor", batch = "NBTPredicateTest")
    public static void NBTPredicateMachineCRTestChanced(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        var inputStack = new ItemStack(Items.FEATHER, 15); // one short, a chance roll needs to fail
        inputStack.getOrCreateTag().putString("bin", "bar");
        itemIn.setStackInSlot(0, inputStack);
        helper.runAfterDelay(4 * 16 + 1, () -> {
            helper.assertTrue(ItemStack.isSameItemSameTags(itemOut.getStackInSlot(0), new ItemStack(Items.CHARCOAL)),
                    "NBT Predicate Chanced test ran the wrong recipe!");
            helper.assertTrue(itemOut.getStackInSlot(0).getCount() == 16,
                    "NBT Predicate Chanced test didn't complete enough recipe runs, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "], not [16]");
            helper.assertFalse(itemIn.getStackInSlot(0).getCount() == 15,
                    "NBT Predicate Chanced test didn't consume items");
            helper.assertFalse(itemIn.getStackInSlot(0).isEmpty(),
                    "NBT Predicate Chanced test consumed too many items");
            helper.succeed();
        });
    }

    @GameTest(template = "singleblock_chem_reactor", batch = "NBTPredicateTest")
    public static void NBTPredicateMachineCRTestRanged(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        var inputStack = new ItemStack(Items.FEATHER, 63); // one short, a range needs to roll not max
        inputStack.getOrCreateTag().putString("bash", "bar");
        itemIn.setStackInSlot(0, inputStack);
        helper.runAfterDelay(4 * 16 + 1, () -> {
            helper.assertTrue(ItemStack.isSameItemSameTags(itemOut.getStackInSlot(0), new ItemStack(Items.COBBLESTONE)),
                    "NBT Predicate Ranged test ran the wrong recipe!");
            helper.assertTrue(itemOut.getStackInSlot(0).getCount() == 16,
                    "NBT Predicate Ranged test didn't complete enough recipe runs, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "], not [16]");
            helper.assertFalse(itemIn.getStackInSlot(0).getCount() == 15,
                    "NBT Predicate Ranged test didn't consume items");
            helper.assertFalse(itemIn.getStackInSlot(0).isEmpty(),
                    "NBT Predicate Ranged test consumed too many items");
            helper.succeed();
        });
    }

    @GameTest(template = "singleblock_chem_reactor", batch = "NBTPredicateTest")
    public static void NBTPredicateMachineCRTestChancedRanged(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        var inputStack = new ItemStack(Items.FEATHER, 63); // one short, a chance or range needs to not max
        inputStack.getOrCreateTag().putString("bash", "botch");
        itemIn.setStackInSlot(0, inputStack);
        helper.runAfterDelay(4 * 16 + 1, () -> {
            helper.assertTrue(ItemStack.isSameItemSameTags(itemOut.getStackInSlot(0), new ItemStack(Items.DEEPSLATE)),
                    "NBT Predicate Chanced Ranged test ran the wrong recipe!");
            helper.assertTrue(itemOut.getStackInSlot(0).getCount() == 16,
                    "NBT Predicate Chanced Ranged test didn't complete enough recipe runs, completed [" +
                            itemOut.getStackInSlot(0).getCount() + "], not [16]");
            helper.assertFalse(itemIn.getStackInSlot(0).getCount() == 15,
                    "NBT Predicate Chanced Ranged test didn't consume items");
            helper.assertFalse(itemIn.getStackInSlot(0).isEmpty(),
                    "NBT Predicate Chanced Ranged test consumed too many items");
            helper.succeed();
        });
    }
}
