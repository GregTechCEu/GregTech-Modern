package com.gregtechceu.gtceu.api.gui;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public class SteamTexture {

    private static final String BRONZE = "bronze";
    private static final String STEEL = "steel";

    private final ResourceTexture bronzeTexture;
    private final ResourceTexture steelTexture;

    private SteamTexture(ResourceTexture bronzeTexture, ResourceTexture steelTexture) {
        this.bronzeTexture = bronzeTexture;
        this.steelTexture = steelTexture;
    }

    public static SteamTexture fullImage(String path) {
        return new SteamTexture(
                new ResourceTexture(String.format(path, BRONZE)),
                new ResourceTexture(String.format(path, STEEL)));
    }

    public ResourceTexture get(boolean isHighPressure) {
        return isHighPressure ? steelTexture : bronzeTexture;
    }
}
