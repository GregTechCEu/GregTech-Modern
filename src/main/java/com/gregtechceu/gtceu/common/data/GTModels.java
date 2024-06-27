package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.common.block.LampBlock;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author KilaBash
 * @date 2023/7/20
 * @implNote GTModels
 */
public class GTModels {

    public static void createModelBlockState(DataGenContext<Block, ? extends Block> ctx,
                                             RegistrateBlockstateProvider prov, ResourceLocation modelLocation) {
        prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(modelLocation));
    }

    public static void createCrossBlockState(DataGenContext<Block, ? extends Block> ctx,
                                             RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(), prov.models().cross(ForgeRegistries.BLOCKS.getKey(ctx.getEntry()).getPath(),
                prov.blockTexture(ctx.getEntry())));
    }

    public static void cellModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelProvider prov) {
        // empty model
        prov.getBuilder("item/" + prov.name(ctx::getEntry) + "_empty")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", prov.modLoc("item/%s/base".formatted(prov.name(ctx))));

        // filled model
        prov.getBuilder("item/" + prov.name(ctx::getEntry) + "_filled")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
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

    public static <
            T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> overrideModel(ResourceLocation predicate,
                                                                                                                  int modelNumber) {
        if (modelNumber <= 0) return NonNullBiConsumer.noop();
        return (ctx, prov) -> {
            var rootModel = prov.generated(ctx::getEntry, prov.modLoc("item/%s/1".formatted(prov.name(ctx))));
            for (int i = 0; i < modelNumber; i++) {
                var subModelBuilder = prov.getBuilder("item/" + prov.name(ctx::getEntry) + "/" + i)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"));
                subModelBuilder.texture("layer0", prov.modLoc("item/%s/%d".formatted(prov.name(ctx), i + 1)));

                rootModel = rootModel.override().predicate(predicate, i / 100f)
                        .model(new ModelFile.UncheckedModelFile(prov.modLoc("item/%s/%d".formatted(prov.name(ctx), i))))
                        .end();
            }
        };
    }

    public static void createTextureModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelProvider prov,
                                          ResourceLocation texture) {
        prov.generated(ctx, texture);
    }

    public static void rubberTreeSaplingModel(DataGenContext<Item, BlockItem> context,
                                              RegistrateItemModelProvider provider) {
        provider.generated(context, provider.modLoc("block/" + provider.name(context)));
    }

    public static void longDistanceItemPipeModel(DataGenContext<Block, ? extends Block> ctx,
                                                 RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(),
                prov.models().cubeAll("long_distance_item_pipeline", prov.modLoc("block/pipe/ld_item_pipe/block")));
    }

    public static void longDistanceFluidPipeModel(DataGenContext<Block, ? extends Block> ctx,
                                                  RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(),
                prov.models().cubeAll("long_distance_fluid_pipeline", prov.modLoc("block/pipe/ld_fluid_pipe/block")));
    }

    public static NonNullBiConsumer<DataGenContext<Block, LampBlock>, RegistrateBlockstateProvider> lampModel(DyeColor color,
                                                                                                              boolean border) {
        return (ctx, prov) -> {
            final String borderPart = (border ? "" : "_borderless");
            ModelFile parentOn = prov.models().getExistingFile(prov.modLoc("block/lamp" + borderPart));
            ModelFile parentOff = prov.models().getExistingFile(prov.modLoc("block/lamp" + borderPart + "_off"));

            prov.getVariantBuilder(ctx.getEntry())
                    .forAllStates(state -> {
                        if (state.getValue(LampBlock.LIGHT)) {
                            ModelBuilder<?> model = prov.models()
                                    .getBuilder(ctx.getName() + (state.getValue(LampBlock.BLOOM) ? "_bloom" : ""))
                                    .parent(parentOn);
                            if (border) {
                                model.texture("active", "block/lamps/" + color.getName());
                                if (state.getValue(LampBlock.BLOOM)) {
                                    model.texture("active_overlay", "block/lamps/" + color.getName() + "_emissive");
                                } else {
                                    model.texture("active_overlay", "block/lamps/" + color.getName());
                                }
                            } else {
                                if (state.getValue(LampBlock.BLOOM)) {
                                    model.texture("active",
                                            "block/lamps/" + color.getName() + "_borderless_emissive");
                                } else {
                                    model.texture("active",
                                            "block/lamps/" + color.getName() + "_borderless");
                                }
                            }
                            return ConfiguredModel.builder()
                                    .modelFile(model)
                                    .build();
                        } else {
                            return ConfiguredModel.builder()
                                    .modelFile(prov.models()
                                            .getBuilder(ctx.getName() + "_off")
                                            .parent(parentOff)
                                            .texture("inactive",
                                                    "block/lamps/" + color.getName() + "_off" + borderPart))
                                    .build();
                        }
                    });
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> randomRotatedModel(ResourceLocation texturePath) {
        return (ctx, prov) -> {
            Block block = ctx.getEntry();
            ModelFile cubeAll = prov.models().cubeAll(ctx.getName(), texturePath);
            ModelFile cubeMirroredAll = prov.models().singleTexture(ctx.getName() + "_mirrored",
                    prov.mcLoc(ModelProvider.BLOCK_FOLDER + "/cube_mirrored_all"), "all", texturePath);
            ConfiguredModel[] models = ConfiguredModel.builder()
                    .modelFile(cubeAll)
                    .rotationY(0)
                    .nextModel()
                    .modelFile(cubeAll)
                    .rotationY(180)
                    .nextModel()
                    .modelFile(cubeMirroredAll)
                    .rotationY(0)
                    .nextModel()
                    .modelFile(cubeMirroredAll)
                    .rotationY(180)
                    .build();
            prov.simpleBlock(block, models);
        };
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
                if (fluidEntry != null && fluidEntry.getBuilder() != null) {
                    if (fluidEntry.getBuilder().still() == null) {
                        ResourceLocation foundTexture = key.getIconType().getBlockTexturePath(iconSet, false);
                        fluidEntry.getBuilder().still(foundTexture);
                    }
                    if (fluidEntry.getBuilder().flowing() == null) {
                        fluidEntry.getBuilder().flowing(fluidEntry.getBuilder().still());
                    }
                    MixinHelpers.addFluidTexture(material, fluidEntry);
                }

                // bucket models.
                Fluid fluid = storage.get(key);
                if (fluid instanceof GTFluid gtFluid) {
                    // read the base bucket model JSON
                    JsonObject original;
                    try (BufferedReader reader = Minecraft.getInstance().getResourceManager()
                            .openAsReader(GTCEu.id("models/item/bucket/bucket.json"))) {
                        original = GsonHelper.parse(reader, true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    JsonObject newJson = original.deepCopy();
                    newJson.addProperty("fluid", BuiltInRegistries.FLUID.getKey(gtFluid).toString());
                    if (gtFluid.getFluidType().isLighterThanAir()) {
                        newJson.addProperty("flip_gas", true);
                    }
                    if (gtFluid.getFluidType().getLightLevel() > 0) {
                        newJson.addProperty("apply_fluid_luminosity", true);
                    }

                    GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(gtFluid.getBucket()), newJson);
                }
            }
        }
    }
}
