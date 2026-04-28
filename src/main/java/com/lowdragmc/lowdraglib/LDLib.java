package com.lowdragmc.lowdraglib;

import net.minecraft.resources.ResourceLocation;

import java.io.File;

public class LDLib {

    public static final String MOD_ID = "ldlib";

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static File getLDLibDir() {
        return new File("ldlib");
    }
}
