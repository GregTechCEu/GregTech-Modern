package com.gregtechceu.gtceu.api.machine.trait;

public record RecipeHandlerGroupColor(int color) implements IGroupColor {

    // Note: An un-dyed hatch is the same as an "indistinct" hatch.
    public static final IGroupColor UNDYED = new RecipeHandlerGroupColor(-1);
}
