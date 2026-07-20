package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.Validator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.DimensionMarker.MAX_TIER;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class DimensionMarkerBuilder extends BuilderBase<DimensionMarker> {

    private Supplier<Item> iconSupplier;
    private int tier = 0;
    @Nullable
    private String overrideName;

    public DimensionMarkerBuilder(ResourceLocation dimKey) {
        super(dimKey);
    }

    @Override
    public RegistryInfo<DimensionMarker> getRegistryType() {
        return GTRegistryInfo.DIMENSION_MARKER;
    }

    @Override
    public DimensionMarker createObject() {
        Validator.validate(
                id,
                Validator.errorIfNull(iconSupplier, "icon"),
                Validator.errorIfOutOfRange(tier, "tier", 0, MAX_TIER - 1));
        return new DimensionMarker(tier, iconSupplier, overrideName);
    }
}
