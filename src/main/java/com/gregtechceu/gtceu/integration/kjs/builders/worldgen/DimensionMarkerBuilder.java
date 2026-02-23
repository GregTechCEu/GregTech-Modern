package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.worldgen.DimensionMarker;
import com.gregtechceu.gtceu.integration.kjs.Validator;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Setter
@Accessors(fluent = true, chain = true)
public class DimensionMarkerBuilder extends BuilderBase<DimensionMarker> {

    private Supplier<Item> iconSupplier;
    private int tier = 0;
    @Nullable
    private Component overrideName;

    public DimensionMarkerBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public DimensionMarker createObject() {
        Validator.validate(
                id,
                Validator.errorIfNull(iconSupplier, "icon"),
                Validator.errorIfOutOfRange(tier, "tier", 0, DimensionMarker.MAX_TIER));
        return new DimensionMarker(tier, iconSupplier, overrideName);
    }
}
