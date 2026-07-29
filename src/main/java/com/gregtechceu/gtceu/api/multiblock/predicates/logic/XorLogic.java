package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.CompactedPredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class XorLogic extends BaseLogic {

    private BasePredicate passedPredicate;
    /// {@code true} if any base predicate have a min count of 0 or -1,
    /// meaning that it is possible that no predicates may be present in the multi.
    protected boolean noneValid;

    public XorLogic(MultiPredicate rootPredicate) {
        super(rootPredicate);
        this.noneValid = isNoneValid(this.rootPredicate);
    }

    @Override
    public void reset() {
        super.reset();
        this.passedPredicate = null;
    }

    private static boolean isNoneValid(MultiPredicate rootPredicate) {
        boolean noneValid = false;
        for (BasePredicate predicate : rootPredicate) {
            if (predicate instanceof CompactedPredicate cp) {
                noneValid |= isNoneValid(cp.expand());
            } else {
                noneValid |= predicate.getMinCount() <= 0 && predicate.getMinSliceCount() <= 0;
            }
        }
        return noneValid;
    }

    @Override
    public boolean test(PredicateContext ctx) {
        int passed = 0;
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate.test(ctx)) {
                passed++;
                if (this.passedPredicate == null) {
                    this.passedPredicate = predicate;
                }
            }
        }
        return passed > 0;
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        if (passedPredicate == null || !passedPredicate.testGlobalMin(ctx)) {
            return ctx.error(onError());
        }
        if (!global) return true;
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate != passedPredicate && ctx.getGlobalCount(predicate) > 0) {
                ctx.error(onError());
                ctx.error(PatternStringError.literal("%s present in multi", predicate));
                return false;
            }
        }
        return true;
    }

    private PatternError onError() {
        MutableComponent component = Component.literal("Need one of:\n")
                .withStyle(ChatFormatting.WHITE);

        for (BasePredicate predicate : this.rootPredicate) {
            for (BlockInfo candidate : predicate.getCandidates()) {
                component.append("  ")
                        .append(candidate.getItemStackForm().getHoverName())
                        .append("\n");
            }
        }

        if (this.passedPredicate != null) {
            component.append("\n")
                    .append("Expected predicate: " + this.passedPredicate);
        }

        return PatternStringError.component(component);
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        if (passedPredicate == null || !passedPredicate.testSliceMin(ctx)) {
            return ctx.error(PatternStringError.literal("need one of: " + rootPredicate));
        }
        if (global) return true;
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate != passedPredicate && ctx.getSliceCount(predicate) > 0) {
                ctx.error(PatternStringError.literal("need one of:\n" + rootPredicate.getPredicateList()));
                ctx.error(PatternStringError.literal(predicate + " present in multi"));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean testMaxCount(BasePredicate passedPredicate, PredicateContext context) {
        if (passedPredicate != this.passedPredicate) {
            // skip flipped check
            context.skipFlipCheck();
            context.error(PatternStringError.literal(passedPredicate + " present in multi"));
            return false;
        }
        return this.passedPredicate.testGlobalMax(context) && this.passedPredicate.testSliceMax(context);
    }

    @Override
    public void predicatePassed(BasePredicate predicate) {
        if (this.passedPredicate == null) {
            this.passedPredicate = predicate;
        }
    }

    @Override
    public MultiPredicate.Logic getType() {
        return MultiPredicate.Logic.XOR;
    }
}
