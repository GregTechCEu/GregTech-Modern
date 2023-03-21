package com.gregtechceu.gtceu.common.block.variant;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.VariantBlock;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nonnull;

public class SteamCasingBlock extends VariantBlock<SteamCasingBlock.CasingType> {

    public SteamCasingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public enum CasingType implements AppendableStringRepresentable {

        BRONZE_HULL("bronze_hull", "bronze"),
        BRONZE_BRICKS_HULL("bronze_bricks_hull", "bricked_bronze"),
        STEEL_HULL("steel_hull", "steel"),
        STEEL_BRICKS_HULL("steel_bricks_hull", "bricked_steel");

        @Getter
        private final String name;
        private final String type;

        CasingType(String name, String type) {
            this.name = name;
            this.type = type;
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

        public ResourceLocation getBottomTexture() {
            return GTCEu.id("block/casings/steam/%s/bottom".formatted(type.toLowerCase()));
        }

        public ResourceLocation getTopTexture() {
            return GTCEu.id("block/casings/steam/%s/top".formatted(type.toLowerCase()));
        }

        public ResourceLocation getSideTexture() {
            return GTCEu.id("block/casings/steam/%s/side".formatted(type.toLowerCase()));
        }
    }
}
