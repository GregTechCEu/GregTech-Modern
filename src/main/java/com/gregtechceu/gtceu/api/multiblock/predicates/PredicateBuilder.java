package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SimplePatternError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Accessors(fluent = true)
public class PredicateBuilder {

    private final String name;
    @Setter
    private Predicate<PredicateContext> predicate;
    private final List<BlockInfo> candidates = new ArrayList<>();
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

    @RemapForJS("candidate")
    public PredicateBuilder candidates(BlockInfo candidate) {
        this.candidates.add(candidate);
        return this;
    }

    @HideFromJS
    public PredicateBuilder candidates(Stream<BlockInfo> candidateStream) {
        return candidates(candidateStream.toList());
    }

    public PredicateBuilder candidates(Collection<BlockInfo> candidates) {
        this.candidates.addAll(candidates);
        return this;
    }

    @RemapForJS("block")
    public PredicateBuilder blocks(Block candidate) {
        this.candidates.add(BlockInfo.fromBlock(candidate));
        return this;
    }

    @HideFromJS
    public PredicateBuilder blocks(Collection<Block> candidates) {
        candidates.forEach(this::blocks);
        return this;
    }

    public PredicateBuilder blocks(Block... candidates) {
        if (candidates.length == 0) return this;
        if (candidates.length == 1) return this.blocks(candidates[0]);
        Arrays.stream(candidates).forEach(this::blocks);
        return this;
    }

    @RemapForJS("state")
    public PredicateBuilder states(BlockState candidate) {
        this.candidates.add(BlockInfo.fromBlockState(candidate));
        return this;
    }

    @HideFromJS
    public PredicateBuilder states(Collection<BlockState> candidates) {
        candidates.forEach(this::states);
        return this;
    }

    public PredicateBuilder states(BlockState... candidates) {
        if (candidates.length == 0) return this;
        if (candidates.length == 1) return this.states(candidates[0]);
        Arrays.stream(candidates).forEach(this::states);
        return this;
    }

    @RemapForJS("fluidState")
    public PredicateBuilder fluidStates(FluidState candidate) {
        this.candidates.add(BlockInfo.fromFluidState(candidate));
        return this;
    }

    @HideFromJS
    public PredicateBuilder fluidStates(Collection<FluidState> candidates) {
        candidates.forEach(this::fluidStates);
        return this;
    }

    public PredicateBuilder fluidStates(FluidState... candidates) {
        if (candidates.length == 0) return this;
        if (candidates.length == 1) return this.fluidStates(candidates[0]);
        Arrays.stream(candidates).forEach(this::fluidStates);
        return this;
    }

    @RemapForJS("fluid")
    public PredicateBuilder fluids(Fluid candidate) {
        this.candidates.add(BlockInfo.fromFluid(candidate));
        return this;
    }

    @HideFromJS
    public PredicateBuilder fluids(Collection<Fluid> candidates) {
        candidates.forEach(this::fluids);
        return this;
    }

    public PredicateBuilder fluids(Fluid... candidates) {
        if (candidates.length == 0) return this;
        if (candidates.length == 1) return this.fluids(candidates[0]);
        Arrays.stream(candidates).forEach(this::fluids);
        return this;
    }

    /// fills candidates with this block tag
    public PredicateBuilder blockTag(TagKey<Block> tag) {
        Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())
                .getTag(tag).forEach(this::blocks);
        return this;
    }

    /// fills candidates with this fluid tag
    public PredicateBuilder fluidTag(TagKey<Fluid> tag) {
        Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                .getTag(tag).forEach(this::fluids);
        return this;
    }

    public MultiPredicate toMultiPredicate() {
        return MultiPredicate.of(build());
    }

    public BasePredicate build() {
        if (this.candidates.isEmpty()) this.candidates.add(BlockInfo.EMPTY);
        return new TestablePredicate(name,
                Objects.requireNonNull(predicate, "predicate == null"),
                Collections.unmodifiableList(candidates),
                this.contents,
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
