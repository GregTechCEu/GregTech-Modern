package com.lowdragmc.lowdraglib;

import net.minecraft.resources.Identifier;

import java.io.File;

public class LDLib {

    public static final String MOD_ID = "ldlib";

    public static Identifier location(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static File getLDLibDir() {
        return new File("ldlib");
    }
}
