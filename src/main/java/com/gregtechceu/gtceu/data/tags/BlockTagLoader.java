package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class BlockTagLoader {

    public static void init(RegistrateTagsProvider<Block> provider) {
        create(provider, CustomTags.CONCRETE_BLOCK, Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE,
                Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE, Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE,
                Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE,
                Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE, Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE,
                Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE);
        create(provider, CustomTags.CONCRETE_POWDER_BLOCK, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER,
                Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER,
                Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER,
                Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER,
                Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER,
                Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER);
        create(provider, CustomTags.ENDSTONE_ORE_REPLACEABLES, Blocks.END_STONE);

        create(provider, BlockTags.REPLACEABLE,
                GTMaterials.Oil.getFluid().defaultFluidState().createLegacyBlock().getBlock(),
                GTMaterials.OilLight.getFluid().defaultFluidState().createLegacyBlock().getBlock(),
                GTMaterials.OilHeavy.getFluid().defaultFluidState().createLegacyBlock().getBlock(),
                GTMaterials.RawOil.getFluid().defaultFluidState().createLegacyBlock().getBlock(),
                GTMaterials.NaturalGas.getFluid().defaultFluidState().createLegacyBlock().getBlock());

        provider.addTag(BlockTags.MINEABLE_WITH_AXE)
                .add(TagEntry.element(GTMachines.WOODEN_DRUM.getId()))
                .add(TagEntry.element(GTMachines.WOODEN_CRATE.getId()));

        create(provider, CustomTags.CLEANROOM_FLOORS, GTBlocks.PLASTCRETE.get(), GTBlocks.CLEANROOM_GLASS.get());
    }

    private static void create(RegistrateTagsProvider<Block> provider, TagPrefix prefix, Material material,
                               Block... rls) {
        create(provider, ChemicalHelper.getBlockTag(prefix, material), rls);
    }

    public static void create(RegistrateTagsProvider<Block> provider, TagKey<Block> tagKey, Block... rls) {
        var builder = provider.addTag(tagKey);
        for (Block block : rls) {
            builder.add(BuiltInRegistries.BLOCK.getResourceKey(block).get());
        }
    }

    public static void create(RegistrateTagsProvider<Block> provider, TagKey<Block> tagKey, ResourceLocation... rls) {
        var builder = provider.addTag(tagKey);
        for (ResourceLocation rl : rls) {
            builder.addOptional(rl);
        }
    }
}
