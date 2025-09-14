package com.gregtechceu.gtceu.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.PropertyFluidFilter;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.*;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.BedrockOreMinerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CharcoalPileIgniterMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitivePumpMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;

import appeng.api.networking.pathing.ChannelMode;
import appeng.core.AEConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.DrillingFluid;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toRomanNumeral;

public class GTMultiMachines {

    //////////////////////////////////////
    // ******* Multiblock *******//
    //////////////////////////////////////
    public static final MultiblockMachineDefinition LARGE_BOILER_BRONZE = registerLargeBoiler("bronze",
            CASING_BRONZE_BRICKS, CASING_BRONZE_PIPE, FIREBOX_BRONZE,
            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"), BoilerFireboxType.BRONZE_FIREBOX,
            ConfigHolder.INSTANCE.machines.largeBoilers.bronzeBoilerMaxTemperature,
            ConfigHolder.INSTANCE.machines.largeBoilers.bronzeBoilerHeatSpeed);
    public static final MultiblockMachineDefinition LARGE_BOILER_STEEL = registerLargeBoiler("steel",
            CASING_STEEL_SOLID, CASING_STEEL_PIPE, FIREBOX_STEEL,
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"), BoilerFireboxType.STEEL_FIREBOX,
            ConfigHolder.INSTANCE.machines.largeBoilers.steelBoilerMaxTemperature,
            ConfigHolder.INSTANCE.machines.largeBoilers.steelBoilerHeatSpeed);
    public static final MultiblockMachineDefinition LARGE_BOILER_TITANIUM = registerLargeBoiler("titanium",
            CASING_TITANIUM_STABLE, CASING_TITANIUM_PIPE, FIREBOX_TITANIUM,
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"), BoilerFireboxType.TITANIUM_FIREBOX,
            ConfigHolder.INSTANCE.machines.largeBoilers.titaniumBoilerMaxTemperature,
            ConfigHolder.INSTANCE.machines.largeBoilers.titaniumBoilerHeatSpeed);
    public static final MultiblockMachineDefinition LARGE_BOILER_TUNGSTENSTEEL = registerLargeBoiler("tungstensteel",
            CASING_TUNGSTENSTEEL_ROBUST, CASING_TUNGSTENSTEEL_PIPE, FIREBOX_TUNGSTENSTEEL,
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
            BoilerFireboxType.TUNGSTENSTEEL_FIREBOX,
            ConfigHolder.INSTANCE.machines.largeBoilers.tungstensteelBoilerMaxTemperature,
            ConfigHolder.INSTANCE.machines.largeBoilers.tungstensteelBoilerHeatSpeed);

    public static final MultiblockMachineDefinition COKE_OVEN = REGISTRATE.multiblock("coke_oven", CokeOvenMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.COKE_OVEN_RECIPES)
            .appearanceBlock(CASING_COKE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XYX", "XXX")
                    .where('X',
                            blocks(CASING_COKE_BRICKS.get()).or(blocks(COKE_OVEN_HATCH.get()).setMaxGlobalLimited(5)))
                    .where('#', Predicates.air())
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_coke_bricks"),
                    GTCEu.id("block/multiblock/coke_oven"))
            .register();

