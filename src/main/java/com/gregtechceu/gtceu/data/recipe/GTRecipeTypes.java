package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.*;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.CannerLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.FormingPressLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.LargeBoilerLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.SteamBoilerLogic;
import com.gregtechceu.gtceu.common.recipe.RPMCondition;
import com.gregtechceu.gtceu.common.recipe.RockBreakerCondition;
import com.gregtechceu.gtceu.data.machine.GTMachines;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.sound.GTSoundEntries;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.simibubi.create.AllBlocks;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.*;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipeTypes
 */
public class GTRecipeTypes {

    public static final String STEAM = "steam";
    public static final String ELECTRIC = "electric";
    public static final String GENERATOR = "generator";
    public static final String MULTIBLOCK = "multiblock";
    public static final String DUMMY = "dummy";
    public static final String KINETIC = "kinetic";

    static {
        GTRegistries.RECIPE_TYPES.unfreeze();
    }

    //////////////////////////////////////
    // ********* Steam **********//
    //////////////////////////////////////
    public final static GTRecipeType STEAM_BOILER_RECIPES = register("steam_boiler", STEAM)
            .setMaxIOSize(1, 0, 1, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BOILER_FUEL.get(true), DOWN_TO_UP)
            .onRecipeBuild((builder, provider) -> {
                // remove the * 12 if SteamBoilerMachine:240 is uncommented
                var duration = (builder.duration / 12 / 80); // copied for large boiler
                if (duration > 0) {
                    GTRecipeTypes.LARGE_BOILER_RECIPES.copyFrom(builder).duration(duration).save(provider);
                }
            })
            .addCustomRecipeLogic(new SteamBoilerLogic())
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FURNACE);

    //////////////////////////////////////
    // ********* Common *********//
    //////////////////////////////////////
    public final static GTRecipeType FURNACE_RECIPES = register("electric_furnace", ELECTRIC, RecipeType.SMELTING)
            .setMaxIOSize(1, 1, 0, 0).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(4))
            .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSteamProgressBar(GuiTextures.PROGRESS_BAR_ARROW_STEAM, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType ALLOY_SMELTER_RECIPES = register("alloy_smelter", ELECTRIC)
            .setMaxIOSize(2, 1, 0, 0).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setIconSupplier(() -> GTMachines.ALLOY_SMELTER[GTValues.LV].asStack())
            .setSteamProgressBar(GuiTextures.PROGRESS_BAR_ARROW_STEAM, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType ARC_FURNACE_RECIPES = register("arc_furnace", ELECTRIC).setMaxIOSize(1, 4, 1, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)
            .onRecipeBuild((recipeBuilder, provider) -> {
                if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty() &&
                        recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList())
                                .isEmpty()) {
                    recipeBuilder.inputFluids(GTMaterials.Oxygen.getFluid(recipeBuilder.duration));
                }
            });

    public final static GTRecipeType ASSEMBLER_RECIPES = register("assembler", ELECTRIC).setMaxIOSize(9, 1, 1, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    public final static GTRecipeType AUTOCLAVE_RECIPES = register("autoclave", ELECTRIC).setMaxIOSize(2, 2, 1, 1)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CRYSTAL_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType BENDER_RECIPES = register("bender", ELECTRIC).setMaxIOSize(2, 1, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, false, GuiTextures.BENDER_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BENDING, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MOTOR);

    public final static GTRecipeType BREWING_RECIPES = register("brewery", ELECTRIC).setMaxIOSize(1, 0, 1, 1)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(128).EUt(4))
            .setSlotOverlay(false, false, GuiTextures.BREWER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    public final static GTRecipeType MACERATOR_RECIPES = register("macerator", ELECTRIC).setMaxIOSize(1, 4, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, LEFT_TO_RIGHT)
            .setIconSupplier(() -> GTMachines.MACERATOR[GTValues.LV].asStack())
            .setSteamProgressBar(GuiTextures.PROGRESS_BAR_MACERATE_STEAM, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MACERATOR);

    public final static GTRecipeType CANNER_RECIPES = register("canner", ELECTRIC).setMaxIOSize(2, 2, 1, 1)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, false, GuiTextures.CANNER_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, GuiTextures.DARK_CANISTER_OVERLAY)
            .setSlotOverlay(true, true, GuiTextures.DARK_CANISTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CANNER, LEFT_TO_RIGHT)
            .addCustomRecipeLogic(new CannerLogic())
            .setSound(GTSoundEntries.BATH);

    public final static GTRecipeType CENTRIFUGE_RECIPES = register("centrifuge", ELECTRIC).setMaxIOSize(2, 6, 1, 6)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(5))
            .setSlotOverlay(false, false, false, GuiTextures.EXTRACTOR_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CENTRIFUGE);

    public final static GTRecipeType CHEMICAL_BATH_RECIPES = register("chemical_bath", ELECTRIC)
            .setMaxIOSize(1, 6, 1, 1).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.LV]))
            .setSlotOverlay(false, false, true, GuiTextures.BREWER_OVERLAY)
            .setSlotOverlay(true, false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BATH);

    public final static GTRecipeType CHEMICAL_RECIPES = register("chemical_reactor", ELECTRIC).setMaxIOSize(2, 2, 3, 2)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.LV]))
            .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
            .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .setSound(GTValues.FOOLS.get() ? GTSoundEntries.SCIENCE : GTSoundEntries.CHEMICAL)
            .setMaxTooltips(4)
            .onRecipeBuild((recipeBuilder, provider) -> GTRecipeTypes.LARGE_CHEMICAL_RECIPES.copyFrom(recipeBuilder)
                    .save(provider));

    public final static GTRecipeType COMPRESSOR_RECIPES = register("compressor", ELECTRIC).setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(200).EUt(2))
            .setSlotOverlay(false, false, GuiTextures.COMPRESSOR_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, LEFT_TO_RIGHT)
            .setIconSupplier(() -> GTMachines.COMPRESSOR[GTValues.LV].asStack())
            .setSteamProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS_STEAM, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COMPRESSOR);

    public final static GTRecipeType CUTTER_RECIPES = register("cutter", ELECTRIC).setMaxIOSize(1, 2, 1, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.SAWBLADE_OVERLAY)
            .setSlotOverlay(true, false, false, GuiTextures.CUTTER_OVERLAY)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_SLICE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CUT)
            .setMaxTooltips(4)
            .onRecipeBuild((recipeBuilder, provider) -> {
                if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty() &&
                        recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList())
                                .isEmpty()) {
                    recipeBuilder
                            .copy(ResourceLocation.parse(recipeBuilder.id.toString() + "_water"))
                            .inputFluids(GTMaterials.Water.getFluid((int) Math.max(4,
                                    Math.min(1000, recipeBuilder.duration * recipeBuilder.EUt() / 320))))
                            .duration(recipeBuilder.duration * 2)
                            .save(provider);

                    recipeBuilder
                            .copy(ResourceLocation.parse(recipeBuilder.id.toString() + "_distilled_water"))
                            .inputFluids(GTMaterials.DistilledWater.getFluid((int) Math.max(3,
                                    Math.min(750, recipeBuilder.duration * recipeBuilder.EUt() / 426))))
                            .duration((int) (recipeBuilder.duration * 1.5))
                            .save(provider);

                    // Don't call buildAndRegister as we are mutating the original recipe and already in the middle of a
                    // buildAndRegister call.
                    // Adding a second call will result in duplicate recipe generation attempts
                    recipeBuilder
                            .inputFluids(GTMaterials.Lubricant.getFluid((int) Math.max(1,
                                    Math.min(250, recipeBuilder.duration * recipeBuilder.EUt() / 1280))))
                            .duration(Math.max(1, recipeBuilder.duration));
                }
            });

    public final static GTRecipeType DISTILLERY_RECIPES = register("distillery", ELECTRIC).setMaxIOSize(1, 1, 1, 1)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, true, GuiTextures.BEAKER_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.BEAKER_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BOILER);

    public final static GTRecipeType ELECTROLYZER_RECIPES = register("electrolyzer", ELECTRIC).setMaxIOSize(2, 6, 1, 6)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, false, GuiTextures.LIGHTNING_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.LIGHTNING_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ELECTROLYZER);

    public final static GTRecipeType ELECTROMAGNETIC_SEPARATOR_RECIPES = register("electromagnetic_separator", ELECTRIC)
            .setMaxIOSize(1, 3, 0, 0).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public final static GTRecipeType EXTRACTOR_RECIPES = register("extractor", ELECTRIC).setMaxIOSize(1, 1, 0, 1)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(2))
            .setSlotOverlay(false, false, GuiTextures.EXTRACTOR_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, LEFT_TO_RIGHT)
            .setIconSupplier(() -> GTMachines.EXTRACTOR[GTValues.LV].asStack())
            .setSteamProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT_STEAM, LEFT_TO_RIGHT);

    public final static GTRecipeType EXTRUDER_RECIPES = register("extruder", ELECTRIC).setMaxIOSize(2, 1, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRUDER, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COMPRESSOR);

    public final static GTRecipeType FERMENTING_RECIPES = register("fermenter", ELECTRIC).setMaxIOSize(1, 1, 1, 1)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(2))
            .setSlotOverlay(false, false, true, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    public final static GTRecipeType FLUID_HEATER_RECIPES = register("fluid_heater", ELECTRIC).setMaxIOSize(1, 0, 1, 1)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, true, GuiTextures.HEATING_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.HEATING_OVERLAY_2)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BOILER);

    public final static GTRecipeType FLUID_SOLIDFICATION_RECIPES = register("fluid_solidifier", ELECTRIC)
            .setMaxIOSize(1, 1, 1, 0).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COOLING);

    public final static GTRecipeType FORGE_HAMMER_RECIPES = register("forge_hammer", ELECTRIC).setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.HAMMER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_HAMMER, UP_TO_DOWN)
            .setIconSupplier(() -> GTMachines.FORGE_HAMMER[GTValues.LV].asStack())
            .setSteamProgressBar(GuiTextures.PROGRESS_BAR_HAMMER_STEAM, UP_TO_DOWN)
            .setSound(GTSoundEntries.FORGE_HAMMER);

    public final static GTRecipeType FORMING_PRESS_RECIPES = register("forming_press", ELECTRIC)
            .setMaxIOSize(6, 1, 0, 0).setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, LEFT_TO_RIGHT)
            .addCustomRecipeLogic(new FormingPressLogic())
            .setSound(GTSoundEntries.COMPRESSOR);

    public final static GTRecipeType LATHE_RECIPES = register("lathe", ELECTRIC).setMaxIOSize(1, 2, 0, 0).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.PIPE_OVERLAY_1)
            .setSlotOverlay(true, false, false, GuiTextures.PIPE_OVERLAY_2)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_LATHE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CUT);

    public final static GTRecipeType MIXER_RECIPES = register("mixer", ELECTRIC).setMaxIOSize(6, 1, 2, 1).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MIXER);

    public final static GTRecipeType ORE_WASHER_RECIPES = register("ore_washer", ELECTRIC).setMaxIOSize(2, 3, 1, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(16))
            .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BATH);

    public final static GTRecipeType PACKER_RECIPES = register("packer", ELECTRIC).setMaxIOSize(2, 2, 0, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(12).duration(10))
            .setSlotOverlay(false, false, true, GuiTextures.BOX_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.BOXED_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_UNPACKER, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    public final static GTRecipeType POLARIZER_RECIPES = register("polarizer", ELECTRIC).setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC);

    public final static GTRecipeType LASER_ENGRAVER_RECIPES = register("laser_engraver", ELECTRIC)
            .setMaxIOSize(2, 1, 0, 0).setEUIO(IO.IN)
            .setSlotOverlay(false, false, true, GuiTextures.LENS_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setMaxTooltips(4)
            .setSound(GTSoundEntries.ELECTROLYZER);

    public final static GTRecipeType SIFTER_RECIPES = register("sifter", ELECTRIC).setMaxIOSize(1, 6, 0, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, UP_TO_DOWN)
            .setSound(new ExistingSoundEntry(SoundEvents.SAND_PLACE, SoundSource.BLOCKS));

    public final static GTRecipeType THERMAL_CENTRIFUGE_RECIPES = register("thermal_centrifuge", ELECTRIC)
            .setMaxIOSize(1, 3, 0, 0).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(30))
            .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CENTRIFUGE);

    public final static GTRecipeType WIREMILL_RECIPES = register("wiremill", ELECTRIC).setMaxIOSize(2, 1, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.WIREMILL_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_WIREMILL, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MOTOR);

    public final static GTRecipeType CIRCUIT_ASSEMBLER_RECIPES = register("circuit_assembler", ELECTRIC)
            .setMaxIOSize(6, 1, 1, 0).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT_ASSEMBLER, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER)
            .setMaxTooltips(4)
            .onRecipeBuild((recipeBuilder, provider) -> {
                if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty() &&
                        recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList())
                                .isEmpty()) {
                    recipeBuilder.copy(ResourceLocation.parse(recipeBuilder.id.toString() + "_soldering_alloy"))
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
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setSlotOverlay(true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, LEFT_TO_RIGHT)
            .setMaxTooltips(4)
            .setOffsetVoltageText(true)
            .setSound(GTSoundEntries.COOLING);

    public final static GTRecipeType AIR_SCRUBBER_RECIPES = register("air_scrubber", ELECTRIC)
            .setMaxIOSize(1, 3, 1, 3).setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, LEFT_TO_RIGHT)
            .setMaxTooltips(4)
            .setSound(GTSoundEntries.COOLING);

    public static final GTRecipeType RESEARCH_STATION_RECIPES = register("research_station", ELECTRIC)
            .setMaxIOSize(2, 1, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSlotOverlay(false, false, GuiTextures.SCANNER_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.RESEARCH_STATION_OVERLAY)
            .setScanner(true)
            .setMaxTooltips(4)
            .setSound(GTValues.FOOLS.get() ? GTSoundEntries.SCIENCE : GTSoundEntries.COMPUTATION);

    public final static GTRecipeType ROCK_BREAKER_RECIPES = register("rock_breaker", ELECTRIC).setMaxIOSize(1, 4, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, LEFT_TO_RIGHT)
            .setIconSupplier(() -> GTMachines.ROCK_CRUSHER[GTValues.LV].asStack())
            .setSteamProgressBar(GuiTextures.PROGRESS_BAR_MACERATE_STEAM, LEFT_TO_RIGHT)
            .prepareBuilder(recipeBuilder -> recipeBuilder.addCondition(RockBreakerCondition.INSTANCE))
            .setUiBuilder((recipe, widgetGroup) -> {
                var fluidA = BuiltInRegistries.FLUID.get(ResourceLocation.parse(recipe.data.getString("fluidA")));
                var fluidB = BuiltInRegistries.FLUID.get(ResourceLocation.parse(recipe.data.getString("fluidB")));
                if (fluidA != Fluids.EMPTY) {
                    FluidTank tank = new FluidTank(1000);
                    tank.setFluid(new FluidStack(fluidA, 1000));
                    widgetGroup.addWidget(new TankWidget(tank, widgetGroup.getSize().width - 30,
                            widgetGroup.getSize().height - 30, false, false)
                            .setBackground(GuiTextures.FLUID_SLOT).setShowAmount(false));
                }
                if (fluidB != Fluids.EMPTY) {
                    FluidTank tank = new FluidTank(1000);
                    tank.setFluid(new FluidStack(fluidB, 1000));
                    widgetGroup.addWidget(new TankWidget(tank, widgetGroup.getSize().width - 30 - 20,
                            widgetGroup.getSize().height - 30, false, false)
                            .setBackground(GuiTextures.FLUID_SLOT).setShowAmount(false));
                }
            })
            .setMaxTooltips(4)
            .setSound(GTSoundEntries.FIRE);

    public static final GTRecipeType SCANNER_RECIPES = register("scanner", ELECTRIC)
            .setMaxIOSize(2, 1, 1, 0)
            .setSlotOverlay(false, false, GuiTextures.DATA_ORB_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.SCANNER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .addCustomRecipeLogic(new ResearchManager.DataStickCopyScannerLogic())
            .setScanner(true)
            .setSound(GTSoundEntries.ELECTROLYZER);

    //////////////////////////////////////
    // ******* Generator *******//
    //////////////////////////////////////
    public final static GTRecipeType COMBUSTION_GENERATOR_FUELS = register("combustion_generator", GENERATOR)
            .setMaxIOSize(0, 0, 1, 0).setEUIO(IO.OUT)
            .setSlotOverlay(false, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COMBUSTION);

    public final static GTRecipeType GAS_TURBINE_FUELS = register("gas_turbine", GENERATOR).setMaxIOSize(0, 0, 1, 0)
            .setEUIO(IO.OUT)
            .setSlotOverlay(false, true, true, GuiTextures.DARK_CANISTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.TURBINE);

    public final static GTRecipeType STEAM_TURBINE_FUELS = register("steam_turbine", GENERATOR).setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.OUT)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.TURBINE);

    public final static GTRecipeType PLASMA_GENERATOR_FUELS = register("plasma_generator", GENERATOR)
            .setMaxIOSize(0, 0, 1, 1).setEUIO(IO.OUT)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.TURBINE);

    //////////////////////////////////////
    // ******* Multiblock *******//
    //////////////////////////////////////
    public final static GTRecipeType LARGE_BOILER_RECIPES = register("large_boiler", MULTIBLOCK)
            .setMaxIOSize(1, 0, 1, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BOILER_FUEL.get(true), DOWN_TO_UP)
            .setMaxTooltips(1)
            .addCustomRecipeLogic(new LargeBoilerLogic())
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType COKE_OVEN_RECIPES = register("coke_oven", MULTIBLOCK).setMaxIOSize(1, 1, 0, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FIRE);

    public final static GTRecipeType PRIMITIVE_BLAST_FURNACE_RECIPES = register("primitive_blast_furnace", MULTIBLOCK)
            .setMaxIOSize(3, 3, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FIRE);

    public final static GTRecipeType BLAST_RECIPES = register("electric_blast_furnace", MULTIBLOCK)
            .setMaxIOSize(3, 3, 1, 1).setEUIO(IO.IN)
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                return LocalizationUtils.format("gtceu.recipe.temperature", temp);
            })
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);

                if (requiredCoil != null && requiredCoil.getMaterial() != null) {
                    return LocalizationUtils.format("gtceu.recipe.coil.tier",
                            I18n.get(requiredCoil.getMaterial().getUnlocalizedName()));
                }
                return "";
            })
            .setUiBuilder((recipe, widgetGroup) -> {
                int temp = recipe.data.getInt("ebf_temp");
                List<List<ItemStack>> items = new ArrayList<>();
                items.add(GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .filter(coil -> coil.getKey().getCoilTemperature() >= temp)
                        .map(coil -> new ItemStack(coil.getValue().get())).toList());
                widgetGroup.addWidget(new SlotWidget(new CycleItemStackHandler(items), 0,
                        widgetGroup.getSize().width - 25, widgetGroup.getSize().height - 32, false, false));
            })
            .setSound(GTSoundEntries.FURNACE);

    public final static GTRecipeType DISTILLATION_RECIPES = register("distillation_tower", MULTIBLOCK)
            .setMaxIOSize(0, 1, 1, 12).setEUIO(IO.IN)
            .setSound(GTSoundEntries.CHEMICAL)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .onRecipeBuild((recipeBuilder, provider) -> {
                if (recipeBuilder.data.getBoolean("disable_distillery")) return;
                if (recipeBuilder.output.containsKey(FluidRecipeCapability.CAP)) {
                    long EUt = EURecipeCapability.CAP
                            .of(recipeBuilder.tickInput.get(EURecipeCapability.CAP).get(0).getContent());
                    Content inputContent = recipeBuilder.input.get(FluidRecipeCapability.CAP).get(0);
                    SizedFluidIngredient input = FluidRecipeCapability.CAP.of(inputContent.getContent());
                    ItemStack[] outputs = recipeBuilder.output.containsKey(ItemRecipeCapability.CAP) ?
                            ItemRecipeCapability.CAP
                                    .of(recipeBuilder.output.get(ItemRecipeCapability.CAP).get(0).getContent())
                                    .getItems() :
                            null;
                    ItemStack outputItem = outputs == null || outputs.length == 0 ? ItemStack.EMPTY : outputs[0];
                    if (input.ingredient().hasNoFluids()) return;
                    List<Content> contents = recipeBuilder.output.get(FluidRecipeCapability.CAP);
                    for (int i = 0; i < contents.size(); ++i) {
                        Content outputContent = contents.get(i);
                        SizedFluidIngredient output = FluidRecipeCapability.CAP.of(outputContent.getContent());
                        if (output.ingredient().hasNoFluids()) continue;
                        GTRecipeBuilder builder = DISTILLERY_RECIPES
                                .recipeBuilder(recipeBuilder.id.getPath() + "_to_" +
                                        BuiltInRegistries.FLUID.getKey(output.getFluids()[0].getFluid()).getPath())
                                .EUt(Math.max(1, EUt / 4)).circuitMeta(i + 1);

                        int ratio = RecipeHelper.getRatioForDistillery(input, output, outputItem);
                        int recipeDuration = (int) (recipeBuilder.duration *
                                OverclockingLogic.STD_DURATION_FACTOR);
                        boolean shouldDivide = ratio != 1;

                        boolean fluidsDivisible = RecipeHelper.isFluidStackDivisibleForDistillery(input, ratio) &&
                                RecipeHelper.isFluidStackDivisibleForDistillery(output, ratio);

                        SizedFluidIngredient dividedInputFluid = new SizedFluidIngredient(input.ingredient(),
                                Math.max(1,
                                        input.amount() / ratio));
                        SizedFluidIngredient dividedOutputFluid = new SizedFluidIngredient(output.ingredient(),
                                Math.max(1,
                                        output.amount() / ratio));

                        if (shouldDivide && fluidsDivisible) {
                            builder.chance(inputContent.chance)
                                    .tierChanceBoost(inputContent.tierChanceBoost)
                                    .inputFluids(dividedInputFluid)
                                    .chance(outputContent.chance)
                                    .tierChanceBoost(outputContent.tierChanceBoost)
                                    .outputFluids(dividedOutputFluid)
                                    .duration(Math.max(1, recipeDuration / ratio));
                        } else if (!shouldDivide) {
                            if (!outputItem.isEmpty()) {
                                builder.outputItems(outputItem);
                            }
                            builder.conditions.addAll(recipeBuilder.conditions);
                            builder.chance(inputContent.chance)
                                    .tierChanceBoost(inputContent.tierChanceBoost)
                                    .inputFluids(input)
                                    .chance(outputContent.chance)
                                    .tierChanceBoost(outputContent.tierChanceBoost)
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

    public static final GTRecipeType EVAPORATION_RECIPES = register("evaporation", ELECTRIC).setMaxIOSize(0, 1, 1, 6)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MOTOR);

    public final static GTRecipeType PYROLYSE_RECIPES = register("pyrolyse_oven", MULTIBLOCK).setMaxIOSize(2, 1, 1, 1)
            .setEUIO(IO.IN)
            .setSound(GTSoundEntries.FIRE);

    public final static GTRecipeType CRACKING_RECIPES = register("cracker", MULTIBLOCK).setMaxIOSize(1, 0, 2, 2)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, true, GuiTextures.CRACKING_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.CRACKING_OVERLAY_2)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRACKING, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FIRE);

    public final static GTRecipeType IMPLOSION_RECIPES = register("implosion_compressor", MULTIBLOCK)
            .setMaxIOSize(3, 2, 0, 0).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(20).EUt(GTValues.VA[GTValues.LV]))
            .setSlotOverlay(false, false, true, GuiTextures.IMPLOSION_OVERLAY_1)
            .setSlotOverlay(false, false, false, GuiTextures.IMPLOSION_OVERLAY_2)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setSound(new ExistingSoundEntry(SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS));

    public final static GTRecipeType VACUUM_RECIPES = register("vacuum_freezer", MULTIBLOCK).setMaxIOSize(1, 1, 2, 1)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.MV]))
            .setSound(GTSoundEntries.COOLING);

    public final static GTRecipeType ASSEMBLY_LINE_RECIPES = register("assembly_line", MULTIBLOCK)
            .setMaxIOSize(16, 1, 4, 0).setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER)
            .setHasResearchSlot(true)
            .setMaxTooltips(4)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe);

    public static final GTRecipeType LARGE_CHEMICAL_RECIPES = register("large_chemical_reactor", MULTIBLOCK)
            .setMaxIOSize(3, 3, 5, 4).setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.LV]))
            .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
            .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
            .setSound(GTValues.FOOLS.get() ? GTSoundEntries.SCIENCE : GTSoundEntries.CHEMICAL)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT)
            .setSmallRecipeMap(CHEMICAL_RECIPES);

    public static final GTRecipeType FUSION_RECIPES = register("fusion_reactor", MULTIBLOCK).setMaxIOSize(0, 0, 2, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)
            .setOffsetVoltageText(true)
            .addDataInfo(data -> LocalizationUtils.format("gtceu.recipe.eu_to_start",
                    NumberFormat.getCompactNumberInstance().format(data.getLong("eu_to_start"))));

    public static final GTRecipeType DUMMY_RECIPES = register("dummy", DUMMY)
            .setXEIVisible(false);

    //////////////////////////////////////
    // ****** Integration *******//
    //////////////////////////////////////
    @Nullable
    public static GTRecipeType CREATE_MIXER_RECIPES;

    public static GTRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        var recipeType = new GTRecipeType(GTCEu.id(name), group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        recipeType.setSerializer(GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName,
                new GTRecipeSerializer()));
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }

    public static void init() {
        GCyMRecipeTypes.init();
        if (GTCEu.isCreateLoaded()) {
            CREATE_MIXER_RECIPES = register("create_mixer", KINETIC).setMaxIOSize(6, 1, 2, 1).setEUIO(IO.IN)
                    .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, LEFT_TO_RIGHT)
                    .setSound(GTSoundEntries.MIXER)
                    .setMaxTooltips(4)
                    .setUiBuilder((recipe, group) -> {
                        if (!recipe.conditions.isEmpty() && recipe.conditions.get(0) instanceof RPMCondition) {
                            var transfer = new CustomItemStackHandler(AllBlocks.SHAFT.asStack());
                            group.addWidget(new SlotWidget(transfer, 0, group.getSize().width - 30,
                                    group.getSize().height - 30, false, false));
                        }
                    });
            MIXER_RECIPES.onRecipeBuild((builder, provider) -> {
                assert CREATE_MIXER_RECIPES != null;
                CREATE_MIXER_RECIPES.copyFrom(builder)
                        .duration(Math.max((builder.duration / 2), 1))
                        .rpm(64)
                        .save(provider);
            });
        }
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.RECIPE_TYPES.getRegistryName());
        }
        ModLoader.postEvent(new GTCEuAPI.RegisterEvent(GTRegistries.RECIPE_TYPES));
        GTRegistries.RECIPE_TYPES.freeze();

        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("facade_cover"),
                FacadeCoverRecipe.SERIALIZER);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("strict_shaped_recipe"),
                StrictShapedRecipe.SERIALIZER);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("shaped_energy_transfer_recipe"),
                ShapedEnergyTransferRecipe.SERIALIZER);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id("tool_head_replace_recipe"),
                ToolHeadReplaceRecipe.SERIALIZER);
    }

    public static GTRecipeType get(String name) {
        return GTRegistries.RECIPE_TYPES.get(GTCEu.appendId(name));
    }
}
