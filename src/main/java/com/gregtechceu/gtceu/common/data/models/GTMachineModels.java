package com.gregtechceu.gtceu.common.data.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.capability.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay;
import com.gregtechceu.gtceu.client.model.machine.overlays.HPCAOverlay;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtceu.common.machine.electric.WorldAcceleratorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.core.mixins.forge.ConfiguredModelListAccessor;
import com.gregtechceu.gtceu.core.mixins.forge.ModelBuilderAccessor;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;

import static com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController.IS_FORMED_PROPERTY;
import static com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine.*;
import static com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay.*;
import static com.gregtechceu.gtceu.common.data.models.GTModels.*;

public class GTMachineModels {

    public static final String OVERLAY_PREFIX = "overlay_";
    public static final String EMISSIVE_SUFFIX = "_emissive";

    // spotless:off
    public static final ResourceLocation SIDED_SIDED_OVERLAY_MODEL = GTCEu.id("block/machine/template/sided/sided");
    public static final ResourceLocation SIDED_SINGLE_OVERLAY_MODEL = GTCEu.id("block/machine/template/sided/single");
    public static final ResourceLocation CUBE_ALL_SIDED_OVERLAY_MODEL = GTCEu.id("block/machine/template/cube_all/sided");
    public static final ResourceLocation CUBE_ALL_SINGLE_OVERLAY_MODEL = GTCEu.id("block/machine/template/cube_all/single");

