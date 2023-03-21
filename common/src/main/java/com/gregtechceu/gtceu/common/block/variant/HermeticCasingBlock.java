package com.gregtechceu.gtceu.common.block.variant;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.VariantBlock;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nonnull;

public class HermeticCasingBlock extends VariantBlock<HermeticCasingBlock.CasingType> {

    public HermeticCasingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public enum CasingType implements StringRepresentable {
        HERMETIC_ULV(makeName(GTValues.VOLTAGE_NAMES[0])),
        HERMETIC_LV(makeName(GTValues.VOLTAGE_NAMES[1])),
        HERMETIC_MV(makeName(GTValues.VOLTAGE_NAMES[2])),
        HERMETIC_HV(makeName(GTValues.VOLTAGE_NAMES[3])),
        HERMETIC_EV(makeName(GTValues.VOLTAGE_NAMES[4])),
        HERMETIC_IV(makeName(GTValues.VOLTAGE_NAMES[5])),
        HERMETIC_LuV(makeName(GTValues.VOLTAGE_NAMES[6])),
        HERMETIC_ZPM(makeName(GTValues.VOLTAGE_NAMES[7])),
        HERMETIC_UV(makeName(GTValues.VOLTAGE_NAMES[8])),
        HERMETIC_UHV(makeName(GTValues.VOLTAGE_NAMES[9])),
        HERMETIC_UEV(makeName(GTValues.VOLTAGE_NAMES[10])),
        HERMETIC_UIV(makeName(GTValues.VOLTAGE_NAMES[11])),
        HERMETIC_UXV(makeName(GTValues.VOLTAGE_NAMES[12])),
        HERMETIC_OpV(makeName(GTValues.VOLTAGE_NAMES[13])),
        HERMETIC_MAX(makeName(GTValues.VOLTAGE_NAMES[14]));

        @Getter
        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String toString() {
            return getName();
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return this.name;
        }

        private static String makeName(String voltageName) {
            return String.join("_", voltageName.toLowerCase().split(" "));
        }

        public ResourceLocation getBottomTexture() {
            return GTCEu.id("block/casings/voltage/%s/bottom".formatted(name().split("_")[1].toLowerCase()));
        }

        public ResourceLocation getTopTexture() {
            return GTCEu.id("block/casings/voltage/%s/top".formatted(name().split("_")[1].toLowerCase()));
        }

        public ResourceLocation getSideTexture() {
            return GTCEu.id("block/casings/voltage/%s/side".formatted(name().split("_")[1].toLowerCase()));
        }
    }
}
