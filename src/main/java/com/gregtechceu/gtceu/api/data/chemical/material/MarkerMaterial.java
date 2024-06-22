package com.gregtechceu.gtceu.api.data.chemical.material;

import net.minecraft.resources.ResourceLocation;

/**
 * MarkerMaterial is type of material used for generic things like material re-registration and use in recipes.
 * Marker material cannot be used to generate any meta items.
 * Marker material can be used only for marking other materials (re-registering) equal to it and then using it in
 * recipes or in getting items.
 * Marker material is not presented in material registry and cannot be used for persistence.
 */
public final class MarkerMaterial extends Material {

    private final ResourceLocation resourceLocation;

    public MarkerMaterial(ResourceLocation resourceLocation) {
        super(resourceLocation);
        this.resourceLocation = resourceLocation;
        // TODO
        // OreDictUnifier.registerMarkerMaterial(this);
    }

    @Override
    protected void registerMaterial() {}

    @Override
    public void verifyMaterial() {}

    @Override
    // since we're not registered, return overriden name
    public String toString() {
        return resourceLocation.toString();
    }
}
