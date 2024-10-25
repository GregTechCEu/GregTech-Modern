package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;

import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.BlockEntry;

import static com.gregtechceu.gtceu.common.data.GTBlocks.createActiveCasing;
import static com.gregtechceu.gtceu.common.data.GTBlocks.createCasingBlock;

public class GCYMBlocks {

    public static void init() {}

    public static final BlockEntry<Block> CASING_NONCONDUCTING = createCasingBlock("nonconducting_casing",
            GTCEu.id("block/casings/gcym/nonconducting_casing"));
    public static final BlockEntry<Block> CASING_VIBRATION_SAFE = createCasingBlock("vibration_safe_casing",
            GTCEu.id("block/casings/gcym/vibration_safe_casing"));
    public static final BlockEntry<Block> CASING_WATERTIGHT = createCasingBlock("watertight_casing",
            GTCEu.id("block/casings/gcym/watertight_casing"));
    public static final BlockEntry<Block> CASING_SECURE_MACERATION = createCasingBlock("secure_maceration_casing",
            GTCEu.id("block/casings/gcym/secure_maceration_casing"));
    public static final BlockEntry<Block> CASING_HIGH_TEMPERATURE_SMELTING = createCasingBlock(
            "high_temperature_smelting_casing", GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"));
    public static final BlockEntry<Block> CASING_LASER_SAFE_ENGRAVING = createCasingBlock("laser_safe_engraving_casing",
            GTCEu.id("block/casings/gcym/laser_safe_engraving_casing"));
    public static final BlockEntry<Block> CASING_LARGE_SCALE_ASSEMBLING = createCasingBlock(
            "large_scale_assembler_casing", GTCEu.id("block/casings/gcym/large_scale_assembling_casing"));
    public static final BlockEntry<Block> CASING_SHOCK_PROOF = createCasingBlock("shock_proof_cutting_casing",
            GTCEu.id("block/casings/gcym/shock_proof_cutting_casing"));
    public static final BlockEntry<Block> CASING_STRESS_PROOF = createCasingBlock("stress_proof_casing",
            GTCEu.id("block/casings/gcym/stress_proof_casing"));
    public static final BlockEntry<Block> CASING_CORROSION_PROOF = createCasingBlock("corrosion_proof_casing",
            GTCEu.id("block/casings/gcym/corrosion_proof_casing"));
    public static final BlockEntry<Block> CASING_REACTION_SAFE = createCasingBlock("reaction_safe_mixing_casing",
            GTCEu.id("block/casings/gcym/reaction_safe_mixing_casing"));
    public static final BlockEntry<Block> CASING_ATOMIC = createCasingBlock("atomic_casing",
            GTCEu.id("block/casings/gcym/atomic_casing"));
    public static final BlockEntry<Block> CASING_INDUSTRIAL_STEAM = createCasingBlock("industrial_steam_casing",
            GTCEu.id("block/casings/gcym/industrial_steam_casing"));

    public static final BlockEntry<ActiveBlock> SLICING_BLADES = createActiveCasing("slicing_blades",
            "block/variant/slicing_blades");
    public static final BlockEntry<ActiveBlock> MOLYBDENUM_DISILICIDE_COIL_BLOCK = createActiveCasing(
            "molybdenum_disilicide_coil_block", "block/variant/molybdenum_disilicide_coil_block");
    public static final BlockEntry<ActiveBlock> ELECTROLYTIC_CELL = createActiveCasing("electrolytic_cell",
            "block/variant/electrolytic_cell");
    public static final BlockEntry<ActiveBlock> CRUSHING_WHEELS = createActiveCasing("crushing_wheels",
            "block/variant/crushing_wheels");
    public static final BlockEntry<ActiveBlock> HEAT_VENT = createActiveCasing("heat_vent", "block/variant/heat_vent");
}
