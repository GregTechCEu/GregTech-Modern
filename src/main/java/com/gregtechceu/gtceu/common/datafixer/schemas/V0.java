package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTCovers;
import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;

import net.minecraft.util.datafix.fixes.References;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
public class V0 extends BaseGTSchema {

    public V0(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes,
                              Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
    }

    // Register schemas for all covers.
    public Map<String, Supplier<TypeTemplate>> registerCoverDefinitions(final Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerCoverDefinitions(schema);

        schema.register(map, "gtceu:facade", () -> DSL.field("facadeState", References.BLOCK_STATE.in(schema)));
        schema.register(map, "gtceu:item_filter", () -> DSL.field("filterMode", DSL.remainder()));
        schema.register(map, "gtceu:fluid_filter", () -> DSL.remainder());
        schema.register(map, "gtceu:infinite_water", () -> DSL.remainder());
        schema.register(map, "gtceu:shutter", () -> DSL.field("workingEnabled", DSL.bool().template()));
        schema.register(map, "gtceu:machine_controller", () -> DSL.fields(
                "isInverted", DSL.bool().template(),
                "minRedstoneStrength", DSL.intType().template(),
                "controllerMode", DSL.string().template()));
        for (CoverDefinition cover : GTCovers.CONVEYORS) {
            schema.register(map, cover.getId().toString(), () -> DSL.and(
                    DSL.fields(
                            "transferRate", DSL.intType().template(),
                            "distributionMode", DSL.string().template(),
                            "io", DSL.string().template()),
                    DSL.fields(
                            "manualIOMode", DSL.string().template(),
                            "isWorkingEnabled", DSL.bool().template(),
                            "filterHandler", DSL.field(
                                    "filterItem", References.ITEM_STACK.in(schema)))));
        }
        for (CoverDefinition cover : GTCovers.ROBOT_ARMS) {
            schema.register(map, cover.getId().toString(), () -> DSL.and(
                    DSL.fields(
                            "transferRate", DSL.intType().template(),
                            "distributionMode", DSL.string().template(),
                            "io", DSL.string().template()),
                    DSL.fields(
                            "manualIOMode", DSL.string().template(),
                            "isWorkingEnabled", DSL.bool().template(),
                            "transferMode", DSL.string().template()),
                    DSL.fields(
                            "globalTransferLimit", DSL.intType().template(),
                            "filterHandler", DSL.field(
                                    "filterItem", References.ITEM_STACK.in(schema)))));
        }
        for (CoverDefinition cover : GTCovers.PUMPS) {
            schema.register(map, cover.getId().toString(), () -> DSL.and(
                    DSL.fields(
                            "currentMilliBucketsPerTick", DSL.longType().template(),
                            "distributionMode", DSL.string().template(),
                            "io", DSL.string().template()),
                    DSL.fields(
                            "manualIOMode", DSL.string().template(),
                            "isWorkingEnabled", DSL.bool().template(),
                            "bucketMode", DSL.string().template()),
                    DSL.fields(
                            "globalTransferLimit", DSL.intType().template(),
                            "filterHandler", DSL.field(
                                    "filterItem", References.ITEM_STACK.in(schema)))));
        }
        for (CoverDefinition cover : GTCovers.FLUID_REGULATORS) {
            schema.register(map, cover.getId().toString(), () -> DSL.and(
                    DSL.fields(
                            "currentMilliBucketsPerTick", DSL.longType().template(),
                            "distributionMode", DSL.string().template(),
                            "io", DSL.string().template()),
                    DSL.fields(
                            "manualIOMode", DSL.string().template(),
                            "isWorkingEnabled", DSL.bool().template(),
                            "bucketMode", DSL.string().template()),
                    DSL.fields("globalTransferLimit", DSL.intType().template(),
                            "transferMode", DSL.string().template(),
                            "globalTransferSizeMillibuckets", DSL.longType().template()),
                    DSL.fields(
                            "filterHandler", DSL.field(
                                    "filterItem", References.ITEM_STACK.in(schema)))));
        }
        schema.register(map, "gtceu:item_voiding", () -> DSL.and(
                DSL.fields(
                        "transferRate", DSL.intType().template(),
                        "distributionMode", DSL.string().template(),
                        "io", DSL.string().template()),
                DSL.fields(
                        "manualIOMode", DSL.string().template(),
                        "isWorkingEnabled", DSL.bool().template(),
                        "filterHandler", DSL.field(
                                "filterItem", References.ITEM_STACK.in(schema))),
                DSL.fields("isEnabled", DSL.bool().template())));
        schema.register(map, "gtceu:item_voiding_advanced", () -> DSL.and(
                DSL.fields(
                        "transferRate", DSL.intType().template(),
                        "distributionMode", DSL.string().template(),
                        "io", DSL.string().template()),
                DSL.fields(
                        "manualIOMode", DSL.string().template(),
                        "isWorkingEnabled", DSL.bool().template(),
                        "filterHandler", DSL.field(
                                "filterItem", References.ITEM_STACK.in(schema))),
                DSL.fields(
                        "isEnabled", DSL.bool().template(),
                        "voidingMode", DSL.string().template(),
                        "globalVoidingLimit", DSL.intType().template())));
        schema.register(map, "gtceu:fluid_voiding", () -> DSL.and(
                DSL.fields(
                        "currentMilliBucketsPerTick", DSL.longType().template(),
                        "distributionMode", DSL.string().template(),
                        "io", DSL.string().template()),
                DSL.fields(
                        "manualIOMode", DSL.string().template(),
                        "isWorkingEnabled", DSL.bool().template(),
                        "bucketMode", DSL.string().template()),
                DSL.fields(
                        "globalTransferLimit", DSL.intType().template(),
                        "filterHandler", DSL.field(
                                "filterItem", References.ITEM_STACK.in(schema))),
                DSL.fields("isEnabled", DSL.bool().template())));
        schema.register(map, "gtceu:fluid_voiding_advanced", () -> DSL.and(
                DSL.fields(
                        "currentMilliBucketsPerTick", DSL.longType().template(),
                        "distributionMode", DSL.string().template(),
                        "io", DSL.string().template()),
                DSL.fields(
                        "manualIOMode", DSL.string().template(),
                        "isWorkingEnabled", DSL.bool().template(),
                        "bucketMode", DSL.string().template()),
                DSL.fields(
                        "globalTransferLimit", DSL.intType().template(),
                        "filterHandler", DSL.field(
                                "filterItem", References.ITEM_STACK.in(schema))),
                DSL.fields(
                        "isEnabled", DSL.bool().template(),
                        "voidingMode", DSL.string().template(),
                        "globalVoidingLimit", DSL.intType().template())));
        schema.register(map, "gtceu:activity_detector", () -> DSL.fields(
                "isWorkingEnabled", DSL.bool().template(),
                "isInverted", DSL.bool().template()));
        schema.register(map, "gtceu:activity_detector_advanced", () -> DSL.fields(
                "isWorkingEnabled", DSL.bool().template(),
                "isInverted", DSL.bool().template()));
        schema.register(map, "gtceu:fluid_detector", () -> DSL.fields(
                "isWorkingEnabled", DSL.bool().template(),
                "isInverted", DSL.bool().template()));
        schema.register(map, "gtceu:fluid_detector_advanced", () -> DSL.and(
                DSL.fields(
                        "isWorkingEnabled", DSL.bool().template(),
                        "isInverted", DSL.bool().template(),
                        "minValue", DSL.longType().template()),
                DSL.fields(
                        "maxValue", DSL.longType().template(),
                        "filterHandler", DSL.field(
                                "filterItem", References.ITEM_STACK.in(schema)))));
        schema.register(map, "gtceu:item_detector", () -> DSL.fields(
                "isWorkingEnabled", DSL.bool().template(),
                "isInverted", DSL.bool().template()));
        schema.register(map, "gtceu:item_detector_advanced", () -> DSL.and(
                DSL.fields(
                        "isWorkingEnabled", DSL.bool().template(),
                        "isInverted", DSL.bool().template(),
                        "minValue", DSL.intType().template()),
                DSL.fields(
                        "maxValue", DSL.intType().template(),
                        "filterHandler", DSL.field(
                                "filterItem", References.ITEM_STACK.in(schema)))));
        schema.register(map, "gtceu:energy_detector", () -> DSL.remainder());
        schema.register(map, "gtceu:energy_detector_advanced", () -> DSL.and(
                DSL.fields(
                        "isWorkingEnabled", DSL.bool().template(),
                        "isInverted", DSL.bool().template(),
                        "minValue", DSL.longType().template()),
                DSL.fields(
                        "maxValue", DSL.longType().template(),
                        "outputAmount", DSL.intType().template(),
                        "usePercent", DSL.bool().template())));
        schema.register(map, "gtceu:maintenance_detector", () -> DSL.remainder());
        for (CoverDefinition cover : GTCovers.SOLAR_PANEL) {
            schema.register(map, cover.getId().toString(), () -> DSL.remainder());
        }
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            registerInventory(schema, map, definition.getId().toString());
        }
        Supplier<TypeTemplate> cover = () -> DSL.fields(
                "side", DSL.intType().template(),
                GTReferences.COVER.in(schema));
        Supplier<TypeTemplate> covers = () -> DSL.and(
                DSL.fields(
                        "up", cover.get(),
                        "down", cover.get(),
                        "north", cover.get()),
                DSL.fields(
                        "south", cover.get(),
                        "west", cover.get(),
                        "east", cover.get()));
        final Supplier<TypeTemplate> pipe = () -> DSL.and(
                DSL.fields(
                        "connections", DSL.intType().template(),
                        "blockedConnections", DSL.intType().template(),
                        "cover", covers.get()
                ),
                DSL.fields(
                        "paintingColor", DSL.intType().template(),
                        "frameMaterial", GTReferences.MATERIAL_NAME.in(schema)
                ));

