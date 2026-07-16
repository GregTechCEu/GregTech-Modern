package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
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
 * for(int sliceI in 0..slices):
 *     for(int stringI in 0..strings):
 *         for(int charI in 0..chars):
 *             pos = startPos()
 *             pos.move(sliceI in sliceDir)
 *             pos.move(stringI in stringDir)
 *             pos.move(charI in charDir)
 *             predicate = slices[sliceI].stringAt(stringI).charAt(charI)
 * }
 * </pre>
 */
public class MultiblockPatternBuilder {

    protected static final Joiner COMMA_JOINER = Joiner.on(",");

    private final int[] dimensions = { -1, -1, -1 };

    private @Nullable OriginOffset offset;
    private @Nullable OriginOffset anchorOffset;
    private char centerChar;
    private @Nullable SliceStrategy sliceStrategy;

    private final List<PatternSlice> slices = new ArrayList<>();

    private final Char2ObjectMap<@Nullable PatternPredicate> symbolMap = new Char2ObjectOpenHashMap<>();

    private final RelativeDirection[] directions = new RelativeDirection[3];

    private MultiblockPatternBuilder(RelativeDirection sliceDir, RelativeDirection stringDir,
                                     RelativeDirection charDir) {
        directions[0] = sliceDir;
        directions[1] = stringDir;
        directions[2] = charDir;
        RelativeDirection.validateFacingsArray(directions);
        // todo is this wanted?
        this.symbolMap.put(' ', PatternPredicate.ANY);
    }

    public MultiblockPatternBuilder sliceRepeatable(int minRepeats, int maxRepeats, String... slice) {
        validateSlice(slice);
        for (String s : slice) {
            for (char c : s.toCharArray()) {
                if (!this.symbolMap.containsKey(c)) {
                    this.symbolMap.put(c, null);
                }
            }
        }

        if (minRepeats > maxRepeats) {
            throw new IllegalArgumentException("minRepeats must be smaller than maxRepeats");
        }
        PatternSlice ps = new PatternSlice(slice);
        ps.minRepeats = minRepeats;
        ps.maxRepeats = maxRepeats;
        slices.add(ps);
        return this;
    }

    public MultiblockPatternBuilder slice(String... slice) {
        return sliceRepeatable(1, 1, slice);
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
     * {@link MultiblockPatternBuilder#start(RelativeDirection slice, RelativeDirection string, RelativeDirection char)
     * FactoryBlockPattern.start(BACK, UP, RIGHT)}
     *
     */
    public static MultiblockPatternBuilder start() {
        return new MultiblockPatternBuilder(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT);
    }

    /**
     * Starts the builder, each pair of {@link RelativeDirection} must be used at exactly once!
     *
     * @param sliceDir  The direction slices progress in, each successive
     *                  {@link MultiblockPatternBuilder#slice(String...)}
     *                  progresses in this direction
     * @param stringDir The direction strings progress in, each successive string in an slice progresses by this
     *                  direction
     * @param charDir   The direction chars progress in, each successive char in a string progresses by this direction
     */
    public static MultiblockPatternBuilder start(RelativeDirection sliceDir, RelativeDirection stringDir,
                                                 RelativeDirection charDir) {
        return new MultiblockPatternBuilder(sliceDir, stringDir, charDir);
    }

    public MultiblockPatternBuilder where(char symbol, PatternPredicate predicate) {
        this.symbolMap.put(symbol, predicate);
        if (predicate.isController()) centerChar = symbol;
        return this;
    }

    public MultiblockPatternBuilder sliceStrategy(SliceStrategy sliceStrategy) {
        this.sliceStrategy = sliceStrategy;
        return this;
    }

    public IBlockPattern build() {
        checkMissingPredicates();
        checkGlobalConstraints();
        this.dimensions[0] = slices.size();
        if (sliceStrategy == null) sliceStrategy = new BasicSliceStrategy();

        sliceStrategy.finish(dimensions, directions, slices);
        return new BlockPattern(slices.toArray(new PatternSlice[0]), sliceStrategy, dimensions,
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

    private void checkGlobalConstraints() {
        Char2IntMap charCount = new Char2IntOpenHashMap();
        for (var slice : slices) {
            for (var string : slice.getPattern()) {
                for (char c : string) {
                    charCount.merge(c, 1, Integer::sum);
                }
            }
        }

        for (var entry : symbolMap.char2ObjectEntrySet()) {
            char symbol = entry.getCharKey();
            PatternPredicate predicate = entry.getValue();
            if (predicate == null) throw new IllegalArgumentException("Predicate for symbol " + symbol + " was null.");

            int maxCount = -1;
            for (var basePredicate : predicate.subPredicates) {
                if (basePredicate.maxCount == -1) {
                    maxCount = -1;
                    break;
                }
                if (basePredicate.minCount == basePredicate.maxCount) {
                    if (maxCount == -1) {
                        maxCount = basePredicate.minCount;
                    } else {
                        maxCount += basePredicate.minCount;
                    }
                }
            }
            if (maxCount == -1) continue;
            if (charCount.get(symbol) > maxCount) {
                throw new IllegalArgumentException("Predicate has global max of " + maxCount + " but appears " +
                        charCount.get(symbol) + " times.");
            }
        }
    }

    public void validateSlice(String[] slice) {
        if (ArrayUtils.isEmpty(slice) || StringUtils.isEmpty(slice[0]))
            throw new IllegalArgumentException("Empty pattern for slice");

        if (dimensions[2] == -1) {
            dimensions[2] = slice[0].length();
        }

        if (dimensions[1] == -1) {
            dimensions[1] = slice.length;
        }

        if (slice.length != dimensions[1]) {
            throw new IllegalArgumentException("Expected slice with height of " + dimensions[1] +
                    ", but was given one with a height of " + slice.length);
        } else {
            for (String s : slice) {
                if (s.length() != dimensions[2]) {
                    throw new IllegalArgumentException(
                            "Not all rows in the given slice are the correct width (expected " + dimensions[2] +
                                    ", found one with " + s.length() + ")");
                }
            }
        }
    }
}
