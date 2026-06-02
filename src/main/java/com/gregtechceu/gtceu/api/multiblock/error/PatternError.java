package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;

import com.mojang.serialization.Codec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class PatternError {

    public abstract Codec<? extends PatternError> codec();

    public static final Codec<PatternError> CODEC = GTRegistries.PATTERN_ERRORS.codec()
            .dispatch(PatternError::codec, Function.identity());

    @Getter
    protected @Nullable BlockPos pos;
    @Getter
    protected List<List<BlockInfo>> candidates;

    public PatternError(@Nullable BlockPos pos, List<List<BlockInfo>> candidates) {
        this.pos = pos;
        this.candidates = candidates;
    }

    public PatternError(@Nullable BlockPos pos, PatternPredicate predicate) {
        this(pos, predicate.getCandidates());
    }

    public PatternError(@Nullable BlockPos pos, @NotNull BasePredicate failingPredicate) {
        this(pos, Collections.singletonList(failingPredicate.getCandidates()));
    }

    @NotNull
    public abstract PatternErrorUI getPatternErrorUIModifier();
}