        schema.register(map, "gtceu:cable", () -> DSL.fields(
                "temperature", DSL.intType().template(),
                pipe.get()));
        schema.register(map, "gtceu:fluid_pipe", pipe);
        schema.register(map, "gtceu:item_pipe", pipe);
        schema.register(map, "gtceu:laser_pipe", () -> DSL.fields(
                "active", DSL.bool().template(),
                pipe.get()));
        schema.register(map, "gtceu:optical_pipe", () -> DSL.fields(
                "isActive", DSL.bool().template(),
                pipe.get()));
        schema.register(map, "gtceu:duct_pipe", pipe);
        return map;
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        Supplier<TypeTemplate> cover = () -> DSL.fields(
                "side", DSL.intType().template(),
                GTReferences.COVER.in(schema));
        Supplier<TypeTemplate> covers = () -> DSL.and(
                DSL.fields(
                        "up", cover.get(),
                        "down", cover.get(),
                        "north", cover.get()),
                DSL.fields(
                        "south", cover.get(),
                        "west", cover.get(),
                        "east", cover.get()));

        schema.register(map, name, () -> DSL.or(
                DSL.fields(
                        "importItems",
                        DSL.field("storage",
                                DSL.field("Items",
                                        DSL.list(References.ITEM_STACK.in(schema)))),
                        "exportItems",
                        DSL.field("storage",
                                DSL.field("Items",
                                        DSL.list(References.ITEM_STACK.in(schema)))),
                        DSL.field("cover", covers.get())),
                DSL.or(
                        DSL.fields(
                                "inventory",
                                DSL.field("storage",
                                        DSL.field("Items",
                                                DSL.list(References.ITEM_STACK.in(schema))))),
                        DSL.or(
                                DSL.fields(
                                        "cache",
                                        DSL.field("storage",
                                                DSL.field("Items",
                                                        DSL.list(References.ITEM_STACK.in(schema))))),
                                DSL.remainder()))));
    }
}
