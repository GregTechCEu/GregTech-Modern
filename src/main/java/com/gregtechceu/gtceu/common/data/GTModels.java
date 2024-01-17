package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author KilaBash
 * @date 2023/7/20
 * @implNote GTModels
 */
public class GTModels {
    public static void createModelBlockState(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov, ResourceLocation modelLocation) {
        prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(modelLocation));
    }

    public static void createCrossBlockState(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(), prov.models().cross(ForgeRegistries.BLOCKS.getKey(ctx.getEntry()).getPath(), prov.blockTexture(ctx.getEntry())));
    }

    public static void cellModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelProvider prov) {
        // empty model
        prov.getBuilder("item/" + prov.name(ctx::getEntry) + "_empty").parent(new ModelFile.UncheckedModelFile("item/generated"))
            .texture("layer0", prov.modLoc("item/%s/base".formatted(prov.name(ctx))));

        // filled model
        prov.getBuilder("item/" + prov.name(ctx::getEntry) + "_filled").parent(new ModelFile.UncheckedModelFile("item/generated"))
            .texture("layer0", prov.modLoc("item/%s/base".formatted(prov.name(ctx))))
            .texture("layer1", prov.modLoc("item/%s/overlay".formatted(prov.name(ctx))));

        // root model
        prov.generated(ctx::getEntry, prov.modLoc("item/%s/base".formatted(prov.name(ctx))))
            .override().predicate(GTCEu.id("fluid_cell"), 0)
            .model(new ModelFile.UncheckedModelFile(prov.modLoc("item/%s_empty".formatted(prov.name(ctx)))))
            .end()
            .override().predicate(GTCEu.id("fluid_cell"), 1)
            .model(new ModelFile.UncheckedModelFile(prov.modLoc("item/%s_filled".formatted(prov.name(ctx)))))
            .end();
    }

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> overrideModel(ResourceLocation predicate, int modelNumber) {
        if (modelNumber <= 0) return NonNullBiConsumer.noop();
        return (ctx, prov) -> {
            var rootModel = prov.generated(ctx::getEntry, prov.modLoc("item/%s/1".formatted(prov.name(ctx))));
            for (int i = 0; i < modelNumber; i++) {
                var subModelBuilder = prov.getBuilder("item/" + prov.name(ctx::getEntry) + "/" + i).parent(new ModelFile.UncheckedModelFile("item/generated"));
                subModelBuilder.texture("layer0", prov.modLoc("item/%s/%d".formatted(prov.name(ctx), i + 1)));

                rootModel = rootModel.override().predicate(predicate, i / 100f).model(new ModelFile.UncheckedModelFile(prov.modLoc("item/%s/%d".formatted(prov.name(ctx), i)))).end();
            }
        };
    }

    public static void createTextureModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelProvider prov, ResourceLocation texture) {
        prov.generated(ctx, texture);
    }

    public static void rubberTreeSaplingModel(DataGenContext<Item, BlockItem> context, RegistrateItemModelProvider provider) {
        provider.generated(context, provider.modLoc("block/" + provider.name(context)));
    }

    public static void longDistanceItemPipeModel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll("long_distance_item_pipeline", prov.modLoc("block/pipe/ld_item_pipe/block")));
    }

    public static void longDistanceFluidPipeModel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll("long_distance_fluid_pipeline", prov.modLoc("block/pipe/ld_fluid_pipe/block")));
    }

    /**
     * register fluid models for materials
     */
    public static void registerMaterialFluidModels() {
        for (var material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);
            if (fluidProperty == null) continue;
            MaterialIconSet iconSet = material.getMaterialIconSet();

            for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                FluidStorage storage = fluidProperty.getStorage();
                // fluid block models.
                FluidStorage.FluidEntry fluidEntry = storage.getEntry(key);
                if (fluidEntry != null) {
                    if (fluidEntry.getStillTexture() == null) {
                        ResourceLocation foundTexture = key.getIconType().getBlockTexturePath(iconSet, false);
                        fluidEntry.setStillTexture(foundTexture);
                    }
                    if (fluidEntry.getFlowTexture() == null) {
                        fluidEntry.setFlowTexture(fluidEntry.getStillTexture());
                    }
                    MixinHelpers.addFluidTexture(material, fluidEntry);
                }

                // bucket models.
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
