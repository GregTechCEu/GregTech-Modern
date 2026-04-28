package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.common.block.StoneTypes;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class BlockTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<Block> provider) {
        var speedConcretes = tag(provider, CustomTags.VERY_FAST_WALKABLE_BLOCKS);
        GTBlocks.STONE_BLOCKS.column(StoneTypes.CONCRETE_LIGHT)
                .forEach((type, block) -> speedConcretes.add(block.get()));
        GTBlocks.STONE_BLOCKS.column(StoneTypes.CONCRETE_DARK)
                .forEach((type, block) -> speedConcretes.add(block.get()));

        var studs = tag(provider, CustomTags.FAST_WALKABLE_BLOCKS);
        GTBlocks.STUDS.forEach((color, block) -> studs.add(block.get()));

        tag(provider, CustomTags.ENDSTONE_ORE_REPLACEABLES)
                .addTag(Tags.Blocks.END_STONES);

        tag(provider, CustomTags.NEEDS_NEUTRONIUM_TOOL);
        tag(provider, CustomTags.NEEDS_DURANIUM_TOOL);

        @SuppressWarnings("unchecked")
        TagKey<Block>[] newToolRequirements = new TagKey[] {
                CustomTags.NEEDS_NEUTRONIUM_TOOL,
                CustomTags.NEEDS_DURANIUM_TOOL
        };
        @SuppressWarnings("unchecked")
        TagKey<Block>[] incorrectForToolTags = new TagKey[] {
                BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
                BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                BlockTags.INCORRECT_FOR_IRON_TOOL,
                BlockTags.INCORRECT_FOR_GOLD_TOOL,
                BlockTags.INCORRECT_FOR_STONE_TOOL,
                BlockTags.INCORRECT_FOR_WOODEN_TOOL
        };
        for (TagKey<Block> tag : incorrectForToolTags) {
            tag(provider, tag).addTags(newToolRequirements);
        }
        // do these manually
        tag(provider, CustomTags.INCORRECT_FOR_NEUTRONIUM_TOOL);
        tag(provider, CustomTags.INCORRECT_FOR_DURANIUM_TOOL).addTag(CustomTags.NEEDS_NEUTRONIUM_TOOL);

        // this is awful. I don't care, though.
        tag(provider, BlockTags.REPLACEABLE)
                .add(GTMaterials.Oil.getFluid().defaultFluidState().createLegacyBlock().getBlock())
                .add(GTMaterials.LightOil.getFluid().defaultFluidState().createLegacyBlock().getBlock())
                .add(GTMaterials.HeavyOil.getFluid().defaultFluidState().createLegacyBlock().getBlock())
                .add(GTMaterials.RawOil.getFluid().defaultFluidState().createLegacyBlock().getBlock())
                .add(GTMaterials.NaturalGas.getFluid().defaultFluidState().createLegacyBlock().getBlock());

        tag(provider, BlockTags.MINEABLE_WITH_AXE)
                .add(GTMachines.WOODEN_DRUM.getBlock())
                .add(GTMachines.WOODEN_CRATE.getBlock());

        // always add the wrench/pickaxe tag as a valid tag to mineable/wrench etc.
        tag(provider, CustomTags.MINEABLE_WITH_WRENCH)
                .addTag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH);
        tag(provider, CustomTags.MINEABLE_WITH_WIRE_CUTTER)
                .addTag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER);

        tag(provider, CustomTags.CLEANROOM_FLOORS);
        TagProviderCompat.addOptionalTag(provider, CustomTags.CLEANROOM_FLOORS,
                Identifier.fromNamespaceAndPath("elevatorid", "elevators"));
        TagProviderCompat.addOptional(provider, CustomTags.CLEANROOM_FLOORS,
                Identifier.fromNamespaceAndPath("enderio", "travel_anchor"));
        TagProviderCompat.addOptional(provider, CustomTags.CLEANROOM_FLOORS,
                Identifier.fromNamespaceAndPath("rftoolsutility", "matter_transmitter"));
        TagProviderCompat.addOptional(provider, CustomTags.CLEANROOM_FLOORS,
                Identifier.fromNamespaceAndPath("rftoolsutility", "matter_receiver"));
        TagProviderCompat.addOptional(provider, CustomTags.CLEANROOM_FLOORS,
                Identifier.fromNamespaceAndPath("rftoolsutility", "dialing_device"));
        TagProviderCompat.addOptional(provider, CustomTags.CLEANROOM_FLOORS,
                Identifier.fromNamespaceAndPath("travelanchors", "travel_anchor"));

        tag(provider, CustomTags.CHARCOAL_PILE_IGNITER_WALLS)
                .addTag(BlockTags.DIRT) // any dirt blocks
                .remove(Blocks.MOSS_BLOCK, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS) // except moss and mud
                .add(Blocks.DIRT_PATH) // path blocks
                .addTag(Tags.Blocks.SANDS).addTag(BlockTags.SAND) // any sand blocks
                .addTag(BlockTags.TERRACOTTA); // any terracotta

        tag(provider, CustomTags.CLEANROOM_DOORS).add(Blocks.IRON_DOOR).addTag(BlockTags.WOODEN_DOORS);
    }

    private static net.minecraft.data.tags.TagAppender<Block, Block> tag(
                                                                         RegistrateTagsProvider.IntrinsicImpl<Block> provider,
                                                                         TagKey<Block> tagKey) {
        return TagProviderCompat.tag(provider, tagKey);
    }
}
