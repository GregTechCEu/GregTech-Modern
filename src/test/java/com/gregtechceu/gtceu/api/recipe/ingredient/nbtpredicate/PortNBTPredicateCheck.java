package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortNBTPredicateCheck {
    public static void main(String[] args) {
        var test = new PortNBTPredicateCheck();
        test.comparesEveryNumericTypeWithoutRequiringMatchingTagTypes();
        test.missingAndNonNumericValuesDoNotMatchNumericComparisons();
        test.nestedCompoundAndListPathsResolveAndRejectInvalidIndices();
        test.predicatesRetainTheirBehaviorAcrossJsonRoundTrips();
        test.retainsIeeeComparisonBehavior();
        System.out.println("NBT predicates: 5 checks passed");
    }

    @Test
    void comparesEveryNumericTypeWithoutRequiringMatchingTagTypes() {
        NumericTag[] values = {ByteTag.valueOf((byte) 7), ShortTag.valueOf((short) 7), IntTag.valueOf(7),
                LongTag.valueOf(7), FloatTag.valueOf(7), DoubleTag.valueOf(7)};
        for (NumericTag value : values) {
            var data = new CompoundTag();
            data.put("amount", value);
            assertTrue(new EqualsNBTPredicate("amount", DoubleTag.valueOf(7)).test(data));
            assertFalse(new EqualsNBTPredicate("amount", IntTag.valueOf(7), true).test(data));
            assertTrue(new ComparisonNBTPredicate("amount", 6).test(data));
            assertTrue(new ComparisonNBTPredicate("amount", 8, true, false).test(data));
            assertFalse(new ComparisonNBTPredicate("amount", 7).test(data));
            assertTrue(new ComparisonNBTPredicate("amount", 7, false, true).test(data));
            assertTrue(new ComparisonNBTPredicate("amount", 7, true, true).test(data));
        }
    }

    @Test
    void missingAndNonNumericValuesDoNotMatchNumericComparisons() {
        var data = new CompoundTag();
        assertFalse(new ComparisonNBTPredicate("missing", 1).test(data));
        assertFalse(new EqualsNBTPredicate("missing", IntTag.valueOf(1), true).test(data));
        data.putString("amount", "7");
        assertFalse(new ComparisonNBTPredicate("amount", 6).test(data));
        assertFalse(new EqualsNBTPredicate("amount", IntTag.valueOf(7)).test(data));
        assertTrue(new EqualsNBTPredicate("amount", StringTag.valueOf("7")).test(data));
    }

    @Test
    void nestedCompoundAndListPathsResolveAndRejectInvalidIndices() {
        var entry = new CompoundTag();
        entry.putDouble("amount", 3.5);
        var list = new ListTag();
        list.add(entry);
        var data = new CompoundTag();
        data.put("tanks", list);
        assertTrue(new ComparisonNBTPredicate("tanks[0].amount", 3).test(data));
        for (String path : new String[]{"tanks[-1].amount", "tanks[1].amount", "tanks[x].amount", "tanks[0].missing"}) {
            assertNull(NBTPredicateUtils.getNestedTag(data, path));
        }
    }

    @Test
    void predicatesRetainTheirBehaviorAcrossJsonRoundTrips() {
        var data = new CompoundTag();
        data.putDouble("amount", 3.5);
        var comparison = new ComparisonNBTPredicate("amount", 3.5, true, true);
        assertTrue(ComparisonNBTPredicate.fromJson(comparison.toJson()).test(data));
        var equality = new EqualsNBTPredicate("amount", DoubleTag.valueOf(3.5), true);
        var restored = EqualsNBTPredicate.fromJson(equality.toJson());
        assertFalse(restored.test(data));
        data.putDouble("amount", 4.5);
        assertTrue(restored.test(data));
    }

    @Test
    void retainsIeeeComparisonBehavior() {
        var data = new CompoundTag();
        data.putDouble("amount", Double.NaN);
        assertFalse(new ComparisonNBTPredicate("amount", 0, false, true).test(data));
        assertFalse(new EqualsNBTPredicate("amount", DoubleTag.valueOf(Double.NaN)).test(data));
        data.putDouble("amount", Double.POSITIVE_INFINITY);
        assertTrue(new ComparisonNBTPredicate("amount", Double.MAX_VALUE).test(data));
    }
}
