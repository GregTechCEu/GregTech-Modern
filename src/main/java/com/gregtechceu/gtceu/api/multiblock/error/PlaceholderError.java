package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;

public class PlaceholderError extends PatternError {

    public static Codec<PlaceholderError> CODEC = Codec.unit(PlaceholderError::instance);

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("placeholder_error"), CODEC);

    private static final PlaceholderError INSTANCE = new PlaceholderError();

    public static PlaceholderError instance() {
        return INSTANCE;
    }

    private PlaceholderError() {}

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> parent.child(Text.str("Placeholder error").asWidget());
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
