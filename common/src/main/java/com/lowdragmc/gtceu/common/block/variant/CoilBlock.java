package com.lowdragmc.gtceu.common.block.variant;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.block.VariantActiveBlock;
import com.lowdragmc.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.gtceu.common.libs.GTMaterials;
import com.lowdragmc.gtceu.utils.GTUtil;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote CoilBlock
 */
@ParametersAreNonnullByDefault
public class CoilBlock extends VariantActiveBlock<CoilBlock.CoilType> {

    public CoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@Nullable  CoilType coilType, ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (coilType != null) {
            if (GTUtil.isShiftDown()) {
                int coilTier = coilType.ordinal();
                tooltip.add(Component.translatable("tile.wire_coil.tooltip_smelter"));
                tooltip.add(Component.translatable("tile.wire_coil.tooltip_parallel_smelter", coilType.level * 32));
                tooltip.add(Component.translatable("tile.wire_coil.tooltip_energy_smelter", Math.max(1, 16 / coilType.energyDiscount)));
                tooltip.add(Component.translatable("tile.wire_coil.tooltip_pyro"));
                tooltip.add(Component.translatable("tile.wire_coil.tooltip_speed_pyro", coilTier == 0 ? 75 : 50 * (coilTier + 1)));
                tooltip.add(Component.translatable("tile.wire_coil.tooltip_cracking"));
                tooltip.add(Component.translatable("tile.wire_coil.tooltip_energy_cracking", 100 - 10 * coilTier));
            } else {
                tooltip.add(Component.translatable("tile.wire_coil.tooltip_extended_info"));
            }
        }
    }

    public enum CoilType implements StringRepresentable {
        CUPRONICKEL("cupronickel", 1800, 1, 1, GTMaterials.Cupronickel, GTCEu.id("block/casings/coils/machine_coil_cupronickel")),
        KANTHAL("kanthal", 2700, 2, 1, GTMaterials.Kanthal, GTCEu.id("block/casings/coils/machine_coil_kanthal")),
        NICHROME("nichrome", 3600, 2, 2, GTMaterials.Nichrome, GTCEu.id("block/casings/coils/machine_coil_nichrome")),
        TUNGSTENSTEEL("tungstensteel", 4500, 4, 2, GTMaterials.TungstenSteel, GTCEu.id("block/casings/coils/machine_coil_tungstensteel")),
        HSS_G("hss_g", 5400, 4, 4, GTMaterials.HSSG, GTCEu.id("block/casings/coils/machine_coil_hssg")),
        NAQUADAH("naquadah", 7200, 8, 4, GTMaterials.Naquadah, GTCEu.id("block/casings/coils/machine_coil_naquadah")),
        TRINIUM("trinium", 9001, 8, 8, GTMaterials.Trinium, GTCEu.id("block/casings/coils/machine_coil_trinium")),
        TRITANIUM("tritanium", 10800, 16, 8, GTMaterials.Tritanium, GTCEu.id("block/casings/coils/machine_coil_tritanium"));

        @Getter
        private final String name;
        //electric blast furnace properties
        @Getter
        private final int coilTemperature;
        //multi smelter properties
        @Getter
        private final int level;
        @Getter
        private final int energyDiscount;
        @Getter
        private final Material material;
        @Getter
        private final ResourceLocation texture;

        CoilType(String name, int coilTemperature, int level, int energyDiscount, Material material, ResourceLocation texture) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.material = material;
            this.texture = texture;
        }

        public int getTier() {
            return this.ordinal();
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
