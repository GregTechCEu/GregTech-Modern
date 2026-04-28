package com.gregtechceu.gtceu.integration.jei;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;

public final class GTJeiIds {

    private GTJeiIds() {}

    public static ResourceLocation id(String path) {
        return from(GTCEu.id(path));
    }

    public static ResourceLocation from(Identifier id) {
        return ResourceLocation.fromIdentifier(id);
    }
}
