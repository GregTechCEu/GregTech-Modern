package com.gregtechceu.gtceu.common.data.models;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtceu.common.machine.electric.WorldAcceleratorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;

import java.util.Locale;

import static com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay.*;
import static com.gregtechceu.gtceu.common.data.models.GTModels.*;

public class GTMachineModels {

    // spotless:off
    public static final ResourceLocation MACHINE_MODEL_LOADER = GTCEu.id("machine");
    public static final ResourceLocation TEXTURE_OVERRIDE_MODEL_LOADER = GTCEu.id("texture_override");

    public static final String OVERLAY_PREFIX = "overlay_";
    public static final String EMISSIVE_POSTFIX = "_emissive";

    public static final ResourceLocation HULL_MODEL = GTCEu.id("block/machine/template/hull_machine");
    public static final ResourceLocation SIDED_OVERLAY_MODEL = GTCEu.id("block/machine/template/sided_overlay_machine");
    public static final ResourceLocation ALL_OVERLAY_MODEL = GTCEu.id("block/machine/template/all_overlay_machine");
    // spotless:on

    // region generic models

    public static MachineBuilder.ModelConstructor createBasicMachineModel(ResourceLocation baseModel) {
        return (ctx, prov, builder) -> {
            var model = prov.models().getExistingFile(baseModel);
            builder.forAllStates(state -> model);
        };
    }

