package com.gregtechceu.gtceu.common.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @author KilaBash
 * @date 2023/7/20
 * @implNote GTModels
 */
public class GTModels {
    @ExpectPlatform
    public static void createModelBlockState(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov, ResourceLocation modelLocation) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void createCrossBlockState(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void cellModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelProvider prov) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> overrideModel(ResourceLocation predicate, int modelNumber) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void createTextureModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelProvider prov, ResourceLocation texture) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void rubberTreeSaplingModel(DataGenContext<Item, BlockItem> context, RegistrateItemModelProvider provider) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void longDistanceItemPipeModel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void longDistanceFluidPipeModel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        throw new AssertionError();
    }
}
