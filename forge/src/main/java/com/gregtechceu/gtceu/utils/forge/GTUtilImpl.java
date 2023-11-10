package com.gregtechceu.gtceu.utils.forge;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;

/**
 * @author KilaBash
 * @date 2023/3/17
 * @implNote GTUtilImpl
 */
public class GTUtilImpl {
    public static int getItemBurnTime(Item item) {
        return ForgeHooks.getBurnTime(item.getDefaultInstance(), RecipeType.SMELTING);
    }

    public static long getPumpBiomeModifier(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_NETHER)) {
            return -1;
        }

        if (biome.is(BiomeTags.IS_DEEP_OCEAN)
                || biome.is(BiomeTags.IS_OCEAN)
                || biome.is(BiomeTags.IS_BEACH)
                || biome.is(BiomeTags.IS_RIVER)) {
            return FluidHelper.getBucket();
        } else if (biome.is(Tags.Biomes.IS_SWAMP)
                || biome.is(Tags.Biomes.IS_WET)) {
            return FluidHelper.getBucket() * 4 / 5;
        } else if (biome.is(BiomeTags.IS_JUNGLE)) {
            return FluidHelper.getBucket() * 35 / 100;
        } else if (biome.is(Tags.Biomes.IS_SNOWY)) {
            return FluidHelper.getBucket() * 3 / 10;
        } else if (biome.is(Tags.Biomes.IS_PLAINS)
                || biome.is(BiomeTags.IS_FOREST)) {
            return FluidHelper.getBucket() / 4;
        } else if (biome.is(Tags.Biomes.IS_COLD)) {
            return FluidHelper.getBucket() * 175 / 1000;
        } else if (biome.is(CustomTags.IS_SANDY)) {
            return FluidHelper.getBucket() * 170 / 1000;
        }
        return FluidHelper.getBucket() / 10;
    }
}
