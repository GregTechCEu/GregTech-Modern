package com.gregtechceu.gtceu.common.block.variant;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.VariantBlock;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nonnull;

public class HermeticCasingBlock extends VariantBlock<HermeticCasingBlock.CasingType> {

    public HermeticCasingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public enum CasingType implements AppendableStringRepresentable {
        HERMETIC_ULV(makeName(GTValues.VOLTAGE_NAMES[0])+"_hermetic"),
        HERMETIC_LV(makeName(GTValues.VOLTAGE_NAMES[1])+"_hermetic"),
        HERMETIC_MV(makeName(GTValues.VOLTAGE_NAMES[2])+"_hermetic"),
        HERMETIC_HV(makeName(GTValues.VOLTAGE_NAMES[3])+"_hermetic"),
        HERMETIC_EV(makeName(GTValues.VOLTAGE_NAMES[4])+"_hermetic"),
        HERMETIC_IV(makeName(GTValues.VOLTAGE_NAMES[5])+"_hermetic"),
        HERMETIC_LuV(makeName(GTValues.VOLTAGE_NAMES[6])+"_hermetic"),
        HERMETIC_ZPM(makeName(GTValues.VOLTAGE_NAMES[7])+"_hermetic"),
        HERMETIC_UV(makeName(GTValues.VOLTAGE_NAMES[8])+"_hermetic"),
        HERMETIC_UHV(makeName(GTValues.VOLTAGE_NAMES[9])+"_hermetic"),
        HERMETIC_UEV(makeName(GTValues.VOLTAGE_NAMES[10])+"_hermetic"),
        HERMETIC_UIV(makeName(GTValues.VOLTAGE_NAMES[11])+"_hermetic"),
        HERMETIC_UXV(makeName(GTValues.VOLTAGE_NAMES[12])+"_hermetic"),
        HERMETIC_OpV(makeName(GTValues.VOLTAGE_NAMES[13])+"_hermetic"),
        HERMETIC_MAX(makeName(GTValues.VOLTAGE_NAMES[14])+"_hermetic");

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
