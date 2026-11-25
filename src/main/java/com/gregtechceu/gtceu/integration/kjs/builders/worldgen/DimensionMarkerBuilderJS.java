package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.integration.kjs.Validator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Setter
@Accessors(fluent = true, chain = true)
public class DimensionMarkerBuilderJS extends BuilderBase<DimensionMarker> {

    private Supplier<Item> iconSupplier;
    private int tier = 0;
    @Nullable
    private String overrideName;

    public DimensionMarkerBuilderJS(ResourceLocation dimKey) {
        super(dimKey);
    }

    @HideFromJS
    public DimensionMarker buildAndRegister() {
        Validator.validate(
                id,
                Validator.errorIfNull(iconSupplier, "icon"),
                Validator.errorIfOutOfRange(tier, "tier", 0, DimensionMarker.MAX_TIER - 1));
        DimensionMarker marker = new DimensionMarker(tier, iconSupplier, overrideName);
        marker.register(id);
        return marker;
    }

    @Override
    public DimensionMarker register() {
        return value = buildAndRegister();
    }
}
