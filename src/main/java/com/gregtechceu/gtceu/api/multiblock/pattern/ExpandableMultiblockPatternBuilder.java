package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Accessors(fluent = true, chain = true)
public class ExpandableMultiblockPatternBuilder {

    @Setter
    protected @Nullable ExpandablePattern.BoundsProvider boundsProvider;
    @Setter
    protected @Nullable ExpandablePattern.BoundsConstraintProvider constraintProvider;
    @Setter
    protected @Nullable ExpandablePattern.PredicateProvider predicateProvider;
    protected final RelativeDirection[] directions = new RelativeDirection[3];
    private final Char2ObjectMap<@Nullable MultiPredicate> symbolMap = new Char2ObjectOpenHashMap<>();

    private ExpandableMultiblockPatternBuilder(RelativeDirection aisleDir, RelativeDirection stringDir,
                                               RelativeDirection charDir) {
        directions[0] = aisleDir;
        directions[1] = stringDir;
        directions[2] = charDir;
        RelativeDirection.validateFacingsArray(directions);
    }

    public static ExpandableMultiblockPatternBuilder start(RelativeDirection aisleDir, RelativeDirection stringDir,
                                                           RelativeDirection charDir) {
        return new ExpandableMultiblockPatternBuilder(aisleDir, stringDir, charDir);
    }

    public static ExpandableMultiblockPatternBuilder start() {
        return new ExpandableMultiblockPatternBuilder(RelativeDirection.BACK, RelativeDirection.UP,
                RelativeDirection.RIGHT);
    }

    public ExpandableMultiblockPatternBuilder where(char symbol, MultiPredicate predicate) {
        this.symbolMap.put(symbol, predicate);
        return this;
    }

    public ExpandablePattern build() {
        Objects.requireNonNull(boundsProvider, "Bound function is null");
        Objects.requireNonNull(predicateProvider, "Predicate function is null");
        ExpandablePattern pattern = new ExpandablePattern(boundsProvider, predicateProvider, symbolMap, directions);
        if (constraintProvider != null) {
            pattern.setBoundsConstraints(constraintProvider);
        }
        return pattern;
    }
}
