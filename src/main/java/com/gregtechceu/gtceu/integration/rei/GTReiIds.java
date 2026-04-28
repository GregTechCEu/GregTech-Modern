package com.gregtechceu.gtceu.integration.rei;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;

public final class GTReiIds {

    private GTReiIds() {}

    public static Identifier id(String path) {
        return GTCEu.id(path);
    }

    public static ResourceLocation from(Identifier id) {
        return ResourceLocation.fromIdentifier(id);
    }

    public static ResourceLocation toResourceLocation(String path) {
        return toResourceLocation(id(path));
    }

    public static ResourceLocation toResourceLocation(Identifier id) {
        return ResourceLocation.fromIdentifier(id);
    }
}
