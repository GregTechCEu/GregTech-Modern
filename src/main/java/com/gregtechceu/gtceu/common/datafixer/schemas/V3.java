package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.datafixer.schemas.AutomaticNamespacedSchema;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.References;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.Map;
import java.util.function.Supplier;


import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ALL_TIERS;
import static com.gregtechceu.gtceu.common.datafixer.schemas.V0.*;
import static com.gregtechceu.gtceu.api.datafixer.types.ExtraDSL.*;
import static com.mojang.datafixers.DSL.*;

public class V3 extends AutomaticNamespacedSchema {

    public V3(int versionKey, Schema parent) {
        super(versionKey, parent, GTCEu.MOD_ID);
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
        registerSteamMachine(schema, map, "steam_solid_boiler", () -> optionalFields(
                "fuelHandler", notifiableItemHandler(schema),
                "ashHandler", notifiableItemHandler(schema),
                steamBoiler.get()
        ));
        registerSteamMachine(schema, map, "steam_liquid_boiler", () -> optionalFields(
                "fuelTank", notifiableFluidTank(schema),
                steamBoiler.get()
        ));
        registerSteamMachine(schema, map, "steam_solar_boiler", steamBoiler);
        registerSimpleSteamMachine(schema, map, "extractor");
        registerSimpleSteamMachine(schema, map, "steam_macerator");
        registerSimpleSteamMachine(schema, map, "compressor");
        registerSimpleSteamMachine(schema, map, "forge_hammer");
        registerSimpleSteamMachine(schema, map, "furnace");
        registerSimpleSteamMachine(schema, map, "alloy_smelter");
        registerSimpleSteamMachine(schema, map, "rock_crusher");
        schema.register(map, "steam_miner", () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "steamTank", notifiableFluidTank(schema),
                traitHolder(schema)
        ));
        // endregion

        // region simple
        registerForTiers(schema, map, "machine_hull", DSL::remainder, ALL_TIERS);
        registerSimpleMachine(schema, map, "electric_furnace", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "alloy_smelter", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "arc_furnace", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "assembler", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "autoclave", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "bender", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "brewery", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "canner", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "centrifuge", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "chemical_bath", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "chemical_reactor", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "compressor", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "cutter", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "distillery", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "electrolyzer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "electromagnetic_separator", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "extractor", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "extruder", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "fermenter", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "fluid_heater", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "fluid_solidifier", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "forge_hammer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "forming_press", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "lathe", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "scanner", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "mixer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "ore_washer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "packer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "polarizer", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "laser_engraver", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "sifter", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "thermal_centrifuge", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "wiremill", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "circuit_assembler", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "macerator", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gas_collector", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "rock_crusher", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "air_scrubber", LOW_TIERS);
        registerSimpleMachine(schema, map, "combustion", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "steam_turbine", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gas_turbine", ELECTRIC_TIERS);
        // endregion

        // region electric
        final int[] transformerTiers = GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? OpV : UV);
        registerForTiers(schema, map, "transformer_1a", DSL::remainder, transformerTiers);
        registerForTiers(schema, map, "transformer_2a", DSL::remainder, transformerTiers);
        registerForTiers(schema, map, "transformer_4a", DSL::remainder, transformerTiers);
        registerForTiers(schema, map, "transformer_16a", DSL::remainder, transformerTiers);

        registerForTiers(schema, map, "1a_energy_converter", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "4a_energy_converter", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "8a_energy_converter", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "16a_energy_converter", DSL::remainder, ALL_TIERS);

        schema.register(map, "long_distance_item_pipeline_endpoint", traitHolder);
        schema.register(map, "long_distance_fluid_pipeline_endpoint", traitHolder);
        schema.register(map, "long_distance_fluid_pipeline_endpoint", traitHolder);

