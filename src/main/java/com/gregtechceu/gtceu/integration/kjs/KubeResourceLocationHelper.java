package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.core.IResourceLocationExtensions;

import net.minecraft.resources.ResourceLocation;

public class KubeResourceLocationHelper {
    public static ResourceLocation toGtceu(ResourceLocation loc) {
        return ((IResourceLocationExtensions)(Object)loc).gtm$asNonImplicit();
    }
}
