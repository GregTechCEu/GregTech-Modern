package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.core.BlockPos;

import java.util.function.BiFunction;

public class ExpandableMultiblockPatternBuilder {

    protected ExpandablePattern.BoundsFunction boundsFunc;
    protected BiFunction<BlockPos.MutableBlockPos, int[], PatternPredicate> predicateFunc;
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
        return new ExpandableMultiblockPatternBuilder(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT);
    }

    public ExpandableMultiblockPatternBuilder boundsFunction(ExpandablePattern.BoundsFunction boundsFunc) {
        this.boundsFunc = boundsFunc;
        return this;
    }

    public ExpandableMultiblockPatternBuilder predicateFunction(BiFunction<BlockPos.MutableBlockPos, int[], PatternPredicate> func) {
        this.predicateFunc = func;
        return this;
    }

    public ExpandablePattern build() {
        if (boundsFunc == null)
            throw new IllegalStateException("Bound function is null, use .boundsFunction(...) on the builder");
        if (predicateFunc == null)
            throw new IllegalStateException("Predicate function is null, use .predicateFunction(...) on the builder");

        return new ExpandablePattern(boundsFunc, predicateFunc, directions);
    }
}
