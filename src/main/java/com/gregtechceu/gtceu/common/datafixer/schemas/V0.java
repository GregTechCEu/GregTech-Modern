package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;
import com.gregtechceu.gtceu.core.mixins.datafixer.V705Accessor;

import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraftforge.fml.loading.LoadingModList;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.datafixer.types.ExtraDSL.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ALL_TIERS;
import static com.mojang.datafixers.DSL.*;

public class V0 extends NamespacedSchema {

    public V0(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    // spotless:off
    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes,
                              Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);

        // add forge registry id map to the level schema
        schema.registerType(false, GTReferences.FORGE_REGISTRY_DATA, () -> optionalFields(
                "minecraft:block", fields(
                        "ids", list(fields("K", References.BLOCK_NAME.in(schema), "V", constType(intType()))),
                        "aliases", list(fields("K", constType(namespacedString()), "V", References.BLOCK_NAME.in(schema))),
                        "overrides", list(fields("K", References.BLOCK_NAME.in(schema), "V", constType(string())))
                ),
                "minecraft:item", fields(
                        "ids", list(fields("K", References.ITEM_NAME.in(schema), "V", constType(intType()))),
                        "aliases", list(fields("K", constType(namespacedString()), "V", References.ITEM_NAME.in(schema))),
                        "overrides", list(fields("K", References.ITEM_NAME.in(schema), "V", constType(string())))
                ),
                "minecraft:fluid", fields(
                        "ids", list(fields("K", GTReferences.FLUID_NAME.in(schema), "V", constType(intType()))),
                        "aliases", list(fields("K", constType(namespacedString()), "V", GTReferences.FLUID_NAME.in(schema))),
                        "overrides", list(fields("K", GTReferences.FLUID_NAME.in(schema), "V", constType(string())))
                ),
                "minecraft:entity_type", fields(
                        "ids", list(fields("K", References.ENTITY_NAME.in(schema), "V", constType(intType()))),
                        "aliases", list(fields("K", constType(namespacedString()), "V", References.ENTITY_NAME.in(schema))),
                        "overrides", list(fields("K", References.ENTITY_NAME.in(schema), "V", constType(string())))
                ),
                "gtceu:material", fields(
                        "ids", list(fields("K", GTReferences.MATERIAL_NAME.in(schema), "V", constType(intType()))),
                        "aliases", list(fields("K", constType(namespacedString()), "V", GTReferences.MATERIAL_NAME.in(schema))),
                        "overrides", list(fields("K", GTReferences.MATERIAL_NAME.in(schema), "V", constType(string())))
                )
        ));


        schema.registerType(false, GTReferences.MATERIAL_NAME, () -> constType(namespacedString()));

        schema.registerType(true, GTReferences.FLUID_STACK, () -> optionalFields(
                "FluidName", GTReferences.FLUID_NAME.in(schema),
                "Tag", remainder()
        ));
        schema.registerType(false, GTReferences.FLUID_NAME, () -> constType(namespacedString()));

