package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.ResourceLocation;

public class ImplicitKubeResourceLocation extends ResourceLocation {

    protected ImplicitKubeResourceLocation(String namespace, String path) {
        super(namespace, path);
    }

    public static ImplicitKubeResourceLocation of(String path) {
        return new ImplicitKubeResourceLocation("kubejs", path);
    }

    public static ResourceLocation toGtceu(ResourceLocation loc) {
        if (loc instanceof ImplicitKubeResourceLocation kube) {
            return GTCEu.id(loc.getPath());
        }
        return loc;
    }
}
