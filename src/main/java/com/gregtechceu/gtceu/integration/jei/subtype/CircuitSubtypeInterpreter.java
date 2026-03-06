package com.gregtechceu.gtceu.integration.jei.subtype;

import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.world.item.ItemStack;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.jetbrains.annotations.Nullable;

public class CircuitSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

    public static final CircuitSubtypeInterpreter INSTANCE = new CircuitSubtypeInterpreter();

    private CircuitSubtypeInterpreter() {}

    @Override
    public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
        return ingredient.getOrDefault(GTDataComponents.CIRCUIT_CONFIG, -100);
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        int config = ingredient.getOrDefault(GTDataComponents.CIRCUIT_CONFIG, -100);
        if (config == -100) {
            return "";
        }
        return Integer.toString(config);
    }
}
