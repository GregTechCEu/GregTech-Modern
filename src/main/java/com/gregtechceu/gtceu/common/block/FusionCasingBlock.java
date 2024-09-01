package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.IFusionCasingType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import lombok.Getter;

public class FusionCasingBlock extends ActiveBlock {

    public FusionCasingBlock(Properties properties, IFusionCasingType casingType) {
        super(properties);
    }

    public enum CasingType implements IFusionCasingType, StringRepresentable {

        SUPERCONDUCTING_COIL("superconducting_coil", 2),
        FUSION_COIL("fusion_coil", 2),
        FUSION_CASING("fusion_casing", 2),
        FUSION_CASING_MK2("fusion_casing_mk2", 3),
        FUSION_CASING_MK3("fusion_casing_mk3", 3);

        private final String name;
        @Getter
        private final int harvestLevel;

        CasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Override
        public ResourceLocation getTexture() {
            return GTCEu.id("block/casings/fusion/%s".formatted(this.name));
        }
    }
}
