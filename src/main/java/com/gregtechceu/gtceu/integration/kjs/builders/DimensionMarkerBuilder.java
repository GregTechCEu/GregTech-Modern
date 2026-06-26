package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Setter
@Accessors(fluent = true, chain = true)
public class DimensionMarkerBuilder extends BuilderBase<DimensionMarker> {

    private Supplier<Item> iconSupplier;
    private int tier = 0;
    @Nullable
    private String overrideName;
    private ResourceKey<Level> dimension;
    public DimensionMarkerBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public RegistryInfo<DimensionMarker> getRegistryType() {
        return GTRegistryInfo.DIMENSION_MARKER;
    }

    @Override
    public DimensionMarker createObject() {
        return new DimensionMarker(dimension, tier, iconSupplier, overrideName);
    }
}