        // add part stats NBT field
        schema.registerType(true, References.ITEM_STACK, () -> hook(
                optionalFields(
                        "id", References.ITEM_NAME.in(schema),
                        "tag", optionalFields(
                                "EntityTag", References.ENTITY_TREE.in(schema),
                                "BlockEntityTag", References.BLOCK_ENTITY.in(schema),
                                "CanDestroy", list(References.BLOCK_NAME.in(schema)),
                                "CanPlaceOn", list(References.BLOCK_NAME.in(schema)),
                                "Items", list(References.ITEM_STACK.in(schema)),
                                "GT.PartStats", optionalFields(
                                        "Material", GTReferences.MATERIAL_NAME.in(schema)
                                )
                        )
                ),
                V705Accessor.gtceu$getAddNames(), Hook.HookFunction.IDENTITY
        ));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        // region steam
        final Supplier<TypeTemplate> steamMachine = () -> optionalFields(
                "steamTank", notifiableFluidTank(schema)
        );
        final Supplier<TypeTemplate> steamBoiler = () -> optionalFields(
                "waterTank", notifiableFluidTank(schema),
                steamMachine.get()
        );
        registerSteamMachine(schema, map, "gtceu:steam_solid_boiler", () -> optionalFields(
                "fuelHandler", notifiableItemHandler(schema),
                "ashHandler", notifiableItemHandler(schema),
                steamBoiler.get()
        ));
        registerSteamMachine(schema, map, "gtceu:steam_liquid_boiler", () -> optionalFields(
                "fuelTank", notifiableFluidTank(schema),
                steamBoiler.get()
        ));
        registerSteamMachine(schema, map, "gtceu:steam_solar_boiler", steamBoiler);
        registerSimpleSteamMachine(schema, map, "gtceu:extractor");
        registerSimpleSteamMachine(schema, map, "gtceu:steam_macerator");
        registerSimpleSteamMachine(schema, map, "gtceu:compressor");
        registerSimpleSteamMachine(schema, map, "gtceu:forge_hammer");
        registerSimpleSteamMachine(schema, map, "gtceu:furnace");
        registerSimpleSteamMachine(schema, map, "gtceu:alloy_smelter");
        registerSimpleSteamMachine(schema, map, "gtceu:rock_crusher");
        schema.register(map, "gtceu:steam_miner", () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "steamTank", notifiableFluidTank(schema)
        ));
        // endregion

        // region simple
        registerForTiers(schema, map, "gtceu:machine_hull", DSL::remainder, ALL_TIERS);
        registerSimpleMachine(schema, map, "gtceu:electric_furnace", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:alloy_smelter", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:arc_furnace", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:assembler", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:autoclave", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:bender", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:brewery", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:canner", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:centrifuge", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:chemical_bath", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:chemical_reactor", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:compressor", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:cutter", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:distillery", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:electrolyzer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:electromagnetic_separator", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:extractor", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:extruder", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:fermenter", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:fluid_heater", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:fluid_solidifier", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:forge_hammer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:forming_press", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:lathe", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:scanner", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:mixer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:ore_washer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:packer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:polarizer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:laser_engraver", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:sifter", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:thermal_centrifuge", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:wiremill", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:circuit_assembler", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:macerator", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:gas_collector", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:rock_crusher", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:air_scrubber", LOW_TIERS);
        registerSimpleMachine(schema, map, "gtceu:combustion", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:steam_turbine", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:gas_turbine", ELECTRIC_TIERS);
        // endregion

        // region electric
        final int[] transformerTiers = GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? OpV : UV);
        registerForTiers(schema, map, "gtceu:transformer_1a", DSL::remainder, transformerTiers);
        registerForTiers(schema, map, "gtceu:transformer_2a", DSL::remainder, transformerTiers);
        registerForTiers(schema, map, "gtceu:transformer_4a", DSL::remainder, transformerTiers);
        registerForTiers(schema, map, "gtceu:transformer_16a", DSL::remainder, transformerTiers);

        registerForTiers(schema, map, "gtceu:1a_energy_converter", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:4a_energy_converter", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:8a_energy_converter", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:16a_energy_converter", DSL::remainder, ALL_TIERS);

        schema.registerSimple(map, "gtceu:long_distance_item_pipeline_endpoint");
        schema.registerSimple(map, "gtceu:long_distance_fluid_pipeline_endpoint");
        schema.registerSimple(map, "gtceu:long_distance_fluid_pipeline_endpoint");

        final Supplier<TypeTemplate> batteryBuffer = () -> optionalFields(
                "batteryInventory", itemHandler(schema)
        );
        registerForTiers(schema, map, "gtceu:battery_buffer_4x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:battery_buffer_8x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:battery_buffer_16x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:charger_4x", batteryBuffer, ALL_TIERS);

        registerForTiers(schema, map, "gtceu:pump", () -> optionalFields(
                "cache", notifiableFluidTank(schema)
        ), LV, MV, HV, EV);
        registerForTiers(schema, map, "gtceu:fisher", () -> optionalFields(
                "cache", notifiableItemHandler(schema),
                "baitHandler", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema)
        ), LV, MV, HV, EV, IV, LuV);
        registerForTiers(schema, map, "gtceu:block_breaker", () -> optionalFields(
                "cache", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema)
        ), LV, MV, HV, EV);
        registerSimpleMachine(schema, map, "gtceu:miner", LV, MV, HV);
        registerForTiers(schema, map, "gtceu:world_accelerator", DSL::remainder, LV, MV, HV, EV, IV, LuV, ZPM, UV);
        registerForTiers(schema, map, "gtceu:item_collector", () -> optionalFields(
                "output", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema),
                "filterInventory", itemHandler(schema)
        ), LV, MV, HV, EV);
        // endregion

        // region storage
        final Supplier<TypeTemplate> itemStorage = () -> optionalFields(
                "inventory", notifiableItemHandler(schema)
        );
        registerForTiers(schema, map, "gtceu:buffer", () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                itemStorage.get()
        ), LV, MV, HV);
        schema.registerSimple(map, "gtceu:creative_energy");
        schema.registerSimple(map, "gtceu:creative_computation_provider");

        final Supplier<TypeTemplate> quantumChest = () -> optionalFields(
                "lockedFluid", GTReferences.FLUID_STACK.in(schema),
                "stored", GTReferences.FLUID_STACK.in(schema)
        );
        final Supplier<TypeTemplate> quantumTank = () -> optionalFields(
                "lockedItem", itemHandler(schema),
                "stored", References.ITEM_STACK.in(schema)
        );
        schema.register(map, "gtceu:creative_chest", quantumChest);
        schema.register(map, "gtceu:creative_tank", quantumTank);
        registerForTiers(schema, map, "gtceu:super_chest", quantumChest, LOW_TIERS);
        registerForTiers(schema, map, "gtceu:quantum_chest", quantumChest, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:super_tank", quantumTank, LOW_TIERS);
        registerForTiers(schema, map, "gtceu:quantum_tank", quantumTank, HIGH_TIERS);

        schema.register(map, "gtceu:wood_crate", itemStorage);
        schema.register(map, "gtceu:bronze_crate", itemStorage);
        schema.register(map, "gtceu:steel_crate", itemStorage);
        schema.register(map, "gtceu:aluminium_crate", itemStorage);
        schema.register(map, "gtceu:stainless_steel_crate", itemStorage);
        schema.register(map, "gtceu:titanium_crate", itemStorage);
        schema.register(map, "gtceu:tungsten_steel_crate", itemStorage);

        final Supplier<TypeTemplate> drum = () -> optionalFields(
                "stored", GTReferences.FLUID_STACK.in(schema)
        );
        schema.register(map, "gtceu:wood_drum", drum);
        schema.register(map, "gtceu:bronze_drum", drum);
        schema.register(map, "gtceu:steel_drum", drum);
        schema.register(map, "gtceu:aluminium_drum", drum);
        schema.register(map, "gtceu:stainless_steel_drum", drum);
        schema.register(map, "gtceu:gold_drum", drum);
        schema.register(map, "gtceu:titanium_drum", drum);
        schema.register(map, "gtceu:tungsten_steel_drum", drum);
        // endregion

        // region part
        final Supplier<TypeTemplate> itemBus = () -> optionalFields(
                "circuitInventory", notifiableItemHandler(schema),
                itemStorage.get()
        );
        registerForTiers(schema, map, "gtceu:input_bus", itemBus, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:output_bus", itemBus, ALL_TIERS);
        final Supplier<TypeTemplate> fluidHatch = () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                "circuitInventory", notifiableItemHandler(schema)
        );
        registerForTiers(schema, map, "gtceu:input_hatch", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:input_hatch_4x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:input_hatch_9x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:output_hatch", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:output_hatch_4x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:output_hatch_9x", fluidHatch, ALL_TIERS);

        registerForTiers(schema, map, "gtceu:energy_input_hatch", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_output_hatch", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_input_hatch_4a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_output_hatch_4a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_input_hatch_16a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_output_hatch_16a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:substation_input_hatch_64a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:substation_output_hatch_64a", DSL::remainder, ALL_TIERS);

        registerForTiers(schema, map, "gtceu:muffler_hatch", itemStorage, ELECTRIC_TIERS);

        schema.register(map, "gtceu:steam_input_bus", itemBus);
        schema.register(map, "gtceu:steam_output_bus", itemBus);
        schema.register(map, "gtceu:steam_input_hatch", fluidHatch);
        schema.registerSimple(map, "gtceu:coke_oven_hatch");
        schema.register(map, "gtceu:pump_hatch", fluidHatch);
        final Supplier<TypeTemplate> maintenanceHatch = () -> optionalFields(
                "itemStackHandler", notifiableItemHandler(schema)
        );
        schema.register(map, "gtceu:maintenance_hatch", maintenanceHatch);
        schema.register(map, "gtceu:configurable_maintenance_hatch", maintenanceHatch);
        schema.register(map, "gtceu:cleaning_maintenance_hatch", maintenanceHatch);
        schema.register(map, "gtceu:auto_maintenance_hatch", maintenanceHatch);

        registerForTiers(schema, map, "gtceu:item_passthrough_hatch", itemBus, ELECTRIC_TIERS);
        registerForTiers(schema, map, "gtceu:fluid_passthrough_hatch", fluidHatch, ELECTRIC_TIERS);
        schema.register(map, "gtceu:reservoir_hatch", fluidHatch);
        final Supplier<TypeTemplate> dualHatch = () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                itemBus.get()
        );
        registerForTiers(schema, map, "gtceu:dual_input_hatch", dualHatch, DUAL_HATCH_TIERS);
        registerForTiers(schema, map, "gtceu:dual_output_hatch", dualHatch, DUAL_HATCH_TIERS);

        registerForTiers(schema, map, "gtceu:diode", DSL::remainder, ELECTRIC_TIERS);
        registerForTiers(schema, map, "gtceu:rotor_holder", () -> optionalFields(
                "rotorMaterial", GTReferences.MATERIAL_NAME.in(schema),
                itemStorage.get()
        ), GTValues.tiersBetween(HV, GTCEuAPI.isHighTier() ? OpV : UV));

        registerForTiers(schema, map, "gtceu:256a_laser_target_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:256a_laser_source_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:1024a_laser_target_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:1024a_laser_source_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:4096a_laser_target_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:4096a_laser_source_hatch", DSL::remainder, HIGH_TIERS);

        schema.registerSimple(map, "gtceu:monitor");
        schema.registerSimple(map, "gtceu:advanced_monitor");
        // endregion

        // region multiblock
        schema.registerSimple(map, "gtceu:bronze_large_boiler");
        schema.registerSimple(map, "gtceu:steel_large_boiler");
        schema.registerSimple(map, "gtceu:titanium_large_boiler");
        schema.registerSimple(map, "gtceu:tungstensteel_large_boiler");

        final Supplier<TypeTemplate> primitiveMachine = () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "importFluids", notifiableFluidTank(schema),
                "exportFluids", notifiableFluidTank(schema)
        );
        schema.register(map, "gtceu:coke_oven", primitiveMachine);
        schema.register(map, "gtceu:primitive_blast_furnace", primitiveMachine);

        schema.registerSimple(map, "gtceu:electric_blast_furnace");
        schema.registerSimple(map, "gtceu:large_chemical_reactor");
        schema.registerSimple(map, "gtceu:implosion_compressor");
        schema.registerSimple(map, "gtceu:pyrolyse_oven");
        schema.registerSimple(map, "gtceu:multi_smelter");
        schema.registerSimple(map, "gtceu:cracker");
        schema.registerSimple(map, "gtceu:distillation_tower");
        schema.registerSimple(map, "gtceu:vacuum_freezer");
        schema.registerSimple(map, "gtceu:assembly_line");
        schema.registerSimple(map, "gtceu:primitive_pump");
        schema.registerSimple(map, "gtceu:steam_grinder");
        schema.registerSimple(map, "gtceu:steam_oven");
        registerForTiers(schema, map, "gtceu:fusion_reactor", DSL::remainder, LuV, ZPM, UV);
        registerForTiers(schema, map, "gtceu:fluid_drilling_rig", DSL::remainder, MV, HV, EV);
        registerForTiers(schema, map, "gtceu:large_miner", DSL::remainder, EV, IV, LuV);
        schema.registerSimple(map, "gtceu:cleanroom");
        schema.registerSimple(map, "gtceu:large_combustion_engine");
        schema.registerSimple(map, "gtceu:extreme_combustion_engine");
        schema.registerSimple(map, "gtceu:steam_large_turbine");
        schema.registerSimple(map, "gtceu:gas_large_turbine");
        schema.registerSimple(map, "gtceu:plasma_large_turbine");
        schema.registerSimple(map, "gtceu:active_transformer");
        schema.registerSimple(map, "gtceu:power_substation");
        registerForTiers(schema, map, "gtceu:bedrock_ore_miner", DSL::remainder, MV, HV, EV);
        schema.registerSimple(map, "gtceu:wooden_tank_valve");
        schema.registerSimple(map, "gtceu:wooden_multiblock_tank");
        schema.registerSimple(map, "gtceu:bronze_tank_valve");
        schema.registerSimple(map, "gtceu:bronze_multiblock_tank");
        schema.registerSimple(map, "gtceu:steel_tank_valve");
        schema.registerSimple(map, "gtceu:steel_multiblock_tank");

        schema.registerSimple(map, "gtceu:central_monitor");

        // region GCYM
        registerForTiers(schema, map, "gtceu:parallel_hatch", DSL::remainder, IV, LuV, ZPM, UV);
        schema.registerSimple(map, "gtceu:large_maceration_tower");
        schema.registerSimple(map, "gtceu:large_chemical_bath");
        schema.registerSimple(map, "gtceu:large_centrifuge");
        schema.registerSimple(map, "gtceu:large_mixer");
        schema.registerSimple(map, "gtceu:large_electrolyzer");
        schema.registerSimple(map, "gtceu:large_electromagnet");
        schema.registerSimple(map, "gtceu:large_packer");
        schema.registerSimple(map, "gtceu:large_assembler");
        schema.registerSimple(map, "gtceu:large_circuit_assembler");
        schema.registerSimple(map, "gtceu:large_arc_smelter");
        schema.registerSimple(map, "gtceu:large_engraving_laser");
        schema.registerSimple(map, "gtceu:large_sifting_funnel");
        schema.registerSimple(map, "gtceu:alloy_blast_smelter");
        schema.registerSimple(map, "gtceu:large_autoclave");
        schema.registerSimple(map, "gtceu:large_material_press");
        schema.registerSimple(map, "gtceu:large_brewer");
        schema.registerSimple(map, "gtceu:large_cutter");
        schema.registerSimple(map, "gtceu:large_extractor");
        schema.registerSimple(map, "gtceu:large_extruder");
        schema.registerSimple(map, "gtceu:large_solidifier");
        schema.registerSimple(map, "gtceu:large_wiremill");
        schema.registerSimple(map, "gtceu:mega_blast_furnace");
        schema.registerSimple(map, "gtceu:mega_vacuum_freezer");
        // endregion
        // endregion

        // region research
        schema.registerSimple(map, "gtceu:research_station");
        schema.register(map, "gtceu:object_holder", () -> optionalFields(
                "heldItems", notifiableItemHandler(schema)
        ));
        schema.registerSimple(map, "gtceu:data_bank");
        schema.registerSimple(map, "gtceu:network_switch");
        schema.registerSimple(map, "gtceu:high_performance_computation_array");
        schema.registerSimple(map, "gtceu:computation_transmitter_hatch");
        schema.registerSimple(map, "gtceu:computation_receiver_hatch");
        schema.registerSimple(map, "gtceu:data_transmitter_hatch");
        schema.registerSimple(map, "gtceu:data_receiver_hatch");
        final Supplier<TypeTemplate> dataAccessHatch = () -> optionalFields(
                "importItems", notifiableItemHandler(schema)
        );
        schema.register(map, "gtceu:basic_data_access_hatch", dataAccessHatch);
        schema.register(map, "gtceu:data_access_hatch", dataAccessHatch);
        schema.register(map, "gtceu:advanced_data_access_hatch", dataAccessHatch);
        schema.register(map, "gtceu:creative_data_access_hatch", dataAccessHatch);
        schema.registerSimple(map, "gtceu:hpca_empty_component");
        schema.registerSimple(map, "gtceu:hpca_computation_component");
        schema.registerSimple(map, "gtceu:hpca_advanced_computation_component");
        schema.registerSimple(map, "gtceu:hpca_heat_sink_component");
        schema.registerSimple(map, "gtceu:hpca_active_cooler_component");
        schema.registerSimple(map, "gtceu:hpca_bridge_component");
        // endregion

        // region AE2 compat
        if (LoadingModList.get().getModFileById(MODID_APPENG) != null) { // only add these if AE2 is loaded
            schema.register(map, "gtceu:me_input_bus", itemBus);
            schema.register(map, "gtceu:me_stocking_input_bus", itemBus);
            schema.register(map, "gtceu:me_output_bus", itemBus);
            schema.register(map, "gtceu:me_input_hatch", fluidHatch);
            schema.register(map, "gtceu:me_stocking_input_hatch", fluidHatch);
            schema.register(map, "gtceu:me_output_hatch", fluidHatch);
            schema.register(map, "gtceu:me_pattern_buffer", () -> optionalFields(
                    "patternInventory", itemHandler(schema),
                    "shareInventory", notifiableItemHandler(schema),
                    "shareTank", notifiableFluidTank(schema),
                    "internalInventory", list(
                            fields(
                                    "inventory", list(References.ITEM_STACK.in(schema)),
                                    "fluidInventory", list(GTReferences.FLUID_STACK.in(schema))
                            )
                    ),
                    itemBus.get()
            ));
            schema.registerSimple(map, "gtceu:me_pattern_buffer_proxy");
        }
        // endregion

        return map;
    }

    private static void registerSimpleMachine(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name, int... tiers) {
        registerForTiers(schema, map, name, () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "importFluids", notifiableFluidTank(schema),
                "exportFluids", notifiableFluidTank(schema),
                "chargerInventory", itemHandler(schema),
                "circuitInventory", notifiableItemHandler(schema)
        ), tiers);
    }

    protected static void registerForTiers(Schema schema, Map<String, Supplier<TypeTemplate>> map,
                                           String name, Supplier<TypeTemplate> template, int... tiers) {
        String namespace = name.substring(0, name.indexOf(':') + 1);
        String path = name.substring(name.indexOf(':') + 1);
        for (int tier : tiers) {
            schema.register(map, namespace + GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + path, template);
        }
    }

    protected static void registerSteamMachine(Schema schema, Map<String, Supplier<TypeTemplate>> map,
                                               String name, Supplier<TypeTemplate> template) {
        String namespace = name.substring(0, name.indexOf(':') + 1);
        String path = name.substring(name.indexOf(':') + 1);
        schema.register(map, namespace + "lp_" + path, template);
        schema.register(map, namespace + "hp_" + path, template);
    }

    protected static void registerSimpleSteamMachine(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        registerSteamMachine(schema, map, name, () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "steamTank", notifiableFluidTank(schema)
        ));
    }

    protected static TypeTemplate itemHandler(Schema schema) {
        return field("Items", list(References.ITEM_STACK.in(schema)));
    }

    protected static TypeTemplate notifiableItemHandler(Schema schema) {
        return field("storage", itemHandler(schema));
    }

    protected static TypeTemplate notifiableFluidTank(Schema schema) {
        return field("storages", list(GTReferences.FLUID_STACK.in(schema)));
    }
    // spotless:on
}
