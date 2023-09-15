package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.RendererBlock;
import com.gregtechceu.gtceu.api.item.RendererBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.lowdragmc.lowdraglib.Platform;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.Map;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTBlocks.createCasingBlock;

public class GCyMBlocks {

    public static void init() {}

    public static final BlockEntry<Block> CASING_NONCONDUCTING = createCasingBlock("nonconducting_casing", GTCEu.id("block/casings/gcym/nonconducting_casing"));
    public static final BlockEntry<Block> CASING_VIBRATION_SAFE = createCasingBlock("vibration_safe_casing", GTCEu.id("block/casings/gcym/vibration_safe_casing"));
    public static final BlockEntry<Block> CASING_WATERTIGHT = createCasingBlock("watertight_casing", GTCEu.id("block/casings/gcym/watertight_casing"));
    public static final BlockEntry<Block> CASING_SECURE_MACERATION = createCasingBlock("secure_maceration_casing", GTCEu.id("block/casings/gcym/secure_maceration_casing"));
    public static final BlockEntry<Block> CASING_HIGH_TEMPERATURE_SMELTING = createCasingBlock("high_temperature_smelting_casing", GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"));
    public static final BlockEntry<Block> CASING_LASER_SAFE_ENGRAVING = createCasingBlock("laser_safe_engraving_casing", GTCEu.id("block/casings/gcym/laser_safe_engraving_casing"));
    public static final BlockEntry<Block> CASING_LARGE_SCALE_ASSEMBLING = createCasingBlock("large_scale_assembler_casing", GTCEu.id("block/casings/gcym/large_scale_assembling_casing"));
    public static final BlockEntry<Block> CASING_SHOCK_PROOF = createCasingBlock("shock_proof_cutting_casing", GTCEu.id("block/casings/gcym/shock_proof_cutting_casing"));
    public static final BlockEntry<Block> CASING_STRESS_PROOF = createCasingBlock("stress_proof_casing", GTCEu.id("block/casings/gcym/stress_proof_casing"));
    public static final BlockEntry<Block> CASING_CORROSION_PROOF = createCasingBlock("corrosion_proof_casing", GTCEu.id("block/casings/gcym/corrosion_proof_casing"));
    public static final BlockEntry<Block> CASING_REACTION_SAFE = createCasingBlock("reaction_safe_mixing_casing", GTCEu.id("block/casings/gcym/reaction_safe_mixing_casing"));
    public static final BlockEntry<Block> CASING_ATOMIC = createCasingBlock("atomic_casing", GTCEu.id("block/casings/gcym/atomic_casing"));
    public static final BlockEntry<Block> CASING_INDUSTRIAL_STEAM = createCasingBlock("industrial_steam_casing", GTCEu.id("block/casings/gcym/industrial_steam_casing"));

    public static final BlockEntry<Block> SLICING_BLADES = REGISTRATE.block("slicing_blades", p -> (Block) new RendererBlock(p,
                    Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                            Map.of("bottom",  GTCEu.id("block/casings/slicing_blades/bottom"),
                                    "top",  GTCEu.id("block/casings/slicing_blades/top"),
                                    "side",  GTCEu.id("block/casings/slicing_blades/side"))) : null))
            .lang("Slicing Blades")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.sound(SoundType.METAL).mapColor(MapColor.METAL))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(NonNullBiConsumer.noop())
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .item(RendererBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public static final BlockEntry<Block> MOLYBDENUM_DISILICIDE_COIL_BLOCK = REGISTRATE
            .block("molybdenum_disilicide_coil_block", Block::new)
            .lang("Molybdenum Disilicide Coil Block")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .item()
            .build()
            .register();

    public static final BlockEntry<Block> ELECTROLYTIC_CELL = REGISTRATE
            .block("electrolytic_cell", Block::new)
            .lang("Electrolytic Cell")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .simpleItem()
            .register();

    public static final BlockEntry<Block> CRUSHING_WHEELS = REGISTRATE
            .block("crushing_wheels", Block::new)
            .lang("Crushing Wheels")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .simpleItem()
            .register();

    public static final BlockEntry<Block> HEAT_VENT = REGISTRATE
            .block("heat_vent", Block::new)
            .lang("Heat Vent")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .simpleItem()
            .register();
}
