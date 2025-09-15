package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class BlockTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<Block> provider) {
        provider.addTag(CustomTags.CONCRETE_BLOCK)
                .add(Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE,
                        Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE,
                        Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
                        Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE);
        provider.addTag(CustomTags.CONCRETE_POWDER_BLOCK)
                .add(Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER,
                        Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER,
                        Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER,
                        Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER,
                        Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER,
                        Blocks.BLACK_CONCRETE_POWDER);

        provider.addTag(CustomTags.ENDSTONE_ORE_REPLACEABLES).add(Blocks.END_STONE);

        provider.addTag(CustomTags.TALL_PLANTS)
                .add(Blocks.SUGAR_CANE, Blocks.CACTUS)
                .add(Blocks.TALL_GRASS, Blocks.LARGE_FERN)
                .add(Blocks.BAMBOO, Blocks.BAMBOO_SAPLING)
                .add(Blocks.CHORUS_FLOWER, Blocks.CHORUS_PLANT)
                .add(Blocks.VINE,
                        Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT,
                        Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT)
                .add(Blocks.PITCHER_CROP)
                .addTag(BlockTags.CAVE_VINES)
                .addTag(BlockTags.TALL_FLOWERS)
                .addOptionalTag(new ResourceLocation("forge:cacti"))
                .addOptionalTag(new ResourceLocation("forge:crops/cactus"))
                .addOptionalTag(new ResourceLocation("forge:crops/sugar_cane"))
                .addOptionalTag(new ResourceLocation("forge:reeds"));

        provider.addTag(BlockTags.REPLACEABLE)
                .add(GTMaterials.Oil.getFluid().defaultFluidState().createLegacyBlock().getBlock())
                .add(GTMaterials.OilLight.getFluid().defaultFluidState().createLegacyBlock().getBlock())
                .add(GTMaterials.OilHeavy.getFluid().defaultFluidState().createLegacyBlock().getBlock())
                .add(GTMaterials.RawOil.getFluid().defaultFluidState().createLegacyBlock().getBlock())
                .add(GTMaterials.NaturalGas.getFluid().defaultFluidState().createLegacyBlock().getBlock());

        provider.addTag(BlockTags.MINEABLE_WITH_AXE)
                .add(GTMachines.WOODEN_DRUM.getBlock())
                .add(GTMachines.WOODEN_CRATE.getBlock());

        // always add the wrench/pickaxe tag as a valid tag to mineable/wrench etc.
        provider.addTag(CustomTags.MINEABLE_WITH_WRENCH)
                .addTag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .add(Blocks.PISTON, Blocks.PISTON_HEAD, Blocks.STICKY_PISTON, Blocks.OBSERVER, Blocks.REDSTONE_LAMP,
                        Blocks.REDSTONE_BLOCK, Blocks.IRON_DOOR, Blocks.IRON_TRAPDOOR,
                        Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
                        Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.HOPPER, Blocks.DISPENSER, Blocks.DROPPER,
                        Blocks.LIGHTNING_ROD, Blocks.DAYLIGHT_DETECTOR, Blocks.BELL);
        provider.addTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER)
                .addTag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER);

        provider.addTag(CustomTags.CLEANROOM_FLOORS)
                .addOptionalTag(new ResourceLocation("elevatorid:elevators"))
                .addOptional(new ResourceLocation("enderio:travel_anchor"))
                .addOptional(new ResourceLocation("rftoolsutility:matter_transmitter"))
                .addOptional(new ResourceLocation("rftoolsutility:matter_receiver"))
                .addOptional(new ResourceLocation("rftoolsutility:dialing_device"))
                .addOptional(new ResourceLocation("travelanchors:travel_anchor"));

        provider.addTag(CustomTags.CHARCOAL_PILE_IGNITER_WALLS)
                .addTag(BlockTags.DIRT) // any dirt blocks
                .remove(Blocks.MOSS_BLOCK, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS) // except moss and mud
                .add(Blocks.DIRT_PATH) // path blocks
                .addTag(Tags.Blocks.SAND).addTag(BlockTags.SAND) // any sand blocks
                .addTag(BlockTags.TERRACOTTA); // any terracotta

        provider.addTag(CustomTags.CLEANROOM_DOORS).add(Blocks.IRON_DOOR).addTag(BlockTags.WOODEN_DOORS);
    }
}
