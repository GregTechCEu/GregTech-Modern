package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SinglePredicate extends BasePredicate {

    private final Predicate<PredicateContext> predicate;
    private final Stream<BlockInfo> candidateStream;
    private final @Nullable String debugName;
    private final @Nullable Consumer<StringBuilder> contents;

    public SinglePredicate(Predicate<PredicateContext> predicate, Stream<BlockInfo> candidateStream,
                           @Nullable String debugName, @Nullable Consumer<StringBuilder> contents) {
        this.predicate = predicate;
        this.candidateStream = candidateStream;
        this.debugName = debugName;
        this.contents = contents;
    }

    @Override
    public boolean test(PredicateContext ctx) {
        return predicate.test(ctx);
    }

    @Override
    public List<BlockInfo> computeCandidates() {
        return candidateStream.toList();
    }

    @Override
    public String getTypeName() {
        return Objects.requireNonNullElse(debugName, "Predicate");
    }

    @Override
    protected void appendContents(StringBuilder builder) {
        if (contents != null) {
            contents.accept(builder);
        }
    }
}
