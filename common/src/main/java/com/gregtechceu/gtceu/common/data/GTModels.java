package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

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

    /**
     * register fluid models for materials
     */
    public static void registerMaterialFluidModels() {
        for (var material : GTRegistries.MATERIALS) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);
            if (fluidProperty == null) continue;

            for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                FluidStorage storage = fluidProperty.getStorage();
                Fluid fluid = storage.get(key);
                if (fluid instanceof GTFluid gtFluid) {
                    FluidStack testFor = FluidStack.create(gtFluid, FluidHelper.getBucket());
                    GTDynamicResourcePack.addItemModel(
                            BuiltInRegistries.ITEM.getKey(gtFluid.getBucket()),
                            new DelegatedModel(GTCEu.id("item/bucket/" + (FluidHelper.isLighterThanAir(testFor) ? "bucket_gas" : "bucket")))
                    );
                }
            }
        }
    }
}
