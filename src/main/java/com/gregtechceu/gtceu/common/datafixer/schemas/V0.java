package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;
import com.gregtechceu.gtceu.core.mixins.datafixer.V705Accessor;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ALL_TIERS;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ELECTRIC_TIERS;

public class V0 extends NamespacedSchema {

    public V0(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    // spotless:off
    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes,
                              Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);

        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook(
                DSL.optionalFields(
                        "id", References.ITEM_NAME.in(schema),
                        "tag", DSL.optionalFields(
                                "EntityTag", References.ENTITY_TREE.in(schema),
                                "BlockEntityTag", References.BLOCK_ENTITY.in(schema),
                                "CanDestroy", DSL.list(References.BLOCK_NAME.in(schema)),
                                "CanPlaceOn", DSL.list(References.BLOCK_NAME.in(schema)),
                                "Items", DSL.list(References.ITEM_STACK.in(schema))
                        )
                ),
                V705Accessor.gtceu$getAddNamesHookFunction(), Hook.HookFunction.IDENTITY)
        );

        schema.registerType(false, GTReferences.MATERIAL_NAME, () -> DSL.constType(namespacedString()));

        schema.registerType(true, GTReferences.FLUID_STACK, () -> DSL.hook(
                DSL.optionalFields(
                        "FluidName", GTReferences.FLUID_NAME.in(schema),
                        "Tag", DSL.remainder()
                ),
                Hook.HookFunction.IDENTITY, Hook.HookFunction.IDENTITY)
        );
        schema.registerType(false, GTReferences.FLUID_NAME, () -> DSL.constType(namespacedString()));
    }

    protected static TypeTemplate simpleMachine(Schema schema) {
        return DSL.optionalFields(
                "importItems", notifiableItemHandler(schema),
                "exportItems", notifiableItemHandler(schema),
                "importFluids", notifiableFluidTank(schema),
                "exportFluids", notifiableFluidTank(schema),
                "energyContainer", notifiableEnergyContainer(schema),
                DSL.optionalFields(
                        "chargerInventory", itemHandler(schema),
                        "circuitInventory", notifiableItemHandler(schema)
                )
        );
    }

    protected static void addForTiers(Schema schema, Map<String, Supplier<TypeTemplate>> map,
                                      String name, Supplier<TypeTemplate> template, int... tiers) {
        for (int tier : GTValues.ALL_TIERS) {
            schema.register(map, "gtceu:" + GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, template);
        }
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        addForTiers(schema, map, "machine_hull", DSL::remainder, ALL_TIERS);
        Supplier<TypeTemplate> simpleMachine = () -> V0.simpleMachine(schema);
        addForTiers(schema, map, "electric_furnace", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "alloy_smelter", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "arc_furnace", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "assembler", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "autoclave", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "bender", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "brewery", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "canner", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "centrifuge", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "chemical_bath", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "chemical_reactor", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "compressor", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "cutter", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "distillery", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "electrolyzer", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "electromagnetic_separator", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "extractor", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "extruder", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "fermenter", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "fluid_heater", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "fluid_solidifier", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "forge_hammer", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "forming_press", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "lathe", simpleMachine, ELECTRIC_TIERS);
        // addForTiers(schema, map, "scanner", simpleMachine, ELECTRIC_TIERS); // Skip scanner; it's added later
        addForTiers(schema, map, "mixer", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "ore_washer", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "packer", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "polarizer", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "laser_engraver", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "sifter", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "thermal_centrifuge", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "wiremill", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "circuit_assembler", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "macerator", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "gas_collector", simpleMachine, ELECTRIC_TIERS);
        addForTiers(schema, map, "rock_crusher", simpleMachine, ELECTRIC_TIERS);

        for (MachineDefinition definition : GTRegistries.MACHINES) {
            registerInventory(schema, map, definition.getId().toString());
        }
        return map;
    }

    protected static TypeTemplate itemHandler(Schema schema) {
        return DSL.field("Items", DSL.list(References.ITEM_STACK.in(schema)));
    }

    protected static TypeTemplate notifiableItemHandler(Schema schema) {
        return DSL.field("storage", itemHandler(schema));
    }

    protected static TypeTemplate notifiableFluidTank(Schema schema) {
        return DSL.field("storages", DSL.list(GTReferences.FLUID_STACK.in(schema))));
    }

    protected static TypeTemplate notifiableEnergyContainer(Schema schema) {
        return DSL.fields(
                "energyStored", DSL.constType(DSL.longType())
        );
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> DSL.or(
                DSL.optionalFields(
                        "importItems", notifiableItemHandler(schema),
                        "exportItems", notifiableItemHandler(schema)
                ),
                DSL.or(
                        DSL.optionalFields("inventory", notifiableItemHandler(schema)),
                        DSL.optionalFields("cache", notifiableItemHandler(schema))
                )
        ));
    }
    // spotless:on
}
