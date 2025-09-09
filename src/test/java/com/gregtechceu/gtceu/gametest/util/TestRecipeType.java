package com.gregtechceu.gtceu.gametest.util;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import lombok.Getter;

public enum TestRecipeType {

    CR_RECIPE_TYPE("cr_tests", TestUtils.createRecipeType("cr_tests")),
    LCR_RECIPE_TYPE("lcr_tests", TestUtils.createRecipeType("lcr_tests")),
    CENTRIFUGE_RECIPE_TYPE("cent_tests", TestUtils.createRecipeType("cent_tests")),
    ROCK_BREAKER_RECIPE_TYPE("rockbreaker_test", TestUtils.createRecipeType("rockbreaker_tests"));

    @Getter
    public final String name;
    @Getter
    public final GTRecipeType type;

    TestRecipeType(String name, GTRecipeType type){
        this.name=name;
        this.type=type;
    }
}
