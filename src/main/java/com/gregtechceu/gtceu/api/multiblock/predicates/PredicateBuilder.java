package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SimplePatternError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Accessors(fluent = true)
public class PredicateBuilder {

    private final String name;
    @Setter
    private Predicate<PredicateContext> predicate;
    private Supplier<Stream<BlockInfo>> candidates = Stream::empty;
    @Setter
    private @Nullable Consumer<StringBuilder> contents;
    private final List<ErrorHandler> errorHandlers = new ArrayList<>();

    public PredicateBuilder(String debugName) {
        this.name = debugName;
    }

    /// Optional method, defaults to {@link #placeholderError(PredicateContext, BasePredicate)} if no custom
    /// error handler is given
    /// @param function Function that takes a {@link PredicateContext} and returns a {@link PatternError}.
    public PredicateBuilder errorFunction(Function<PredicateContext, PatternError> function) {
        return errorHandler((context, failingPredicate) -> context.appendError(function.apply(context)));
    }

    /// @param onError functional interface whose parameters are a
    /// {@code (PredicateContext, BasePredicate)}
    public PredicateBuilder errorHandler(ErrorHandler onError) {
        this.errorHandlers.add(onError);
        return this;
    }

    public PredicateBuilder candidates(Stream<BlockInfo> candidates) {
        this.candidates = () -> candidates;
        return this;
    }

    public PredicateBuilder candidatesSupplier(Supplier<Stream<BlockInfo>> candidates) {
        this.candidates = candidates;
        return this;
    }

    /// fills candidates and sets string contents with this block tag
    public PredicateBuilder blockTag(TagKey<Block> tag) {
        this.candidates = () -> Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())
                .getTag(tag).stream().map(BlockInfo::fromBlock);
        this.contents = builder -> builder.append(tag.location());
        return this;
    }

    /// fills candidates and sets string contents with this fluid tag
    public PredicateBuilder fluidTag(TagKey<Fluid> tag) {
        this.candidates = () -> Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                .getTag(tag).stream().map(BlockInfo::fromFluid);
        this.contents = builder -> builder.append(tag.location());
        return this;
    }

    public MultiPredicate toMultiPredicate() {
        return MultiPredicate.of(build());
    }

    public BasePredicate build() {
        return new TestablePredicate(name,
                Objects.requireNonNull(predicate, "predicate == null"),
                candidates.get(),
                contents,
                composeErrorHandlers());
    }

    private ErrorHandler composeErrorHandlers() {
        if (errorHandlers.isEmpty()) return this::placeholderError;
        Validate.noNullElements(errorHandlers);
        if (errorHandlers.size() == 1) return errorHandlers.get(0);
        List<ErrorHandler> errorHandlers = Collections.unmodifiableList(this.errorHandlers);
        return (context, failingPredicate) -> {
            for (ErrorHandler handler : errorHandlers) {
                handler.appendError(context, failingPredicate);
            }
        };
    }

    private void placeholderError(PredicateContext ctx, BasePredicate failingPredicate) {
        ctx.appendError(new SimplePatternError(ctx.pos(), List.of(failingPredicate.getCandidates())));
    }
}
