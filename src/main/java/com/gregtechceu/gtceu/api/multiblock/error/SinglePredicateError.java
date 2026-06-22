package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class SinglePredicateError extends PatternError {

    public static final Codec<SinglePredicateError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SinglePredicateError.ErrorType.CODEC.fieldOf("error_type").forGetter(e -> e.type),
            Codec.INT.fieldOf("actual_count").forGetter(e -> e.actualCount),
            Codec.INT.fieldOf("pred_min_count").forGetter(e -> e.predMinCount),
            Codec.INT.fieldOf("pred_max_count").forGetter(e -> e.predMaxCount),
            Codec.INT.fieldOf("pred_min_layer_count").forGetter(e -> e.predMinLayerCount),
            Codec.INT.fieldOf("pred_max_layer_count").forGetter(e -> e.predMaxLayerCount),
            Codec.STRING.fieldOf("name").forGetter(e -> e.debugName),
            Codec.list(BlockInfo.CODEC).fieldOf("candidates").forGetter(e -> e.candidates))
            .apply(instance, SinglePredicateError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("single_predicate_error"), CODEC);

    public final ErrorType type;
    public final int actualCount;
    // Fields from BasePredicate that we need
    public final List<BlockInfo> candidates;
    public final int predMinCount;
    public final int predMaxCount;
    public final int predMinLayerCount;
    public final int predMaxLayerCount;
    public final String debugName;

    public SinglePredicateError(BasePredicate failingPredicate, ErrorType type, int actualCount) {
        this(type, actualCount, failingPredicate.minCount, failingPredicate.maxCount, failingPredicate.minSliceCount,
                failingPredicate.maxSliceCount, failingPredicate.getPredicateName(),
                failingPredicate.getCandidates());
    }

    public SinglePredicateError(ErrorType type, int actualCount, int minCount, int maxCount, int minLayerCount,
                                int maxLayerCount, String name, List<BlockInfo> candidates) {
        super(null, Collections.singletonList(candidates));
        this.type = type;
        this.actualCount = actualCount;
        this.candidates = candidates;
        this.predMinCount = minCount;
        this.predMaxCount = maxCount;
        this.predMinLayerCount = minLayerCount;
        this.predMaxLayerCount = maxLayerCount;
        this.debugName = name;
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            parent.child(Text.of(Component.translatable(debugName)).asWidget());
            switch (type) {
                case MAX_COUNT -> {
                    parent.child(Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.max_count",
                            predMaxCount, actualCount)).asWidget());
                }
                case MIN_COUNT -> {
                    parent.child(Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.min_count",
                            predMinCount, actualCount)).asWidget());
                }
                case MAX_LAYER_COUNT -> {
                    parent.child(
                            Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.max_layer_count",
                                    predMaxLayerCount, actualCount)).asWidget());
                }
                case MIN_LAYER_COUNT -> {
                    parent.child(
                            Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.min_layer_count",
                                    predMinLayerCount, actualCount)).asWidget());
                }
            }
            parent.child(new ContextMenuButton<>("predicate")
                    .menuList(l -> l
                            .maxSize(40)
                            .children(candidates, candidate -> {
                                return new ItemDrawable(candidate.getItemStackForm()).asWidget()
                                        .tooltip(r -> r.add(candidate.getItemStackForm().getHoverName()));
                            })));
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
    public PatternErrorType type() {
        return TYPE;
    }
}
