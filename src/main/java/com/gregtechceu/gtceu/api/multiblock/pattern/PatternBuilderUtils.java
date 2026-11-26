package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder.COMMA_JOINER;

public class PatternBuilderUtils {

    @SuppressWarnings("ConstantValue")
    public static void checkNullPredicates(Char2ObjectMap<MultiPredicate> symbolMap) {
        List<Character> list = new ArrayList<>();
        for (var entry : symbolMap.char2ObjectEntrySet()) {
            if (entry.getValue() == null) {
                list.add(entry.getCharKey());
            }
        }

        if (!list.isEmpty()) {
            throw new IllegalArgumentException("Predicates for character(s) " +
                    COMMA_JOINER.join(list) + " are null or do not exist");
        }
    }
}
