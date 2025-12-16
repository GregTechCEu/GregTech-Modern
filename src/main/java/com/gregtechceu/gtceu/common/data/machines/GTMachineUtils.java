package com.gregtechceu.gtceu.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.PropertyFluidFilter;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.item.QuantumTankMachineItem;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.MultiblockTankMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeCombustionEngineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.TankValvePartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.common.machine.storage.DrumMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;
import static com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine.TANK_CAPACITY;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.utils.FormattingUtil.*;

public class GTMachineUtils {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? MAX : UHV);
    public static final int[] ELECTRIC_TIERS = GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? OpV : UV);
    public static final int[] LOW_TIERS = GTValues.tiersBetween(LV, EV);
    public static final int[] HIGH_TIERS = GTValues.tiersBetween(IV, GTCEuAPI.isHighTier() ? OpV : UHV);
    public static final int[] MULTI_HATCH_TIERS = GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV);
    public static final int[] DUAL_HATCH_TIERS = GTValues.tiersBetween(LuV, GTCEuAPI.isHighTier() ? MAX : UHV);

    public static final Int2IntFunction defaultTankSizeFunction = tier -> (tier <= GTValues.LV ? 8 :
            tier == GTValues.MV ? 12 : tier == GTValues.HV ? 16 : tier == GTValues.EV ? 32 : 64) *
            FluidType.BUCKET_VOLUME;
    public static final Int2IntFunction hvCappedTankSizeFunction = tier -> (tier <= GTValues.LV ? 8 :
            tier == GTValues.MV ? 12 : 16) * FluidType.BUCKET_VOLUME;
    public static final Int2IntFunction largeTankSizeFunction = tier -> (tier <= GTValues.LV ? 32 :
            tier == GTValues.MV ? 48 : 64) * FluidType.BUCKET_VOLUME;
    public static final Int2IntFunction steamGeneratorTankSizeFunction = tier -> Math.min(16 * (1 << (tier - 1)), 64) *
            FluidType.BUCKET_VOLUME;
    public static final Int2IntFunction genericGeneratorTankSizeFunction = tier -> Math.min(4 * (1 << (tier - 1)),
            16) * FluidType.BUCKET_VOLUME;

    public static Object2IntMap<MachineDefinition> DRUM_CAPACITY = new Object2IntArrayMap<>();

    public static final PartAbility[] DUAL_INPUT_HATCH_ABILITIES = new PartAbility[] {
            PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
    };

    public static final PartAbility[] DUAL_OUTPUT_HATCH_ABILITIES = new PartAbility[] {
            PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS,
    };

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction,
                                                             boolean hasPollutionDebuff) {
        return registerSimpleMachines(REGISTRATE, name, recipeType, tankScalingFunction, hasPollutionDebuff);
    }

    public static MachineDefinition[] registerSimpleMachines(GTRegistrate registrate, String name,
                                                             GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction,
                                                             boolean hasPollutionDebuff) {
        return registerSimpleMachines(registrate, name, recipeType, tankScalingFunction, hasPollutionDebuff,
                ELECTRIC_TIERS);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction) {
        return registerSimpleMachines(REGISTRATE, name, recipeType, tankScalingFunction);
    }

    public static MachineDefinition[] registerSimpleMachines(GTRegistrate registrate, String name,
                                                             GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction) {
        return registerSimpleMachines(registrate, name, recipeType, tankScalingFunction, false);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType) {
        return registerSimpleMachines(REGISTRATE, name, recipeType);
    }

    public static MachineDefinition[] registerSimpleMachines(GTRegistrate registrate, String name,
                                                             GTRecipeType recipeType) {
        return registerSimpleMachines(registrate, name, recipeType, defaultTankSizeFunction);
    }

    public static MachineDefinition[] registerSimpleMachines(GTRegistrate registrate,
                                                             String name,
                                                             GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction,
                                                             boolean hasPollutionDebuff,
                                                             int... tiers) {
        return registerTieredMachines(registrate, name,
                (holder, tier) -> new SimpleTieredMachine(holder, tier, tankScalingFunction), (tier, builder) -> {
                    if (hasPollutionDebuff) {
                        builder.recipeModifiers(GTRecipeModifiers.ENVIRONMENT_REQUIREMENT
                                .apply(GTMedicalConditions.CARBON_MONOXIDE_POISONING, 100 * tier),
                                GTRecipeModifiers.OC_NON_PERFECT)
                                .conditionalTooltip(defaultEnvironmentRequirement(),
                                        ConfigHolder.INSTANCE.gameplay.environmentalHazards);
                    } else {
                        builder.recipeModifier(GTRecipeModifiers.OC_NON_PERFECT);
                    }
                    return builder
                            .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                            .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                            .rotationState(RotationState.NON_Y_AXIS)
                            .recipeType(recipeType)
                            .workableTieredHullModel(GTCEu.id("block/machines/" + name))
                            .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                    tankScalingFunction.applyAsInt(tier), true))
                            .register();
                },
                tiers);
    }

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        return registerTieredMachines(REGISTRATE, name, factory, builder, tiers);
    }

    public static MachineDefinition[] registerTieredMachines(GTRegistrate registrate,
                                                             String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = registrate
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSteamMachines(String name,
                                                                                   BiFunction<IMachineBlockEntity, Boolean, MetaMachine> factory,
                                                                                   BiFunction<Boolean, MachineBuilder<MachineDefinition>, MachineDefinition> builder) {
        return registerSteamMachines(REGISTRATE, name, factory, builder);
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSteamMachines(GTRegistrate registrate, String name,
                                                                                   BiFunction<IMachineBlockEntity, Boolean, MetaMachine> factory,
                                                                                   BiFunction<Boolean, MachineBuilder<MachineDefinition>, MachineDefinition> builder) {
        MachineDefinition lowTier = builder.apply(false,
                registrate.machine("lp_%s".formatted(name), holder -> factory.apply(holder, false))
                        .langValue("Low Pressure " + FormattingUtil.toEnglishName(name))
                        .tier(0));
        MachineDefinition highTier = builder.apply(true,
                registrate.machine("hp_%s".formatted(name), holder -> factory.apply(holder, true))
                        .langValue("High Pressure " + FormattingUtil.toEnglishName(name))
                        .tier(1));
        return Pair.of(lowTier, highTier);
    }

    public static MachineDefinition[] registerFluidHatches(String name, String displayName, String tooltip,
                                                           IO io, int initialCapacity, int slots,
                                                           int[] tiers, PartAbility... abilities) {
        return registerFluidHatches(REGISTRATE, name, displayName, tooltip, io, initialCapacity, slots, tiers,
                abilities);
    }

    public static MachineDefinition[] registerFluidHatches(GTRegistrate registrate, String name, String displayName,
                                                           String tooltip,
                                                           IO io, int initialCapacity, int slots,
                                                           int[] tiers, PartAbility... abilities) {
        final String pipeOverlay;
        if (slots >= 9) {
            pipeOverlay = "overlay_pipe_9x";
        } else if (slots >= 4) {
            pipeOverlay = "overlay_pipe_4x";
        } else {
            pipeOverlay = null;
        }
        final String ioOverlay = io == OUT ? OVERLAY_FLUID_HATCH_OUTPUT : OVERLAY_FLUID_HATCH_INPUT;
        final String emissiveOverlay = io == OUT ? "overlay_pipe_out_emissive" : "overlay_pipe_in_emissive";
        return registerTieredMachines(registrate, name,
                (holder, tier) -> new FluidHatchPartMachine(holder, tier, io, initialCapacity, slots),
                (tier, builder) -> {
                    builder.langValue(VNF[tier] + ' ' + displayName)
                            .rotationState(RotationState.ALL)
                            .colorOverlayTieredHullModel(ioOverlay, pipeOverlay, emissiveOverlay)
                            .abilities(abilities)
                            .modelProperty(IS_FORMED, false)
                            .tooltips(Component.translatable("gtceu.machine." + tooltip + ".tooltip"))
                            .allowCoverOnFront(true);

                    if (slots == 1) {
                        builder.tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                FormattingUtil
                                        .formatNumbers(FluidHatchPartMachine.getTankCapacity(initialCapacity, tier))));
                    } else {
                        builder.tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                slots, FormattingUtil
                                        .formatNumbers(FluidHatchPartMachine.getTankCapacity(initialCapacity, tier))));
                    }
                    return builder.register();
                },
                tiers);
    }

    public static MachineDefinition[] registerTransformerMachines(String langName, int baseAmp) {
        return registerTransformerMachines(REGISTRATE, langName, baseAmp);
    }

    public static MachineDefinition[] registerTransformerMachines(GTRegistrate registrate, String langName,
                                                                  int baseAmp) {
        return registerTieredMachines(registrate, "transformer_%da".formatted(baseAmp),
                (holder, tier) -> new TransformerMachine(holder, tier, baseAmp),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .itemColor((itemStack, index) -> switch (index) {
                            case 1 -> ConfigHolder.INSTANCE.client.getDefaultPaintingColor();
                            case 2 -> VC[tier + 1];
                            case 3 -> VC[tier];
                            default -> -1;
                        })
                        .modelProperty(GTMachineModelProperties.IS_TRANSFORM_UP, false)
                        .model(createTransformerModel(baseAmp))
                        .langValue("%s %sTransformer".formatted(VCF[tier] + VOLTAGE_NAMES[tier] + ChatFormatting.RESET,
                                langName.isEmpty() ? "" : langName + " "))
                        .tooltips(Component.translatable("gtceu.machine.transformer.description"),
                                Component.translatable("gtceu.machine.transformer.tooltip_tool_usage"),
                                Component.translatable("gtceu.machine.transformer.tooltip_transform_down",
                                        baseAmp, FormattingUtil.formatNumbers(GTValues.V[tier + 1]),
                                        GTValues.VNF[tier + 1],
                                        baseAmp * 4, FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]),
                                Component.translatable("gtceu.machine.transformer.tooltip_transform_up",
                                        baseAmp * 4, FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier],
                                        baseAmp, FormattingUtil.formatNumbers(GTValues.V[tier + 1]),
                                        GTValues.VNF[tier + 1]))
                        .register(),
                GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? OpV : UV));
        // UHV not needed, as a UV transformer transforms up to UHV
    }

    public static MachineDefinition[] registerSimpleGenerator(String name,
                                                              GTRecipeType recipeType,
                                                              Int2IntFunction tankScalingFunction,
                                                              float hazardStrengthPerOperation,
                                                              int... tiers) {
        return registerSimpleGenerator(REGISTRATE, name, recipeType, tankScalingFunction, hazardStrengthPerOperation,
                tiers);
    }

    public static MachineDefinition[] registerSimpleGenerator(GTRegistrate registrate, String name,
                                                              GTRecipeType recipeType,
                                                              Int2IntFunction tankScalingFunction,
                                                              float hazardStrengthPerOperation,
                                                              int... tiers) {
        return registerTieredMachines(registrate, name,
                (holder, tier) -> new SimpleGeneratorMachine(holder, tier, hazardStrengthPerOperation * tier,
                        tankScalingFunction),
                (tier, builder) -> builder
                        .langValue("%s %s Generator %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                        .editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .rotationState(RotationState.ALL)
                        .recipeType(recipeType)
                        .recipeModifier(SimpleGeneratorMachine::recipeModifier, true)
                        .addOutputLimit(ItemRecipeCapability.CAP, 0)
                        .addOutputLimit(FluidRecipeCapability.CAP, 0)
                        .simpleGeneratorModel(GTCEu.id("block/generators/" + name))
                        .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                tankScalingFunction.applyAsInt(tier), false))
                        .register(),
                tiers);
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSimpleSteamMachines(String name,
                                                                                         GTRecipeType recipeType) {
        return registerSimpleSteamMachines(REGISTRATE, name, recipeType);
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSimpleSteamMachines(GTRegistrate registrate,
                                                                                         String name,
                                                                                         GTRecipeType recipeType) {
        return registerSteamMachines(registrate, "steam_" + name, SimpleSteamMachine::new,
                (pressure, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .recipeType(recipeType)
                        .recipeModifier(SimpleSteamMachine::recipeModifier)
                        .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                        .workableSteamHullModel(pressure, GTCEu.id("block/machines/" + name))
                        .register());
    }

    public static MachineDefinition[] registerBatteryBuffer(int batterySlotSize) {
        return registerBatteryBuffer(REGISTRATE, batterySlotSize);
    }

    public static MachineDefinition[] registerBatteryBuffer(GTRegistrate registrate, int batterySlotSize) {
        return registerTieredMachines(registrate, "battery_buffer_" + batterySlotSize + "x",
                (holder, tier) -> new BatteryBufferMachine(holder, tier, batterySlotSize),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .model(GTMachineModels.createBatteryBufferModel(batterySlotSize))
                        .langValue("%s %sx Battery Buffer".formatted(
                                VCF[tier] + VOLTAGE_NAMES[tier] + ChatFormatting.RESET,
                                batterySlotSize))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                        batterySlotSize),
                                Component.translatable("gtceu.universal.tooltip.voltage_in_out",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in_till",
                                        batterySlotSize * BatteryBufferMachine.AMPS_PER_BATTERY),
                                Component.translatable("gtceu.universal.tooltip.amperage_out_till", batterySlotSize))
                        .register(),
                ALL_TIERS);
    }

    public static MachineDefinition[] registerCharger(int itemSlotSize) {
        return registerCharger(REGISTRATE, itemSlotSize);
    }

    public static MachineDefinition[] registerCharger(GTRegistrate registrate, int itemSlotSize) {
        return registerTieredMachines(registrate, "charger_" + itemSlotSize + "x",
                (holder, tier) -> new ChargerMachine(holder, tier, itemSlotSize),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .modelProperty(GTMachineModelProperties.CHARGER_STATE, ChargerMachine.State.IDLE)
                        .model(GTMachineModels.createChargerModel())
                        .langValue("%s %sx Turbo Charger".formatted(
                                VCF[tier] + VOLTAGE_NAMES[tier] + ChatFormatting.RESET,
                                itemSlotSize))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.item_storage_capacity", itemSlotSize),
                                Component.translatable("gtceu.universal.tooltip.voltage_in_out",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in_till",
                                        itemSlotSize * ChargerMachine.AMPS_PER_ITEM))
                        .register(),
                ALL_TIERS);
    }

    public static MachineDefinition[] registerConverter(int amperage) {
        return registerConverter(REGISTRATE, amperage);
    }

    public static MachineDefinition[] registerConverter(GTRegistrate registrate, int amperage) {
        final var tab = registrate.creativeModeTab();

        if (!ConfigHolder.INSTANCE.compat.energy.enableFEConverters) {
            registrate.creativeModeTab(() -> null);
        }

        MachineDefinition[] converters = registerTieredMachines(registrate, amperage + "a_energy_converter",
                (holder, tier) -> new ConverterMachine(holder, tier, amperage),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .langValue("%s %s§eA§r Energy Converter".formatted(VCF[tier] + VN[tier] + ChatFormatting.RESET,
                                amperage))
                        .modelProperty(GTMachineModelProperties.IS_FE_TO_EU, false)
                        .model(GTMachineModels.createConverterModel(amperage))
                        .tooltips(Component.translatable("gtceu.machine.energy_converter.description"),
                                Component.translatable("gtceu.machine.energy_converter.tooltip_tool_usage"),
                                Component.translatable("gtceu.machine.energy_converter.tooltip_conversion_native",
                                        FeCompat.toFeLong(V[tier] * amperage,
                                                FeCompat.ratio(true)),
                                        amperage, V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.machine.energy_converter.tooltip_conversion_eu", amperage,
                                        V[tier], GTValues.VNF[tier],
                                        FeCompat.toFeLong(V[tier] * amperage,
                                                FeCompat.ratio(false))))
                        .register(),
                ALL_TIERS);

        if (!ConfigHolder.INSTANCE.compat.energy.enableFEConverters) {
            registrate.creativeModeTab(() -> tab);
        }
        return converters;
    }

    public static MachineDefinition[] registerLaserHatch(IO io, int amperage, PartAbility ability) {
        return registerLaserHatch(REGISTRATE, io, amperage, ability);
    }

    public static MachineDefinition[] registerLaserHatch(GTRegistrate registrate, IO io, int amperage,
                                                         PartAbility ability) {
        String name = io == IN ? "target" : "source";
        return registerTieredMachines(registrate, amperage + "a_laser_" + name + "_hatch",
                (holder, tier) -> new LaserHatchPartMachine(holder, io, tier, amperage), (tier, builder) -> builder
                        .langValue(VNF[tier] + "§r " + FormattingUtil.formatNumbers(amperage) + "§eA§r Laser " +
                                FormattingUtil.toEnglishName(name) + " Hatch")
                        .rotationState(RotationState.ALL)
                        .tooltips(Component.translatable("gtceu.machine.laser_hatch." + name + ".tooltip"),
                                Component.translatable("gtceu.machine.laser_hatch.both.tooltip"),
                                Component.translatable("gtceu.universal.tooltip.voltage_" + (io == IN ? "in" : "out"),
                                        FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in", amperage),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        FormattingUtil
                                                .formatNumbers(
                                                        EnergyHatchPartMachine.getHatchEnergyCapacity(tier, amperage))),
                                Component.translatable("gtceu.part_sharing.disabled"))
                        .abilities(ability)
                        .modelProperty(IS_FORMED, false)
                        .overlayTieredHullModel("laser_" + name + "_hatch")
                        .register(),
                HIGH_TIERS);
    }

    public static MachineDefinition registerCrate(Material material, int capacity, String lang) {
        return registerCrate(REGISTRATE, material, capacity, lang);
    }

    public static MachineDefinition registerCrate(GTRegistrate registrate, Material material, int capacity,
                                                  String lang) {
        final boolean wooden = material.hasProperty(PropertyKey.WOOD);

        return registrate.machine(material.getName() + "_crate", holder -> new CrateMachine(holder, material, capacity))
                .langValue(lang)
                .rotationState(RotationState.NONE)
                .tooltips(Component.translatable("gtceu.universal.tooltip.item_storage_capacity", capacity))
                .modelProperty(GTMachineModelProperties.IS_TAPED, false)
                .model(GTMachineModels.createCrateModel(wooden))
                .paintingColor(wooden ? 0xFFFFFF : material.getMaterialRGB())
                .itemColor((s, t) -> wooden ? 0xFFFFFF : material.getMaterialRGB())
                .register();
    }

    public static MachineDefinition registerDrum(Material material, int capacity, String lang) {
        return registerDrum(REGISTRATE, material, capacity, lang);
    }

    public static MachineDefinition registerDrum(GTRegistrate registrate, Material material, int capacity,
                                                 String lang) {
        boolean wooden = material.hasProperty(PropertyKey.WOOD);
        var definition = registrate
                .machine(material.getName() + "_drum", MachineDefinition::new,
                        holder -> new DrumMachine(holder, material, capacity), MetaMachineBlock::new,
                        (holder, prop) -> DrumMachineItem.create(holder, prop, material),
                        MetaMachineBlockEntity::new)
                .langValue(lang)
                .rotationState(RotationState.NONE)
                .simpleModel(GTCEu.id("block/machine/template/drum/" + (wooden ? "wooden" : "metal") + "_drum"))
                .tooltipBuilder((stack, list) -> {
                    TANK_TOOLTIPS.accept(stack, list);
                    if (material.hasProperty(PropertyKey.FLUID_PIPE)) {
                        FluidPipeProperties pipeprops = material.getProperty(PropertyKey.FLUID_PIPE);
                        pipeprops.appendTooltips(list, false, true);
                    }
                })
                .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"),
                        Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                FormattingUtil.formatNumbers(capacity)))
                .paintingColor(wooden ? 0xFFFFFF : material.getMaterialRGB())
                .itemColor((s, i) -> wooden ? 0xFFFFFF : material.getMaterialRGB())
                .register();
        DRUM_CAPACITY.put(definition, capacity);
        return definition;
    }

    public static MachineDefinition[] registerQuantumTanks(String name, int... tiers) {
        return registerQuantumTanks(REGISTRATE, name, tiers);
    }

    public static MachineDefinition[] registerQuantumTanks(GTRegistrate registrate, String name, int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            long maxAmount = 4000 * FluidType.BUCKET_VOLUME * (long) Math.pow(2, tier - 1);
            var register = registrate.machine(
                    GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                    MachineDefinition::new, (holder) -> new QuantumTankMachine(holder, tier, maxAmount),
                    MetaMachineBlock::new, QuantumTankMachineItem::new,
                    MetaMachineBlockEntity::new)
                    .langValue(toEnglishName(name) + " " + LVT[tier])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .allowExtendedFacing(true)
                    .model(createTieredHullMachineModel(GTCEu.id("block/machine/template/quantum/quantum_tank"))
                            .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::createQuantumTankRender)))
                    .hasBER(true)
                    .tooltipBuilder(TANK_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                    FormattingUtil.formatNumbers(maxAmount)))
                    .tier(tier)
                    .register();
            TANK_CAPACITY.put(register, maxAmount);
            definitions[tier] = register;
        }
        return definitions;
    }

    public static MachineDefinition[] registerQuantumChests(String name, int... tiers) {
        return registerQuantumChests(REGISTRATE, name, tiers);
    }

    public static MachineDefinition[] registerQuantumChests(GTRegistrate registrate, String name, int... tiers) {
        return registerTieredMachines(registrate, name,
                (holder, tier) -> new QuantumChestMachine(holder, tier,
                        tier == MAX ? Long.MAX_VALUE : 4_000_000 * (long) Math.pow(2, tier - 1)),
                (tier, builder) -> builder.langValue(toEnglishName(name) + " " + LVT[tier])
                        .blockProp(BlockBehaviour.Properties::dynamicShape)
                        .rotationState(RotationState.ALL)
                        .allowExtendedFacing(true)
                        .model(createTieredHullMachineModel(GTCEu.id("block/machine/template/quantum/quantum_chest"))
                                .andThen(
                                        b -> b.addDynamicRenderer(DynamicRenderHelper::createQuantumChestRender)))
                        .hasBER(true)
                        .tooltipBuilder(CHEST_TOOLTIPS)
                        .tooltips(Component.translatable("gtceu.machine.quantum_chest.tooltip"),
                                Component.translatable("gtceu.universal.tooltip.item_storage_total",
                                        FormattingUtil.formatNumbers(tier == MAX ? Long.MAX_VALUE :
                                                4_000_000 * (long) Math.pow(2, tier - 1))))
                        .register(),
                tiers);
    }

    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////
    // multi register helpers

    public static MultiblockMachineDefinition registerMultiblockTank(String name, String displayName, int capacity,
                                                                     Supplier<? extends Block> casing,
                                                                     Supplier<? extends Block> valve,
                                                                     @Nullable PropertyFluidFilter filter,
                                                                     BiConsumer<MultiblockMachineBuilder, ResourceLocation> rendererSetup) {
        return registerMultiblockTank(REGISTRATE, name, displayName, capacity, casing, valve, filter, rendererSetup);
    }

    public static MultiblockMachineDefinition registerMultiblockTank(GTRegistrate registrate, String name,
                                                                     String displayName, int capacity,
                                                                     Supplier<? extends Block> casing,
                                                                     Supplier<? extends Block> valve,
                                                                     @Nullable PropertyFluidFilter filter,
                                                                     BiConsumer<MultiblockMachineBuilder, ResourceLocation> rendererSetup) {
        MultiblockMachineBuilder builder = registrate
                .multiblock(name, holder -> new MultiblockTankMachine(holder, capacity, filter))
                .langValue(displayName)
                .tooltips(
                        Component.translatable("gtceu.machine.multiblock.tank.tooltip"),
                        Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", capacity),
                        (filter != null) ? Component.translatable("gtceu.fluid_pipe.max_temperature",
                                filter.getMaxFluidTemperature()) : null)
                .rotationState(RotationState.ALL)
                .recipeType(DUMMY_RECIPES)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("CCC", "CCC", "CCC")
                        .aisle("CCC", "C#C", "CCC")
                        .aisle("CCC", "CSC", "CCC")
                        .where('S', controller(blocks(definition.get())))
                        .where('C', blocks(casing.get())
                                .or(blocks(valve.get()).setMaxGlobalLimited(2, 0)))
                        .where('#', air())
                        .build())
                .appearanceBlock(casing);
        rendererSetup.accept(builder, GTCEu.id("block/multiblock/multiblock_tank"));
        return builder.register();
    }

    public static MachineDefinition registerTankValve(String name, String displayName, boolean isMetal,
                                                      BiConsumer<MachineBuilder<?>, ResourceLocation> rendererSetup) {
        return registerTankValve(REGISTRATE, name, displayName, isMetal, rendererSetup);
    }

    public static MachineDefinition registerTankValve(GTRegistrate registrate, String name, String displayName,
                                                      boolean isMetal,
                                                      BiConsumer<MachineBuilder<?>, ResourceLocation> rendererSetup) {
        MachineBuilder<MachineDefinition> builder = registrate
                .machine(name, holder -> new TankValvePartMachine(holder, isMetal))
                .langValue(displayName)
                .tooltips(Component.translatable("gtceu.machine.tank_valve.tooltip"),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .rotationState(RotationState.ALL);
        rendererSetup.accept(builder, GTCEu.id("block/multiblock/tank_valve"));
        return builder.register();
    }

    public static MultiblockMachineDefinition[] registerTieredMultis(String name,
                                                                     BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
                                                                     BiFunction<Integer, MultiblockMachineBuilder, MultiblockMachineDefinition> builder,
                                                                     int... tiers) {
        return registerTieredMultis(REGISTRATE, name, factory, builder, tiers);
    }

    public static MultiblockMachineDefinition[] registerTieredMultis(GTRegistrate registrate, String name,
                                                                     BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
                                                                     BiFunction<Integer, MultiblockMachineBuilder, MultiblockMachineDefinition> builder,
                                                                     int... tiers) {
        MultiblockMachineDefinition[] definitions = new MultiblockMachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = registrate
                    .multiblock(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static MultiblockMachineDefinition registerLargeBoiler(String name, Supplier<? extends Block> casing,
                                                                  Supplier<? extends Block> pipe,
                                                                  Supplier<? extends Block> fireBox,
                                                                  ResourceLocation texture, BoilerFireboxType firebox,
                                                                  int maxTemperature, int heatSpeed) {
        return registerLargeBoiler(REGISTRATE, name, casing, pipe, fireBox, texture, firebox, maxTemperature,
                heatSpeed);
    }

    public static MultiblockMachineDefinition registerLargeBoiler(GTRegistrate registrate, String name,
                                                                  Supplier<? extends Block> casing,
                                                                  Supplier<? extends Block> pipe,
                                                                  Supplier<? extends Block> fireBox,
                                                                  ResourceLocation texture, BoilerFireboxType firebox,
                                                                  int maxTemperature, int heatSpeed) {
        // spotless:off
        return registrate
                .multiblock("%s_large_boiler".formatted(name),
                        holder -> new LargeBoilerMachine(holder, maxTemperature, heatSpeed))
                .langValue("Large %s Boiler".formatted(FormattingUtil.toEnglishName(name)))
                .allowExtendedFacing(false)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.LARGE_BOILER_RECIPES)
                .recipeModifier(LargeBoilerMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .partAppearance((controller, part, side) ->
                        controller.self().getPos().below().getY() == part.self().getPos().getY() ?
                                         fireBox.get().defaultBlockState() : casing.get().defaultBlockState())
                .pattern((definition) -> {
                    TraceabilityPredicate fireboxPred = blocks(ALL_FIREBOXES.get(firebox).get()).setMinGlobalLimited(3)
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1));

                    if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
                        fireboxPred = fireboxPred.or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1));
                    }

                    return FactoryBlockPattern.start()
                            .aisle("XXX", "CCC", "CCC", "CCC")
                            .aisle("XXX", "CPC", "CPC", "CCC")
                            .aisle("XXX", "CSC", "CCC", "CCC")
                            .where('S', Predicates.controller(blocks(definition.getBlock())))
                            .where('P', blocks(pipe.get()))
                            .where('X', fireboxPred)
                            .where('C', blocks(casing.get()).setMinGlobalLimited(20)
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)
                                            .setPreviewCount(1)))
                            .build();
                })
                .recoveryItems(
                        () -> new ItemLike[] {
                                GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(createWorkableCasingMachineModel(texture,
                        GTCEu.id("block/multiblock/generator/large_%s_boiler".formatted(name)))
                        .andThen(b -> b.addDynamicRenderer(() -> DynamicRenderHelper.makeBoilerPartRender(firebox, casing))))
                .tooltips(
                        Component.translatable("gtceu.multiblock.large_boiler.max_temperature", maxTemperature + 274,
                                maxTemperature),
                        Component.translatable("gtceu.multiblock.large_boiler.heat_time_tooltip",
                                maxTemperature / heatSpeed / 20),
                        Component.translatable("gtceu.multiblock.large_boiler.explosion_tooltip")
                                .withStyle(ChatFormatting.DARK_RED))
                .register();
        // spotless:on
    }

    public static MultiblockMachineDefinition registerLargeCombustionEngine(String name, int tier,
                                                                            Supplier<? extends Block> casing,
                                                                            Supplier<? extends Block> gear,
                                                                            Supplier<? extends Block> intake,
                                                                            ResourceLocation casingTexture,
                                                                            ResourceLocation overlayModel) {
        return registerLargeCombustionEngine(REGISTRATE, name, tier, casing, gear, intake, casingTexture, overlayModel);
    }

    public static MultiblockMachineDefinition registerLargeCombustionEngine(GTRegistrate registrate,
                                                                            String name, int tier,
                                                                            Supplier<? extends Block> casing,
                                                                            Supplier<? extends Block> gear,
                                                                            Supplier<? extends Block> intake,
                                                                            ResourceLocation casingTexture,
                                                                            ResourceLocation overlayModel) {
        return registrate.multiblock(name, holder -> new LargeCombustionEngineMachine(holder, tier))
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
                .generator(true)
                .recipeModifier(LargeCombustionEngineMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("XXX", "XDX", "XXX")
                        .aisle("XCX", "CGC", "XCX")
                        .aisle("XCX", "CGC", "XCX")
                        .aisle("AAA", "AYA", "AAA")
                        .where('X', blocks(casing.get()))
                        .where('G', blocks(gear.get()))
                        .where('C', blocks(casing.get()).setMinGlobalLimited(3)
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(autoAbilities(true, true, false)))
                        .where('D',
                                ability(PartAbility.OUTPUT_ENERGY,
                                        IntStream.of(ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV)
                                                .filter(t -> t >= tier)
                                                .toArray())
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                                                GTValues.VN[tier])))
                        .where('A',
                                blocks(intake.get())
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_1")))
                        .where('Y', controller(blocks(definition.getBlock())))
                        .build())
                .recoveryItems(
                        () -> new ItemLike[] {
                                GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .workableCasingModel(casingTexture, overlayModel)
                .tooltips(
                        Component.translatable("gtceu.universal.tooltip.base_production_eut", V[tier]),
                        Component.translatable("gtceu.universal.tooltip.uses_per_hour_lubricant",
                                FluidType.BUCKET_VOLUME),
                        tier > EV ?
                                Component.translatable("gtceu.machine.large_combustion_engine.tooltip.boost_extreme",
                                        V[tier] * 4) :
                                Component.translatable("gtceu.machine.large_combustion_engine.tooltip.boost_regular",
                                        V[tier] * 3))
                .register();
    }

    public static MultiblockMachineDefinition registerLargeTurbine(String name, int tier, GTRecipeType recipeType,
                                                                   Supplier<? extends Block> casing,
                                                                   Supplier<? extends Block> gear,
                                                                   ResourceLocation casingTexture,
                                                                   ResourceLocation overlayModel) {
        return registerLargeTurbine(REGISTRATE, name, tier, recipeType, casing, gear, casingTexture, overlayModel);
    }

    public static MultiblockMachineDefinition registerLargeTurbine(GTRegistrate registrate,
                                                                   String name, int tier, GTRecipeType recipeType,
                                                                   Supplier<? extends Block> casing,
                                                                   Supplier<? extends Block> gear,
                                                                   ResourceLocation casingTexture,
                                                                   ResourceLocation overlayModel) {
        return registerLargeTurbine(registrate, name, tier, recipeType, casing, gear, casingTexture, overlayModel,
                true);
    }

    public static MultiblockMachineDefinition registerLargeTurbine(String name, int tier, GTRecipeType recipeType,
                                                                   Supplier<? extends Block> casing,
                                                                   Supplier<? extends Block> gear,
                                                                   ResourceLocation casingTexture,
                                                                   ResourceLocation overlayModel,
                                                                   boolean needsMuffler) {
        return registerLargeTurbine(REGISTRATE, name, tier, recipeType, casing, gear, casingTexture, overlayModel,
                needsMuffler);
    }

    public static MultiblockMachineDefinition registerLargeTurbine(GTRegistrate registrate,
                                                                   String name, int tier, GTRecipeType recipeType,
                                                                   Supplier<? extends Block> casing,
                                                                   Supplier<? extends Block> gear,
                                                                   ResourceLocation casingTexture,
                                                                   ResourceLocation overlayModel,
                                                                   boolean needsMuffler) {
        return registrate.multiblock(name, holder -> new LargeTurbineMachine(holder, tier))
                .rotationState(RotationState.ALL)
                .recipeType(recipeType)
                .generator(true)
                .recipeModifier(LargeTurbineMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("CCCC", "CHHC", "CCCC")
                        .aisle("CHHC", "RGGR", "CHHC")
                        .aisle("CCCC", "CSHC", "CCCC")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('G', blocks(gear.get()))
                        .where('C', blocks(casing.get()))
                        .where('R',
                                new TraceabilityPredicate(
                                        new SimplePredicate(
                                                state -> MetaMachine.getMachine(state.getWorld(),
                                                        state.getPos()) instanceof IRotorHolderMachine rotorHolder &&
                                                        state.getWorld()
                                                                .getBlockState(state.getPos()
                                                                        .relative(rotorHolder.self().getFrontFacing()))
                                                                .isAir(),
                                                () -> PartAbility.ROTOR_HOLDER.getAllBlocks().stream()
                                                        .map(BlockInfo::fromBlock).toArray(BlockInfo[]::new)))
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_3"))
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                                                VN[tier]))
                                        .setExactLimit(1)
                                        .or(abilities(PartAbility.OUTPUT_ENERGY)).setExactLimit(1))
                        .where('H', blocks(casing.get())
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(autoAbilities(true, needsMuffler, false)))
                        .build())
                .recoveryItems(
                        () -> new ItemLike[] {
                                GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .workableCasingModel(casingTexture, overlayModel)
                .tooltips(
                        Component.translatable("gtceu.universal.tooltip.base_production_eut", V[tier] * 2),
                        Component.translatable("gtceu.multiblock.turbine.efficiency_tooltip", VNF[tier]))
                .register();
    }

    // Tooltips
    public static Component explosion() {
        if (ConfigHolder.INSTANCE.machines.shouldWeatherOrTerrainExplosion)
            return Component.translatable("gtceu.universal.tooltip.terrain_resist");
        return null;
    }

    public static Component environmentRequirement(MedicalCondition condition) {
        return Component.translatable("gtceu.recipe.environmental_hazard.reverse",
                Component.translatable("gtceu.medical_condition." + condition.name));
    }

    public static Component defaultEnvironmentRequirement() {
        return environmentRequirement(GTMedicalConditions.CARBON_MONOXIDE_POISONING);
    }

    public static BiConsumer<ItemStack, List<Component>> TANK_TOOLTIPS = (stack, list) -> {
        if (stack.hasTag()) {
            String key = stack.getTag().contains("stored") ? "stored" : "Fluid";
            FluidStack stored = FluidStack.loadFluidStackFromNBT(stack.getOrCreateTagElement(key));
            long storedAmount = stack.getOrCreateTag().getLong("storedAmount");
            if (storedAmount == 0 && !stored.isEmpty()) storedAmount = stored.getAmount();
            list.add(1, Component.translatable("gtceu.universal.tooltip.fluid_stored", stored.getDisplayName(),
                    FormattingUtil.formatNumbers(storedAmount)));
        }
    };

    public static BiConsumer<ItemStack, List<Component>> CHEST_TOOLTIPS = (stack, list) -> {
        if (stack.hasTag()) {
            ItemStack itemStack = ItemStack.of(stack.getOrCreateTagElement("stored"));
            long storedAmount = stack.getOrCreateTag().getLong("storedAmount");
            list.add(1, Component.translatable("gtceu.universal.tooltip.item_stored", itemStack.getHoverName(),
                    FormattingUtil.formatNumbers(storedAmount)));
        }
    };

    public static Component[] workableTiered(int tier, long voltage, long energyCapacity, GTRecipeType recipeType,
                                             long tankCapacity, boolean input) {
        List<Component> tooltipComponents = new ArrayList<>();
        tooltipComponents
                .add(input ?
                        Component.translatable("gtceu.universal.tooltip.voltage_in",
                                FormattingUtil.formatNumbers(voltage), GTValues.VNF[tier]) :
                        Component.translatable("gtceu.universal.tooltip.voltage_out",
                                FormattingUtil.formatNumbers(voltage), GTValues.VNF[tier]));
        tooltipComponents
                .add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                        FormattingUtil.formatNumbers(energyCapacity)));
        if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0 ||
                recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0)
            tooltipComponents
                    .add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                            FormattingUtil.formatNumbers(tankCapacity)));
        return tooltipComponents.toArray(Component[]::new);
    }

    public static void init() {}
}