    public static final Int2ObjectMap<ResourceLocation> TIERED_HULL_MODELS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        for (int tier : GTValues.ALL_TIERS) {
            String vn = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            map.put(tier, GTCEu.id("block/casings/voltage/" + vn));
        }
        map.defaultReturnValue(GTCEu.id("block/casings/voltage/lv"));
    });
    public static final ResourceLocation LP_STEAM_HULL_MODEL = GTCEu.id("block/casings/steam/bricked_bronze");
    public static final ResourceLocation HP_STEAM_HULL_MODEL = GTCEu.id("block/casings/steam/bricked_steel");

    public static final ResourceLocation OVERLAY_MODEL = GTCEu.id("block/overlay/front");
    public static final ResourceLocation OVERLAY_COLOR_RING_MODEL = GTCEu.id("block/overlay/front_color_ring");
    public static final ResourceLocation OVERLAY_EMISSIVE_MODEL = GTCEu.id("block/overlay/2_layer/front_emissive");
    public static final ResourceLocation OVERLAY_EMISSIVE_COLOR_RING_MODEL = GTCEu.id("block/overlay/2_layer/front_emissive_color_ring");
    // spotless:on

    // region generic models

    public static MachineBuilder.ModelInitializer createBasicMachineModel(ResourceLocation baseModel) {
        return (ctx, prov, builder) -> {
            var model = prov.models().getExistingFile(baseModel);
            builder.forAllStatesModels(state -> model);
        };
    }

    public static MachineBuilder.ModelInitializer createBasicReplaceableTextureMachineModel(ResourceLocation baseModel) {
        return (ctx, prov, builder) -> {
            var model = prov.models().getExistingFile(baseModel);
            builder.forAllStatesModels(state -> model);
            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createTieredHullMachineModel(ResourceLocation parentModel) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(parentModel));
            tieredHullTextures(model, builder.getOwner().getTier());

            builder.forAllStatesModels(state -> model);
        };
    }

    public static MachineBuilder.ModelInitializer createOverlayTieredHullMachineModel(ResourceLocation overlayModel) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(overlayModel));
            tieredHullTextures(model, builder.getOwner().getTier());
            builder.forAllStatesModels(state -> model);

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createColorOverlayTieredHullMachineModel(ResourceLocation overlay,
                                                                                           @Nullable ResourceLocation emissiveOverlay) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                BlockModelBuilder model = colorOverlayHullModel(overlay, emissiveOverlay, state, prov.models());
                tieredHullTextures(model, builder.getOwner().getTier());
                return tieredHullTextures(model, builder.getOwner().getTier());
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createSingleOverlayTieredHullMachineModel(ResourceLocation overlayTexture,
                                                                                            ResourceLocation emissiveOverlayTexture) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(SIDED_SINGLE_OVERLAY_MODEL))
                    .texture("overlay", overlayTexture)
                    .texture("overlay_emissive", emissiveOverlayTexture);
            tieredHullTextures(model, builder.getOwner().getTier());
            builder.forAllStatesModels(state -> model);

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createWorkableTieredHullMachineModel(ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested().parent(tieredHullModel(prov.models(), builder));
                return addWorkableOverlays(overlays, status, model);
            });
        };
    }

    public static MachineBuilder.ModelInitializer createOverlaySteamHullMachineModel(ResourceLocation overlayModel) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                boolean steel = state.getOptionalValue(SteamMachine.STEEL_PROPERTY).orElse(false);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(overlayModel));
                steamCasingTextures(model, steel);
                return model;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createColorOverlaySteamHullMachineModel(ResourceLocation overlay,
                                                                                          @Nullable ResourceLocation emissiveOverlay) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                BlockModelBuilder model = colorOverlayHullModel(overlay, emissiveOverlay, state, prov.models());
                steamCasingTextures(model, state.getOptionalValue(SteamMachine.STEEL_PROPERTY).orElse(false));
                return model;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static final ResourceLocation VENT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_steam_vent");

    // spotless:off
    public static MachineBuilder.ModelInitializer createWorkableSteamHullMachineModel(boolean highPressure, ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());
            ModelFile parent = steamHullModel(prov.models(), highPressure);

            makeWorkableOverlayPart(prov.models(), builder, parent, overlays, RecipeLogic.Status.IDLE);
            makeWorkableOverlayPart(prov.models(), builder, parent, overlays, RecipeLogic.Status.WORKING);
            makeWorkableOverlayPart(prov.models(), builder, parent, overlays, RecipeLogic.Status.WAITING);
            makeWorkableOverlayPart(prov.models(), builder, parent, overlays, RecipeLogic.Status.SUSPEND);

            if (!builder.getOwner().defaultRenderState().hasProperty(IExhaustVentMachine.VENT_DIRECTION_PROPERTY)) {
                return;
            }

            ModelFile ventModel = prov.models().getBuilder(VENT_OVERLAY.toString())
                    .texture("steam_vent", VENT_OVERLAY)
                    .element()
                    .from(-0.002f, -0.002f, -0.002f).to(16.002f, 16.002f, 16.002f)
                    .face(Direction.NORTH).uvs(0, 0, 16, 16).texture("#steam_vent").cullface(Direction.NORTH).end()
                    .end();
            for (RelativeDirection relative : RelativeDirection.VALUES) {
                Direction dir = relative.global;
                builder.part().modelFile(ventModel)
                        .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? 270 : 0)
                        .rotationY(dir.getAxis().isVertical() ? 0 : ((int) dir.toYRot() + 180) % 360)
                        .addModel()
                        .condition(IExhaustVentMachine.VENT_DIRECTION_PROPERTY, relative);
            }
        };
    }
    // spotless:on

    private static void makeWorkableOverlayPart(BlockModelProvider models,
                                                MachineModelBuilder<BlockModelBuilder> builder, ModelFile parentModel,
                                                WorkableOverlays overlays, RecipeLogic.Status status) {
        BlockModelBuilder model = models.nested().parent(parentModel);
        addWorkableOverlays(overlays, status, model);
        builder.part(model).condition(RecipeLogic.STATUS_PROPERTY, status);
    }

    public static MachineBuilder.ModelInitializer createWorkableCasingMachineModel(ResourceLocation baseCasingTexture,
                                                                                   ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(CUBE_ALL_SIDED_OVERLAY_MODEL))
                        .texture("all", baseCasingTexture);
                return addWorkableOverlays(overlays, status, model);
            });
            builder.addTextureOverride("all", baseCasingTexture);
        };
    }

    // spotless:off
    public static MachineBuilder.ModelInitializer createSidedOverlayCasingMachineModel(ResourceLocation baseCasingTexture,
                                                                                       ResourceLocation overlayModel) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(overlayModel));
            casingTextures(model, baseCasingTexture);
            builder.forAllStatesModels(state -> model);

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createSidedWorkableCasingMachineModel(ResourceLocation baseCasingTexture,
                                                                                        ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_SIDED_OVERLAY_MODEL));
                casingTextures(model, baseCasingTexture);
                return addWorkableOverlays(overlays, status, model);
            });

            var texturePath = baseCasingTexture;
            if (!texturePath.getPath().endsWith("/")) {
                texturePath = texturePath.withSuffix("/");
            }
            builder.addTextureOverride("bottom", texturePath.withSuffix("bottom"));
            builder.addTextureOverride("top", texturePath.withSuffix("top"));
            builder.addTextureOverride("side", texturePath.withSuffix("side"));
        };
    }

    // endregion

    // region per-machine models

    // spotless:off
    public static final String OVERLAY_FLUID_HATCH_TEX = "overlay_fluid_hatch";
    public static final String OVERLAY_FLUID_HATCH_HALF_PX_TEX = "overlay_fluid_hatch_half_px_out";
    public static final String OVERLAY_ITEM_HATCH = "overlay_item_hatch";
    // spotless:on

    public static MachineBuilder.ModelInitializer createSimpleGeneratorModel(ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStatesModels(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(GTCEu.id("block/overlay/2_layer/tinted/front")))
                        .texture("overlay", ENERGY_OUT_1A.getIoPart())
                        .texture("overlay_tinted", ENERGY_OUT_1A.getTintedPart());
                tieredHullTextures(model, builder.getOwner().getTier());
                addWorkableOverlays(overlays, status, model);

                return model;
            });
        };
    }

    public static MachineBuilder.ModelInitializer createBatteryBufferModel(int inventorySize) {
        return (ctx, prov, builder) -> {
            var overlay = OUT_OVERLAYS_FOR_AMP.get(inventorySize);

            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                    .texture("overlay_in_io", overlay.getIoPart())
                    .texture("overlay_in_tinted", overlay.getTintedPart())
                    .texture("overlay_out_io", BLANK_TEXTURE);
            tieredHullTextures(model, builder.getOwner().getTier());

            builder.forAllStatesModels(state -> model);
        };
    }

    // spotless:off
    public static final ResourceLocation CHARGER_IDLE = GTCEu.id("block/machines/charger/overlay_charger_idle");
    public static final ResourceLocation CHARGER_RUNNING = GTCEu.id("block/machines/charger/overlay_charger_running");
    public static final ResourceLocation CHARGER_RUNNING_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_running_emissive");
    public static final ResourceLocation CHARGER_FINISHED = GTCEu.id("block/machines/charger/overlay_charger_finished");
    public static final ResourceLocation CHARGER_FINISHED_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_finished_emissive");
    
    public static MachineBuilder.ModelInitializer createChargerModel() {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(renderState -> {
                ChargerMachine.State state = renderState.getValue(ChargerMachine.STATE_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_SIDED_OVERLAY_MODEL));
                tieredHullTextures(model, builder.getOwner().getTier());

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

    public static MachineBuilder.ModelInitializer createConverterModel(int amperage) {
        return (ctx, prov, builder) -> {
            final EnergyIOOverlay energyIn = IN_OVERLAYS_FOR_AMP.get(amperage);
            final EnergyIOOverlay energyOut = OUT_OVERLAYS_FOR_AMP.get(amperage);

            BlockModelBuilder euToFeModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                    .texture("overlay_in_io", energyIn.getIoPart())
                    .texture("overlay_in_tinted", energyIn.getTintedPart())
                    .texture("overlay_out_io", CONVERTER_FE_OUT);
            tieredHullTextures(euToFeModel, builder.getOwner().getTier());
            BlockModelBuilder feToEuModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                    .texture("overlay_in_io", energyOut.getIoPart())
                    .texture("overlay_in_tinted", energyOut.getTintedPart())
                    .texture("overlay_out_io", CONVERTER_FE_IN);
            tieredHullTextures(feToEuModel, builder.getOwner().getTier());

            builder.partialState()
                    .with(ConverterMachine.FE_TO_EU_PROPERTY, false)
                    .setModel(euToFeModel)
                    .partialState()
                    .with(ConverterMachine.FE_TO_EU_PROPERTY, true)
                    .setModel(feToEuModel)
                    .end();
        };
    }

    public static MachineBuilder.ModelInitializer createCrateModel(boolean wooden) {
        return (ctx, prov, builder) -> {
            String modelPath = "block/machine/template/crate/" + (wooden ? "wooden" : "metal") + "_crate";
            ModelFile baseModel = prov.models().getExistingFile(GTCEu.id(modelPath));
            ModelFile tapedModel = prov.models().getExistingFile(GTCEu.id(modelPath + "_taped"));

            builder.forAllStatesModels(state -> {
                if (state.getOptionalValue(CrateMachine.TAPED_PROPERTY).orElse(false)) {
                    return tapedModel;
                } else {
                    return baseModel;
                }
            });
        };
    }

    public static MachineBuilder.ModelInitializer createDiodeModel() {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(renderState -> {
                DiodePartMachine.AmpMode mode = renderState.getValue(DiodePartMachine.AMP_MODE_PROPERTY);
                final EnergyIOOverlay energyIn = IN_OVERLAYS_FOR_AMP.get(mode.getAmpValue());
                final EnergyIOOverlay energyOut = OUT_OVERLAYS_FOR_AMP.get(mode.getAmpValue());

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(TRANSFORMER_LIKE))
                        .texture("overlay_in_io", energyIn.getIoPart())
                        .texture("overlay_in_tinted", energyIn.getTintedPart())
                        .texture("overlay_out_io", energyOut.getIoPart())
                        .texture("overlay_out_tinted", energyOut.getTintedPart());
                tieredHullTextures(model, builder.getOwner().getTier());
                return model;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createTransformerModel(int baseAmp) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(renderState -> {
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
                tieredHullTextures(model, builder.getOwner().getTier());
                return model;
            });
        };
    }

    // spotless:off
    public static final ResourceLocation ROTOR_HOLDER_BLOCK = GTCEu.id("block/machine/template/rotor_holder/block");
    public static final ResourceLocation ROTOR_HOLDER_OVERLAY = GTCEu.id("block/machine/template/rotor_holder/overlay");
    public static final ResourceLocation ROTOR_HOLDER_ROTOR_IDLE = GTCEu.id("block/machine/template/rotor_holder/rotor_idle");
    public static final ResourceLocation ROTOR_HOLDER_ROTOR_SPINNING = GTCEu.id("block/machine/template/rotor_holder/rotor_spinning");
    // spotless:on

    public static MachineBuilder.ModelInitializer createRotorHolderModel() {
        return (ctx, prov, builder) -> {
            var blockModel = prov.models().nested()
                    .parent(prov.models().getExistingFile(ROTOR_HOLDER_BLOCK));
            tieredHullTextures(blockModel, builder.getOwner().getTier());

            builder.part(blockModel).end();
            builder.part(ROTOR_HOLDER_OVERLAY).condition(IS_FORMED_PROPERTY, true).end();
            makeRotorHolderPart(builder, ROTOR_HOLDER_ROTOR_IDLE, false, false);
            makeRotorHolderPart(builder, ROTOR_HOLDER_ROTOR_IDLE.withSuffix(EMISSIVE_SUFFIX), false, true);
            makeRotorHolderPart(builder, ROTOR_HOLDER_ROTOR_SPINNING, true, false);
            makeRotorHolderPart(builder, ROTOR_HOLDER_ROTOR_SPINNING.withSuffix(EMISSIVE_SUFFIX), true, true);

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    private static void makeRotorHolderPart(MachineModelBuilder<BlockModelBuilder> builder, ResourceLocation model,
                                            boolean spinning, boolean emissive) {
        builder.part(model)
                .condition(IS_FORMED_PROPERTY, true)
                .condition(HAS_ROTOR_PROPERTY, true)
                .condition(ROTOR_SPINNING_PROPERTY, spinning)
                .condition(EMISSIVE_ROTOR_PROPERTY, emissive);
    }

    public static final ImmutableMap<Material, ResourceLocation> MATERIALS_TO_CASING_TEXTURES = Util.make(() -> {
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
    public static MachineBuilder.ModelInitializer createWorldAcceleratorModel(ResourceLocation beModeModelPath, ResourceLocation rtModeModelPath) {
        return (ctx, prov, builder) -> {
            WorkableOverlays rtOverlays = WorkableOverlays.get(rtModeModelPath, prov.getExistingFileHelper());
            WorkableOverlays beOverlays = WorkableOverlays.get(beModeModelPath, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                boolean rtMode = state.getValue(WorldAcceleratorMachine.RANDOM_TICK_PROPERTY);
                WorkableOverlays overlays = rtMode ? rtOverlays : beOverlays;

                boolean active = state.getValue(IWorkable.ACTIVE_PROPERTY);
                boolean workingEnabled = state.getValue(WorldAcceleratorMachine.WORKING_ENABLED_PROPERTY);
                RecipeLogic.Status status = active ?
                        workingEnabled ?
                                RecipeLogic.Status.WORKING :
                                RecipeLogic.Status.SUSPEND :
                        RecipeLogic.Status.IDLE;

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_SIDED_OVERLAY_MODEL));
                tieredHullTextures(model, builder.getOwner().getTier());

                return addWorkableOverlays(overlays, status, model);
            });
        };
    }

    public static final ResourceLocation MAINTENANCE_TAPED_OVERLAY = GTCEu.id("block/overlay/machine/overlay_maintenance_taped");
    // spotless:on

    public static MachineBuilder.ModelInitializer createMaintenanceModel(ResourceLocation overlayModel) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                var baseModel = prov.models().nested()
                        .parent(prov.models().getExistingFile(overlayModel));
                tieredHullTextures(baseModel, builder.getOwner().getTier());

                if (!state.getValue(IMaintenanceMachine.MAINTENANCE_TAPED_PROPERTY)) {
                    baseModel.texture("overlay_2", MAINTENANCE_TAPED_OVERLAY);
                }
                return baseModel;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    // spotless:off
    public static final ResourceLocation HPCA_PART_MODEL = GTCEu.id("block/machine/template/hpca_part_machine");
    public static final ResourceLocation COMPUTER_CASING_TEXTURE = GTCEu.id("block/casings/hpca/computer_casing/");
    public static final ResourceLocation ADVANCED_COMPUTER_CASING_TEXTURE = GTCEu.id("block/casings/hpca/advanced_computer_casing/");

    public static MachineBuilder.ModelInitializer createHPCAPartModel(boolean advanced,
                                                                      ResourceLocation normalTexture,
                                                                      ResourceLocation damagedTexture) {
        return (ctx, prov, builder) -> {
            ResourceLocation textures = advanced ? ADVANCED_COMPUTER_CASING_TEXTURE : COMPUTER_CASING_TEXTURE;
            HPCAOverlay overlay = HPCAOverlay.get(normalTexture, damagedTexture, prov.getExistingFileHelper());

            var baseModel = prov.models().withExistingParent(ctx.getName() + "_base", HPCA_PART_MODEL);
            casingTexture(baseModel, "bottom", textures);
            casingTexture(baseModel, "top", textures);
            casingTexture(baseModel, "front", textures);
            casingTexture(baseModel, "back", textures);
            casingTexture(baseModel, "side", textures);

            builder.forAllStatesModels(state -> {
                boolean damaged = state.getValue(IHPCAComponentHatch.HPCA_PART_DAMAGED_PROPERTY);
                boolean active = state.getValue(IWorkable.ACTIVE_PROPERTY);

                return prov.models().nested().parent(baseModel)
                        .texture("overlay", overlay.getTexture(active, damaged))
                        .texture("overlay_emissive", overlay.getEmissiveTexture(active, damaged));
            });
        };
    }

    public static final ResourceLocation OVERLAY_SCREEN_TEXTURE = GTCEu.id("block/overlay/machine/overlay_screen");
    public static final ResourceLocation OVERLAY_QTANK_EMISSIVE_TEXTURE = GTCEu.id("block/overlay/machine/overlay_qtank_emissive");

    public static MachineBuilder.ModelInitializer createFisherModel() {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(GTCEu.id("block/overlay/2_layer/top_emissive")))
                    .texture("overlay", OVERLAY_SCREEN_TEXTURE)
                    .texture("overlay_emissive", OVERLAY_QTANK_EMISSIVE_TEXTURE);
            tieredHullTextures(model, builder.getOwner().getTier());

            builder.forAllStatesModels(state -> model);
        };
    }

    public static MachineBuilder.ModelInitializer createItemCollectorModel(ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                boolean active = state.getValue(IWorkable.ACTIVE_PROPERTY);
                boolean workingEnabled = state.getValue(WorldAcceleratorMachine.WORKING_ENABLED_PROPERTY);
                RecipeLogic.Status status = active ?
                                            workingEnabled ?
                                            RecipeLogic.Status.WORKING :
                                            RecipeLogic.Status.SUSPEND :
                                            RecipeLogic.Status.IDLE;

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_SIDED_OVERLAY_MODEL));
                tieredHullTextures(model, builder.getOwner().getTier());

                return addWorkableOverlays(overlays, status, model);
            });
        };
    }

    // endregion

    // region helper functions

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> createMachineModel(MachineBuilder.ModelInitializer modelInitializer) {
        return (ctx, prov) -> {
            Block block = ctx.getEntry();
            if (!(block instanceof IMachineBlock machineBlock)) {
                throw new IllegalArgumentException(
                        "passed block must be a machine block, is " + block.getClass().getName());
            }
            MachineDefinition definition = machineBlock.getDefinition();

            MachineModelBuilder<BlockModelBuilder> builder = prov.models().getBuilder("block/machine/" + ctx.getName())
                    .customLoader(MachineModelBuilder.begin(definition));
            modelInitializer.configureModel(ctx, prov, builder);
            final BlockModelBuilder model = builder.end();
            model.parent(prov.models().getExistingFile(prov.mcLoc("block/block")));

            BlockStateProvider.ConfiguredModelList modelList = null;
            if (!builder.getModels().isEmpty()) {
                modelList = builder.getModels().get(builder.partialState());
            } else if (!builder.getParts().isEmpty()) {
                modelList = builder.getParts().get(0).models;
            }
            if (modelList != null) {
                var models = ((ConfiguredModelListAccessor) modelList).gtceu$getModels();
                if (!models.isEmpty()) {
                    String texture = findParticleTexture(models.get(0).model, prov.getExistingFileHelper());
                    if (texture != null) {
                        model.texture("particle", texture);
                    }
                }
            }

            var generator = prov.multiVariantGenerator(block,
                    Variant.variant().with(VariantProperties.MODEL, model.getLocation()));
            PropertyDispatch dispatch = GTBlockstateProvider.createFacingDispatch(definition);
            if (dispatch != null) {
                generator.with(dispatch);
            }
        };
    }
    // spotless:on

    private static @Nullable String findParticleTexture(ModelFile modelFile, ExistingFileHelper existingFileHelper) {
        if (modelFile instanceof ModelBuilderAccessor b) {
            return b.gtceu$getTextures().get("particle");
        } else {
            try {
                Resource res = existingFileHelper.getResource(modelFile.getLocation(),
                        GTBlockstateProvider.MODEL.getPackType(),
                        GTBlockstateProvider.MODEL.getSuffix(), GTBlockstateProvider.MODEL.getPrefix());

                try (BufferedReader reader = res.openAsReader()) {
                    JsonObject json = GsonHelper.parse(reader, true);
                    if (json.has("textures")) {
                        return GsonHelper.getAsString(json.getAsJsonObject("textures"), "particle", null);
                    }
                }
            } catch (IOException e) {
                GTCEu.LOGGER.error("guh? couldn't find model at {} for particle textures",
                        modelFile.getUncheckedLocation(), e);
            }
        }
        return null;
    }

    public static ConfiguredModel[] addWorkableOverlays(WorkableOverlays overlays, RecipeLogic.Status status,
                                                        BlockModelBuilder model) {
        for (var entry : overlays.getTextures().entrySet()) {
            var face = entry.getKey();
            var textures = entry.getValue();

            ResourceLocation overlay = textures.getTexture(status);
            ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

            if (overlay != BLANK_TEXTURE) {
                model.texture(OVERLAY_PREFIX + face.getName(), overlay);
            }
            if (overlayEmissive != BLANK_TEXTURE) {
                model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_SUFFIX, overlayEmissive);
            }
        }
        return ConfiguredModel.builder().modelFile(model).build();
    }

    public static BlockModelBuilder colorOverlayHullModel(ResourceLocation overlayTexture,
                                                          @Nullable ResourceLocation emissiveOverlayTexture,
                                                          MachineRenderState state, BlockModelProvider models) {
        ResourceLocation parent;
        if (state.getOptionalValue(IPaintable.IS_PAINTED_PROPERTY).orElse(false)) {
            parent = emissiveOverlayTexture != null ? OVERLAY_EMISSIVE_COLOR_RING_MODEL : OVERLAY_COLOR_RING_MODEL;
        } else {
            parent = emissiveOverlayTexture != null ? OVERLAY_EMISSIVE_MODEL : OVERLAY_MODEL;
        }
        BlockModelBuilder model = models.nested().parent(models.getExistingFile(parent))
                .texture("overlay", overlayTexture);
        if (emissiveOverlayTexture != null) {
            model.texture("overlay_emissive", emissiveOverlayTexture);
        }
        return model;
    }

    public static ModelFile tieredHullModel(BlockModelProvider models,
                                            MachineModelBuilder<BlockModelBuilder> builder) {
        return tieredHullModel(models, builder.getOwner().getTier());
    }

    public static ModelFile tieredHullModel(BlockModelProvider models, int tier) {
        return models.getExistingFile(TIERED_HULL_MODELS.get(tier));
    }

    public static ModelFile steamHullModel(BlockModelProvider models, boolean highPressure) {
        return models.getExistingFile(highPressure ? HP_STEAM_HULL_MODEL : LP_STEAM_HULL_MODEL);
    }

    public static ResourceLocation getTieredHullTexture(int tier) {
        return GTCEu.id("block/casings/voltage/%s/".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT)));
    }

    public static BlockModelBuilder tieredHullTextures(BlockModelBuilder model, int tier) {
        return casingTextures(model, getTieredHullTexture(tier));
    }

    public static ResourceLocation getSteamCasingTexture(boolean steel) {
        return GTCEu.id("block/casings/steam/%s/".formatted(steel ? "steel" : "bronze"));
    }

    public static BlockModelBuilder steamCasingTextures(BlockModelBuilder model, boolean steel) {
        return casingTextures(model, getSteamCasingTexture(steel));
    }

    public static void casingTexture(BlockModelBuilder model, String key, ResourceLocation texturePath) {
        model.texture(key, texturePath.withSuffix(key));
    }

    public static BlockModelBuilder casingTextures(BlockModelBuilder model, ResourceLocation texturePath) {
        if (!texturePath.getPath().endsWith("/")) {
            texturePath = texturePath.withSuffix("/");
        }
        casingTexture(model, "bottom", texturePath);
        casingTexture(model, "top", texturePath);
        casingTexture(model, "side", texturePath);
        return model;
    }

    // endregion
}
