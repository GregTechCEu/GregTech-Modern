package com.gregtechceu.gtceu.common.data.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.*;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.block.*;
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
import net.minecraftforge.client.model.generators.*;

import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.io.BufferedReader;
import java.io.IOException;

public class GTModels {

    public static final ResourceLocation BLANK_TEXTURE = GTCEu.id("block/void");

    // region BLOCK MODELS

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> createModelBlockState(ResourceLocation modelLocation) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(modelLocation));
        };
    }

    public static void createCrossBlockState(DataGenContext<Block, ? extends Block> ctx,
                                             RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(), prov.models().cross(ctx.getName(), prov.blockTexture(ctx.getEntry())));
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
                                    .getBuilder(ctx.getName() +
                                            (state.getValue(GTBlockStateProperties.BLOOM) ? "_bloom" : ""))
                                    .parent(parentOn);
                            if (border) {
                                model.texture("active", "block/lamps/" + color.getName());
                                if (state.getValue(GTBlockStateProperties.BLOOM)) {
                                    model.texture("active_overlay", "block/lamps/" + color.getName() + "_emissive");
                                } else {
                                    model.texture("active_overlay", "block/lamps/" + color.getName());
                                }
                            } else {
                                if (state.getValue(GTBlockStateProperties.BLOOM)) {
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

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> createSidedCasingModel(ResourceLocation texture) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    texture.withSuffix("/side"),
                    texture.withSuffix("/bottom"),
                    texture.withSuffix("/top")));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> cubeAllModel(ResourceLocation texture) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll(ctx.getName(), texture));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> createMachineCasingModel(String tierName) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(),
                    prov.models()
                            .withExistingParent("%s_machine_casing".formatted(tierName),
                                    GTCEu.id("block/cube/tinted/bottom_top"))
                            .texture("bottom", GTCEu.id("block/casings/voltage/%s/bottom".formatted(tierName)))
                            .texture("top", GTCEu.id("block/casings/voltage/%s/top".formatted(tierName)))
                            .texture("side", GTCEu.id("block/casings/voltage/%s/side".formatted(tierName))));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> createHermeticCasingModel(String tierName) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models()
                    .withExistingParent("%s_hermetic_casing".formatted(tierName), GTCEu.id("block/hermetic_casing"))
                    .texture("bot_bottom", GTCEu.id("block/casings/voltage/%s/bottom".formatted(tierName)))
                    .texture("bot_top", GTCEu.id("block/casings/voltage/%s/top".formatted(tierName)))
                    .texture("bot_side", GTCEu.id("block/casings/voltage/%s/side".formatted(tierName))));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> createSteamCasingModel(String material) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/steam/%s/side".formatted(material)),
                    GTCEu.id("block/casings/steam/%s/bottom".formatted(material)),
                    GTCEu.id("block/casings/steam/%s/top".formatted(material))));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, CoilBlock>, RegistrateBlockstateProvider> createCoilModel(ICoilType coilType) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeAll(name, coilType.getTexture());
            ModelFile active = prov.models().withExistingParent(name + "_active", GTCEu.id("block/cube_2_layer/all"))
                    .texture("bot_all", coilType.getTexture())
                    .texture("top_all", coilType.getTexture().withSuffix("_bloom"));
            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false).modelForState().modelFile(inactive)
                    .addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true).modelForState().modelFile(active)
                    .addModel();
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, BatteryBlock>, RegistrateBlockstateProvider> createBatteryBlockModel(IBatteryData batteryData) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/battery/" + batteryData.getBatteryName() + "/side"),
                    GTCEu.id("block/casings/battery/" + batteryData.getBatteryName() + "/top"),
                    GTCEu.id("block/casings/battery/" + batteryData.getBatteryName() + "/top")));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, FusionCasingBlock>, RegistrateBlockstateProvider> createFusionCasingModel(IFusionCasingType casingType) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeAll(name, casingType.getTexture());
            ModelFile active = prov.models().withExistingParent(name + "_active", GTCEu.id("block/cube_2_layer/all"))
                    .texture("bot_all", casingType.getTexture())
                    .texture("top_all", new ResourceLocation(casingType.getTexture() + "_bloom"));
            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false).modelForState().modelFile(inactive)
                    .addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true).modelForState().modelFile(active)
                    .addModel();
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> createCleanroomFilterModel(IFilterType type) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models()
                    .cubeAll(ctx.getName(), GTCEu.id("block/casings/cleanroom/" + type.getSerializedName())));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockstateProvider> createActiveModel(ResourceLocation modelPath) {
        return (ctx, prov) -> {
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().getExistingFile(modelPath);
            ModelFile active = prov.models().getExistingFile(modelPath.withSuffix("_active"));
            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false).modelForState().modelFile(inactive)
                    .addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true).modelForState().modelFile(active)
                    .addModel();
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockstateProvider> createFireboxModel(BoilerFireboxType type) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeBottomTop(name, type.side(), type.bottom(), type.top());
            ModelFile active = prov.models().withExistingParent(name + "_active", GTCEu.id("block/fire_box_active"))
                    .texture("side", type.side())
                    .texture("bottom", type.bottom())
                    .texture("top", type.top());
            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState().modelFile(inactive).addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true)
                    .modelForState().modelFile(active).addModel();
        };
    }

    // endregion

    // region RUNTIME GEN

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

    // endregion
}
