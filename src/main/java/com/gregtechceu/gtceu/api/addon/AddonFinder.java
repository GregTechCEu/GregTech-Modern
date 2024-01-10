package com.gregtechceu.gtceu.api.addon;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.List;

public class AddonFinder {

    protected static List<IGTAddon> cache = null;

    @ExpectPlatform
    public static List<IGTAddon> getAddons() {
        throw new AssertionError();
    }
}
