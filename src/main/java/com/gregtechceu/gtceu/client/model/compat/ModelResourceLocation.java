package com.gregtechceu.gtceu.client.model.compat;

import net.minecraft.resources.Identifier;

public record ModelResourceLocation(Identifier location, String variant) {

    @Override
    public String toString() {
        return location + "#" + variant;
    }
}
