package com.gregtechceu.gtceu.api.multiblock.error;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.MapCodec;

public class PlaceholderError extends PatternError {

    public static final MapCodec<PlaceholderError> CODEC = MapCodec.unit(PlaceholderError::instance);

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
        return GTPatternErrors.PLACEHOLDER_ERROR.value();
    }
}
