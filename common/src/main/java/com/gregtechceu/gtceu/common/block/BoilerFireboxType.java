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

    public static BoilerFireboxType BRONZE_FIREBOX = new BoilerFireboxType("bronze_firebox",
            GTCEu.id("block/casings/solid/steam_machine_casing"),
            GTCEu.id("block/casings/solid/steam_machine_casing"),
            GTCEu.id("block/casings/firebox/bronze_firebox_casing")
    );
    public static BoilerFireboxType STEEL_FIREBOX = new BoilerFireboxType("steel_firebox",
            GTCEu.id("block/casings/solid/solid_machine_casing"),
            GTCEu.id("block/casings/solid/solid_machine_casing"),
            GTCEu.id("block/casings/firebox/steel_firebox_casing")
    );
    public static BoilerFireboxType TITANIUM_FIREBOX = new BoilerFireboxType("titanium_firebox",
            GTCEu.id("block/casings/solid/stable_machine_casing"),
            GTCEu.id("block/casings/solid/stable_machine_casing"),
            GTCEu.id("block/casings/firebox/titanium_firebox_casing")
    );
    public static BoilerFireboxType TUNGSTENSTEEL_FIREBOX = new BoilerFireboxType("tungstensteel_firebox",
            GTCEu.id("block/casings/solid/robust_machine_casing"),
            GTCEu.id("block/casings/solid/robust_machine_casing"),
            GTCEu.id("block/casings/firebox/tungstensteel_firebox_casing")
    );

    @Nonnull
    @Override
    public String toString() {
        return name();
    }
}
