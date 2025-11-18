package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;

import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates.*;
import static com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates.eq;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class NBTPredicateTest {

    @BeforeBatch(batch = "NBTPredicateTest")
    public static void prepare(ServerLevel level) {}

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
}