    public static MachineBuilder.ModelConstructor createTieredHullMachineModel(ResourceLocation parentModel) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(parentModel));
            hullTextures(model, builder.getOwner().getTier());

            builder.forAllStates(state -> model);
        };
    }

    // spotless:off
    public static MachineBuilder.ModelConstructor createOverlayTieredHullMachineModel(ResourceLocation overlayModel) {
        return (ctx, prov, builder) -> {
            var baseModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(HULL_MODEL));
            hullTextures(baseModel, builder.getOwner().getTier());

            var model = prov.models().nested()
                    .customLoader(CompositeModelBuilder::begin)
                    .child("base", baseModel)
                    .child("overlay", prov.models().nested()
                            .parent(prov.models().getExistingFile(overlayModel)))
                    .end();
            builder.forAllStates(state -> model);
        };
    }
    // spotless:on

    public static MachineBuilder.ModelConstructor createWorkableTieredHullMachineModel(ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_OVERLAY_MODEL));
                hullTextures(model, builder.getOwner().getTier());

                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay != null) {
                        model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    }
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }

                return model;
            });
        };
    }

    public static MachineBuilder.ModelConstructor createSteamHullMachineModel(ResourceLocation parentModel) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(parentModel));
            steamHullTextures(model);

            builder.forAllStates(state -> model);
        };
    }

    // spotless:off
    public static MachineBuilder.ModelConstructor createOverlaySteamHullMachineModel(ResourceLocation overlayModel) {
        return (ctx, prov, builder) -> {
            var baseModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(HULL_MODEL));
            steamHullTextures(baseModel);

            var model = prov.models().nested()
                    .customLoader(CompositeModelBuilder::begin)
                    .child("base", steamHullTextures(
                            prov.models().nested()
                                    .parent(prov.models().getExistingFile(HULL_MODEL)))
                    )
                    .child("overlay", prov.models().nested()
                            .parent(prov.models().getExistingFile(overlayModel)))
                    .end();
            builder.forAllStates(state -> model);
        };
    }
    // spotless:on

    public static final ResourceLocation VENT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_steam_vent");

    // spotless:off
    public static MachineBuilder.ModelConstructor createWorkableSteamHullMachineModel(boolean highPressure, ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_OVERLAY_MODEL));
                steamHullTextures(model, highPressure);

                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay != null) {
                        model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    }
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }

                Direction steamVent = state.getValue(IExhaustVentMachine.VENT_DIRECTION_PROPERTY);
                model = prov.models().nested()
                        .customLoader(CompositeModelBuilder::begin)
                        .child("base", model)
                        .child("steam_vent", prov.models().nested()
                                .texture("steam_vent", VENT_OVERLAY)
                                .element()
                                .face(steamVent).texture("#steam_vent").cullface(steamVent).end()
                                .end())
                        .end();

                return model;
            });
        };
    }
    // spotless:on
    public static MachineBuilder.ModelConstructor createOverlayCasingMachineModel(ResourceLocation baseCasingTexture,
                                                                                  ResourceLocation overlayModel) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder baseModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(ALL_OVERLAY_MODEL))
                    .texture("all", baseCasingTexture);

            var model = prov.models().nested()
                    .customLoader(CompositeModelBuilder::begin)
                    .child("base", baseModel)
                    .child("overlay", prov.models().nested()
                            .parent(prov.models().getExistingFile(overlayModel)))
                    .end();
            builder.forAllStates(state -> model);
        };
    }

    public static MachineBuilder.ModelConstructor createWorkableCasingMachineModel(ResourceLocation baseCasingTexture,
                                                                                   ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(ALL_OVERLAY_MODEL))
                        .texture("all", baseCasingTexture);

                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay != null) {
                        model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    }
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }

                return model;
            });
        };
    }

    // spotless:off
    public static MachineBuilder.ModelConstructor createSidedOverlayCasingMachineModel(ResourceLocation baseCasingTexture,
                                                                                       ResourceLocation overlayModel) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder baseModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(HULL_MODEL))
                    .texture("bottom", baseCasingTexture.withSuffix("/bottom"))
                    .texture("top", baseCasingTexture.withSuffix("/top"))
                    .texture("side", baseCasingTexture.withSuffix("/side"));

            var model = prov.models().nested()
                    .customLoader(CompositeModelBuilder::begin)
                    .child("base", baseModel)
                    .child("overlay", prov.models().nested()
                            .parent(prov.models().getExistingFile(overlayModel)))
                    .end();
            builder.forAllStates(state -> model);
        };
    }

    public static MachineBuilder.ModelConstructor createSidedWorkableCasingMachineModel(ResourceLocation baseCasingTexture,
                                                                                        ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(ALL_OVERLAY_MODEL))
                        .texture("bottom", baseCasingTexture.withSuffix("/bottom"))
                        .texture("top", baseCasingTexture.withSuffix("/top"))
                        .texture("side", baseCasingTexture.withSuffix("/side"));

                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay != null) {
                        model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    }
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }

                return model;
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
    // spotless:on

    public static ResourceLocation getHullTexture(int tier) {
        return GTCEu.id("block/casings/voltage/%s/".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT)));
    }

    public static void hullTexture(BlockModelBuilder model, String key, int tier) {
        model.texture(key, getHullTexture(tier).withSuffix(key));
    }

    public static BlockModelBuilder hullTextures(BlockModelBuilder model, int tier) {
        hullTexture(model, "bottom", tier);
        hullTexture(model, "top", tier);
        hullTexture(model, "side", tier);
        return model;
    }

    public static ResourceLocation getSteamHullTexture(String variant) {
        return GTCEu.id("block/casings/steam/%s/".formatted(variant));
    }

    public static void steamHullTexture(BlockModelBuilder model, String key, String variant) {
        model.texture(key, getSteamHullTexture(variant).withSuffix(key));
    }

    public static BlockModelBuilder steamHullTextures(BlockModelBuilder model, String variant) {
        steamHullTexture(model, "bottom", variant);
        steamHullTexture(model, "top", variant);
        steamHullTexture(model, "side", variant);
        return model;
    }

    public static BlockModelBuilder steamHullTextures(BlockModelBuilder model) {
        return steamHullTextures(model, ConfigHolder.INSTANCE.machines.steelSteamMultiblocks ? "steel" : "bronze");
    }

    public static BlockModelBuilder steamHullTextures(BlockModelBuilder model, boolean highTier) {
        return steamHullTextures(model, highTier ? "bricked_steel" : "bricked_bronze");
    }

    public static BlockModelBuilder casingTextures(BlockModelBuilder model, ResourceLocation texture) {
        model.texture("bottom", texture);
        model.texture("top", texture);
        model.texture("side", texture);
        return model;
    }

    // endregion

    // region per-machine models

    public static MachineBuilder.ModelConstructor createSimpleGeneratorModel(ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_OVERLAY_MODEL));
                hullTextures(model, builder.getOwner().getTier());

                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay != null) {
                        model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    }
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }

                var energyOverlayModel = prov.models().nested()
                        .parent(prov.models().getExistingFile(GTCEu.id("block/overlay/tinted/front_2")))
                        .texture("overlay", ENERGY_OUT_1A.getIoPart())
                        .texture("overlay_tinted", ENERGY_OUT_1A.getTintedPart());

                model = prov.models().nested()
                        .customLoader(CompositeModelBuilder::begin)
                        .child("base", model)
                        .child("energy_out", energyOverlayModel)
                        .end();
                return model;
            });
        };
    }

    public static MachineBuilder.ModelConstructor createBatteryBufferModel(int inventorySize) {
        return (ctx, prov, builder) -> {
            var overlay = OUT_OVERLAYS_FOR_AMP.get(inventorySize);
            
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                    .texture("overlay_in_io", overlay.getIoPart())
                    .texture("overlay_in_tinted", overlay.getTintedPart())
                    .texture("overlay_out_io", BLANK_TEXTURE);
            hullTextures(model, builder.getOwner().getTier());

            builder.forAllStates(state -> model);
        };
    }

    // spotless:off
    public static final ResourceLocation CHARGER_IDLE = GTCEu.id("block/machines/charger/overlay_charger_idle");
    public static final ResourceLocation CHARGER_RUNNING = GTCEu.id("block/machines/charger/overlay_charger_running");
    public static final ResourceLocation CHARGER_RUNNING_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_running_emissive");
    public static final ResourceLocation CHARGER_FINISHED = GTCEu.id("block/machines/charger/overlay_charger_finished");
    public static final ResourceLocation CHARGER_FINISHED_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_finished_emissive");
    
    public static MachineBuilder.ModelConstructor createChargerModel() {
        return (ctx, prov, builder) -> {
            builder.forAllStates(renderState -> {
                ChargerMachine.State state = renderState.getValue(ChargerMachine.STATE_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_OVERLAY_MODEL));
                hullTextures(model, builder.getOwner().getTier());

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
                return model;
            });
        };
    }
    // spotless:on

    public static final ResourceLocation TRANSFORMER_LIKE = GTCEu.id("block/machine/template/transformer_like_machine");

    public static final ResourceLocation CONVERTER_FE_IN = GTCEu.id("block/overlay/converter/converter_native_in");
    public static final ResourceLocation CONVERTER_FE_OUT = GTCEu.id("block/overlay/converter/converter_native_out");

    public static MachineBuilder.ModelConstructor createConverterModel(int amperage) {
        return (ctx, prov, builder) -> {
            final EnergyIOOverlay energyIn = IN_OVERLAYS_FOR_AMP.get(amperage);
            final EnergyIOOverlay energyOut = OUT_OVERLAYS_FOR_AMP.get(amperage);

            BlockModelBuilder euToFeModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                    .texture("overlay_in_io", energyIn.getIoPart())
                    .texture("overlay_in_tinted", energyIn.getTintedPart())
                    .texture("overlay_out_io", CONVERTER_FE_OUT);
            hullTextures(euToFeModel, builder.getOwner().getTier());
            BlockModelBuilder feToEuModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                    .texture("overlay_in_io", energyOut.getIoPart())
                    .texture("overlay_in_tinted", energyOut.getTintedPart())
                    .texture("overlay_out_io", CONVERTER_FE_IN);
            hullTextures(feToEuModel, builder.getOwner().getTier());

            builder.partialState()
                    .with(ConverterMachine.FE_TO_EU_PROPERTY, false)
                    .setModel(euToFeModel)
                    .partialState()
                    .with(ConverterMachine.FE_TO_EU_PROPERTY, true)
                    .setModel(feToEuModel)
                    .end();
        };
    }

    public static MachineBuilder.ModelConstructor createCrateModel(boolean wooden) {
        return (ctx, prov, builder) -> {
            ResourceLocation baseModel = wooden ?
                    GTCEu.id("block/cube/all") :
                    GTCEu.id("block/cube/tinted/all");
            ResourceLocation layerModel = wooden ?
                    GTCEu.id("block/cube_2_layer/all") :
                    GTCEu.id("block/cube_2_layer/tinted_bot/all");

            var baseTextureName = GTCEu.id("block/storage/crates/" + (wooden ? "wooden" : "metal") + "_crate");

            ModelFile untaped = prov.models().nested()
                    .parent(prov.models().getExistingFile(baseModel))
                    .texture("all", baseTextureName);
            ModelFile taped = prov.models().nested()
                    .parent(prov.models().getExistingFile(layerModel))
                    .texture("bot_all", baseTextureName)
                    .texture("top_all", GTCEu.id("block/overlay/machine/overlay_crate_taped"));

            builder.partialState()
                    .with(CrateMachine.TAPED_PROPERTY, false)
                    .setModel(untaped)
                    .partialState()
                    .with(CrateMachine.TAPED_PROPERTY, true)
                    .setModel(taped)
                    .end();
        };
    }

    public static MachineBuilder.ModelConstructor createDiodeModel() {
        return (ctx, prov, builder) -> {
            builder.forAllStates(renderState -> {
                DiodePartMachine.AmpMode mode = renderState.getValue(DiodePartMachine.AMP_MODE_PROPERTY);
                final EnergyIOOverlay energyIn = IN_OVERLAYS_FOR_AMP.get(mode.getAmpValue());
                final EnergyIOOverlay energyOut = OUT_OVERLAYS_FOR_AMP.get(mode.getAmpValue());

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                        .texture("overlay_in_io", energyIn.getIoPart())
                        .texture("overlay_in_tinted", energyIn.getTintedPart())
                        .texture("overlay_out_io", energyOut.getIoPart())
                        .texture("overlay_out_tinted", energyOut.getTintedPart());
                hullTextures(model, builder.getOwner().getTier());
                return model;
            });
        };
    }

    public static MachineBuilder.ModelConstructor createTransformerModel(int baseAmp) {
        return (ctx, prov, builder) -> {
            builder.forAllStates(renderState -> {
                boolean transformUp = renderState.getValue(TransformerMachine.TRANSFORM_UP_PROPERTY);
                EnergyIOOverlay frontFace = (transformUp ? OUT_OVERLAYS_FOR_AMP : IN_OVERLAYS_FOR_AMP)
                        .get(baseAmp);
                EnergyIOOverlay otherFace = (transformUp ? IN_OVERLAYS_FOR_AMP : OUT_OVERLAYS_FOR_AMP)
                        .get(baseAmp * 4);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                        .texture("overlay_in_io", frontFace.getIoPart())
                        .texture("overlay_in_tinted", frontFace.getTintedPart())
                        .texture("overlay_out_io", otherFace.getIoPart())
                        .texture("overlay_out_tinted", otherFace.getTintedPart());
                hullTextures(model, builder.getOwner().getTier());
                return model;
            });
        };
    }

    // spotless:off
    public static final ResourceLocation ROTOR_HOLDER_MODEL = GTCEu.id("block/machine/template/rotor_holder_machine");
    public static final ResourceLocation ROTOR_HOLDER_BASE_RING = GTCEu.id("block/multiblock/large_turbine/base_ring");
    public static final ResourceLocation ROTOR_HOLDER_BASE_BG = GTCEu.id("block/multiblock/large_turbine/base_bg");
    public static final ResourceLocation ROTOR_HOLDER_IDLE = GTCEu.id("block/multiblock/large_turbine/rotor_idle");
    public static final ResourceLocation ROTOR_HOLDER_SPINNING = GTCEu.id("block/multiblock/large_turbine/rotor_spinning");
    // spotless:on

    public static MachineBuilder.ModelConstructor createRotorHolderModel() {
        return (ctx, prov, builder) -> {

            builder.forAllStates(state -> {
                var model = prov.models().nested()
                        .customLoader(CompositeModelBuilder::begin)
                        .child("holder", hullTextures(
                                prov.models().nested()
                                        .parent(prov.models().getExistingFile(ROTOR_HOLDER_MODEL)),
                                builder.getOwner().getTier())
                        );

                if (!state.getValue(RotorHolderPartMachine.HAS_CONTROLLER_PROPERTY)) {
                    return model.end();
                }
                BlockModelBuilder rotorModel = prov.models().nested()
                        .texture("ring", ROTOR_HOLDER_BASE_RING)
                        .texture("base", ROTOR_HOLDER_BASE_BG)
                        .element()
                        .from(-16, -16, 0).to(32, 32, -0.002f)
                        .face(Direction.NORTH).uvs(0, 0, 1, 1).texture("#ring").end()
                        .end()
                        .element()
                        .from(-16, -16, 0).to(32, 32, -0.004f)
                        .face(Direction.NORTH).uvs(0, 0, 1, 1).texture("#base").end()
                        .end();
                model.child("rotor", rotorModel);
                if (!state.getValue(RotorHolderPartMachine.HAS_ROTOR_PROPERTY)) {
                    return model.end();
                }

                boolean spinning = state.getValue(RotorHolderPartMachine.ROTOR_SPINNING_PROPERTY);
                rotorModel.texture("rotor", spinning ? ROTOR_HOLDER_SPINNING : ROTOR_HOLDER_IDLE)
                        .element()
                        .from(-16, -16, 0).to(32, 32, -0.006f)
                        .face(Direction.NORTH).uvs(0, 0, 1, 1).texture("#rotor").tintindex(2).end()
                        .end();
                if (state.getValue(RotorHolderPartMachine.EMISSIVE_ROTOR_PROPERTY)) {
                    rotorModel.element(2)
                            .emissivity(15, 15).ao(false)
                            .face(Direction.NORTH).tintindex(-101);
                }
                return model.end();
            });
        };
    }

    public static final ResourceLocation PIPE_IN_OVERLAY = GTCEu.id("block/overlay/machine/overlay_pipe_in");
    public static final ImmutableMap<Material, ResourceLocation> MATERIALS_TO_CASING_MODELS = Util.make(() -> {
        ImmutableMap.Builder<Material, ResourceLocation> builder = ImmutableMap.builder();
        builder.put(GTMaterials.Bronze, GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"));
        builder.put(GTMaterials.Invar, GTCEu.id("block/casings/solid/machine_casing_heatproof"));
        builder.put(GTMaterials.Aluminium, GTCEu.id("block/casings/solid/machine_casing_frost_proof"));
        builder.put(GTMaterials.Steel, GTCEu.id("block/casings/solid/machine_casing_solid_steel"));
        builder.put(GTMaterials.StainlessSteel, GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"));
        builder.put(GTMaterials.Titanium, GTCEu.id("block/casings/solid/machine_casing_stable_titanium"));
        builder.put(GTMaterials.TungstenSteel, GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"));
        builder.put(GTMaterials.Polytetrafluoroethylene, GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"));
        builder.put(GTMaterials.HSSE, GTCEu.id("block/casings/solid/machine_casing_sturdy_hsse"));

        return builder.build();
    });

    // spotless:off
    public static MachineBuilder.ModelConstructor createWorldAcceleratorModel(ResourceLocation beModeModelPath, ResourceLocation rtModeModelPath) {
        return (ctx, prov, builder) -> {
            WorkableOverlays rtOverlays = WorkableOverlays.get(rtModeModelPath, prov.getExistingFileHelper());
            WorkableOverlays beOverlays = WorkableOverlays.get(beModeModelPath, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                boolean rtMode = state.getValue(WorldAcceleratorMachine.RANDOM_TICK_PROPERTY);
                WorkableOverlays overlays = rtMode ? rtOverlays : beOverlays;

                boolean active = state.getValue(WorldAcceleratorMachine.ACTIVE_PROPERTY);
                boolean workingEnabled = state.getValue(WorldAcceleratorMachine.WORKING_ENABLED_PROPERTY);
                RecipeLogic.Status status = active ?
                        workingEnabled ?
                                RecipeLogic.Status.WORKING :
                                RecipeLogic.Status.SUSPEND :
                        RecipeLogic.Status.IDLE;

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_OVERLAY_MODEL));
                hullTextures(model, builder.getOwner().getTier());

                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay != null) {
                        model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    }
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }
                return model;
            });
        };
    }
    // spotless:on

    // endregion
}
