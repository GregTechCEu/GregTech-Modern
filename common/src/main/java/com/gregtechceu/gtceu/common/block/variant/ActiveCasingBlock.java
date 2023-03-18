package com.gregtechceu.gtceu.common.block.variant;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.VariantActiveBlock;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/3/9
 * @implNote ActiveBlock
 */
public class ActiveCasingBlock extends VariantActiveBlock<ActiveCasingBlock.CasingType> {

    public ActiveCasingBlock(Properties properties) {
        super(properties);
    }

    public enum CasingType implements StringRepresentable {
        ENGINE_INTAKE_CASING("engine_intake", GTCEu.id("block/variant/engine_intake"), GTCEu.id("block/variant/engine_intake_active")),
        EXTREME_ENGINE_INTAKE_CASING("extreme_engine_intake", GTCEu.id("block/variant/extreme_engine_intake"), GTCEu.id("block/variant/extreme_engine_intake_active")),
        ASSEMBLY_LINE_CASING("assembly_line", GTCEu.id("block/variant/assembly_line"), GTCEu.id("block/variant/assembly_line_active"));

        @Getter
        private final String name;
        @Getter
        private final ResourceLocation model;
        @Getter
        private final ResourceLocation activeModel;

        CasingType(String name, ResourceLocation model, ResourceLocation activeModel) {
            this.name = name;
            this.model = model;
            this.activeModel = activeModel;
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
