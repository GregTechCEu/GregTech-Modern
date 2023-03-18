package com.lowdragmc.gtceu.common.block.variant;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.GTValues;
import com.lowdragmc.gtceu.api.block.VariantBlock;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote HullCasingBlock
 */
public class HullCasingBlock extends VariantBlock<HullCasingBlock.CasingType> {

    public HullCasingBlock(Properties properties) {
        super(properties);
    }

    public enum CasingType implements StringRepresentable {
        ULV(makeName(GTValues.VOLTAGE_NAMES[0])),
        LV(makeName(GTValues.VOLTAGE_NAMES[1])),
        MV(makeName(GTValues.VOLTAGE_NAMES[2])),
        HV(makeName(GTValues.VOLTAGE_NAMES[3])),
        EV(makeName(GTValues.VOLTAGE_NAMES[4])),
        IV(makeName(GTValues.VOLTAGE_NAMES[5])),
        LuV(makeName(GTValues.VOLTAGE_NAMES[6])),
        ZPM(makeName(GTValues.VOLTAGE_NAMES[7])),
        UV(makeName(GTValues.VOLTAGE_NAMES[8])),
        UHV(makeName(GTValues.VOLTAGE_NAMES[9])),
        UEV(makeName(GTValues.VOLTAGE_NAMES[10])),
        UIV(makeName(GTValues.VOLTAGE_NAMES[11])),
        UXV(makeName(GTValues.VOLTAGE_NAMES[12])),
        OpV(makeName(GTValues.VOLTAGE_NAMES[13])),
        MAX(makeName(GTValues.VOLTAGE_NAMES[14]));

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
            return GTCEu.id("block/casings/voltage/%s/bottom".formatted(name().toLowerCase()));
        }

        public ResourceLocation geTopTexture() {
            return GTCEu.id("block/casings/voltage/%s/top".formatted(name().toLowerCase()));
        }

        public ResourceLocation getSideTexture() {
            return GTCEu.id("block/casings/voltage/%s/side".formatted(name().toLowerCase()));
        }
    }
}
