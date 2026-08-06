package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.recipe.*;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.*;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.recipe.gui.GTRecipeUIModifiers;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.ModLoader;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.progress.CircularProgressDrawable;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class GTRecipeTypes {

    public static final String STEAM = "steam";
    public static final String ELECTRIC = "electric";
    public static final String GENERATOR = "generator";
    public static final String MULTIBLOCK = "multiblock";
    public static final String DUMMY = "dummy";

    static {
        GTRegistries.RECIPE_TYPES.unfreeze();
        GTRegistries.RECIPE_CATEGORIES.unfreeze();
    }

    //////////////////////////////////////
    // ********* Steam **********//
    //////////////////////////////////////
    public final static GTRecipeType STEAM_BOILER_RECIPES = register("steam_boiler", STEAM)
            .setMaxIOSize(1, 0, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BOILER_FUEL_STEEL))
            .onRecipeBuild((builder, provider) -> {
                // all LBB recipes' duration is 1/4 the small boiler recipe's duration
                int duration = builder.duration / 4;
                if (duration > 0) {
                    GTRecipeTypes.LARGE_BOILER_RECIPES.copyFrom(builder).duration(duration).save(provider);
                }
            })
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FURNACE);

    //////////////////////////////////////
    // ********* Common *********//
    //////////////////////////////////////
    public final static GTRecipeType FURNACE_RECIPES = register("electric_furnace", ELECTRIC, RecipeType.SMELTING)
            .setMaxIOSize(1, 1, 0, 0).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(4))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_1))
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType ALLOY_SMELTER_RECIPES = register("alloy_smelter", ELECTRIC)
            .setMaxIOSize(2, 1, 0, 0).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_1))
            .setIconSupplier(() -> GTMachines.ALLOY_SMELTER[GTValues.LV].asStack())
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType ARC_FURNACE_RECIPES = register("arc_furnace", ELECTRIC).setMaxIOSize(1, 4, 1, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT,
                            l -> GTMuiWidgets.createGrid(4, 2, true, 's')))
            .setSound(GTSoundEntries.ARC)
            .onRecipeBuild((recipeBuilder, provider) -> {
                if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty() &&
                        recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList())
                                .isEmpty()) {
                    recipeBuilder.inputFluids(GTMaterials.Oxygen.getFluid(recipeBuilder.duration));
                }
            })
            .addCustomRecipeLogic(ArcFurnaceLogic.INSTANCE);

    public final static GTRecipeType ASSEMBLER_RECIPES = register("assembler", ELECTRIC).setMaxIOSize(9, 1, 1, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER))
            .setSound(GTSoundEntries.ASSEMBLER);

    public final static GTRecipeType AUTOCLAVE_RECIPES = register("autoclave", ELECTRIC).setMaxIOSize(2, 2, 1, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.DUST_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CRYSTAL_OVERLAY))
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType BENDER_RECIPES = register("bender", ELECTRIC).setMaxIOSize(2, 1, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BENDING)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.BENDER_OVERLAY)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.INT_CIRCUIT_OVERLAY))
            .setSound(GTSoundEntries.MOTOR);

    public final static GTRecipeType BREWING_RECIPES = register("brewery", ELECTRIC).setMaxIOSize(1, 0, 1, 1)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(128).EUt(4))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.BREWER_OVERLAY))
            .addCustomRecipeLogic(BreweryLogic.INSTANCE)
            .setSound(GTSoundEntries.CHEMICAL);

    public final static GTRecipeType MACERATOR_RECIPES = register("macerator", ELECTRIC).setMaxIOSize(1, 4, 0, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(150).EUt(2))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MACERATE)
                    .setMachineLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT, (machine, layout) -> {
                        var slots = layout.getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP);
                        var width = 3;
                        if (machine instanceof ITieredMachine tieredMachine) {
                            if (tieredMachine.getTier() < GTValues.HV) {
                                slots = 1;
                                width = 1;
                            } else if (tieredMachine.getTier() == GTValues.HV) {
                                slots = 3;
                            } else {
                                slots = 4;
                                width = 2;
                            }
                        }

                        return GTMuiWidgets.createGrid(slots, width, true, 's');
                    })
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
                    .setItemSlotsOverlay(IO.OUT, 0, 3, GTGuiTextures.DUST_OVERLAY)
                    .addRecipeUIModifier(RecipeUIModifier
                            .textLine(Text.lang("gtceu.recipe.byproduct_tier", GTValues.VNF[GTValues.HV]))))
            .setIconSupplier(() -> GTMachines.MACERATOR[GTValues.LV].asStack())
            .addCustomRecipeLogic(MaceratorLogic.INSTANCE)
            .setSound(GTSoundEntries.MACERATOR);

    public final static GTRecipeType CANNER_RECIPES = register("canner", ELECTRIC).setMaxIOSize(2, 2, 1, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CANNER)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CANNER_OVERLAY)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.CANISTER_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CANISTER_OVERLAY)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.DARK_CANISTER_OVERLAY)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.DARK_CANISTER_OVERLAY))
            .addCustomRecipeLogic(CannerLogic.INSTANCE)
            .setSound(GTSoundEntries.BATH);

    public final static GTRecipeType CENTRIFUGE_RECIPES = register("centrifuge", ELECTRIC).setMaxIOSize(2, 6, 1, 6)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(5))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.EXTRACTOR_OVERLAY)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.CANISTER_OVERLAY)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY))
            .setSound(GTSoundEntries.CENTRIFUGE);

    public final static GTRecipeType CHEMICAL_BATH_RECIPES = register("chemical_bath", ELECTRIC)
            .setMaxIOSize(1, 6, 1, 1).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.LV]))
            .UI(builder -> builder
                    .setProgressBarSupplier((l, v, m) -> {
                        return new CircularProgressDrawable()
                                .emptyTexture(GTGuiTextures.PROGRESS_BATH[0])
                                .filledTexture(GTGuiTextures.PROGRESS_BATH[1])
                                .clockwise()
                                .asWidget()
                                .value(v);
                    })
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.BREWER_OVERLAY)
                    .setItemSlotsOverlay(IO.OUT, 0, 5, GTGuiTextures.DUST_OVERLAY)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY))
            .setSound(GTSoundEntries.BATH);

    public final static GTRecipeType CHEMICAL_RECIPES = register("chemical_reactor", ELECTRIC).setMaxIOSize(2, 2, 3, 2)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.LV]))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.MOLECULAR_OVERLAY_1)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.MOLECULAR_OVERLAY_2)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.MOLECULAR_OVERLAY_3)
                    .setFluidSlotOverlay(IO.IN, 1, GTGuiTextures.MOLECULAR_OVERLAY_4)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.VIAL_OVERLAY_1)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.VIAL_OVERLAY_2))
            .setSound(GTValues.FOOLS.getAsBoolean() ? GTSoundEntries.SCIENCE : GTSoundEntries.CHEMICAL)
            .onRecipeBuild((recipeBuilder, provider) -> GTRecipeTypes.LARGE_CHEMICAL_RECIPES.copyFrom(recipeBuilder)
                    .save(provider));

    public final static GTRecipeType COMPRESSOR_RECIPES = register("compressor", ELECTRIC).setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(200).EUt(2))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_COMPRESS)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.COMPRESSOR_OVERLAY))
            .setIconSupplier(() -> GTMachines.COMPRESSOR[GTValues.LV].asStack())
            .setSound(GTSoundEntries.COMPRESSOR);

    public final static GTRecipeType CUTTER_RECIPES = register("cutter", ELECTRIC).setMaxIOSize(1, 2, 1, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CUTTER)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SAWBLADE_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CUTTER_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 1, GTGuiTextures.DUST_OVERLAY))
            .setSound(GTSoundEntries.CUT)
            .onRecipeBuild((recipeBuilder, provider) -> {
                if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty() &&
                        recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList())
                                .isEmpty()) {
                    recipeBuilder.copy(recipeBuilder.id.withSuffix("_water"))
                            .inputFluids(GTMaterials.Water.getFluid((int) GTMath.clamp(
                                    recipeBuilder.duration * recipeBuilder.EUt().getTotalEU() / 320, 4, 1000)))
                            .duration(recipeBuilder.duration * 2)
                            .save(provider);

                    recipeBuilder.copy((recipeBuilder.id.withSuffix("_distilled_water")))
                            .inputFluids(GTMaterials.DistilledWater.getFluid((int) GTMath.clamp(
                                    recipeBuilder.duration * recipeBuilder.EUt().getTotalEU() / 426, 3, 750)))
                            .duration((int) (recipeBuilder.duration * 1.5))
                            .save(provider);

                    // Don't call buildAndRegister as we are mutating the original recipe and already in the middle of a
                    // buildAndRegister call.
                    // Adding a second call will result in duplicate recipe generation attempts
                    recipeBuilder.inputFluids(GTMaterials.Lubricant.getFluid((int) GTMath.clamp(
                            recipeBuilder.duration * recipeBuilder.EUt().getTotalEU() / 1280, 1, 250)));
                }
            });

    public final static GTRecipeType DISTILLERY_RECIPES = register("distillery", ELECTRIC).setMaxIOSize(1, 1, 1, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.BEAKER_OVERLAY_1)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.BEAKER_OVERLAY_4))
            // .setSlotOverlay(false, true, GuiTextures.BEAKER_OVERLAY_1)
            // .setSlotOverlay(true, true, GuiTextures.BEAKER_OVERLAY_4)
            // .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            // .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            // .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BOILER);

    public final static GTRecipeType ELECTROLYZER_RECIPES = register("electrolyzer", ELECTRIC).setMaxIOSize(2, 6, 1, 6)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.LIGHTNING_OVERLAY_1)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.CANISTER_OVERLAY)
                    .setFluidSlotsOverlay(IO.IN, 0, 5, GTGuiTextures.LIGHTNING_OVERLAY_2))
            .setSound(GTSoundEntries.ELECTROLYZER);

    public final static GTRecipeType ELECTROMAGNETIC_SEPARATOR_RECIPES = register("electromagnetic_separator", ELECTRIC)
            .setMaxIOSize(1, 3, 0, 0).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MAGNET)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY))
            .setSound(GTSoundEntries.ARC);

    public final static GTRecipeType EXTRACTOR_RECIPES = register("extractor", ELECTRIC).setMaxIOSize(1, 1, 0, 1)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(2))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.EXTRACTOR_OVERLAY))
            .setIconSupplier(() -> GTMachines.EXTRACTOR[GTValues.LV].asStack());

    public final static GTRecipeType EXTRUDER_RECIPES = register("extruder", ELECTRIC).setMaxIOSize(2, 1, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRUDER)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.MOLD_OVERLAY))
            // .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)
            // .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRUDER, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COMPRESSOR);

    public final static GTRecipeType FERMENTING_RECIPES = register("fermenter", ELECTRIC).setMaxIOSize(1, 1, 1, 1)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(2))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.DUST_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY))
            // .setSlotOverlay(false, false, true, GuiTextures.DUST_OVERLAY)
            // .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            // .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    public final static GTRecipeType FLUID_HEATER_RECIPES = register("fluid_heater", ELECTRIC).setMaxIOSize(1, 0, 1, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.HEATING_OVERLAY_1)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.HEATING_OVERLAY_2))
            // .setSlotOverlay(false, true, GuiTextures.HEATING_OVERLAY_1)
            // .setSlotOverlay(true, true, GuiTextures.HEATING_OVERLAY_2)
            // .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            // .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BOILER);

    public final static GTRecipeType FLUID_SOLIDFICATION_RECIPES = register("fluid_solidifier", ELECTRIC)
            .setMaxIOSize(1, 1, 1, 0).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SOLIDIFIER_OVERLAY))
            // .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
            // .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COOLING);

    private static final GTRecipeTypeUILayout.ProgressWidgetSupplier forgeHammerProgressBar = (layout, value,
                                                                                               machine) -> Flow.col()
                                                                                                       .coverChildren()
                                                                                                       .child(new ProgressWidget()
                                                                                                               .value(value)
                                                                                                               .name("progressBar")
                                                                                                               .texture(
                                                                                                                       GTGuiTextures.PROGRESS_HAMMER
                                                                                                                               .get(machine),
                                                                                                                       ProgressDrawable.Direction.DOWN)
                                                                                                               .size(20))
                                                                                                       .child(GTGuiTextures.PROGRESS_HAMMER_BASE
                                                                                                               .get(machine)
                                                                                                               .asWidget()
                                                                                                               .height(5));

    public final static GTRecipeType FORGE_HAMMER_RECIPES = register("forge_hammer", ELECTRIC).setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_HAMMER)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.HAMMER_OVERLAY)
                    .setProgressBarSupplier(forgeHammerProgressBar))
            .setIconSupplier(() -> GTMachines.FORGE_HAMMER[GTValues.LV].asStack())
            .setSound(GTSoundEntries.FORGE_HAMMER);

    public final static GTRecipeType FORMING_PRESS_RECIPES = register("forming_press", ELECTRIC)
            .setMaxIOSize(6, 1, 0, 0).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_COMPRESS))
            .addCustomRecipeLogic(FormingPressLogic.INSTANCE)
            .setSound(GTSoundEntries.COMPRESSOR);

    public final static GTRecipeType LATHE_RECIPES = register("lathe", ELECTRIC).setMaxIOSize(1, 2, 0, 0).setEUIO(IO.IN)
            .UI(builder -> builder
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.PIPE_OVERLAY_1)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.PIPE_OVERLAY_2)
                    .setItemSlotOverlay(IO.OUT, 1, GTGuiTextures.DUST_OVERLAY)
                    .setProgressBarSupplier((layout, value, machine) -> Flow.row().coverChildren()
                            .child(new ProgressWidget()
                                    .value(value)
                                    .name("progressBar")
                                    .texture(GTGuiTextures.PROGRESS_BAR_LATHE, ProgressDrawable.Direction.RIGHT)
                                    .size(20))
                            .child(GTGuiTextures.PROGRESS_BAR_LATHE_BASE.asWidget().width(5))))
            .setSound(GTSoundEntries.CUT);

    public final static GTRecipeType MIXER_RECIPES = register("mixer", ELECTRIC).setMaxIOSize(6, 1, 2, 1).setEUIO(IO.IN)
            .UI(builder -> builder
                    .setProgressBarSupplier((l, v, m) -> {
                        return new CircularProgressDrawable()
                                .emptyTexture(GTGuiTextures.PROGRESS_MIXER[0])
                                .filledTexture(GTGuiTextures.PROGRESS_MIXER[1])
                                .clockwise()
                                .asWidget()
                                .value(v);
                    })
                    .setItemSlotsOverlay(IO.IN, 0, 5, GTGuiTextures.DUST_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY))
            .setSound(GTSoundEntries.MIXER);

    public final static GTRecipeType ORE_WASHER_RECIPES = register("ore_washer", ELECTRIC).setMaxIOSize(2, 3, 1, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(16))
            .UI(builder -> builder
                    .setProgressBarSupplier((l, v, m) -> {
                        return new CircularProgressDrawable()
                                .emptyTexture(GTGuiTextures.PROGRESS_BATH[0])
                                .filledTexture(GTGuiTextures.PROGRESS_BATH[1])
                                .clockwise()
                                .asWidget()
                                .value(v);
                    })
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY))
            .setSound(GTSoundEntries.BATH);

    public final static GTRecipeType PACKER_RECIPES = register("packer", ELECTRIC).setMaxIOSize(2, 2, 0, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(12).duration(10))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_PACKER)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.BOX_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.BOX_OVERLAY))
            .setSound(GTSoundEntries.ASSEMBLER);

    public final static GTRecipeType POLARIZER_RECIPES = register("polarizer", ELECTRIC).setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MAGNET))
            .setSound(GTSoundEntries.ARC);

    public final static GTRecipeType LASER_ENGRAVER_RECIPES = register("laser_engraver", ELECTRIC)
            .setMaxIOSize(2, 1, 0, 0).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.LENS_OVERLAY))
            .setSound(GTSoundEntries.ELECTROLYZER);

    public final static GTRecipeType SIFTER_RECIPES = register("sifter", ELECTRIC).setMaxIOSize(1, 6, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_SIFTER))
            .setSound(new ExistingSoundEntry(SoundEvents.SAND_PLACE, SoundSource.BLOCKS));

    public final static GTRecipeType THERMAL_CENTRIFUGE_RECIPES = register("thermal_centrifuge", ELECTRIC)
            .setMaxIOSize(1, 3, 0, 0).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(30))
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY))
            .setSound(GTSoundEntries.CENTRIFUGE);

    public final static GTRecipeType WIREMILL_RECIPES = register("wiremill", ELECTRIC).setMaxIOSize(2, 1, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_WIREMILL)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.WIREMILL_OVERLAY))
            .setSound(GTSoundEntries.MOTOR);

    public final static GTRecipeType CIRCUIT_ASSEMBLER_RECIPES = register("circuit_assembler", ELECTRIC)
            .setMaxIOSize(6, 1, 1, 0).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CIRCUIT_ASSEMBLER)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CIRCUIT_OVERLAY))
            .setSound(GTSoundEntries.ASSEMBLER)
            .onRecipeBuild((recipeBuilder, provider) -> {
                if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty() &&
                        recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList())
                                .isEmpty()) {
                    recipeBuilder.copy(recipeBuilder.id.withSuffix("_soldering_alloy"))
                            .inputFluids(GTMaterials.SolderingAlloy
                                    .getFluid(Math.max(1, (GTValues.L / 2) * recipeBuilder.getSolderMultiplier())))
                            .save(provider);

                    // Don't call buildAndRegister as we are mutating the original recipe and already in the middle of a
                    // buildAndRegister call.
                    // Adding a second call will result in duplicate recipe generation attempts
                    recipeBuilder.inputFluids(
                            GTMaterials.Tin.getFluid(Math.max(1, GTValues.L * recipeBuilder.getSolderMultiplier())));
                }
            });

    public final static GTRecipeType GAS_COLLECTOR_RECIPES = register("gas_collector", ELECTRIC)
            .setMaxIOSize(1, 0, 0, 1).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.CENTRIFUGE_OVERLAY))
            .setSound(GTSoundEntries.COOLING);

    public final static GTRecipeType AIR_SCRUBBER_RECIPES = register("air_scrubber", ELECTRIC)
            .setMaxIOSize(1, 3, 1, 3).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
            .setSound(GTSoundEntries.COOLING);

    public static final GTRecipeType RESEARCH_STATION_RECIPES = register("research_station", ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxSize(IO.IN, GTRecipeCapabilities.CWU, 1)
            .setMaxIOSize(2, 1, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SCANNER_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.RESEARCH_STATION_OVERLAY))
            .setScanner(true)
            .setMaxTooltips(4)
            .setSound(GTValues.FOOLS.getAsBoolean() ? GTSoundEntries.SCIENCE : GTSoundEntries.COMPUTATION);

    public final static GTRecipeType ROCK_BREAKER_RECIPES = register("rock_breaker", ELECTRIC).setMaxIOSize(1, 4, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MACERATE)
                    .setMachineLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT, (machine, layout) -> {

                        return GTMuiWidgets.createGrid(4, 2, true, 's');
                    })
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.DUST_OVERLAY)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY))
            .setIconSupplier(() -> GTMachines.ROCK_CRUSHER[GTValues.LV].asStack())
            .setSound(GTSoundEntries.FIRE);

    public static final GTRecipeType SCANNER_RECIPES = register("scanner", ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(2, 1, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.DATA_ORB_OVERLAY)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.SCANNER_OVERLAY))
            .addCustomRecipeLogic(new ResearchManager.DataStickCopyScannerLogic())
            .setScanner(true)
            .setSound(GTSoundEntries.ELECTROLYZER);

    //////////////////////////////////////
    // ******* Generator *******//
    //////////////////////////////////////
    public final static GTRecipeType COMBUSTION_GENERATOR_FUELS = register("combustion_generator", GENERATOR)
            .setMaxIOSize(0, 0, 1, 0).setEUIO(IO.OUT)
            .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_2)
                    .setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setSound(GTSoundEntries.COMBUSTION);

    public final static GTRecipeType GAS_TURBINE_FUELS = register("gas_turbine", GENERATOR).setMaxIOSize(0, 0, 1, 0)
            .setEUIO(IO.OUT)
            .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.DARK_CANISTER_OVERLAY)
                    .setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
            .setSound(GTSoundEntries.TURBINE);

    public final static GTRecipeType STEAM_TURBINE_FUELS = register("steam_turbine", GENERATOR).setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.OUT)
            .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
            .setSound(GTSoundEntries.TURBINE);

    public final static GTRecipeType PLASMA_GENERATOR_FUELS = register("plasma_generator", GENERATOR)
            .setMaxIOSize(0, 0, 1, 1).setEUIO(IO.OUT)
            .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
            .setSound(GTSoundEntries.TURBINE);

    //////////////////////////////////////
    // ******* Multiblock *******//
    //////////////////////////////////////
    public final static GTRecipeType LARGE_BOILER_RECIPES = register("large_boiler", MULTIBLOCK)
            .setMaxIOSize(1, 0, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BOILER_FUEL_STEEL))
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType COKE_OVEN_RECIPES = register("coke_oven", MULTIBLOCK).setMaxIOSize(1, 1, 0, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FIRE);

    public final static GTRecipeType PRIMITIVE_BLAST_FURNACE_RECIPES = register("primitive_blast_furnace", MULTIBLOCK)
            .setMaxIOSize(3, 3, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FIRE);

    public final static GTRecipeType BLAST_RECIPES = register("electric_blast_furnace", MULTIBLOCK)
            .setMaxIOSize(3, 3, 1, 1).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .addRecipeUIModifier(GTRecipeUIModifiers.TEMP_COIL_INFO))
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType DISTILLATION_RECIPES = register("distillation_tower", MULTIBLOCK)
            .setMaxIOSize(0, 1, 1, 12).setEUIO(IO.IN)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .onRecipeBuild((recipeBuilder, provider) -> {
                if (recipeBuilder.data.getBoolean("disable_distillery")) return;
                if (recipeBuilder.output.containsKey(FluidRecipeCapability.CAP)) {
                    Content inputContent = recipeBuilder.input.get(FluidRecipeCapability.CAP).get(0);
                    FluidIngredient input = FluidRecipeCapability.CAP.of(inputContent.content());
                    ItemStack[] outputs = recipeBuilder.output.containsKey(ItemRecipeCapability.CAP) ?
                            ItemRecipeCapability.CAP
                                    .of(recipeBuilder.output.get(ItemRecipeCapability.CAP).get(0).content())
                                    .getItems() :
                            null;
                    ItemStack outputItem = outputs == null || outputs.length == 0 ? ItemStack.EMPTY : outputs[0];
                    if (input.isEmpty()) return;
                    List<Content> contents = recipeBuilder.output.get(FluidRecipeCapability.CAP);
                    for (int i = 0; i < contents.size(); ++i) {
                        Content outputContent = contents.get(i);
                        FluidIngredient output = FluidRecipeCapability.CAP.of(outputContent.content());
                        if (output.isEmpty()) continue;
                        GTRecipeBuilder builder = DISTILLERY_RECIPES
                                .recipeBuilder(recipeBuilder.id.getPath() + "_to_" +
                                        BuiltInRegistries.FLUID.getKey(output.getStacks()[0].getFluid()).getPath())
                                .EUt(Math.max(1, recipeBuilder.EUt().voltage() / 4), recipeBuilder.EUt().amperage())
                                .circuitMeta(i + 1);

                        int ratio = RecipeHelper.getRatioForDistillery(input, output, outputItem);
                        int recipeDuration = (int) (recipeBuilder.duration * OverclockingLogic.STD_DURATION_FACTOR_INV);
                        boolean shouldDivide = ratio != 1;

                        boolean fluidsDivisible = RecipeHelper.isFluidStackDivisibleForDistillery(input, ratio) &&
                                RecipeHelper.isFluidStackDivisibleForDistillery(output, ratio);

                        FluidIngredient dividedInputFluid = input.copy();
                        dividedInputFluid.setAmount(Math.max(1, dividedInputFluid.getAmount() / ratio));
                        FluidIngredient dividedOutputFluid = output.copy();
                        dividedOutputFluid.setAmount(Math.max(1, dividedOutputFluid.getAmount() / ratio));

                        if (shouldDivide && fluidsDivisible) {
                            builder.chance(inputContent.chance())
                                    .tierChanceBoost(inputContent.tierChanceBoost())
                                    .inputFluids(dividedInputFluid)
                                    .chance(outputContent.chance())
                                    .tierChanceBoost(outputContent.tierChanceBoost())
                                    .outputFluids(dividedOutputFluid)
                                    .duration(Math.max(1, recipeDuration / ratio));
                        } else if (!shouldDivide) {
                            if (!outputItem.isEmpty()) {
                                builder.outputItems(outputItem);
                            }
                            builder.conditions.addAll(recipeBuilder.conditions);
                            builder.chance(inputContent.chance())
                                    .tierChanceBoost(inputContent.tierChanceBoost())
                                    .inputFluids(input)
                                    .chance(outputContent.chance())
                                    .tierChanceBoost(outputContent.tierChanceBoost())
                                    .outputFluids(output)
                                    .duration(recipeDuration)
                                    .save(provider);
                            continue;
                        }

                        if (!outputItem.isEmpty()) {
                            boolean itemsDivisible = outputItem.getCount() % ratio == 0 && fluidsDivisible;

                            if (fluidsDivisible && itemsDivisible) {
                                ItemStack stack = outputItem.copy();
                                stack.setCount(stack.getCount() / ratio);

                                builder.outputItems(stack);
                            }
                        }
                        builder.save(provider);
                    }
                }
            });

    public final static GTRecipeType PYROLYSE_RECIPES = register("pyrolyse_oven", MULTIBLOCK).setMaxIOSize(2, 1, 1, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setSound(GTSoundEntries.FIRE);

    public final static GTRecipeType CRACKING_RECIPES = register("cracker", MULTIBLOCK).setMaxIOSize(1, 0, 2, 2)
            .setEUIO(IO.IN)
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_CRACKING)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CRACKING_OVERLAY_1)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.CRACKING_OVERLAY_2)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CIRCUIT_OVERLAY))
            .setSound(GTSoundEntries.FIRE);

    public final static GTRecipeType IMPLOSION_RECIPES = register("implosion_compressor", MULTIBLOCK)
            .setMaxIOSize(3, 2, 0, 0).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(20).EUt(GTValues.VA[GTValues.LV]))
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 2, GTGuiTextures.IMPLOSION_OVERLAY_1)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.IMPLOSION_OVERLAY_2)
                    .setItemSlotOverlay(IO.OUT, 1, GTGuiTextures.DUST_OVERLAY))
            .setSound(new ExistingSoundEntry(SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS));

    public final static GTRecipeType VACUUM_RECIPES = register("vacuum_freezer", MULTIBLOCK).setMaxIOSize(1, 1, 2, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.MV]))
            .setSound(GTSoundEntries.COOLING);

    public final static GTRecipeType ASSEMBLY_LINE_RECIPES = register("assembly_line", MULTIBLOCK)
            .setMaxIOSize(16, 1, 4, 0).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setSound(GTSoundEntries.ASSEMBLER)
            .setHasResearchSlot(true)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe);

    public static final GTRecipeType LARGE_CHEMICAL_RECIPES = register("large_chemical_reactor", MULTIBLOCK)
            .setMaxIOSize(3, 3, 5, 4).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.LV]))
            .UI(builder -> builder
                    .setItemSlotsOverlay(IO.IN, 0, 1, GTGuiTextures.MOLECULAR_OVERLAY_1)
                    .setItemSlotOverlay(IO.IN, 2, GTGuiTextures.MOLECULAR_OVERLAY_2)
                    .setFluidSlotsOverlay(IO.IN, 0, 1, GTGuiTextures.MOLECULAR_OVERLAY_3)
                    .setFluidSlotOverlay(IO.IN, 2, GTGuiTextures.MOLECULAR_OVERLAY_4)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.VIAL_OVERLAY_1)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.VIAL_OVERLAY_1)
                    .setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setSound(GTValues.FOOLS.getAsBoolean() ? GTSoundEntries.SCIENCE : GTSoundEntries.CHEMICAL)
            .setSmallRecipeMap(CHEMICAL_RECIPES);

    public static final GTRecipeType FUSION_RECIPES = register("fusion_reactor", MULTIBLOCK).setMaxIOSize(0, 0, 2, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_FUSION)
                    .addRecipeUIModifier(FusionReactorMachine::addEUToStartLabel))
            .setSound(GTSoundEntries.ARC)
            .setMaxTooltips(4);

    public static final GTRecipeType DUMMY_RECIPES = register("dummy", DUMMY)
            .setXEIVisible(false);

    public static GTRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        var recipeType = new GTRecipeType(GTCEu.id(name), group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }

    public static void init() {
        GCYMRecipeTypes.init();
        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.RECIPE_TYPES.getRegistryName());
        }
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.RECIPE_TYPES, GTRecipeType.class));
        GTRegistries.RECIPE_TYPES.freeze();

        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("machine"),
                GTRecipeSerializer.SERIALIZER);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("crafting_facade_cover"),
                FacadeCoverRecipe.SERIALIZER);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("crafting_shaped_strict"),
                StrictShapedRecipe.SERIALIZER);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("crafting_shaped_energy_transfer"),
                ShapedEnergyTransferRecipe.SERIALIZER);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("crafting_tool_head_replace"),
                ToolHeadReplaceRecipe.SERIALIZER);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("crafting_shaped_fluid_container"),
                ShapedFluidContainerRecipe.SERIALIZER);
    }

    public static GTRecipeType get(String name) {
        return GTRegistries.RECIPE_TYPES.get(GTCEu.id(name));
    }
}
