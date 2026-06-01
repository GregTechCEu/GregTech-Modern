package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class SinglePredicateError extends PatternError {

    public final BasePredicate predicate;
    public final ErrorType type;
    public final int actualCount;

    public SinglePredicateError(BasePredicate failingPredicate, ErrorType type, int actualCount) {
        super(null, failingPredicate);
        this.predicate = failingPredicate;
        this.type = type;
        this.actualCount = actualCount;
    }

    @Override
    public List<List<ItemStack>> getCandidates() {
        return Collections.singletonList(predicate.getCandidateStacks());
    }

    @Override
    public PatternErrorUI applyErrorInformation() {
        return (parent) -> {
            Component predName = predicate.getCandidateStacks().get(0).getHoverName();
            switch (type) {
                case MAX_COUNT -> {
                    parent.child(Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.max_count",
                            predicate.maxCount,
                            actualCount)).asWidget());
                    parent.child(Text.of(predName).asWidget());
                }
                case MIN_COUNT -> {
                    parent.child(Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.min_count",
                            predicate.minCount,
                            actualCount)).asWidget());
                    parent.child(Text.of(predName).asWidget());
                }
                case MAX_LAYER_COUNT -> {
                    parent.child(
                            Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.max_layer_count",
                                    predicate.maxLayerCount, actualCount)).asWidget());
                    parent.child(Text.of(predName).asWidget());
                }
                case MIN_LAYER_COUNT -> {
                    parent.child(
                            Text.of(Component.translatable("gtceu.multiblock.pattern.error.limited.min_layer_count",
                                    predicate.minLayerCount, actualCount)).asWidget());
                    parent.child(Text.of(predName).asWidget());
                }
            }
        };
    }

    @Getter
    public enum ErrorType {

        MAX_COUNT("max_count"),
        MIN_COUNT("min_count"),
        MAX_LAYER_COUNT("max_layer_count"),
        MIN_LAYER_COUNT("min_layer_count");

        final String name;

        ErrorType(String name) {
            this.name = name;
        }
    }
}
