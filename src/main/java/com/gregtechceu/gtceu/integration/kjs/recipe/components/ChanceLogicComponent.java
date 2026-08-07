package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class ChanceLogicComponent implements RecipeComponent<ChanceLogic> {

    // spotless:off
    public static final RecipeComponentType<ChanceLogic> CHANCE_LOGIC = RecipeComponentType.unit(GTCEu.id("chance_logic"), new ChanceLogicComponent());
    // spotless:on

    @Override
    public Codec<ChanceLogic> codec() {
        return GTRegistries.CHANCE_LOGICS.byNameCodec();
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ChanceLogic.class)
                .or(TypeInfo.of(ResourceLocation.class))
                .or(TypeInfo.of(String.class));
    }

    @Override
    public String toString() {
        return "chance_logic";
    }

    @Override
    public RecipeComponentType<ChanceLogic> type() {
        return CHANCE_LOGIC;
    }
}
