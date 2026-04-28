package com.gregtechceu.gtceu.common.data.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.*;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTItemModelGenerator;
import com.gregtechceu.gtceu.client.model.item.GTItemModelProperties;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GTModels {

    public static final Identifier BLANK_TEXTURE = GTCEu.id("block/void");

    public static final String ACTIVE_SUFFIX = "_active";

    // region BLOCK MODELS

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> createModelBlockState(Identifier modelLocation) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(modelLocation));
        };
    }

    public static void createCrossBlockState(DataGenContext<Block, ? extends Block> ctx,
                                             GTBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll(ctx.getName(), prov.blockTexture(ctx.getEntry())));
    }

    public static void cellModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelGenerator prov) {
        // empty model
        var empty = itemBuilder(prov, "item/" + GTItemModelGenerator.name(ctx::getEntry) + "_empty")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", prov.modLoc("item/%s/base".formatted(GTItemModelGenerator.name(ctx))));
        emit(prov, empty);

        // filled model
        var filled = itemBuilder(prov, "item/" + GTItemModelGenerator.name(ctx::getEntry) + "_filled")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", prov.modLoc("item/%s/base".formatted(GTItemModelGenerator.name(ctx))))
                .texture("layer1", prov.modLoc("item/%s/overlay".formatted(GTItemModelGenerator.name(ctx))));
        emit(prov, filled);

        // root model
        var root = itemBuilder(prov, "item/" + GTItemModelGenerator.name(ctx::getEntry))
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", prov.modLoc("item/%s/base".formatted(GTItemModelGenerator.name(ctx))))
                .override().predicate(GTCEu.id("fluid_cell"), 0)
                .model(new ModelFile.UncheckedModelFile(
                        prov.modLoc("item/%s_empty".formatted(GTItemModelGenerator.name(ctx)))))
                .end()
                .override().predicate(GTCEu.id("fluid_cell"), 1)
                .model(new ModelFile.UncheckedModelFile(
                        prov.modLoc("item/%s_filled".formatted(GTItemModelGenerator.name(ctx)))))
                .end();
        emit(prov, root);
        prov.itemModelOutput.accept(ctx.getEntry(), ItemModelUtils.plainModel(root.getLocation()));
    }

    public static <
            T extends Item> NonNullSupplier<NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator>> overrideModel(Identifier predicate,
                                                                                                                                    int modelNumber) {
        if (modelNumber <= 0) return NonNullBiConsumer::noop;
        return () -> (ctx, prov) -> {
            RangeSelectItemModelProperty rangeProperty = getRangeProperty(predicate);
            String itemName = GTItemModelGenerator.name(ctx);
            List<RangeSelectItemModel.Entry> entries = new ArrayList<>(modelNumber);
            var rootModel = itemBuilder(prov, "item/" + GTItemModelGenerator.name(ctx))
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", prov.modLoc("item/%s/1".formatted(GTItemModelGenerator.name(ctx))));
            for (int i = 0; i < modelNumber; i++) {
                var subModelBuilder = itemBuilder(prov,
                        "item/" + GTItemModelGenerator.name(ctx::getEntry) + "/" + i)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"));
                subModelBuilder.texture("layer0",
                        prov.modLoc("item/%s/%d".formatted(GTItemModelGenerator.name(ctx), i + 1)));
                emit(prov, subModelBuilder);

                entries.add(ItemModelUtils.override(ItemModelUtils.plainModel(subModelBuilder.getLocation()),
                        i / 100f));
            }
            emit(prov, rootModel);
            if (rangeProperty == null) {
                prov.itemModelOutput.accept(ctx.getEntry(), ItemModelUtils.plainModel(rootModel.getLocation()));
            } else {
                ItemModel.Unbaked fallback = ItemModelUtils.plainModel(prov.modLoc("item/%s/0".formatted(itemName)));
                prov.itemModelOutput.accept(ctx.getEntry(),
                        ItemModelUtils.rangeSelect(rangeProperty, fallback, entries));
            }
        };
    }

    private static RangeSelectItemModelProperty getRangeProperty(Identifier predicate) {
        if (GTItemModelProperties.BATTERY.equals(predicate)) {
            return GTItemModelProperties.BatteryCharge.INSTANCE;
        }
        if (GTItemModelProperties.ELECTRIC_JETPACK.equals(predicate)) {
            return GTItemModelProperties.ElectricJetpackCharge.INSTANCE;
        }
        if (GTItemModelProperties.CIRCUIT.equals(predicate)) {
            return GTItemModelProperties.CircuitConfiguration.INSTANCE;
        }
        return null;
    }

    public static void createLighterModel(DataGenContext<Item, ? extends Item> ctx,
                                          RegistrateItemModelGenerator prov) {
        String itemName = GTItemModelGenerator.name(ctx);
        prov.itemModelOutput.accept(ctx.getEntry(), ItemModelUtils.conditional(
                GTItemModelProperties.LighterOpen.INSTANCE,
                ItemModelUtils.plainModel(prov.modLoc("item/" + itemName + "_open")),
                ItemModelUtils.plainModel(prov.modLoc("item/" + itemName + "_closed"))));
    }

    public static void createNanoSaberModel(DataGenContext<Item, ? extends Item> ctx,
                                            RegistrateItemModelGenerator prov) {
        var rootModel = GTItemModelGenerator.generated(prov, ctx::getEntry,
                prov.modLoc("item/nano_saber/normal"));
        var activeModel = GTItemModelGenerator.emit(prov,
                GTItemModelGenerator.getBuilder(prov, "item/nano_saber/active")
                        .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                        .texture("layer0", prov.modLoc("item/nano_saber/active")));

        prov.itemModelOutput.accept(ctx.getEntry(), ItemModelUtils.conditional(
                GTItemModelProperties.NanoSaberActive.INSTANCE,
                ItemModelUtils.plainModel(activeModel.getLocation()),
                ItemModelUtils.plainModel(rootModel.getLocation())));
    }

    public static void createTextureModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelGenerator prov,
                                          Identifier texture) {
        var model = itemBuilder(prov, "item/" + GTItemModelGenerator.name(ctx))
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", texture);
        emit(prov, model);
        prov.itemModelOutput.accept(ctx.getEntry(), ItemModelUtils.plainModel(model.getLocation()));
    }

    public static void createExistingItemModel(DataGenContext<Item, ? extends Item> ctx,
                                               RegistrateItemModelGenerator prov) {
        prov.itemModelOutput.accept(ctx.getEntry(),
                ItemModelUtils.plainModel(prov.modLoc("item/" + GTItemModelGenerator.name(ctx))));
    }

    public static void rubberTreeSaplingModel(DataGenContext<Item, BlockItem> context,
                                              RegistrateItemModelGenerator provider) {
        createTextureModel(context, provider, provider.modLoc("block/" + GTItemModelGenerator.name(context)));
    }

    private static net.neoforged.neoforge.client.model.generators.ItemModelBuilder itemBuilder(
                                                                                               RegistrateItemModelGenerator provider,
                                                                                               String path) {
        return GTItemModelGenerator.getBuilder(provider, path);
    }

    private static void emit(RegistrateItemModelGenerator provider,
                             net.neoforged.neoforge.client.model.generators.ItemModelBuilder builder) {
        GTItemModelGenerator.emit(provider, builder);
    }

    public static NonNullBiConsumer<DataGenContext<Block, LampBlock>, GTBlockstateProvider> lampModel(DyeColor color,
                                                                                                      boolean border) {
        return (ctx, prov) -> {
            final String borderPart = (border ? "" : "_borderless");
            ModelFile parentOn = prov.models().getExistingFile(prov.modLoc("block/lamp" + borderPart));
            ModelFile parentOff = prov.models().getExistingFile(prov.modLoc("block/lamp" + borderPart + "_off"));

            ModelBuilder<?> model = prov.models().getBuilder(ctx.getName()).parent(parentOn);
            if (border) {
                model.texture("active", "block/lamps/" + color.getName());
                model.texture("active_overlay", "block/lamps/" + color.getName());
            } else {
                model.texture("active", "block/lamps/" + color.getName() + "_borderless");
            }
            prov.simpleBlock(ctx.getEntry(), model);
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, GTBlockstateProvider> randomRotatedModel(Identifier texturePath) {
        return (ctx, prov) -> {
            Block block = ctx.getEntry();
            ModelFile cubeAll = prov.models().cubeAll(ctx.getName(), texturePath);
            prov.simpleBlock(block, cubeAll);
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, GTBlockstateProvider> createSidedCasingModel(Identifier texture) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    texture.withSuffix("/side"),
                    texture.withSuffix("/bottom"),
                    texture.withSuffix("/top")));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> cubeAllModel(Identifier texture) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll(ctx.getName(), texture));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, GTBlockstateProvider> createMachineCasingModel(String tierName) {
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

    public static NonNullBiConsumer<DataGenContext<Block, Block>, GTBlockstateProvider> createHermeticCasingModel(String tierName) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models()
                    .withExistingParent("%s_hermetic_casing".formatted(tierName), GTCEu.id("block/hermetic_casing"))
                    .texture("bot_bottom", GTCEu.id("block/casings/voltage/%s/bottom".formatted(tierName)))
                    .texture("bot_top", GTCEu.id("block/casings/voltage/%s/top".formatted(tierName)))
                    .texture("bot_side", GTCEu.id("block/casings/voltage/%s/side".formatted(tierName))));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, GTBlockstateProvider> createSteamCasingModel(String material) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/steam/%s/side".formatted(material)),
                    GTCEu.id("block/casings/steam/%s/bottom".formatted(material)),
                    GTCEu.id("block/casings/steam/%s/top".formatted(material))));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, CoilBlock>, GTBlockstateProvider> createCoilModel(ICoilType coilType) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeAll(name, coilType.getTexture());
            ModelFile active = prov.models().withExistingParent(name + "_active", GTCEu.id("block/cube_2_layer/all"))
                    .texture("bot_all", coilType.getTexture())
                    .texture("top_all", coilType.getTexture().withSuffix("_bloom"));
            prov.simpleBlock(block, inactive);
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, BatteryBlock>, GTBlockstateProvider> createBatteryBlockModel(IBatteryData batteryData) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/battery/" + batteryData.getBatteryName() + "/side"),
                    GTCEu.id("block/casings/battery/" + batteryData.getBatteryName() + "/top"),
                    GTCEu.id("block/casings/battery/" + batteryData.getBatteryName() + "/top")));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, FusionCasingBlock>, GTBlockstateProvider> createFusionCasingModel(IFusionCasingType casingType) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeAll(name, casingType.getTexture());
            ModelFile active = prov.models().withExistingParent(name + "_active", GTCEu.id("block/cube_2_layer/all"))
                    .texture("bot_all", casingType.getTexture())
                    .texture("top_all", casingType.getTexture().withSuffix("_bloom"));
            prov.simpleBlock(block, inactive);
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, GTBlockstateProvider> createCleanroomFilterModel(IFilterType type) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models()
                    .cubeAll(ctx.getName(), GTCEu.id("block/casings/cleanroom/" + type.getSerializedName())));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, GTBlockstateProvider> createActiveModel(Identifier modelPath) {
        return (ctx, prov) -> {
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().getExistingFile(modelPath);
            ModelFile active = prov.models().getExistingFile(modelPath.withSuffix("_active"));
            prov.simpleBlock(block, inactive);
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, GTBlockstateProvider> createFireboxModel(BoilerFireboxType type) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeBottomTop(name, type.side(), type.bottom(), type.top());
            ModelFile active = prov.models().withExistingParent(name + "_active", GTCEu.id("block/fire_box_active"))
                    .texture("side", type.side())
                    .texture("bottom", type.bottom())
                    .texture("top", type.top());
            prov.simpleBlock(block, inactive);
        };
    }

    public static void createPipeBlockModel(DataGenContext<Block, ? extends PipeBlock<?, ?, ?>> ctx,
                                            GTBlockstateProvider prov) {
        // the pipe model generator handles adding its models to the provider by itself
        ctx.getEntry().createPipeModel(prov).initModels();
    }

    // endregion

    // region RUNTIME GEN

    /**
     * register fluid models for materials
     */
    public static void registerMaterialFluidModels() {
        for (var material : GTCEuAPI.materialManager) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);
            if (fluidProperty == null) continue;

            for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                FluidStorage storage = fluidProperty.getStorage();
                // fluid block models.
                FluidStorage.FluidEntry fluidEntry = storage.getEntry(key);
                if (fluidEntry != null && fluidEntry.getBuilder() != null) {
                    MixinHelpers.addFluidTexture(material, fluidEntry);
                }

                // bucket models.
                Fluid fluid = storage.get(key);
                if (fluid instanceof GTFluid gtFluid) {
                    // read the base bucket model JSON
                    JsonObject original;
                    try (BufferedReader reader = Minecraft.getInstance().getResourceManager()
                            .openAsReader(GTCEu.id("models/item/bucket/bucket.json"))) {
                        original = GsonHelper.parse(reader);
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
