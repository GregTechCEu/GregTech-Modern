package com.gregtechceu.gtceu.common.block.variant;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.VariantBlock;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote CasingBlock
 */
public class CasingBlock extends VariantBlock<CasingBlock.CasingType> {

    public CasingBlock(Properties properties) {
        super(properties);
    }

    public enum CasingType implements StringRepresentable {
        BRONZE_BRICKS("bronze_bricks", GTCEu.id("block/casings/solid/machine_bronze_plated_bricks")),
        PRIMITIVE_BRICKS("primitive_bricks", GTCEu.id("block/casings/solid/machine_primitive_bricks")),
        INVAR_HEATPROOF("invar_heatproof", GTCEu.id("block/casings/solid/machine_casing_heatproof")),
        ALUMINIUM_FROSTPROOF("aluminium_frostproof", GTCEu.id("block/casings/solid/machine_casing_frost_proof")),
        STEEL_SOLID("steel_solid", GTCEu.id("block/casings/solid/machine_casing_solid_steel")),
        STAINLESS_CLEAN("stainless_clean", GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel")),
        TITANIUM_STABLE("titanium_stable", GTCEu.id("block/casings/solid/machine_casing_stable_titanium")),
        TUNGSTENSTEEL_ROBUST("tungstensteel_robust", GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel")),
        COKE_BRICKS("coke_bricks", GTCEu.id("block/casings/solid/machine_coke_bricks")),
        PTFE_INERT("ptfe_inert", GTCEu.id("block/casings/solid/machine_casing_inert_ptfe")),
        HSSE_STURDY("hsse_sturdy", GTCEu.id("block/casings/solid/machine_casing_study_hsse")),
        GRATE_CASING("grate", GTCEu.id("block/casings/pipe/machine_casing_grate")),
        ASSEMBLY_CONTROL("assembly_control", GTCEu.id("block/casings/mechanic/machine_casing_assembly_control")),
        ASSEMBLY_LINE_CASING("assembly_line", GTCEu.id("block/casings/pipe/machine_casing_grate")),
        POLYTETRAFLUOROETHYLENE_PIPE("polytetrafluoroethylene_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_polytetrafluoroethylene")),
        LAMINATED_GLASS("laminated_glass", GTCEu.id("block/casings/transparent/laminated_glass")),
        BRONZE_GEARBOX("bronze_gearbox",  GTCEu.id("block/casings/gearbox/machine_casing_gearbox_bronze")),
        STEEL_GEARBOX("steel_gearbox",  GTCEu.id("block/casings/gearbox/machine_casing_gearbox_steel")),
        STAINLESS_STEEL_GEARBOX("stainless_steel_gearbox",  GTCEu.id("block/casings/gearbox/machine_casing_gearbox_stainless_steel")),
        TITANIUM_GEARBOX("titanium_gearbox",  GTCEu.id("block/casings/gearbox/machine_casing_gearbox_titanium")),
        TUNGSTENSTEEL_GEARBOX("tungstensteel_gearbox",  GTCEu.id("block/casings/gearbox/machine_casing_gearbox_tungstensteel")),

        STEEL_TURBINE_CASING("steel_turbine_casing",  GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel")),
        TITANIUM_TURBINE_CASING("titanium_turbine_casing",  GTCEu.id("block/casings/mechanic/machine_casing_turbine_titanium")),
        STAINLESS_TURBINE_CASING("stainless_turbine_casing",  GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel")),
        TUNGSTENSTEEL_TURBINE_CASING("tungstensteel_turbine_casing",  GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel")),
        BRONZE_PIPE("bronze_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_bronze")),
        STEEL_PIPE("steel_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_steel")),
        TITANIUM_PIPE("titanium_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_titanium")),
        TUNGSTENSTEEL_PIPE("tungstensteel_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_tungstensteel"));

        @Getter
        private final String name;
        @Getter
        private final ResourceLocation texture;

        CasingType(String name, ResourceLocation texture) {
            this.name = name;
            this.texture = texture;
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

    }
}
