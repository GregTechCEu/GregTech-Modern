package com.lowdragmc.lowdraglib.jei;

public enum IngredientIO {

    INPUT,
    OUTPUT,
    BOTH,
    CATALYST,
    RENDER_ONLY;

    public com.lowdragmc.lowdraglib2.integration.xei.IngredientIO toLDLib2() {
        return switch (this) {
            case INPUT, BOTH -> com.lowdragmc.lowdraglib2.integration.xei.IngredientIO.INPUT;
            case OUTPUT -> com.lowdragmc.lowdraglib2.integration.xei.IngredientIO.OUTPUT;
            case CATALYST -> com.lowdragmc.lowdraglib2.integration.xei.IngredientIO.CATALYST;
            case RENDER_ONLY -> com.lowdragmc.lowdraglib2.integration.xei.IngredientIO.NONE;
        };
    }
}
