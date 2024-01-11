package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/3/9
 * @implNote BoilerFireBoxCasingBlock
 */
public record BoilerFireboxType(@Getter String name, @Getter ResourceLocation bottom, @Getter ResourceLocation top, @Getter ResourceLocation side) {

    public static BoilerFireboxType BRONZE_FIREBOX = new BoilerFireboxType("bronze_firebox", GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks")
            , GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks")
            , GTCEu.id("block/casings/firebox/machine_casing_firebox_bronze"));
    public static BoilerFireboxType STEEL_FIREBOX = new BoilerFireboxType("steel_firebox", GTCEu.id("block/casings/solid/machine_casing_solid_steel")
            , GTCEu.id("block/casings/solid/machine_casing_solid_steel")
            , GTCEu.id("block/casings/firebox/machine_casing_firebox_steel"));
    public static BoilerFireboxType TITANIUM_FIREBOX = new BoilerFireboxType("titanium_firebox", GTCEu.id("block/casings/solid/machine_casing_stable_titanium")
            , GTCEu.id("block/casings/solid/machine_casing_stable_titanium")
            , GTCEu.id("block/casings/firebox/machine_casing_firebox_titanium"));
    public static BoilerFireboxType TUNGSTENSTEEL_FIREBOX = new BoilerFireboxType("tungstensteel_firebox", GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel")
            , GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel")
            , GTCEu.id("block/casings/firebox/machine_casing_firebox_tungstensteel"));

    @Nonnull
    @Override
    public String toString() {
        return name();
    }
}
