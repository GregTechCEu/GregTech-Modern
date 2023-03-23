package com.gregtechceu.gtceu.common.block.variant;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.VariantActiveBlock;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/3/9
 * @implNote BoilerFireBoxCasingBlock
 */
public class BoilerFireBoxCasingBlock extends VariantActiveBlock<BoilerFireBoxCasingBlock.CasingType> {

    public BoilerFireBoxCasingBlock(Properties properties) {
        super(properties);
    }

    public enum CasingType implements AppendableStringRepresentable {
        BRONZE_FIREBOX("bronze_firebox", GTCEu.id("block/casings/solid/machine_bronze_plated_bricks")
                , GTCEu.id("block/casings/solid/machine_bronze_plated_bricks")
                , GTCEu.id("block/casings/firebox/machine_casing_firebox_bronze")),
        STEEL_FIREBOX("steel_firebox", GTCEu.id("block/casings/solid/machine_casing_solid_steel")
                , GTCEu.id("block/casings/solid/machine_casing_solid_steel")
                , GTCEu.id("block/casings/firebox/machine_casing_firebox_steel")),
        TITANIUM_FIREBOX("titanium_firebox", GTCEu.id("block/casings/solid/machine_casing_stable_titanium")
                , GTCEu.id("block/casings/solid/machine_casing_stable_titanium")
                , GTCEu.id("block/casings/firebox/machine_casing_firebox_titanium")),
        TUNGSTENSTEEL_FIREBOX("tungstensteel_firebox", GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel")
                , GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel")
                , GTCEu.id("block/casings/firebox/machine_casing_firebox_tungstensteel"));

        @Getter
        private final String name;
        @Getter
        private final ResourceLocation bottom, top, side;

        CasingType(String name, ResourceLocation bottom, ResourceLocation top, ResourceLocation side) {
            this.name = name;
            this.bottom = bottom;
            this.top = top;
            this.side = side;
        }

        @Nonnull
        @Override
        public String toString() {
            return getName();
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return name;
        }

    }
}