    public static final MultiblockMachineDefinition PRIMITIVE_BLAST_FURNACE = REGISTRATE
            .multiblock("primitive_blast_furnace", PrimitiveBlastFurnaceMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES)
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_primitive_bricks"),
                    GTCEu.id("block/multiblock/primitive_blast_furnace"))
                    .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::createPBFLavaRender)))
            .hasBER(true)
            .appearanceBlock(CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX", "XXX")
                    .aisle("XXX", "X&X", "X#X", "X#X")
                    .aisle("XXX", "XYX", "XXX", "XXX")
                    .where('X', blocks(CASING_PRIMITIVE_BRICKS.get()))
                    .where('#', Predicates.air())
                    .where('&', Predicates.air()
                            .or(Predicates.custom(bws -> GTUtil.isBlockSnow(bws.getBlockState()), null)))
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .build())
            .register();

    public static final MultiblockMachineDefinition ELECTRIC_BLAST_FURNACE = REGISTRATE
            .multiblock("electric_blast_furnace", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifiers(GTRecipeModifiers::ebfOverclock, BATCH_MODE)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "CCC", "CCC", "XXX")
                    .aisle("XXX", "C#C", "C#C", "XMX")
                    .aisle("XSX", "CCC", "CCC", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(9)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, false)))
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('C', heatingCoils())
                    .where('#', air())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("ISO", "CCC", "CCC", "XMX")
                        .aisle("FXD", "C#C", "C#C", "XHX")
                        .aisle("EEX", "CCC", "CCC", "XXX")
                        .where('X', CASING_INVAR_HEATPROOF.getDefaultState())
                        .where('S', definition, Direction.NORTH)
                        .where('#', Blocks.AIR.defaultBlockState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.WEST)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.EAST)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.UP)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH);
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .recoveryItems(
                    () -> new ItemLike[] {
                            GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/multiblock/electric_blast_furnace"))
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.0"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.1"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.2"))
            .additionalDisplay((controller, components) -> {
                // spotless:off
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component.translatable(
                                    FormattingUtil.formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) + "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
                // spotless:on
            })
            .register();

    public static final MultiblockMachineDefinition LARGE_CHEMICAL_REACTOR = REGISTRATE
            .multiblock("large_chemical_reactor", WorkableElectricMultiblockMachine::new)
            .conditionalTooltip(defaultEnvironmentRequirement(),
                    ConfigHolder.INSTANCE.gameplay.environmentalHazards)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.LARGE_CHEMICAL_RECIPES)
            .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT, OC_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_PTFE_INERT)
            .pattern(definition -> {
                var casing = blocks(CASING_PTFE_INERT.get()).setMinGlobalLimited(10);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, false, false));
                return FactoryBlockPattern.start()
                        .aisle("XXX", "XCX", "XXX")
                        .aisle("XCX", "CPC", "XCX")
                        .aisle("XXX", "XSX", "XXX")
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where('X', casing.or(abilities))
                        .where('P', blocks(CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                        .where('C', Predicates.heatingCoils().setExactLimit(1)
                                .or(abilities)
                                .or(casing))
                        .build();
            })
            .shapeInfos(definition -> {
                ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var baseBuilder = MultiblockShapeInfo.builder()
                        .where('S', definition, Direction.NORTH)
                        .where('X', CASING_PTFE_INERT.getDefaultState())
                        .where('P', CASING_POLYTETRAFLUOROETHYLENE_PIPE.getDefaultState())
                        .where('C', COIL_CUPRONICKEL.getDefaultState())
                        .where('I', ITEM_IMPORT_BUS[3], Direction.NORTH)
                        .where('E', ENERGY_INPUT_HATCH[3], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[3], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[3], Direction.NORTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('H', FLUID_EXPORT_HATCH[3], Direction.NORTH);
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XXX", "XPX", "XXX")
                        .aisle("XEX", "XCX", "XXX")
                        .build());
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XXX", "XPX", "XCX")
                        .aisle("XEX", "XXX", "XXX")
                        .build());
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XCX", "XPX", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build());
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XXX", "CPX", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build());
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XXX", "XPC", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build());
                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                    GTCEu.id("block/multiblock/large_chemical_reactor"))
            .register();

    public static final MultiblockMachineDefinition IMPLOSION_COMPRESSOR = REGISTRATE
            .multiblock("implosion_compressor", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.IMPLOSION_RECIPES)
            .recipeModifiers(OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, true, false)))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static final MultiblockMachineDefinition PYROLYSE_OVEN = REGISTRATE
            .multiblock("pyrolyse_oven", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.PYROLYSE_RECIPES)
            .recipeModifiers(GTRecipeModifiers::pyrolyseOvenOverclock, BATCH_MODE)
            .appearanceBlock(MACHINE_CASING_ULV)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.get())))
                    .where('X',
                            blocks(MACHINE_CASING_ULV.get()).setMinGlobalLimited(6)
                                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                    .or(Predicates.autoAbilities(true, true, false)))
                    .where('C', Predicates.heatingCoils())
                    .where('#', Predicates.air())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("IXO", "XSX", "FMD")
                        .aisle("CCC", "C#C", "CCC")
                        .aisle("CCC", "C#C", "CCC")
                        .aisle("EEX", "XHX", "XXX")
                        .where('S', definition, Direction.NORTH)
                        .where('X', MACHINE_CASING_ULV.getDefaultState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('#', Blocks.AIR.defaultBlockState());
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/voltage/ulv/side"),
                    GTCEu.id("block/multiblock/pyrolyse_oven"))
            .tooltips(Component.translatable("gtceu.machine.pyrolyse_oven.tooltip.1"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.pyrolyse_oven.speed",
                            coilMachine.getCoilTier() == 0 ? 75 : 50 * (coilMachine.getCoilTier() + 1)));
                }
            })
            .register();

    public static final MultiblockMachineDefinition MULTI_SMELTER = REGISTRATE
            .multiblock("multi_smelter", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeTypes(GTRecipeTypes.FURNACE_RECIPES, GTRecipeTypes.ALLOY_SMELTER_RECIPES)
            .recipeModifiers(GTRecipeModifiers::multiSmelterParallel, BATCH_MODE)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.electric_furnace"), Component.translatable("gtceu.alloy_smelter")))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "CCC", "XXX")
                    .aisle("XXX", "C#C", "XMX")
                    .aisle("XSX", "CCC", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(9)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, false)))
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('C', heatingCoils())
                    .where('#', air())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("ISO", "CCC", "XMX")
                        .aisle("XXX", "C#C", "XHX")
                        .aisle("EEX", "CCC", "XXX")
                        .where('S', definition, Direction.NORTH)
                        .where('X', CASING_INVAR_HEATPROOF.getDefaultState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('#', Blocks.AIR.defaultBlockState());
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .recoveryItems(
                    () -> new ItemLike[] {
                            GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/multiblock/multi_furnace"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.multi_furnace.heating_coil_level",
                            coilMachine.getCoilType().getLevel()));
                    components.add(Component.translatable("gtceu.multiblock.multi_furnace.heating_coil_discount",
                            coilMachine.getCoilType().getEnergyDiscount()));
                }
            })
            .register();

    public static final MultiblockMachineDefinition CRACKER = REGISTRATE
            .multiblock("cracker", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.CRACKING_RECIPES)
            .recipeModifiers(GTRecipeModifiers::crackerOverclock, BATCH_MODE)
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("HCHCH", "HCHCH", "HCHCH")
                    .aisle("HCHCH", "H###H", "HCHCH")
                    .aisle("HCHCH", "HCOCH", "HCHCH")
                    .where('O', Predicates.controller(blocks(definition.get())))
                    .where('H', blocks(CASING_STAINLESS_CLEAN.get()).setMinGlobalLimited(12)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, true, false)))
                    .where('#', Predicates.air())
                    .where('C', Predicates.heatingCoils())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("FCICD", "HCSCH", "HCMCH")
                        .aisle("ECHCH", "H###H", "HCHCH")
                        .aisle("ECHCH", "HCXCH", "HCHCH")
                        .where('S', definition, Direction.NORTH)
                        .where('H', CASING_STAINLESS_CLEAN.getDefaultState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.WEST)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('X', MUFFLER_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('#', Blocks.AIR.defaultBlockState());
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/cracking_unit"))
            .tooltips(Component.translatable("gtceu.machine.cracker.tooltip.1"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.cracking_unit.energy",
                            100 - 10 * coilMachine.getCoilTier()));
                }
            })
            .register();

    public static final MultiblockMachineDefinition DISTILLATION_TOWER = REGISTRATE
            .multiblock("distillation_tower", DistillationTowerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DISTILLATION_RECIPES)
            .recipeModifiers(OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .pattern(definition -> {
                TraceabilityPredicate exportPredicate = abilities(PartAbility.EXPORT_FLUIDS_1X);
                if (GTCEu.Mods.isAE2Loaded()) {
                    exportPredicate = exportPredicate.or(blocks(GTAEMachines.FLUID_EXPORT_HATCH_ME.get()));
                }
                exportPredicate.setMaxLayerLimited(1);
                TraceabilityPredicate maint = autoAbilities(true, false, false)
                        .setMaxGlobalLimited(1);
                return FactoryBlockPattern.start(RIGHT, BACK, UP)
                        .aisle("YSY", "YYY", "YYY")
                        .aisle("ZZZ", "Z#Z", "ZZZ")
                        .aisle("XXX", "X#X", "XXX").setRepeatable(0, 10)
                        .aisle("XXX", "XXX", "XXX")
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where('Y', blocks(CASING_STAINLESS_CLEAN.get())
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                        .setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                                .or(maint))
                        .where('Z', blocks(CASING_STAINLESS_CLEAN.get())
                                .or(exportPredicate)
                                .or(maint))
                        .where('X', blocks(CASING_STAINLESS_CLEAN.get()).or(exportPredicate))
                        .where('#', Predicates.air())
                        .build();
            })
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .where('C', definition, Direction.NORTH)
                        .where('S', CASING_STAINLESS_CLEAN.getDefaultState())
                        .where('X', ITEM_EXPORT_BUS[HV], Direction.NORTH)
                        .where('I', FLUID_IMPORT_HATCH[HV], Direction.NORTH)
                        .where('E', ENERGY_INPUT_HATCH[HV], Direction.SOUTH)
                        .where('M', MAINTENANCE_HATCH, Direction.SOUTH)
                        .where('#', Blocks.AIR.defaultBlockState())
                        .where('F', FLUID_EXPORT_HATCH[HV], Direction.SOUTH);
                List<String> front = new ArrayList<>(15);
                front.add("XCI");
                front.add("SSS");
                List<String> middle = new ArrayList<>(15);
                middle.add("SSS");
                middle.add("SSS");
                List<String> back = new ArrayList<>(15);
                back.add("MES");
                back.add("SFS");
                for (int i = 1; i <= 11; ++i) {
                    front.add("SSS");
                    middle.add(1, "S#S");
                    back.add("SFS");
                    var copy = builder.shallowCopy()
                            .aisle(front.toArray(String[]::new))
                            .aisle(middle.toArray(String[]::new))
                            .aisle(back.toArray(String[]::new));
                    shapeInfos.add(copy.build());
                }
                return shapeInfos;
            })
            .allowExtendedFacing(false)
            .partSorter(Comparator.comparingInt(p -> p.self().getPos().getY()))
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/distillation_tower"))
            .register();

    public static final MultiblockMachineDefinition VACUUM_FREEZER = REGISTRATE
            .multiblock("vacuum_freezer", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.VACUUM_RECIPES)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_ALUMINIUM_FROSTPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_ALUMINIUM_FROSTPROOF.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/vacuum_freezer"))
            .register();

    public static final MultiblockMachineDefinition ASSEMBLY_LINE = REGISTRATE
            .multiblock("assembly_line", AssemblyLineMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
            .alwaysTryModifyRecipe(true)
            .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT, OC_NON_PERFECT)
            .appearanceBlock(CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start(BACK, UP, RIGHT)
                    .aisle("FIF", "RTR", "SAG", "#Y#")
                    .aisle("FIF", "RTR", "DAG", "#Y#").setRepeatable(3, 15)
                    .aisle("FOF", "RTR", "DAG", "#Y#")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('F', blocks(CASING_STEEL_SOLID.get())
                            .or(!ConfigHolder.INSTANCE.machines.orderedAssemblyLineFluids ?
                                    Predicates.abilities(PartAbility.IMPORT_FLUIDS_1X,
                                            PartAbility.IMPORT_FLUIDS_4X, PartAbility.IMPORT_FLUIDS_9X) :
                                    Predicates.abilities(PartAbility.IMPORT_FLUIDS_1X).setMaxGlobalLimited(4)))
                    .where('O',
                            Predicates.abilities(PartAbility.EXPORT_ITEMS)
                                    .addTooltips(Component.translatable("gtceu.multiblock.pattern.location_end")))
                    .where('Y',
                            blocks(CASING_STEEL_SOLID.get()).or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                                    .setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                    .where('I', blocks(ITEM_IMPORT_BUS[0].getBlock()))
                    .where('G', blocks(CASING_GRATE.get()))
                    .where('A', blocks(CASING_ASSEMBLY_CONTROL.get()))
                    .where('R', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('T', blocks(CASING_ASSEMBLY_LINE.get()))
                    .where('D', dataHatchPredicate(blocks(CASING_GRATE.get())))
                    .where('#', Predicates.any())
                    .build())
            .partSorter(AssemblyLineMachine::partSorter)
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/assembly_line"))
            .register();

    public static final MultiblockMachineDefinition PRIMITIVE_PUMP = REGISTRATE
            .multiblock("primitive_pump", PrimitivePumpMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(CASING_PUMP_DECK)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXX", "##F#", "##F#")
                    .aisle("XXHX", "F##F", "FFFF")
                    .aisle("SXXX", "##F#", "##F#")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_PUMP_DECK.get()))
                    .where('F', Predicates.frames(GTMaterials.TreatedWood))
                    .where('H',
                            Predicates.abilities(PartAbility.PUMP_FLUID_HATCH)
                                    .or(blocks(FLUID_EXPORT_HATCH[ULV].get(), FLUID_EXPORT_HATCH[LV].get())))
                    .where('#', Predicates.any())
                    .build())
            .allowExtendedFacing(false)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(createSidedWorkableCasingMachineModel(GTCEu.id("block/casings/pump_deck"),
                    GTCEu.id("block/multiblock/primitive_pump"))
                    .andThen(builder -> {
                        builder.replaceForAllStates((state, models) -> {
                            for (int i = 0; i < models.length; i++) {
                                models[i] = ConfiguredModel.builder()
                                        .modelFile(models[i].model).uvLock(true)
                                        .buildLast();
                            }
                            return models;
                        });
                    }))
            .register();

    public static final MultiblockMachineDefinition STEAM_GRINDER = REGISTRATE
            .multiblock("steam_grinder", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(CASING_BRONZE_BRICKS)
            .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
            .recipeModifier(SteamParallelMultiblockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('#', Predicates.air())
                    .where('X', blocks(CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(14)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/steam_grinder"))
            .register();

    public static final MultiblockMachineDefinition STEAM_OVEN = REGISTRATE
            .multiblock("steam_oven", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(CASING_BRONZE_BRICKS)
            .recipeType(GTRecipeTypes.FURNACE_RECIPES)
            .recipeModifier(SteamParallelMultiblockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("FFF", "XXX", " X ")
                    .aisle("FFF", "X#X", " X ")
                    .aisle("FFF", "XSX", " X ")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('X', blocks(CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(6)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1)))
                    .where('F', blocks(FIREBOX_BRONZE.get())
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .build())
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/steam_oven"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> DynamicRenderHelper.makeBoilerPartRender(
                                    BoilerFireboxType.BRONZE_FIREBOX, CASING_BRONZE_BRICKS))))
            .register();

    public static final MultiblockMachineDefinition[] FUSION_REACTOR = registerTieredMultis("fusion_reactor",
            FusionReactorMachine::new, (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .langValue("Fusion Reactor Computer MK %s".formatted(toRomanNumeral(tier - 5)))
                    .recipeType(GTRecipeTypes.FUSION_RECIPES)
                    .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT,
                            FusionReactorMachine::recipeModifier, BATCH_MODE)
                    .tooltips(
                            Component.translatable("gtceu.machine.fusion_reactor.capacity",
                                    FusionReactorMachine.calculateEnergyStorageFactor(tier, 16) / 1000000L),
                            Component.translatable("gtceu.machine.fusion_reactor.overclocking"),
                            Component.translatable("gtceu.multiblock.%s_fusion_reactor.description"
                                    .formatted(VN[tier].toLowerCase(Locale.ROOT))))
                    .appearanceBlock(() -> FusionReactorMachine.getCasingState(tier))
                    .pattern((definition) -> {
                        var casing = blocks(FusionReactorMachine.getCasingState(tier));
                        return FactoryBlockPattern.start()
                                .aisle("###############", "######OGO######", "###############")
                                .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                                .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                                .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                                .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                                .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                                .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                                .aisle("#C###########C#", "GAG#########GAG", "#C###########C#")
                                .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                                .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                                .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                                .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                                .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                                .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                                .aisle("###############", "######OSO######", "###############")
                                .where('S', controller(blocks(definition.get())))
                                .where('G', blocks(FUSION_GLASS.get()).or(casing))
                                .where('E', casing.or(
                                        blocks(PartAbility.INPUT_ENERGY.getBlockRange(tier, UV).toArray(Block[]::new))
                                                .setMinGlobalLimited(1).setPreviewCount(16)))
                                .where('C', casing)
                                .where('K', blocks(FusionReactorMachine.getCoilState(tier)))
                                .where('O', casing.or(abilities(PartAbility.EXPORT_FLUIDS)))
                                .where('A', air())
                                .where('I', casing.or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(2)))
                                .where('#', any())
                                .build();
                    })
                    .shapeInfos((controller) -> {
                        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();

                        MultiblockShapeInfo.ShapeInfoBuilder baseBuilder = MultiblockShapeInfo.builder()
                                .aisle("###############", "######NMN######", "###############")
                                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                                .aisle("####CC###CC####", "###w##SGS##e###", "####CC###CC####")
                                .aisle("###C#######C###", "##nKsG###GsKn##", "###C#######C###")
                                .aisle("##C#########C##", "#G#e#######w#G#", "##C#########C##")
                                .aisle("##C#########C##", "#G#G#######G#G#", "##C#########C##")
                                .aisle("#D###########D#", "W#E#########W#E", "#U###########U#")
                                .aisle("#C###########C#", "G#G#########G#G", "#C###########C#")
                                .aisle("#D###########D#", "W#E#########W#E", "#U###########U#")
                                .aisle("##C#########C##", "#G#G#######G#G#", "##C#########C##")
                                .aisle("##C#########C##", "#G#e#######w#G#", "##C#########C##")
                                .aisle("###C#######C###", "##sKnG###GnKs##", "###C#######C###")
                                .aisle("####CC###CC####", "###w##NGN##e###", "####CC###CC####")
                                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                                .aisle("###############", "######SGS######", "###############")
                                .where('M', controller, Direction.NORTH)
                                .where('C', FusionReactorMachine.getCasingState(tier))
                                .where('G', FUSION_GLASS.get())
                                .where('K', FusionReactorMachine.getCoilState(tier))
                                .where('W', GTMachines.FLUID_EXPORT_HATCH[tier], Direction.WEST)
                                .where('E', GTMachines.FLUID_EXPORT_HATCH[tier], Direction.EAST)
                                .where('S', GTMachines.FLUID_EXPORT_HATCH[tier], Direction.SOUTH)
                                .where('N', GTMachines.FLUID_EXPORT_HATCH[tier], Direction.NORTH)
                                .where('w', GTMachines.ENERGY_INPUT_HATCH[tier], Direction.WEST)
                                .where('e', GTMachines.ENERGY_INPUT_HATCH[tier], Direction.EAST)
                                .where('s', GTMachines.ENERGY_INPUT_HATCH[tier], Direction.SOUTH)
                                .where('n', GTMachines.ENERGY_INPUT_HATCH[tier], Direction.NORTH)
                                .where('U', GTMachines.FLUID_IMPORT_HATCH[tier], Direction.UP)
                                .where('D', GTMachines.FLUID_IMPORT_HATCH[tier], Direction.DOWN)
                                .where('#', Blocks.AIR.defaultBlockState());

                        shapeInfos.add(baseBuilder.shallowCopy()
                                .where('G', FusionReactorMachine.getCasingState(tier))
                                .build());
                        shapeInfos.add(baseBuilder.build());
                        return shapeInfos;
                    })
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableCasingMachineModel(FusionReactorMachine.getCasingType(tier).getTexture(),
                            GTCEu.id("block/multiblock/fusion_reactor"))
                            .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::createFusionRingRender)))
                    .hasBER(true)
                    .register(),
            LuV, ZPM, UV);

    public static final MultiblockMachineDefinition[] FLUID_DRILLING_RIG = registerTieredMultis(
            "fluid_drilling_rig", FluidDrillMachine::new, (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .langValue("%s Fluid Drilling Rig %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(DUMMY_RECIPES)
                    .tooltips(
                            Component.translatable("gtceu.machine.fluid_drilling_rig.description"),
                            Component.translatable("gtceu.machine.fluid_drilling_rig.depletion",
                                    FormattingUtil.formatNumbers(100.0 / FluidDrillMachine.getDepletionChance(tier))),
                            Component.translatable("gtceu.universal.tooltip.energy_tier_range", GTValues.VNF[tier],
                                    GTValues.VNF[tier + 1]),
                            Component.translatable("gtceu.machine.fluid_drilling_rig.production",
                                    FluidDrillMachine.getRigMultiplier(tier),
                                    FormattingUtil.formatNumbers(FluidDrillMachine.getRigMultiplier(tier) * 1.5)))
                    .appearanceBlock(() -> FluidDrillMachine.getCasingState(tier))
                    .pattern((definition) -> FactoryBlockPattern.start()
                            .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                            .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .where('S', controller(blocks(definition.get())))
                            .where('X', blocks(FluidDrillMachine.getCasingState(tier)).setMinGlobalLimited(3)
                                    .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                            .setMaxGlobalLimited(2))
                                    .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1)))
                            .where('C', blocks(FluidDrillMachine.getCasingState(tier)))
                            .where('F', blocks(FluidDrillMachine.getFrameState(tier)))
                            .where('#', any())
                            .build())
                    .workableCasingModel(FluidDrillMachine.getBaseTexture(tier),
                            GTCEu.id("block/multiblock/fluid_drilling_rig"))
                    .register(),
            MV, HV, EV);

    public static final MultiblockMachineDefinition[] LARGE_MINER = registerTieredMultis("large_miner",
            (holder, tier) -> new LargeMinerMachine(holder, tier, 64 / tier, 2 * tier - 5, tier, 8 - (tier - 5)),
            (tier, builder) -> builder
                    .rotationState(RotationState.NON_Y_AXIS)
                    .langValue("%s Large Miner %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
                    .appearanceBlock(() -> LargeMinerMachine.getCasingState(tier))
                    .pattern((definition) -> FactoryBlockPattern.start()
                            .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                            .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .where('S', controller(blocks(definition.getBlock())))
                            .where('X', blocks(LargeMinerMachine.getCasingState(tier))
                                    .or(abilities(PartAbility.EXPORT_ITEMS).setExactLimit(1).setPreviewCount(1))
                                    .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1).setPreviewCount(1))
                                    .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                            .setMaxGlobalLimited(2).setPreviewCount(1)))
                            .where('C', blocks(LargeMinerMachine.getCasingState(tier)))
                            .where('F', frames(LargeMinerMachine.getMaterial(tier)))
                            .where('#', any())
                            .build())
                    .allowExtendedFacing(true)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableCasingMachineModel(
                            MATERIALS_TO_CASING_TEXTURES.get(LargeMinerMachine.getMaterial(tier)),
                            GTCEu.id("block/multiblock/large_miner"))
                            .andThen((ctx, prov, modelBuilder) -> {
                                // replace the parent model for the formed large miner
                                modelBuilder.replaceForAllStates((state, models) -> {
                                    if (!state.getValue(GTMachineModelProperties.IS_FORMED)) {
                                        return models;
                                    }

                                    var parentModel = prov.models()
                                            .getExistingFile(GTCEu.id("block/machine/large_miner_active"));
                                    for (ConfiguredModel model : models) {
                                        ((BlockModelBuilder) model.model).parent(parentModel);
                                    }
                                    return models;
                                });
                            }))
                    .tooltips(
                            Component.translatable("gtceu.machine.large_miner.%s.tooltip"
                                    .formatted(VN[tier].toLowerCase(Locale.ROOT))),
                            Component.translatable("gtceu.machine.miner.multi.description"))
                    .tooltipBuilder((stack, tooltip) -> {
                        int workingAreaChunks = (2 * tier - 5);
                        tooltip.add(Component.translatable("gtceu.machine.miner.multi.modes"));
                        tooltip.add(Component.translatable("gtceu.machine.miner.multi.production"));
                        tooltip.add(Component.translatable("gtceu.machine.miner.fluid_usage", 8 - (tier - 5),
                                DrillingFluid.getLocalizedName()));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.working_area_chunks",
                                workingAreaChunks, workingAreaChunks));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.energy_tier_range",
                                GTValues.VNF[tier], GTValues.VNF[tier + 1]));
                    })
                    .register(),
            EV, IV, LuV);

    public static final MultiblockMachineDefinition CLEANROOM = REGISTRATE
            .multiblock("cleanroom", CleanroomMachine::new)
            .rotationState(RotationState.NONE)
            .recipeType(DUMMY_RECIPES)
            .appearanceBlock(PLASTCRETE)
            .tooltips(Component.translatable("gtceu.machine.cleanroom.tooltip.0"),
                    Component.translatable("gtceu.machine.cleanroom.tooltip.1"),
                    Component.translatable("gtceu.machine.cleanroom.tooltip.2"),
                    Component.translatable("gtceu.machine.cleanroom.tooltip.3"))
            .tooltipBuilder((stack, tooltip) -> {
                if (GTUtil.isCtrlDown()) {
                    tooltip.add(Component.empty());
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.4"));
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.5"));
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.6"));
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.7"));
                    // tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.8"));
                    if (GTCEu.Mods.isAE2Loaded()) {
                        tooltip.add(
                                Component.translatable(AEConfig.instance().getChannelMode() == ChannelMode.INFINITE ?
                                        "gtceu.machine.cleanroom.tooltip.ae2.no_channels" :
                                        "gtceu.machine.cleanroom.tooltip.ae2.channels"));
                    }
                    tooltip.add(Component.empty());
                } else {
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.hold_ctrl"));
                }
            })
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "X   X", "X   X", "X   X", "XFFFX")
                    .aisle("XXXXX", "X   X", "X   X", "X   X", "XFSFX")
                    .aisle("XXXXX", "X   X", "X   X", "X   X", "XFFFX")
                    .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .where('X', blocks(GTBlocks.PLASTCRETE.get())
                            .or(blocks(GTBlocks.CLEANROOM_GLASS.get()))
                            .or(abilities(PartAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30, 3))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3, 2))
                            .or(blocks(ConfigHolder.INSTANCE.machines.enableMaintenance ?
                                    GTMachines.MAINTENANCE_HATCH.getBlock() : PLASTCRETE.get()).setExactLimit(1))
                            .or(blocks(Blocks.IRON_DOOR).setMaxGlobalLimited(8)))
                    .where('S', controller(blocks(definition.getBlock())))
                    .where(' ', any())
                    .where('E', abilities(PartAbility.INPUT_ENERGY))
                    .where('F', cleanroomFilters())
                    .where('I', abilities(PartAbility.PASSTHROUGH_HATCH))
                    .build())
            .shapeInfos((controller) -> {
                ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("XXXXX", "XIHLX", "XXDXX", "XXXXX", "XXXXX")
                        .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                        .aisle("XXXXX", "X   X", "G   G", "X   X", "XFSFX")
                        .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                        .aisle("XMXEX", "XXOXX", "XXRXX", "XXXXX", "XXXXX")
                        .where('X', GTBlocks.PLASTCRETE)
                        .where('G', GTBlocks.CLEANROOM_GLASS)
                        .where('S', GTMultiMachines.CLEANROOM.getBlock())
                        .where(' ', Blocks.AIR)
                        .where('E', GTMachines.ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', GTMachines.ITEM_PASSTHROUGH_HATCH[GTValues.LV], Direction.NORTH)
                        .where('L', GTMachines.FLUID_PASSTHROUGH_HATCH[GTValues.LV], Direction.NORTH)
                        .where('H', GTMachines.HULL[GTValues.HV], Direction.NORTH)
                        .where('D', GTMachines.DIODE[GTValues.HV], Direction.NORTH)
                        .where('O',
                                Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                                        .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER))
                        .where('R', Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
                if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
                    builder.where('M', GTMachines.MAINTENANCE_HATCH, Direction.SOUTH);
                } else {
                    builder.where('M', GTBlocks.PLASTCRETE.get());
                }
                GTCEuAPI.CLEANROOM_FILTERS.values()
                        .forEach(block -> shapeInfo.add(builder.where('F', block.get()).build()));
                return shapeInfo;
            })
            .allowExtendedFacing(false)
            .allowFlip(false)
            .workableCasingModel(GTCEu.id("block/casings/cleanroom/plascrete"),
                    GTCEu.id("block/multiblock/cleanroom"))
            .register();

    public static final MultiblockMachineDefinition LARGE_COMBUSTION_ENGINE = registerLargeCombustionEngine(
            "large_combustion_engine", EV,
            CASING_TITANIUM_STABLE, CASING_TITANIUM_GEARBOX, CASING_ENGINE_INTAKE,
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
            GTCEu.id("block/multiblock/generator/large_combustion_engine"));

    public static final MultiblockMachineDefinition EXTREME_COMBUSTION_ENGINE = registerLargeCombustionEngine(
            "extreme_combustion_engine", IV,
            CASING_TUNGSTENSTEEL_ROBUST, CASING_TUNGSTENSTEEL_GEARBOX, CASING_EXTREME_ENGINE_INTAKE,
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
            GTCEu.id("block/multiblock/generator/extreme_combustion_engine"));

    public static final MultiblockMachineDefinition LARGE_STEAM_TURBINE = registerLargeTurbine("steam_large_turbine",
            HV,
            GTRecipeTypes.STEAM_TURBINE_FUELS,
            CASING_STEEL_TURBINE, CASING_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"),
            GTCEu.id("block/multiblock/generator/large_steam_turbine"),
            false);

    public static final MultiblockMachineDefinition LARGE_GAS_TURBINE = registerLargeTurbine("gas_large_turbine", EV,
            GTRecipeTypes.GAS_TURBINE_FUELS,
            CASING_STAINLESS_TURBINE, CASING_STAINLESS_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel"),
            GTCEu.id("block/multiblock/generator/large_gas_turbine"),
            true);

    public static final MultiblockMachineDefinition LARGE_PLASMA_TURBINE = registerLargeTurbine("plasma_large_turbine",
            IV,
            GTRecipeTypes.PLASMA_GENERATOR_FUELS,
            CASING_TUNGSTENSTEEL_TURBINE, CASING_TUNGSTENSTEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"),
            GTCEu.id("block/multiblock/generator/large_plasma_turbine"),
            false);

    public static final MultiblockMachineDefinition ACTIVE_TRANSFORMER = REGISTRATE
            .multiblock("active_transformer", ActiveTransformerMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(HIGH_POWER_CASING)
            .tooltips(Component.translatable("gtceu.machine.active_transformer.tooltip.0"),
                    Component.translatable("gtceu.machine.active_transformer.tooltip.1"))
            .tooltipBuilder(
                    (stack,
                     components) -> components.add(Component.translatable("gtceu.machine.active_transformer.tooltip.2")
                             .append(Component.translatable("gtceu.machine.active_transformer.tooltip.3")
                                     .withStyle(TooltipHelper.RAINBOW_HSL_SLOW))))
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "XCX", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(GTBlocks.HIGH_POWER_CASING.get()).setMinGlobalLimited(12)
                            .or(ActiveTransformerMachine.getHatchPredicates()))
                    .where('C', blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/hpca/high_power_casing"),
                    GTCEu.id("block/multiblock/data_bank"))
            .register();

    public static final MultiblockMachineDefinition POWER_SUBSTATION = REGISTRATE
            .multiblock("power_substation", PowerSubstationMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(Component.translatable("gtceu.machine.power_substation.tooltip.0"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.1"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.2",
                            PowerSubstationMachine.MAX_BATTERY_LAYERS),
                    Component.translatable("gtceu.machine.power_substation.tooltip.3"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.4",
                            PowerSubstationMachine.PASSIVE_DRAIN_MAX_PER_STORAGE / 1000))
            .tooltipBuilder(
                    (stack,
                     components) -> components.add(Component.translatable("gtceu.machine.power_substation.tooltip.5")
                             .append(Component.translatable("gtceu.machine.power_substation.tooltip.6")
                                     .withStyle(TooltipHelper.RAINBOW_HSL_SLOW))))
            .appearanceBlock(CASING_PALLADIUM_SUBSTATION)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "XCCCX", "XCCCX", "XCCCX", "XXXXX")
                    .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG")
                    .setRepeatable(1, PowerSubstationMachine.MAX_BATTERY_LAYERS)
                    .aisle("GGGGG", "GGGGG", "GGGGG", "GGGGG", "GGGGG")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CASING_PALLADIUM_SUBSTATION.get()))
                    .where('X',
                            blocks(CASING_PALLADIUM_SUBSTATION.get())
                                    .setMinGlobalLimited(PowerSubstationMachine.MIN_CASINGS)
                                    .or(autoAbilities(true, false, false))
                                    .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.SUBSTATION_INPUT_ENERGY,
                                            PartAbility.INPUT_LASER).setMinGlobalLimited(1))
                                    .or(abilities(PartAbility.OUTPUT_ENERGY, PartAbility.SUBSTATION_OUTPUT_ENERGY,
                                            PartAbility.OUTPUT_LASER).setMinGlobalLimited(1)))
                    .where('G', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('B', Predicates.powerSubstationBatteries())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("ICSCO", "NCMCT", "GGGGG", "GGGGG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GGGGG", "GGGGG", "GGGGG")
                        .where('S', definition, Direction.NORTH)
                        .where('C', CASING_PALLADIUM_SUBSTATION)
                        .where('G', CASING_LAMINATED_GLASS)
                        .where('I', GTMachines.ENERGY_INPUT_HATCH[HV], Direction.NORTH)
                        .where('N', GTMachines.SUBSTATION_ENERGY_INPUT_HATCH[EV], Direction.NORTH)
                        .where('O', GTMachines.ENERGY_OUTPUT_HATCH[HV], Direction.NORTH)
                        .where('T', GTMachines.SUBSTATION_ENERGY_OUTPUT_HATCH[EV], Direction.NORTH)
                        .where('M',
                                ConfigHolder.INSTANCE.machines.enableMaintenance ?
                                        GTMachines.MAINTENANCE_HATCH.getBlock().defaultBlockState().setValue(
                                                GTMachines.MAINTENANCE_HATCH.get().getRotationState().property,
                                                Direction.NORTH) :
                                        CASING_PALLADIUM_SUBSTATION.get().defaultBlockState());

                GTCEuAPI.PSS_BATTERIES.entrySet().stream()
                        // filter out empty batteries in example structures, though they are still
                        // allowed in the predicate (so you can see them on right-click)
                        .filter(entry -> entry.getKey().getCapacity() > 0)
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(entry -> shapeInfo.add(builder.where('B', entry.getValue().get()).build()));

                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_palladium_substation"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static final MultiblockMachineDefinition CHARCOAL_PILE_IGNITER = REGISTRATE
            .multiblock("charcoal_pile_igniter", CharcoalPileIgniterMachine::new)
            .rotationState(RotationState.NONE)
            .recipeType(DUMMY_RECIPES)
            .appearanceBlock(BRONZE_HULL)
            .pattern((def) -> FactoryBlockPattern.start()
                    .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                    .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                    .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DSD ")
                    .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                    .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                    .where('S', controller(blocks(def.getBlock())))
                    .where('B', blocks(Blocks.BRICKS))
                    .where('X', blocks(Blocks.DIRT))
                    .where('D', blocks(Blocks.DIRT))
                    .where('C', blocks(Blocks.OAK_LOG))
                    .build())
            .allowFlip(false)
            .allowExtendedFacing(false)
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/charcoal_pile_igniter"))
            .register();

    public static MultiblockMachineDefinition[] BEDROCK_ORE_MINER = registerTieredMultis(
            "bedrock_ore_miner", BedrockOreMinerMachine::new, (tier, builder) -> builder
                    .rotationState(RotationState.NON_Y_AXIS)
                    .langValue("%s Bedrock Ore Miner %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(DUMMY_RECIPES)
                    .tooltips(
                            Component.translatable("gtceu.machine.bedrock_ore_miner.description"),
                            Component.translatable("gtceu.machine.bedrock_ore_miner.depletion",
                                    FormattingUtil.formatNumbers(
                                            100.0 / BedrockOreMinerMachine.getDepletionChance(tier))),
                            Component.translatable("gtceu.universal.tooltip.energy_tier_range",
                                    GTValues.VNF[tier], GTValues.VNF[tier + 1]),
                            Component.translatable("gtceu.machine.bedrock_ore_miner.production",
                                    BedrockOreMinerMachine.getRigMultiplier(tier),
                                    FormattingUtil.formatNumbers(
                                            BedrockOreMinerMachine.getRigMultiplier(tier) * 1.5)))
                    .appearanceBlock(() -> BedrockOreMinerMachine.getCasingState(tier))
                    .pattern((definition) -> FactoryBlockPattern.start()
                            .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                            .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .where('S', controller(blocks(definition.get())))
                            .where('X',
                                    blocks(BedrockOreMinerMachine.getCasingState(tier)).setMinGlobalLimited(3)
                                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                                    .setMaxGlobalLimited(2))
                                            .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1)))
                            .where('C', blocks(BedrockOreMinerMachine.getCasingState(tier)))
                            .where('F', blocks(BedrockOreMinerMachine.getFrameState(tier)))
                            .where('#', any())
                            .build())
                    .workableCasingModel(BedrockOreMinerMachine.getBaseTexture(tier),
                            GTCEu.id("block/multiblock/bedrock_ore_miner"))
                    .register(),
            MV, HV, EV);

    // Multiblock Tanks
    public static final MachineDefinition WOODEN_TANK_VALVE = GTMachineUtils.registerTankValve(
            "wooden_tank_valve", "Wooden Tank Valve", false,
            (builder, overlay) -> builder.sidedWorkableCasingModel(GTCEu.id("block/casings/wood_wall"), overlay));
    public static final MultiblockMachineDefinition WOODEN_MULTIBLOCK_TANK = registerMultiblockTank(
            "wooden_multiblock_tank", "Wooden Multiblock Tank", 250 * 1000,
            CASING_WOOD_WALL, WOODEN_TANK_VALVE::getBlock,
            new PropertyFluidFilter(340, false, false, false, false),
            (builder, overlay) -> builder.sidedWorkableCasingModel(GTCEu.id("block/casings/wood_wall"), overlay));

    public static final MachineDefinition BRONZE_TANK_VALVE = GTMachineUtils.registerTankValve(
            "bronze_tank_valve", "Bronze Tank Valve", true,
            (builder, overlay) -> builder
                    .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"), overlay));
    public static final MultiblockMachineDefinition BRONZE_MULTIBLOCK_TANK = registerMultiblockTank(
            "bronze_multiblock_tank", "Bronze Multiblock Tank", 500 * 1000,
            CASING_BRONZE_BRICKS, BRONZE_TANK_VALVE::getBlock,
            new PropertyFluidFilter(1696, true, false, false, false),
            (builder, overlay) -> builder
                    .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"), overlay));

    public static final MachineDefinition STEEL_TANK_VALVE = GTMachineUtils.registerTankValve(
            "steel_tank_valve", "Steel Tank Valve", true,
            (builder, overlay) -> builder.workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"), overlay));
    public static final MultiblockMachineDefinition STEEL_MULTIBLOCK_TANK = registerMultiblockTank(
            "steel_multiblock_tank", "Steel Multiblock Tank", 1000 * 1000,
            CASING_STEEL_SOLID, STEEL_TANK_VALVE::getBlock,
            null,
            (builder, overlay) -> builder.workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"), overlay));

    public static final MultiblockMachineDefinition CENTRAL_MONITOR = REGISTRATE
            .multiblock("central_monitor", CentralMonitorMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(DUMMY_RECIPES)
            .appearanceBlock(CASING_ALUMINIUM_FROSTPROOF)
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("BCB", "BBB", "BBB", "BBB")
                    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('B', CentralMonitorMachine.getMultiPredicate())
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/central_monitor"))
                    .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::createCentralMonitorRender)))
            .hasBER(true)
            .register();

    public static void init() {}
}
