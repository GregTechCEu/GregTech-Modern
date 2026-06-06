package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ExpandableMultiblockPatternBuilder {

    protected @Nullable ExpandablePattern.BoundsFunction boundsFunc;
    protected @Nullable BiFunction<BlockPos.MutableBlockPos, List<Integer>, PatternPredicate> predicateFunc;
    protected final RelativeDirection[] directions = new RelativeDirection[3];

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

    public ExpandableMultiblockPatternBuilder boundsFunction(ExpandablePattern.BoundsFunction boundsFunc) {
        this.boundsFunc = boundsFunc;
        return this;
    }

    public ExpandableMultiblockPatternBuilder predicateFunction(BiFunction<BlockPos.MutableBlockPos, List<Integer>, PatternPredicate> func) {
        this.predicateFunc = func;
        return this;
    }

    public ExpandablePattern build() {
        Objects.requireNonNull(boundsFunc, "Bound function is null, use .boundsFunction(...) on the builder");
        Objects.requireNonNull(predicateFunc, "Predicate function is null, use .predicateFunction(...) on the builder");
        return new ExpandablePattern(boundsFunc, predicateFunc, directions);
    }
}
