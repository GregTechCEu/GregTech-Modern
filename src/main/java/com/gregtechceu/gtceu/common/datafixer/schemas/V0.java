package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ALL_TIERS;
import static com.gregtechceu.gtceu.api.datafixer.types.ExtraDSL.*;
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
                "minecraft:block", optionalFields(
                        "ids", compoundList(References.BLOCK_NAME.in(schema), constType(intType())),
                        "aliases", compoundList(DSL.constType(namespacedString()), References.BLOCK_NAME.in(schema))
                ),
                "minecraft:item", optionalFields(
                        "ids", compoundList(References.ITEM_NAME.in(schema), constType(intType())),
                        "aliases", compoundList(DSL.constType(namespacedString()), References.ITEM_NAME.in(schema))
                ),
                "minecraft:fluid", optionalFields(
                        "ids", compoundList(GTReferences.FLUID_NAME.in(schema), constType(intType())),
                        "aliases", compoundList(DSL.constType(namespacedString()), GTReferences.FLUID_NAME.in(schema))
                ),
                "minecraft:entity_type", optionalFields(
                        "ids", compoundList(References.ENTITY_NAME.in(schema), constType(intType())),
                        "aliases", compoundList(DSL.constType(namespacedString()), References.ENTITY_NAME.in(schema))
                )
        ));


        schema.registerType(false, GTReferences.MATERIAL_NAME, () -> constType(namespacedString()));

        schema.registerType(true, GTReferences.FLUID_STACK, () -> optionalFields(
                "FluidName", GTReferences.FLUID_NAME.in(schema),
                "Tag", remainder()
        ));
        schema.registerType(false, GTReferences.FLUID_NAME, () -> constType(namespacedString()));
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
                "steamTank", notifiableFluidTank(schema)
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

        schema.registerSimple(map, "long_distance_item_pipeline_endpoint");
        schema.registerSimple(map, "long_distance_fluid_pipeline_endpoint");
        schema.registerSimple(map, "long_distance_fluid_pipeline_endpoint");

        final Supplier<TypeTemplate> batteryBuffer = () -> optionalFields(
                "batteryInventory", itemHandler(schema)
        );
        registerForTiers(schema, map, "battery_buffer_4x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "battery_buffer_8x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "battery_buffer_16x", batteryBuffer, ALL_TIERS);
        registerForTiers(schema, map, "charger_4x", batteryBuffer, ALL_TIERS);

        registerForTiers(schema, map, "pump", () -> optionalFields(
                "cache", notifiableFluidTank(schema)
        ), LV, MV, HV, EV);
        registerForTiers(schema, map, "fisher", () -> optionalFields(
                "cache", notifiableItemHandler(schema),
                "baitHandler", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema)
        ), LV, MV, HV, EV, IV, LuV);
        registerForTiers(schema, map, "block_breaker", () -> optionalFields(
                "cache", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema)
        ), LV, MV, HV, EV);
        registerSimpleMachine(schema, map, "miner", LV, MV, HV);
        registerForTiers(schema, map, "world_accelerator", DSL::remainder, LV, MV, HV, EV, IV, LuV, ZPM, UV);
        registerForTiers(schema, map, "item_collector", () -> optionalFields(
                "output", notifiableItemHandler(schema),
                "chargerInventory", itemHandler(schema),
                "filterInventory", itemHandler(schema)
        ), LV, MV, HV, EV);
        // endregion

        // region storage
        final Supplier<TypeTemplate> itemStorage = () -> optionalFields(
                "inventory", notifiableItemHandler(schema)
        );
        registerForTiers(schema, map, "buffer", () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                itemStorage.get()
        ), LV, MV, HV);
        schema.registerSimple(map, "creative_energy");
        schema.registerSimple(map, "creative_computation_provider");

        final Supplier<TypeTemplate> quantumChest = () -> optionalFields(
                "lockedFluid", GTReferences.FLUID_STACK.in(schema),
                "stored", GTReferences.FLUID_STACK.in(schema)
        );
        final Supplier<TypeTemplate> quantumTank = () -> optionalFields(
                "lockedItem", itemHandler(schema),
                "stored", References.ITEM_STACK.in(schema)
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
                "stored", GTReferences.FLUID_STACK.in(schema)
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
                "circuitInventory", notifiableItemHandler(schema),
                itemStorage.get()
        );
        registerForTiers(schema, map, "input_bus", itemBus, ALL_TIERS);
        registerForTiers(schema, map, "output_bus", itemBus, ALL_TIERS);
        final Supplier<TypeTemplate> fluidHatch = () -> optionalFields(
                "tank", notifiableFluidTank(schema),
                "circuitInventory", notifiableItemHandler(schema)
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
        schema.registerSimple(map, "coke_oven_hatch");
        schema.register(map, "pump_hatch", fluidHatch);
        final Supplier<TypeTemplate> maintenanceHatch = () -> optionalFields(
                "itemStackHandler", notifiableItemHandler(schema)
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

        schema.registerSimple(map, "monitor");
        schema.registerSimple(map, "advanced_monitor");
        // endregion

        // region multiblock
        schema.registerSimple(map, "bronze_large_boiler");
        schema.registerSimple(map, "steel_large_boiler");
        schema.registerSimple(map, "titanium_large_boiler");
        schema.registerSimple(map, "tungstensteel_large_boiler");

        final Supplier<TypeTemplate> primitiveMachine = () -> optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "importFluids", notifiableFluidTank(schema),
                "exportFluids", notifiableFluidTank(schema)
        );
        schema.register(map, "coke_oven", primitiveMachine);
        schema.register(map, "primitive_blast_furnace", primitiveMachine);

        schema.registerSimple(map, "electric_blast_furnace");
        schema.registerSimple(map, "large_chemical_reactor");
        schema.registerSimple(map, "implosion_compressor");
        schema.registerSimple(map, "pyrolyse_oven");
        schema.registerSimple(map, "multi_smelter");
        schema.registerSimple(map, "cracker");
        schema.registerSimple(map, "distillation_tower");
        schema.registerSimple(map, "vacuum_freezer");
        schema.registerSimple(map, "assembly_line");
        schema.registerSimple(map, "primitive_pump");
        schema.registerSimple(map, "steam_grinder");
        schema.registerSimple(map, "steam_oven");
        registerForTiers(schema, map, "fusion_reactor", DSL::remainder, LuV, ZPM, UV);
        registerForTiers(schema, map, "fluid_drilling_rig", DSL::remainder, MV, HV, EV);
        registerForTiers(schema, map, "large_miner", DSL::remainder, EV, IV, LuV);
        schema.registerSimple(map, "cleanroom");
        schema.registerSimple(map, "large_combustion_engine");
        schema.registerSimple(map, "extreme_combustion_engine");
        schema.registerSimple(map, "steam_large_turbine");
        schema.registerSimple(map, "gas_large_turbine");
        schema.registerSimple(map, "plasma_large_turbine");
        schema.registerSimple(map, "active_transformer");
        schema.registerSimple(map, "power_substation");
        registerForTiers(schema, map, "bedrock_ore_miner", DSL::remainder, MV, HV, EV);
        schema.registerSimple(map, "wooden_tank_valve");
        schema.registerSimple(map, "wooden_multiblock_tank");
        schema.registerSimple(map, "bronze_tank_valve");
        schema.registerSimple(map, "bronze_multiblock_tank");
        schema.registerSimple(map, "steel_tank_valve");
        schema.registerSimple(map, "steel_multiblock_tank");

        schema.registerSimple(map, "central_monitor");

        // region GCYM
        registerForTiers(schema, map, "parallel_hatch", DSL::remainder, IV, LuV, ZPM, UV);
        schema.registerSimple(map, "large_maceration_tower");
        schema.registerSimple(map, "large_chemical_bath");
        schema.registerSimple(map, "large_centrifuge");
        schema.registerSimple(map, "large_mixer");
        schema.registerSimple(map, "large_electrolyzer");
        schema.registerSimple(map, "large_electromagnet");
        schema.registerSimple(map, "large_packer");
        schema.registerSimple(map, "large_assembler");
        schema.registerSimple(map, "large_circuit_assembler");
        schema.registerSimple(map, "large_arc_smelter");
        schema.registerSimple(map, "large_engraving_laser");
        schema.registerSimple(map, "large_sifting_funnel");
        schema.registerSimple(map, "alloy_blast_smelter");
        schema.registerSimple(map, "large_autoclave");
        schema.registerSimple(map, "large_material_press");
        schema.registerSimple(map, "large_brewer");
        schema.registerSimple(map, "large_cutter");
        schema.registerSimple(map, "large_extractor");
        schema.registerSimple(map, "large_extruder");
        schema.registerSimple(map, "large_solidifier");
        schema.registerSimple(map, "large_wiremill");
        schema.registerSimple(map, "mega_blast_furnace"); // TODO add fixer to rename to "rotary_hearth_furnace"
        schema.registerSimple(map, "mega_vacuum_freezer"); // TODO add fixer to rename to "bulk_blast_chiller"
        // endregion
        // endregion

        // region research
        schema.registerSimple(map, "research_station");
        schema.register(map, "object_holder", () -> optionalFields(
                "heldItems", notifiableItemHandler(schema)
        ));
        schema.registerSimple(map, "data_bank");
        schema.registerSimple(map, "network_switch");
        schema.registerSimple(map, "high_performance_computation_array");
        schema.registerSimple(map, "computation_transmitter_hatch");
        schema.registerSimple(map, "computation_receiver_hatch");
        schema.registerSimple(map, "data_transmitter_hatch");
        schema.registerSimple(map, "data_receiver_hatch");
        final Supplier<TypeTemplate> dataAccessHatch = () -> optionalFields(
                "importItems", notifiableItemHandler(schema)
        );
        schema.register(map, "basic_data_access_hatch", dataAccessHatch);
        schema.register(map, "data_access_hatch", dataAccessHatch);
        schema.register(map, "advanced_data_access_hatch", dataAccessHatch);
        schema.register(map, "creative_data_access_hatch", dataAccessHatch);
        schema.registerSimple(map, "hpca_empty_component");
        schema.registerSimple(map, "hpca_computation_component");
        schema.registerSimple(map, "hpca_advanced_computation_component");
        schema.registerSimple(map, "hpca_heat_sink_component");
        schema.registerSimple(map, "hpca_active_cooler_component");
        schema.registerSimple(map, "hpca_bridge_component");
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
            schema.registerSimple(map, "me_pattern_buffer_proxy");
        }
        // endregion

        // register all remaining machines as very 'plain' types
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            String id = definition.getId().toString();
            if (!map.containsKey(id)) {
                registerSimple(map, id);
            }
        }

        return map;
    }

    @Override
    public void register(final Map<String, Supplier<TypeTemplate>> map, String name, final Supplier<TypeTemplate> template) {
        if (name.indexOf(':') == -1) {
            name = "gtceu:" + name;
        }
        map.put(name, template);
    }

    protected static void registerSimpleMachine(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name, int... tiers) {
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
        for (int tier : tiers) {
            schema.register(map, GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, template);
        }
    }

    protected static void registerSteamMachine(Schema schema, Map<String, Supplier<TypeTemplate>> map,
                                               String name, Supplier<TypeTemplate> template) {
        schema.register(map, "lp_%s".formatted(name), template);
        schema.register(map, "hp_%s".formatted(name), template);
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
