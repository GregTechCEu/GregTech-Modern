package com.gregtechceu.gtceu.common.data.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.data.model.builder.ConfiguredMachineModel;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Locale;

import static com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay.*;

public class GTMachineModels {

    public static final ResourceLocation MACHINE_MODEL_LOADER = GTCEu.id("machine");
    public static final ResourceLocation TEXTURE_OVERRIDE_MODEL_LOADER = GTCEu.id("texture_override");
    
    public static final ResourceLocation BLANK_TEXTURE = GTCEu.id("block/void");
    public static final String OVERLAY_PREFIX = "overlay_";
    public static final String EMISSIVE_POSTFIX = "_emissive";

    // region generic models

    public static MachineBuilder.ModelConstructor createBasicMachineModel(ResourceLocation baseModel) {
        return (ctx, prov, builder) -> {
            builder.forAllStates(state -> {
                ModelFile model = new ModelFile.ExistingModelFile(baseModel, prov.getExistingFileHelper());
                return ConfiguredMachineModel.builder().modelFile(model).build();
            });
        };
    }

    public static MachineBuilder.ModelConstructor createTieredHullMachineModel(ResourceLocation parentModel) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().withExistingParent(ctx.getName(), parentModel);
            hullTextures(model, builder.getOwner().getTier());

