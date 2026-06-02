package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Collections;

public class SinglePredicateError extends PatternError {

    public static final Codec<SinglePredicateError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SinglePredicateError.ErrorType.CODEC.fieldOf("error_type").forGetter(e -> e.type),
            Codec.INT.fieldOf("actual_count").forGetter(e -> e.actualCount),
            Codec.INT.fieldOf("pred_min_count").forGetter(e -> e.predMinCount),
            Codec.INT.fieldOf("pred_max_count").forGetter(e -> e.predMaxCount),
            Codec.INT.fieldOf("pred_min_layer_count").forGetter(e -> e.predMinLayerCount),
            Codec.INT.fieldOf("pred_max_layer_count").forGetter(e -> e.predMaxLayerCount),
            BlockInfo.CODEC.fieldOf("candidate").forGetter(e -> e.candidate))
            .apply(instance, SinglePredicateError::new));
    public static ResourceLocation ID = GTCEu.id("single_predicate_error");

    public final ErrorType type;
    public final int actualCount;
    // Fields from BasePredicate that we need
    public final BlockInfo candidate;
    public final int predMinCount;
    public final int predMaxCount;
    public final int predMinLayerCount;
    public final int predMaxLayerCount;

    public SinglePredicateError(BasePredicate failingPredicate, ErrorType type, int actualCount) {
        this(type, actualCount, failingPredicate.minCount, failingPredicate.maxCount, failingPredicate.minLayerCount,
                failingPredicate.maxLayerCount,
                failingPredicate.getCandidates().stream().findFirst().orElseThrow(
                        () -> new IllegalStateException(
                                "SinglePredicateError was created with empty failingPredicate.getCandidates()")));
    }

    public SinglePredicateError(ErrorType type, int actualCount, int minCount, int maxCount, int minLayerCount,
                                int maxLayerCount, BlockInfo candidate) {
        super(null, Collections.singletonList(Collections.singletonList(candidate)));
        this.type = type;
        this.actualCount = actualCount;
        this.candidate = candidate;
        this.predMinCount = minCount;
        this.predMaxCount = maxCount;
        this.predMinLayerCount = minLayerCount;
        this.predMaxLayerCount = maxLayerCount;
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            Component predName = candidate.getItemStackForm().getHoverName();
            switch (type) {
                case MAX_COUNT -> {
                    parent.child(Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.max_count",
                            predMaxCount,
                            actualCount)).asWidget());
                    parent.child(Text.of(predName).asWidget());
                }
                case MIN_COUNT -> {
                    parent.child(Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.min_count",
                            predMinCount,
                            actualCount)).asWidget());
                    parent.child(Text.of(predName).asWidget());
                }
                case MAX_LAYER_COUNT -> {
                    parent.child(
                            Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.max_layer_count",
                                    predMaxLayerCount, actualCount)).asWidget());
                    parent.child(Text.of(predName).asWidget());
                }
                case MIN_LAYER_COUNT -> {
                    parent.child(
                            Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.min_layer_count",
                                    predMinLayerCount, actualCount)).asWidget());
                    parent.child(Text.of(predName).asWidget());
                }
            }
        };
    }

    @Getter
    public enum ErrorType implements StringRepresentable {

        MAX_COUNT("max_count"),
        MIN_COUNT("min_count"),
        MAX_LAYER_COUNT("max_layer_count"),
        MIN_LAYER_COUNT("min_layer_count");

        final String name;

        ErrorType(String name) {
            this.name = name;
        }

        public static final Codec<ErrorType> CODEC = StringRepresentable.fromEnum(ErrorType::values);

        @Override
        public String getSerializedName() {
            return getName();
        }
    }

    @Override
    public Codec<? extends PatternError> codec() {
        return CODEC;
    }
}
