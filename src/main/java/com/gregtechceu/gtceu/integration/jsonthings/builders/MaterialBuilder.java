package com.gregtechceu.gtceu.integration.jsonthings.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;

import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import lombok.Getter;

public class MaterialBuilder extends BaseBuilder<Material, MaterialBuilder> {

    @Getter
    private final Material.Builder internal;

    protected MaterialBuilder(ThingParser<MaterialBuilder> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
        this.internal = new Material.Builder(registryName);
    }

    public static MaterialBuilder begin(ThingParser<MaterialBuilder> ownerParser, ResourceLocation registryName) {
        return new MaterialBuilder(ownerParser, registryName);
    }

    @Override
    protected String getThingTypeDisplayName() {
        return "Material";
    }

    @Override
    protected Material buildInternal() {
        return internal.buildAndRegister();
    }
}