            ConfiguredMachineModel[] models = ConfiguredMachineModel.builder().modelFile(model).build();
            builder.forAllStates(state -> models);
        };
    }

    public static MachineBuilder.ModelConstructor createOverlayTieredHullMachineModel(ResourceLocation overlayModelName) {
        return (ctx, prov, builder) -> {
            final ExistingFileHelper helper = prov.getExistingFileHelper();

            ResourceLocation parentModelName = GTCEu.id("block/machine/template/hull_machine");
            var parentModel = new ModelFile.ExistingModelFile(parentModelName, helper);
            var overlayModel = new ModelFile.ExistingModelFile(overlayModelName, helper);
            
            var model = prov.models().getBuilder(ctx.getName())
                    .customLoader(CompositeModelBuilder::begin)
                    .child("base", hullTextures(
                            prov.models().nested().parent(parentModel),
                            builder.getOwner().getTier())
                    )
                    .child("overlay", prov.models().nested().parent(overlayModel))
                    .end();
            ConfiguredMachineModel[] models = ConfiguredMachineModel.builder().modelFile(model).build();
            builder.forAllStates(state -> models);
        };
    }

    public static MachineBuilder.ModelConstructor createWorkableTieredHullMachineModel(ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            final ResourceLocation parentModel = GTCEu.id("block/machine/template/hull_overlay_machine");
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().withExistingParent(
                        ctx.getName() + "/" + status.getSerializedName(),
                        parentModel);
                hullTextures(model, builder.getOwner().getTier());
                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay == null) continue;
                    model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }

                return ConfiguredMachineModel.builder().modelFile(model).build();
            });
        };
    }

    public static MachineBuilder.ModelConstructor createWorkableCasingMachineModel(ResourceLocation baseCasingTexture,
                                                                                   ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            final ResourceLocation parentModel = GTCEu.id("block/machine/template/hull_overlay_machine");
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().withExistingParent(
                        ctx.getName() + "/" + status.getSerializedName(),
                        parentModel);
                hullTextures(model, builder.getOwner().getTier());
                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay == null) continue;
                    model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }

                return ConfiguredMachineModel.builder().modelFile(model).build();
            });
        };
    }

    // endregion

    // region helper functions

    public static NonNullBiConsumer<DataGenContext<Block, Block>, GTBlockstateProvider> createMachineModel(MachineBuilder.ModelConstructor model) {
        return (ctx, prov) -> {
            Block block = ctx.getEntry();
            if (!(block instanceof IMachineBlock machineBlock)) {
                throw new IllegalArgumentException(
                        "passed block must be a machine block, is " + block.getClass().getName());
            }
            MachineDefinition definition = machineBlock.getDefinition();

            MachineModelBuilder<BlockModelBuilder> builder = prov.models().getBuilder(ctx.getName())
                    .customLoader(MachineModelBuilder.begin(definition));
            model.configureModel(ctx, prov, builder);

            final ModelFile built = builder.end();
            var generator = prov.multiVariantGenerator(block,
                    Variant.variant().with(VariantProperties.MODEL, built.getLocation()));
            PropertyDispatch dispatch = GTBlockstateProvider.createFacingDispatch(definition);
            if (dispatch != null) {
                generator.with(dispatch);
            }
        };
    }

    public static ResourceLocation getHullTexture(int tier) {
        return GTCEu.id("block/casings/voltage/%s".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT)));
    }

    public static ResourceLocation getHullTexture(int tier, String key) {
        return getHullTexture(tier).withSuffix("/" + key);
    }

    public static void hullTexture(BlockModelBuilder model, String key, int tier) {
        model.texture(key, getHullTexture(tier, key));
    }

    public static BlockModelBuilder hullTextures(BlockModelBuilder model, int tier) {
        hullTexture(model, "bottom", tier);
        hullTexture(model, "top", tier);
        hullTexture(model, "side", tier);
        return model;
    }

    public static BlockModelBuilder casingTextures(BlockModelBuilder model, ResourceLocation texture) {
        model.texture("bottom", texture);
        model.texture("top", texture);
        model.texture("side", texture);
        return model;
    }

    // endregion

    // region per-machine models

    public static MachineBuilder.ModelConstructor createBatteryBufferModel(int inventorySize) {
        return (ctx, prov, builder) -> {
            var overlay = OUT_OVERLAYS_FOR_AMP.get(inventorySize);
            
            BlockModelBuilder model = prov.models()
                    .withExistingParent(ctx.getName(), GTCEu.id("block/machine/template/converter_machine"))
                    .texture("overlay_eu_io", overlay.getIoPart())
                    .texture("overlay_eu_tinted", overlay.getTintedPart())
                    .texture("overlay_fe", BLANK_TEXTURE);
            hullTextures(model, builder.getOwner().getTier());

            builder.partialState()
                    .setModels(ConfiguredMachineModel.builder().modelFile(model).build())
                    .end();
        };
    }

    public static final ResourceLocation CHARGER_IDLE = GTCEu.id("block/machines/charger/overlay_charger_idle");
    public static final ResourceLocation CHARGER_RUNNING = GTCEu.id("block/machines/charger/overlay_charger_running");
    public static final ResourceLocation CHARGER_RUNNING_EMISSIVE = GTCEu
            .id("block/machines/charger/overlay_charger_running_emissive");
    public static final ResourceLocation CHARGER_FINISHED = GTCEu.id("block/machines/charger/overlay_charger_finished");
    public static final ResourceLocation CHARGER_FINISHED_EMISSIVE = GTCEu
            .id("block/machines/charger/overlay_charger_finished_emissive");
    
    public static MachineBuilder.ModelConstructor createChargerModel() {
        return (ctx, prov, modelBuilder) -> {
            final ResourceLocation parentModel = GTCEu.id("block/machine/template/hull_overlay_machine");

            modelBuilder.forAllStates(renderState -> {
                ChargerMachine.State state = renderState.getValue(ChargerMachine.STATE_PROPERTY);

                BlockModelBuilder model = prov.models().withExistingParent(
                        ctx.getName() + "/" + state.getSerializedName(),
                        parentModel);
                hullTextures(model, modelBuilder.getOwner().getTier());
                switch (state) {
                    case IDLE -> {
                        model.texture("overlay_front", CHARGER_IDLE);
                    }
                    case RUNNING -> {
                        model.texture("overlay_front", CHARGER_RUNNING);
                        model.texture("overlay_front_emissive", CHARGER_RUNNING_EMISSIVE);
                    }
                    case FINISHED -> {
                        model.texture("overlay_front", CHARGER_FINISHED);
                        model.texture("overlay_front_emissive", CHARGER_FINISHED_EMISSIVE);
                    }
                }
                return ConfiguredMachineModel.builder().modelFile(model).build();
            });
        };
    }

    public static final ResourceLocation CONVERTER_FE_IN = GTCEu.id("block/overlay/converter/converter_native_in");
    public static final ResourceLocation CONVERTER_FE_OUT = GTCEu.id("block/overlay/converter/converter_native_out");

    public static MachineBuilder.ModelConstructor createConverterModel(int amperage) {
        return (ctx, prov, builder) -> {
            final ResourceLocation parentModel = GTCEu.id("block/machine/template/converter_machine");
            final EnergyIOOverlay energyIn = IN_OVERLAYS_FOR_AMP.get(amperage);
            final EnergyIOOverlay energyOut = OUT_OVERLAYS_FOR_AMP.get(amperage);

            BlockModelBuilder euToFeModel = prov.models()
                    .withExistingParent(ctx.getName() + "_eu_to_fe", parentModel)
                    .texture("overlay_eu_io", energyIn.getIoPart())
                    .texture("overlay_eu_tinted", energyIn.getTintedPart())
                    .texture("overlay_fe", CONVERTER_FE_OUT);
            hullTextures(euToFeModel, builder.getOwner().getTier());
            BlockModelBuilder feToEuModel = prov.models()
                    .withExistingParent(ctx.getName() + "_fe_to_eu", parentModel)
                    .texture("overlay_eu_io", energyOut.getIoPart())
                    .texture("overlay_eu_tinted", energyOut.getTintedPart())
                    .texture("overlay_fe", CONVERTER_FE_IN);
            hullTextures(feToEuModel, builder.getOwner().getTier());

            builder.partialState()
                    .with(ConverterMachine.FE_TO_EU_PROPERTY, false)
                    .setModels(ConfiguredMachineModel.builder().modelFile(euToFeModel).build())
                    .partialState()
                    .with(ConverterMachine.FE_TO_EU_PROPERTY, true)
                    .setModels(ConfiguredMachineModel.builder().modelFile(feToEuModel).build())
                    .end();
        };
    }

    public static MachineBuilder.ModelConstructor createCrateModel(boolean wooden) {
        return (ctx, prov, builder) -> {
            ResourceLocation baseModelName = wooden ?
                    GTCEu.id("block/cube/all") :
                    GTCEu.id("block/cube/tinted/all");
            ResourceLocation layerModelName = wooden ?
                    GTCEu.id("block/cube_2_layer/all") :
                    GTCEu.id("block/cube_2_layer/tinted_bot/all");

            var baseTextureName = GTCEu.id("block/storage/crates/" + (wooden ? "wooden" : "metal") + "_crate");

            ModelFile untaped = prov.models().withExistingParent(ctx.getName() + "_untaped", baseModelName)
                    .texture("all", baseTextureName);
            ModelFile taped = prov.models().withExistingParent(ctx.getName() + "_taped", layerModelName)
                    .texture("bot_all", baseTextureName)
                    .texture("top_all", GTCEu.id("block/overlay/machine/overlay_crate_taped"));

            builder.partialState()
                    .with(CrateMachine.TAPED_PROPERTY, false)
                    .setModels(ConfiguredMachineModel.builder().modelFile(untaped).build())
                    .partialState()
                    .with(CrateMachine.TAPED_PROPERTY, true)
                    .setModels(ConfiguredMachineModel.builder().modelFile(taped).build())
                    .end();
        };
    }

    // endregion

}