        final Supplier<TypeTemplate> batteryBuffer = () -> optionalFields(
                "batteryInventory", itemHandler(schema),
                traitHolder(schema)
        );
        registerForTiers(schema, map, "battery_buffer_4x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "battery_buffer_8x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "battery_buffer_16x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "charger_4x", batteryBuffer, ALL_TIERS);

        registerForTiers(schema, map, "pump", () -> optionalFields(
                "cache", notifiableFluidTank(schema),
                traitHolder(schema)
        ), LV, MV, HV, EV);
        registerForTiers(schema, map, "fisher", () -> optionalFields(
                "cache", notifiableItemHandler(schema),
                "baitHandler", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema),
                traitHolder(schema)
        ), LV, MV, HV, EV, IV, LuV);
        registerForTiers(schema, map, "block_breaker", () -> optionalFields(
                "cache", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema),
                traitHolder(schema)
        ), LV, MV, HV, EV);
        registerSimpleMachine(schema, map, "miner", LV, MV, HV);
        registerForTiers(schema, map, "world_accelerator", DSL::remainder, LV, MV, HV, EV, IV, LuV, ZPM, UV);
        registerForTiers(schema, map, "item_collector", () -> optionalFields(
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
        registerForTiers(schema, map, "buffer", () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                itemStorage.get()
        ), LV, MV, HV);
        schema.register(map, "creative_energy", traitHolder);
        schema.register(map, "creative_computation_provider", traitHolder);

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
        schema.register(map, "creative_chest", quantumChest);
        schema.register(map, "creative_tank", quantumTank);
        registerForTiers(schema, map, "super_chest", quantumChest, LOW_TIERS);
        registerForTiers(schema, map, "quantum_chest", quantumChest, HIGH_TIERS);
        registerForTiers(schema, map, "super_tank", quantumTank, LOW_TIERS);
        registerForTiers(schema, map, "quantum_tank", quantumTank, HIGH_TIERS);

        schema.register(map, "wood_crate", itemStorage);
        schema.register(map, "bronze_crate", itemStorage);
        schema.register(map, "steel_crate", itemStorage);
        schema.register(map, "aluminium_crate", itemStorage);
        schema.register(map, "stainless_steel_crate", itemStorage);
        schema.register(map, "titanium_crate", itemStorage);
        schema.register(map, "tungsten_steel_crate", itemStorage);

        final Supplier<TypeTemplate> drum = () -> optionalFields(
                "stored", GTReferences.FLUID_STACK.in(schema),
                traitHolder(schema)
        );
        schema.register(map, "wood_drum", drum);
        schema.register(map, "bronze_drum", drum);
        schema.register(map, "steel_drum", drum);
        schema.register(map, "aluminium_drum", drum);
        schema.register(map, "stainless_steel_drum", drum);
        schema.register(map, "gold_drum", drum);
        schema.register(map, "titanium_drum", drum);
        schema.register(map, "tungsten_steel_drum", drum);
        // endregion

        // region part
        final Supplier<TypeTemplate> itemBus = () -> optionalFields(
                "circuitSlot", notifiableItemHandler(schema),
                itemStorage.get()
        );
        registerForTiers(schema, map, "input_bus", itemBus, ALL_TIERS);
        registerForTiers(schema, map, "output_bus", itemBus, ALL_TIERS);
        final Supplier<TypeTemplate> fluidHatch = () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                "circuitSlot", notifiableItemHandler(schema),
                traitHolder(schema)
        );
        registerForTiers(schema, map, "input_hatch", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "input_hatch_4x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "input_hatch_9x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "output_hatch", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "output_hatch_4x", fluidHatch, ALL_TIERS);
        registerForTiers(schema, map, "output_hatch_9x", fluidHatch, ALL_TIERS);

        registerForTiers(schema, map, "energy_input_hatch", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "energy_output_hatch", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "energy_input_hatch_4a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "energy_output_hatch_4a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "energy_input_hatch_16a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "energy_output_hatch_16a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "substation_input_hatch_64a", DSL::remainder, ALL_TIERS);
        registerForTiers(schema, map, "substation_output_hatch_64a", DSL::remainder, ALL_TIERS);

        registerForTiers(schema, map, "muffler_hatch", itemStorage, ELECTRIC_TIERS);

        schema.register(map, "steam_input_bus", itemBus);
        schema.register(map, "steam_output_bus", itemBus);
        schema.register(map, "steam_input_hatch", fluidHatch);
        schema.register(map, "coke_oven_hatch", traitHolder);
        schema.register(map, "pump_hatch", fluidHatch);
        final Supplier<TypeTemplate> maintenanceHatch = () -> optionalFields(
                "itemStackHandler", notifiableItemHandler(schema),
                traitHolder(schema)
        );
        schema.register(map, "maintenance_hatch", maintenanceHatch);
        schema.register(map, "configurable_maintenance_hatch", maintenanceHatch);
        schema.register(map, "cleaning_maintenance_hatch", maintenanceHatch);
        schema.register(map, "auto_maintenance_hatch", maintenanceHatch);

        registerForTiers(schema, map, "item_passthrough_hatch", itemBus, ELECTRIC_TIERS);
        registerForTiers(schema, map, "fluid_passthrough_hatch", fluidHatch, ELECTRIC_TIERS);
        schema.register(map, "reservoir_hatch", fluidHatch);
        final Supplier<TypeTemplate> dualHatch = () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                itemBus.get()
        );
        registerForTiers(schema, map, "dual_input_hatch", dualHatch, DUAL_HATCH_TIERS);
        registerForTiers(schema, map, "dual_output_hatch", dualHatch, DUAL_HATCH_TIERS);

        registerForTiers(schema, map, "diode", DSL::remainder, ELECTRIC_TIERS);
        registerForTiers(schema, map, "rotor_holder", () -> optionalFields(
                "rotorMaterial", GTReferences.MATERIAL_NAME.in(schema),
                itemStorage.get()
        ), GTValues.tiersBetween(HV, GTCEuAPI.isHighTier() ? OpV : UV));

        registerForTiers(schema, map, "256a_laser_target_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "256a_laser_source_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "1024a_laser_target_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "1024a_laser_source_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "4096a_laser_target_hatch", DSL::remainder, HIGH_TIERS);
        registerForTiers(schema, map, "4096a_laser_source_hatch", DSL::remainder, HIGH_TIERS);

        schema.register(map, "monitor", traitHolder);
        schema.register(map, "advanced_monitor", traitHolder);
        // endregion

        // region multiblock
        schema.register(map, "bronze_large_boiler", traitHolder);
        schema.register(map, "steel_large_boiler", traitHolder);
        schema.register(map, "titanium_large_boiler", traitHolder);
        schema.register(map, "tungstensteel_large_boiler", traitHolder);

        final Supplier<TypeTemplate> primitiveMachine = () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "importFluids", notifiableFluidTank(schema),
                "exportFluids", notifiableFluidTank(schema),
                traitHolder(schema)
        );
        schema.register(map, "coke_oven", primitiveMachine);
        schema.register(map, "primitive_blast_furnace", primitiveMachine);

        schema.register(map, "electric_blast_furnace", traitHolder);
        schema.register(map, "large_chemical_reactor", traitHolder);
        schema.register(map, "implosion_compressor", traitHolder);
        schema.register(map, "pyrolyse_oven", traitHolder);
        schema.register(map, "multi_smelter", traitHolder);
        schema.register(map, "cracker", traitHolder);
        schema.register(map, "distillation_tower", traitHolder);
        schema.register(map, "vacuum_freezer", traitHolder);
        schema.register(map, "assembly_line", traitHolder);
        schema.register(map, "primitive_pump", traitHolder);
        schema.register(map, "steam_grinder", traitHolder);
        schema.register(map, "steam_oven", traitHolder);
        registerForTiers(schema, map, "fusion_reactor", DSL::remainder, LuV, ZPM, UV);
        registerForTiers(schema, map, "fluid_drilling_rig", DSL::remainder, MV, HV, EV);
        registerForTiers(schema, map, "large_miner", DSL::remainder, EV, IV, LuV);
        schema.register(map, "cleanroom", traitHolder);
        schema.register(map, "large_combustion_engine", traitHolder);
        schema.register(map, "extreme_combustion_engine", traitHolder);
        schema.register(map, "steam_large_turbine", traitHolder);
        schema.register(map, "gas_large_turbine", traitHolder);
        schema.register(map, "plasma_large_turbine", traitHolder);
        schema.register(map, "active_transformer", traitHolder);
        schema.register(map, "power_substation", traitHolder);
        registerForTiers(schema, map, "bedrock_ore_miner", DSL::remainder, MV, HV, EV);
        schema.register(map, "wooden_tank_valve", traitHolder);
        schema.register(map, "wooden_multiblock_tank", traitHolder);
        schema.register(map, "bronze_tank_valve", traitHolder);
        schema.register(map, "bronze_multiblock_tank", traitHolder);
        schema.register(map, "steel_tank_valve", traitHolder);
        schema.register(map, "steel_multiblock_tank", traitHolder);

        schema.register(map, "central_monitor", traitHolder);

        // region GCYM
        registerForTiers(schema, map, "parallel_hatch", DSL::remainder, IV, LuV, ZPM, UV);
        schema.register(map, "large_maceration_tower", traitHolder);
        schema.register(map, "large_chemical_bath", traitHolder);
        schema.register(map, "large_centrifuge", traitHolder);
        schema.register(map, "large_mixer", traitHolder);
        schema.register(map, "large_electrolyzer", traitHolder);
        schema.register(map, "large_electromagnet", traitHolder);
        schema.register(map, "large_packer", traitHolder);
        schema.register(map, "large_assembler", traitHolder);
        schema.register(map, "large_circuit_assembler", traitHolder);
        schema.register(map, "large_arc_smelter", traitHolder);
        schema.register(map, "large_engraving_laser", traitHolder);
        schema.register(map, "large_sifting_funnel", traitHolder);
        schema.register(map, "alloy_blast_smelter", traitHolder);
        schema.register(map, "large_autoclave", traitHolder);
        schema.register(map, "large_material_press", traitHolder);
        schema.register(map, "large_brewer", traitHolder);
        schema.register(map, "large_cutter", traitHolder);
        schema.register(map, "large_extractor", traitHolder);
        schema.register(map, "large_extruder", traitHolder);
        schema.register(map, "large_solidifier", traitHolder);
        schema.register(map, "large_wiremill", traitHolder);
        schema.register(map, "mega_blast_furnace", traitHolder); // TODO add fixer to rename to "rotary_hearth_furnace"
        schema.register(map, "mega_vacuum_freezer", traitHolder); // TODO add fixer to rename to "bulk_blast_chiller"
        // endregion
        // endregion

        // region research
        schema.register(map, "research_station", traitHolder);
        schema.register(map, "object_holder", () -> optionalFields(
                "heldItems", notifiableItemHandler(schema),
                traitHolder(schema)
        ));
        schema.register(map, "data_bank", traitHolder);
        schema.register(map, "network_switch", traitHolder);
        schema.register(map, "high_performance_computation_array", traitHolder);
        schema.register(map, "computation_transmitter_hatch", traitHolder);
        schema.register(map, "computation_receiver_hatch", traitHolder);
        schema.register(map, "data_transmitter_hatch", traitHolder);
        schema.register(map, "data_receiver_hatch", traitHolder);
        final Supplier<TypeTemplate> dataAccessHatch = () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                traitHolder(schema)
        );
        schema.register(map, "basic_data_access_hatch", dataAccessHatch);
        schema.register(map, "data_access_hatch", dataAccessHatch);
        schema.register(map, "advanced_data_access_hatch", dataAccessHatch);
        schema.register(map, "creative_data_access_hatch", dataAccessHatch);
        schema.register(map, "hpca_empty_component", traitHolder);
        schema.register(map, "hpca_computation_component", traitHolder);
        schema.register(map, "hpca_advanced_computation_component", traitHolder);
        schema.register(map, "hpca_heat_sink_component", traitHolder);
        schema.register(map, "hpca_active_cooler_component", traitHolder);
        schema.register(map, "hpca_bridge_component", traitHolder);
        // endregion

        // region AE2 compat
        if (LoadingModList.get().getModFileById(MODID_APPENG) != null) { // only add these if AE2 is loaded
            schema.register(map, "me_input_bus", itemBus);
            schema.register(map, "me_stocking_input_bus", itemBus);
            schema.register(map, "me_output_bus", itemBus);
            schema.register(map, "me_input_hatch", fluidHatch);
            schema.register(map, "me_stocking_input_hatch", fluidHatch);
            schema.register(map, "me_output_hatch", fluidHatch);
            schema.register(map, "me_pattern_buffer", () -> optionalFields(
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
            schema.register(map, "me_pattern_buffer_proxy", traitHolder);
        }
        // endregion

        // register all remaining machines as ""plain"" types
        // IDEK if this works because the registry might not be loaded at this point... oh well.
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            String id = definition.getId().toString();
            if (!map.containsKey(id)) {
                schema.register(map, id, () -> traitHolder(schema));
            }
        }

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
