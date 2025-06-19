package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record BoilerFireboxType(String name, ResourceLocation bottom, ResourceLocation top, ResourceLocation side) {

    public static final Map<String, BoilerFireboxType> FIREBOX_TYPES = new HashMap<>();
    public static final Codec<BoilerFireboxType> CODEC = Codec.STRING.comapFlatMap(name -> {
        BoilerFireboxType type = FIREBOX_TYPES.get(name);
        if (type != null) {
            return DataResult.success(type);
        } else {
            return DataResult.error(() -> "A firebox type named " + name + " does not exist");
        }
    }, BoilerFireboxType::name);

    public BoilerFireboxType {
        FIREBOX_TYPES.put(name, this);
    }

    public static BoilerFireboxType BRONZE_FIREBOX = new BoilerFireboxType("bronze_firebox",
            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
            GTCEu.id("block/casings/firebox/machine_casing_firebox_bronze"));
    public static BoilerFireboxType STEEL_FIREBOX = new BoilerFireboxType("steel_firebox",
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
            GTCEu.id("block/casings/firebox/machine_casing_firebox_steel"));
    public static BoilerFireboxType TITANIUM_FIREBOX = new BoilerFireboxType("titanium_firebox",
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
            GTCEu.id("block/casings/firebox/machine_casing_firebox_titanium"));
    public static BoilerFireboxType TUNGSTENSTEEL_FIREBOX = new BoilerFireboxType("tungstensteel_firebox",
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
            GTCEu.id("block/casings/firebox/machine_casing_firebox_tungstensteel"));

    @NotNull
    @Override
    public String toString() {
        return name();
    }
}
