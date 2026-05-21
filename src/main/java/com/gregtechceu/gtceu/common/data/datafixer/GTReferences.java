package com.gregtechceu.gtceu.common.data.datafixer;

import com.mojang.datafixers.DSL;

public class GTReferences {

    public static final DSL.TypeReference MATERIAL_STACK = () -> "material_stack";
    public static final DSL.TypeReference MATERIAL_NAME = () -> "material_name";

    public static final DSL.TypeReference FLUID_STACK = () -> "fluid_stack";
    public static final DSL.TypeReference FLUID_NAME = () -> "fluid_name";
}