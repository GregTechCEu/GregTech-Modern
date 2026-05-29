package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder class for {@link BlockPattern}<br />
 * When the multiblock is placed, its facings are concrete. Then, the {@link RelativeDirection}s passed into
 * {@link MultiblockPatternBuilder#start(RelativeDirection, RelativeDirection, RelativeDirection)} are ways in which the
 * pattern progresses. It can be thought like this, where startPos() is either defined via
 * {@link MultiblockPatternBuilder#startOffset(OriginOffset)}, or automatically detected(for legacy compat only, you
 * should
 * use {@link MultiblockPatternBuilder#startOffset(OriginOffset)} always for new code):
 *
 * <pre>
 * {@code
 * for(int aisleI in 0..aisles):
 *     for(int stringI in 0..strings):
 *         for(int charI in 0..chars):
 *             pos = startPos()
 *             pos.move(aisleI in aisleDir)
 *             pos.move(stringI in stringDir)
 *             pos.move(charI in charDir)
 *             predicate = aisles[aisleI].stringAt(stringI).charAt(charI)
 * }
 * </pre>
 */
public class MultiblockPatternBuilder {

    protected static final Joiner COMMA_JOINER = Joiner.on(",");

    private final int[] dimensions = { -1, -1, -1 };

    private @Nullable OriginOffset offset;
    private @Nullable OriginOffset anchorOffset;
    private char centerChar;
    private @Nullable AisleStrategy aisleStrategy;

    private final List<PatternAisle> aisles = new ArrayList<>();

    private final Char2ObjectMap<@Nullable PatternPredicate> symbolMap = new Char2ObjectOpenHashMap<>();

    private final RelativeDirection[] directions = new RelativeDirection[3];

    private MultiblockPatternBuilder(RelativeDirection aisleDir, RelativeDirection stringDir,
                                     RelativeDirection charDir) {
        directions[0] = aisleDir;
        directions[1] = stringDir;
        directions[2] = charDir;
        RelativeDirection.validateFacingsArray(directions);
        this.symbolMap.put(' ', PatternPredicate.ANY);
    }

    public MultiblockPatternBuilder aisleRepeatable(int minRepeats, int maxRepeats, String... aisle) {
        validateAisle(aisle);
        for (String s : aisle) {
            for (char c : s.toCharArray()) {
                if (!this.symbolMap.containsKey(c)) {
                    this.symbolMap.put(c, null);
                }
            }
        }

        if (minRepeats > maxRepeats) {
            throw new IllegalArgumentException("minRepeats must be smaller than maxRepeats");
        }
        PatternAisle pa = new PatternAisle(aisle);
        pa.minRepeats = minRepeats;
        pa.maxRepeats = maxRepeats;
        aisles.add(pa);
        return this;
    }

    public MultiblockPatternBuilder aisle(String... aisle) {
        return aisleRepeatable(1, 1, aisle);
    }

    public MultiblockPatternBuilder startOffset(OriginOffset offset) {
        this.offset = offset;
        return this;
    }

    public MultiblockPatternBuilder anchorOffset(OriginOffset anchorOffset) {
        this.anchorOffset = anchorOffset;
        return this;
    }

    /**
     * Start a new multiblock pattern builder, this is equivalent to
     * {@link MultiblockPatternBuilder#start(RelativeDirection aisle, RelativeDirection string, RelativeDirection char)
     * FactoryBlockPattern.start(BACK, UP, RIGHT)}
     *
     */
    public static MultiblockPatternBuilder start() {
        return new MultiblockPatternBuilder(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT);
    }

    /**
     * Starts the builder, each pair of {@link RelativeDirection} must be used at exactly once!
     *
     * @param aisleDir  The direction aisles progress in, each successive
     *                  {@link MultiblockPatternBuilder#aisle(String...)}
     *                  progresses in this direction
     * @param stringDir The direction strings progress in, each successive string in an aisle progresses by this
     *                  direction
     * @param charDir   The direction chars progress in, each successive char in a string progresses by this direction
     */
    public static MultiblockPatternBuilder start(RelativeDirection aisleDir, RelativeDirection stringDir,
                                                 RelativeDirection charDir) {
        return new MultiblockPatternBuilder(aisleDir, stringDir, charDir);
    }

    public MultiblockPatternBuilder where(char symbol, PatternPredicate predicate) {
        this.symbolMap.put(symbol, predicate);
        if (predicate.isController()) centerChar = symbol;
        return this;
    }

    public MultiblockPatternBuilder aisleStrategy(AisleStrategy aisleStrategy) {
        this.aisleStrategy = aisleStrategy;
        return this;
    }

    public IBlockPattern build() {
        checkMissingPredicates();
        this.dimensions[0] = aisles.size();
        if (aisleStrategy == null) aisleStrategy = new BasicAisleStrategy();

        aisleStrategy.finish(dimensions, directions, aisles);
        return new BlockPattern(aisles.toArray(new PatternAisle[0]), aisleStrategy, dimensions,
                directions, offset, anchorOffset, symbolMap, centerChar);
    }

    private void checkMissingPredicates() {
        List<Character> list = new ArrayList<>();

        for (var entry : this.symbolMap.char2ObjectEntrySet()) {
            if (entry.getValue() == null) {
                list.add(entry.getCharKey());
            }
        }

        if (!list.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOINER.join(list) + " are missing");
        }
    }

    public void validateAisle(String[] aisle) {
        if (ArrayUtils.isEmpty(aisle) || StringUtils.isEmpty(aisle[0]))
            throw new IllegalArgumentException("Empty pattern for aisle");

        if (dimensions[2] == -1) {
            dimensions[2] = aisle[0].length();
        }

        if (dimensions[1] == -1) {
            dimensions[1] = aisle.length;
        }

        if (aisle.length != dimensions[1]) {
            throw new IllegalArgumentException("Expected aisle with height of " + dimensions[1] +
                    ", but was given one with a height of " + aisle.length);
        } else {
            for (String s : aisle) {
                if (s.length() != dimensions[2]) {
                    throw new IllegalArgumentException(
                            "Not all rows in the given aisle are the correct width (expected " + dimensions[2] +
                                    ", found one with " + s.length() + ")");
                }
            }
        }
    }
}
