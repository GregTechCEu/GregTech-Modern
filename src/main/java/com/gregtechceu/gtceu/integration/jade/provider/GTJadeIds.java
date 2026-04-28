package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;

public final class GTJadeIds {

    private GTJadeIds() {}

    public static Identifier id(String path) {
        return GTCEu.id(path);
    }

    public static ResourceLocation from(Identifier id) {
        return ResourceLocation.fromIdentifier(id);
    }

    public static ResourceLocation from(ResourceLocation id) {
        return id;
    }

    public static ResourceLocation toResourceLocation(String path) {
        return toResourceLocation(id(path));
    }

    public static ResourceLocation toResourceLocation(Identifier id) {
        return ResourceLocation.fromIdentifier(id);
    }

    public static ResourceLocation toResourceLocation(ResourceLocation id) {
        return id;
    }
}
