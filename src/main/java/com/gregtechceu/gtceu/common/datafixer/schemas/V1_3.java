package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;

import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraftforge.fml.loading.LoadingModList;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ALL_TIERS;
import static com.gregtechceu.gtceu.common.datafixer.schemas.V0.*;
import static com.mojang.datafixers.DSL.*;

public class V1_3 extends NamespacedSchema {

    public V1_3(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    // spotless:off
    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        final Supplier<TypeTemplate> traitHolder = () -> traitHolder(schema);

        // region steam
        final Supplier<TypeTemplate> steamMachine = () -> optionalFields(
                "steamTank", notifiableFluidTank(schema),
                traitHolder(schema)
        );
        final Supplier<TypeTemplate> steamBoiler = () -> optionalFields(
                "waterTank", notifiableFluidTank(schema),
                steamMachine.get()
        );
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
                "steamTank", notifiableFluidTank(schema),
                traitHolder(schema)
        ));
        // endregion

        // region simple
        registerForTiers(schema, map, "gtceu:machine_hull", traitHolder, ALL_TIERS);
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
        registerForTiers(schema, map, "gtceu:transformer_1a", traitHolder, transformerTiers);
        registerForTiers(schema, map, "gtceu:transformer_2a", traitHolder, transformerTiers);
        registerForTiers(schema, map, "gtceu:transformer_4a", traitHolder, transformerTiers);
        registerForTiers(schema, map, "gtceu:transformer_16a", traitHolder, transformerTiers);

        registerForTiers(schema, map, "gtceu:1a_energy_converter", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:4a_energy_converter", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:8a_energy_converter", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:16a_energy_converter", traitHolder, ALL_TIERS);

        schema.register(map, "gtceu:long_distance_item_pipeline_endpoint", traitHolder);
        schema.register(map, "gtceu:long_distance_fluid_pipeline_endpoint", traitHolder);
        schema.register(map, "gtceu:long_distance_fluid_pipeline_endpoint", traitHolder);

        final Supplier<TypeTemplate> batteryBuffer = () -> optionalFields(
                "batteryInventory", itemHandler(schema),
                traitHolder(schema)
        );
        registerForTiers(schema, map, "gtceu:battery_buffer_4x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:battery_buffer_8x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:battery_buffer_16x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:charger_4x", batteryBuffer, ALL_TIERS);

        registerForTiers(schema, map, "gtceu:pump", () -> optionalFields(
                "cache", notifiableFluidTank(schema),
                traitHolder(schema)
        ), LV, MV, HV, EV);
        registerForTiers(schema, map, "gtceu:fisher", () -> optionalFields(
                "cache", notifiableItemHandler(schema),
                "baitHandler", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema),
                traitHolder(schema)
        ), LV, MV, HV, EV, IV, LuV);
        registerForTiers(schema, map, "gtceu:block_breaker", () -> optionalFields(
                "cache", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema),
                traitHolder(schema)
        ), LV, MV, HV, EV);
        registerSimpleMachine(schema, map, "gtceu:miner", LV, MV, HV);
        registerForTiers(schema, map, "gtceu:world_accelerator", traitHolder, LV, MV, HV, EV, IV, LuV, ZPM, UV);
        registerForTiers(schema, map, "gtceu:item_collector", () -> optionalFields(
                "output", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema),
                "filterInventory", itemHandler(schema),
                traitHolder(schema)
        ), LV, MV, HV, EV);
        // endregion

        // region storage
        final Supplier<TypeTemplate> itemStorage = () -> optionalFields(
                "inventory", notifiableItemHandler(schema),
                traitHolder(schema)
        );
        registerForTiers(schema, map, "gtceu:buffer", () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                itemStorage.get()
        ), LV, MV, HV);
        schema.register(map, "gtceu:creative_energy", traitHolder);
        schema.register(map, "gtceu:creative_computation_provider", traitHolder);

        final Supplier<TypeTemplate> quantumChest = () -> optionalFields(
                "lockedFluid", GTReferences.FLUID_STACK.in(schema),
                "stored", GTReferences.FLUID_STACK.in(schema),
                traitHolder(schema)
        );
        final Supplier<TypeTemplate> quantumTank = () -> optionalFields(
                "lockedItem", itemHandler(schema),
                "stored", References.ITEM_STACK.in(schema),
                traitHolder(schema)
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
                "stored", GTReferences.FLUID_STACK.in(schema),
                traitHolder(schema)
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
                "circuitSlot", notifiableItemHandler(schema),
                itemStorage.get()
        );
        registerForTiers(schema, map, "gtceu:input_bus", itemBus, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:output_bus", itemBus, ALL_TIERS);
        final Supplier<TypeTemplate> fluidHatch = () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                "circuitSlot", notifiableItemHandler(schema),
                traitHolder(schema)
        );
        registerForTiers(schema, map, "gtceu:input_hatch", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:input_hatch_4x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:input_hatch_9x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:output_hatch", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:output_hatch_4x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:output_hatch_9x", fluidHatch, ALL_TIERS);

        registerForTiers(schema, map, "gtceu:energy_input_hatch", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_output_hatch", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_input_hatch_4a", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_output_hatch_4a", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_input_hatch_16a", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:energy_output_hatch_16a", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:substation_input_hatch_64a", traitHolder, ALL_TIERS);
        registerForTiers(schema, map, "gtceu:substation_output_hatch_64a", traitHolder, ALL_TIERS);

        registerForTiers(schema, map, "gtceu:muffler_hatch", itemStorage, ELECTRIC_TIERS);

        schema.register(map, "gtceu:steam_input_bus", itemBus);
        schema.register(map, "gtceu:steam_output_bus", itemBus);
        schema.register(map, "gtceu:steam_input_hatch", fluidHatch);
        schema.register(map, "gtceu:coke_oven_hatch", traitHolder);
        schema.register(map, "gtceu:pump_hatch", fluidHatch);
        final Supplier<TypeTemplate> maintenanceHatch = () -> optionalFields(
                "itemStackHandler", notifiableItemHandler(schema),
                traitHolder(schema)
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

        registerForTiers(schema, map, "gtceu:diode", traitHolder, ELECTRIC_TIERS);
        registerForTiers(schema, map, "gtceu:rotor_holder", () -> optionalFields(
                "rotorMaterial", GTReferences.MATERIAL_NAME.in(schema),
                itemStorage.get()
        ), GTValues.tiersBetween(HV, GTCEuAPI.isHighTier() ? OpV : UV));

        registerForTiers(schema, map, "gtceu:256a_laser_target_hatch", traitHolder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:256a_laser_source_hatch", traitHolder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:1024a_laser_target_hatch", traitHolder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:1024a_laser_source_hatch", traitHolder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:4096a_laser_target_hatch", traitHolder, HIGH_TIERS);
        registerForTiers(schema, map, "gtceu:4096a_laser_source_hatch", traitHolder, HIGH_TIERS);

        schema.register(map, "gtceu:monitor", traitHolder);
        schema.register(map, "gtceu:advanced_monitor", traitHolder);
        // endregion

        // region multiblock
        schema.register(map, "gtceu:bronze_large_boiler", traitHolder);
        schema.register(map, "gtceu:steel_large_boiler", traitHolder);
        schema.register(map, "gtceu:titanium_large_boiler", traitHolder);
        schema.register(map, "gtceu:tungstensteel_large_boiler", traitHolder);

        final Supplier<TypeTemplate> primitiveMachine = () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "importFluids", notifiableFluidTank(schema),
                "exportFluids", notifiableFluidTank(schema),
                traitHolder(schema)
        );
        schema.register(map, "gtceu:coke_oven", primitiveMachine);
        schema.register(map, "gtceu:primitive_blast_furnace", primitiveMachine);

        schema.register(map, "gtceu:electric_blast_furnace", traitHolder);
        schema.register(map, "gtceu:large_chemical_reactor", traitHolder);
        schema.register(map, "gtceu:implosion_compressor", traitHolder);
        schema.register(map, "gtceu:pyrolyse_oven", traitHolder);
        schema.register(map, "gtceu:multi_smelter", traitHolder);
        schema.register(map, "gtceu:cracker", traitHolder);
        schema.register(map, "gtceu:distillation_tower", traitHolder);
        schema.register(map, "gtceu:vacuum_freezer", traitHolder);
        schema.register(map, "gtceu:assembly_line", traitHolder);
        schema.register(map, "gtceu:primitive_pump", traitHolder);
        schema.register(map, "gtceu:steam_grinder", traitHolder);
        schema.register(map, "gtceu:steam_oven", traitHolder);
        registerForTiers(schema, map, "gtceu:fusion_reactor", traitHolder, LuV, ZPM, UV);
        registerForTiers(schema, map, "gtceu:fluid_drilling_rig", traitHolder, MV, HV, EV);
        registerForTiers(schema, map, "gtceu:large_miner", traitHolder, EV, IV, LuV);
        schema.register(map, "gtceu:cleanroom", traitHolder);
        schema.register(map, "gtceu:large_combustion_engine", traitHolder);
        schema.register(map, "gtceu:extreme_combustion_engine", traitHolder);
        schema.register(map, "gtceu:steam_large_turbine", traitHolder);
        schema.register(map, "gtceu:gas_large_turbine", traitHolder);
        schema.register(map, "gtceu:plasma_large_turbine", traitHolder);
        schema.register(map, "gtceu:active_transformer", traitHolder);
        schema.register(map, "gtceu:power_substation", traitHolder);
        registerForTiers(schema, map, "gtceu:bedrock_ore_miner", traitHolder, MV, HV, EV);
        schema.register(map, "gtceu:wooden_tank_valve", traitHolder);
        schema.register(map, "gtceu:wooden_multiblock_tank", traitHolder);
        schema.register(map, "gtceu:bronze_tank_valve", traitHolder);
        schema.register(map, "gtceu:bronze_multiblock_tank", traitHolder);
        schema.register(map, "gtceu:steel_tank_valve", traitHolder);
        schema.register(map, "gtceu:steel_multiblock_tank", traitHolder);

        schema.register(map, "gtceu:central_monitor", traitHolder);

        // region GCYM
        registerForTiers(schema, map, "gtceu:parallel_hatch", traitHolder, IV, LuV, ZPM, UV);
        schema.register(map, "gtceu:large_maceration_tower", traitHolder);
        schema.register(map, "gtceu:large_chemical_bath", traitHolder);
        schema.register(map, "gtceu:large_centrifuge", traitHolder);
        schema.register(map, "gtceu:large_mixer", traitHolder);
        schema.register(map, "gtceu:large_electrolyzer", traitHolder);
        schema.register(map, "gtceu:large_electromagnet", traitHolder);
        schema.register(map, "gtceu:large_packer", traitHolder);
        schema.register(map, "gtceu:large_assembler", traitHolder);
        schema.register(map, "gtceu:large_circuit_assembler", traitHolder);
        schema.register(map, "gtceu:large_arc_smelter", traitHolder);
        schema.register(map, "gtceu:large_engraving_laser", traitHolder);
        schema.register(map, "gtceu:large_sifting_funnel", traitHolder);
        schema.register(map, "gtceu:alloy_blast_smelter", traitHolder);
        schema.register(map, "gtceu:large_autoclave", traitHolder);
        schema.register(map, "gtceu:large_material_press", traitHolder);
        schema.register(map, "gtceu:large_brewer", traitHolder);
        schema.register(map, "gtceu:large_cutter", traitHolder);
        schema.register(map, "gtceu:large_extractor", traitHolder);
        schema.register(map, "gtceu:large_extruder", traitHolder);
        schema.register(map, "gtceu:large_solidifier", traitHolder);
        schema.register(map, "gtceu:large_wiremill", traitHolder);
        schema.register(map, "gtceu:mega_blast_furnace", traitHolder);
        schema.register(map, "gtceu:mega_vacuum_freezer", traitHolder);
        // endregion
        // endregion

        // region research
        schema.register(map, "gtceu:research_station", traitHolder);
        schema.register(map, "gtceu:object_holder", () -> optionalFields(
                "heldItems", notifiableItemHandler(schema),
                traitHolder(schema)
        ));
        schema.register(map, "gtceu:data_bank", traitHolder);
        schema.register(map, "gtceu:network_switch", traitHolder);
        schema.register(map, "gtceu:high_performance_computation_array", traitHolder);
        schema.register(map, "gtceu:computation_transmitter_hatch", traitHolder);
        schema.register(map, "gtceu:computation_receiver_hatch", traitHolder);
        schema.register(map, "gtceu:data_transmitter_hatch", traitHolder);
        schema.register(map, "gtceu:data_receiver_hatch", traitHolder);
        final Supplier<TypeTemplate> dataAccessHatch = () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                traitHolder(schema)
        );
        schema.register(map, "gtceu:basic_data_access_hatch", dataAccessHatch);
        schema.register(map, "gtceu:data_access_hatch", dataAccessHatch);
        schema.register(map, "gtceu:advanced_data_access_hatch", dataAccessHatch);
        schema.register(map, "gtceu:creative_data_access_hatch", dataAccessHatch);
        schema.register(map, "gtceu:hpca_empty_component", traitHolder);
        schema.register(map, "gtceu:hpca_computation_component", traitHolder);
        schema.register(map, "gtceu:hpca_advanced_computation_component", traitHolder);
        schema.register(map, "gtceu:hpca_heat_sink_component", traitHolder);
        schema.register(map, "gtceu:hpca_active_cooler_component", traitHolder);
        schema.register(map, "gtceu:hpca_bridge_component", traitHolder);
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
            schema.register(map, "gtceu:me_pattern_buffer_proxy", traitHolder);
        }
        // endregion

        return map;
    }

    protected static void registerSimpleMachine(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name, int... tiers) {
        registerForTiers(schema, map, name, () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "importFluids", notifiableFluidTank(schema),
                "exportFluids", notifiableFluidTank(schema),
                traitHolder(schema)
        ), tiers);
    }

    protected static TypeTemplate traitHolder(Schema schema) {
        return optionalFields(
                "traitHolder", optionalFields(
                        // TODO I'm not sure if this is all of the item/fluid fields? - add missing ones if not
                        "batterySlot", notifiableItemHandler(schema),
                        or(
                                optionalFields("circuit", notifiableItemHandler(schema)),
                                optionalFields("circuitSlot", notifiableItemHandler(schema))
                        )
                )
        );
    }
    // spotless:on
}
