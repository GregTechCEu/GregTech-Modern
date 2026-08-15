package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateItemTagsProvider;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class ItemTagLoader {

    @SuppressWarnings({ "DataFlowIssue", "unchecked" })
    public static void init(RegistrateItemTagsProvider provider) {
        provider.addTag(GTTags.Items.DOUGHS).addTag(GTTags.Items.DOUGHS_WHEAT);
        provider.addTag(GTTags.Items.GRAINS_WHEAT).addOptional(GTMaterialItems.MATERIAL_ITEMS.get(dust, Wheat));
        provider.addTag(GTTags.Items.GRAINS).addTag(GTTags.Items.GRAINS_WHEAT);

        provider.copy(GTTags.Blocks.CONCRETES, GTTags.Items.CONCRETES);
        provider.copy(GTTags.Blocks.CONCRETE_POWDERS, GTTags.Items.CONCRETE_POWDERS);

        // spotless:off
        // the coral blocks: alive, dead, both
        provider.addTag(GTTags.Items.CORAL_BLOCKS_ALIVE)
                .add(Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK, Items.TUBE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK);
        provider.addTag(GTTags.Items.CORAL_BLOCKS_DEAD)
                .add(Items.DEAD_BRAIN_CORAL_BLOCK, Items.DEAD_BUBBLE_CORAL_BLOCK, Items.DEAD_FIRE_CORAL_BLOCK, Items.DEAD_TUBE_CORAL_BLOCK, Items.DEAD_HORN_CORAL_BLOCK);
        provider.addTag(GTTags.Items.CORAL_BLOCKS)
                .addTag(GTTags.Items.CORAL_BLOCKS_ALIVE)
                .addTag(GTTags.Items.CORAL_BLOCKS_DEAD);

        // the coral plants (the V-shaped pointy ones)
        provider.addTag(GTTags.Items.CORAL_PLANTS_ALIVE)
                        .add(Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL, Items.TUBE_CORAL, Items.HORN_CORAL);
        provider.addTag(GTTags.Items.CORAL_PLANTS_DEAD)
                .add(Items.DEAD_BRAIN_CORAL, Items.DEAD_BUBBLE_CORAL, Items.DEAD_FIRE_CORAL, Items.DEAD_TUBE_CORAL, Items.DEAD_HORN_CORAL);
        provider.addTag(GTTags.Items.CORAL_PLANTS)
                .addTag(GTTags.Items.CORAL_PLANTS_ALIVE)
                .addTag(GTTags.Items.CORAL_PLANTS_DEAD);

        // the coral fans (the flat circular ones)
        provider.addTag(GTTags.Items.CORAL_FANS_ALIVE)
                .add(Items.BRAIN_CORAL_FAN, Items.BUBBLE_CORAL_FAN, Items.FIRE_CORAL_FAN, Items.TUBE_CORAL_FAN, Items.HORN_CORAL_FAN);
        provider.addTag(GTTags.Items.CORAL_FANS_DEAD)
                .add(Items.DEAD_BRAIN_CORAL_FAN, Items.DEAD_BUBBLE_CORAL_FAN, Items.DEAD_FIRE_CORAL_FAN, Items.DEAD_TUBE_CORAL_FAN, Items.DEAD_HORN_CORAL_FAN);
        provider.addTag(GTTags.Items.CORAL_FANS)
                .addTag(GTTags.Items.CORAL_FANS_ALIVE)
                .addTag(GTTags.Items.CORAL_FANS_DEAD);

        // all the coral items (except the blocks)
        provider.addTag(GTTags.Items.CORALS_ALIVE)
                .addTag(GTTags.Items.CORAL_PLANTS_ALIVE)
                .addTag(GTTags.Items.CORAL_FANS_ALIVE);
        provider.addTag(GTTags.Items.CORALS_DEAD)
                .addTag(GTTags.Items.CORAL_PLANTS_DEAD)
                .addTag(GTTags.Items.CORAL_FANS_DEAD);
        provider.addTag(GTTags.Items.CORALS)
                .addTag(GTTags.Items.CORALS_ALIVE)
                .addTag(GTTags.Items.CORALS_DEAD);
        // spotless:on

        provider.addTag(GTTags.Items.LENSES).addTags(GTTags.Items.LENSES_BY_COLOR.values())
                .addTag(GTTags.Items.LENSES_GLASS);

        provider.addTag(GTTags.Items.LENSES_WHITE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Glass))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, NetherStar));
        provider.addTag(GTTags.Items.LENSES_LIGHT_BLUE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Diamond));
        provider.addTag(GTTags.Items.LENSES_RED)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Ruby));
        provider.addTag(GTTags.Items.LENSES_GREEN)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Emerald));
        provider.addTag(GTTags.Items.LENSES_BLUE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Sapphire));
        provider.addTag(GTTags.Items.LENSES_PURPLE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Amethyst));

        provider.addTag(GTTags.Items.PISTONS).add(Items.PISTON, Items.STICKY_PISTON);

        // add treated wood stick to vanilla sticks tag
        // noinspection DataFlowIssue ChemicalHelper#getTag can't return null with treated wood rod
        provider.addTag(Tags.Items.RODS_WOODEN)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.rod, TreatedWood));

        // add treated and untreated wood plates to vanilla planks tag
        provider.addTag(ItemTags.PLANKS)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(plate, TreatedWood))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(plate, Wood));

        // add (likely empty) tags for the high tier circuits so we can add them to the `gtceu:circuits` tag easily
        provider.addTag(GTTags.Items.CIRCUITS_UEV);
        provider.addTag(GTTags.Items.CIRCUITS_UIV);
        provider.addTag(GTTags.Items.CIRCUITS_UXV);
        provider.addTag(GTTags.Items.CIRCUITS_OpV);
        provider.addTag(GTTags.Items.CIRCUITS_MAX);
        provider.addTag(GTTags.Items.CIRCUITS).addTags(GTTags.Items.CIRCUITS_ARRAY);

        provider.addTag(GTTags.Items.BATTERIES).addTags(GTTags.Items.BATTERIES_ARRAY);

        // Add highTierContent items as optional entries so it doesn't error
        provider.addTag(GTTags.Items.ELECTRIC_MOTORS)
                .addOptional(GTItems.ELECTRIC_MOTOR_UHV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UEV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UIV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UXV)
                .addOptional(GTItems.ELECTRIC_MOTOR_OpV);

        provider.addTag(GTTags.Items.ELECTRIC_PUMPS)
                .addOptional(GTItems.ELECTRIC_PUMP_UHV)
                .addOptional(GTItems.ELECTRIC_PUMP_UEV)
                .addOptional(GTItems.ELECTRIC_PUMP_UIV)
                .addOptional(GTItems.ELECTRIC_PUMP_UXV)
                .addOptional(GTItems.ELECTRIC_PUMP_OpV);

        provider.addTag(GTTags.Items.FLUID_REGULATORS)
                .addOptional(GTItems.FLUID_REGULATOR_UHV)
                .addOptional(GTItems.FLUID_REGULATOR_UEV)
                .addOptional(GTItems.FLUID_REGULATOR_UIV)
                .addOptional(GTItems.FLUID_REGULATOR_UXV)
                .addOptional(GTItems.FLUID_REGULATOR_OpV);

        provider.addTag(GTTags.Items.CONVEYOR_MODULES)
                .addOptional(GTItems.CONVEYOR_MODULE_UHV)
                .addOptional(GTItems.CONVEYOR_MODULE_UEV)
                .addOptional(GTItems.CONVEYOR_MODULE_UIV)
                .addOptional(GTItems.CONVEYOR_MODULE_UXV)
                .addOptional(GTItems.CONVEYOR_MODULE_OpV);

        provider.addTag(GTTags.Items.ELECTRIC_PISTONS)
                .addOptional(GTItems.ELECTRIC_PISTON_UHV)
                .addOptional(GTItems.ELECTRIC_PISTON_UEV)
                .addOptional(GTItems.ELECTRIC_PISTON_UIV)
                .addOptional(GTItems.ELECTRIC_PISTON_UXV)
                .addOptional(GTItems.ELECTRIC_PISTON_OpV);

        provider.addTag(GTTags.Items.ROBOT_ARMS)
                .addOptional(GTItems.ROBOT_ARM_UHV)
                .addOptional(GTItems.ROBOT_ARM_UEV)
                .addOptional(GTItems.ROBOT_ARM_UIV)
                .addOptional(GTItems.ROBOT_ARM_UXV)
                .addOptional(GTItems.ROBOT_ARM_OpV);

        provider.addTag(GTTags.Items.FIELD_GENERATORS)
                .addOptional(GTItems.FIELD_GENERATOR_UHV)
                .addOptional(GTItems.FIELD_GENERATOR_UEV)
                .addOptional(GTItems.FIELD_GENERATOR_UIV)
                .addOptional(GTItems.FIELD_GENERATOR_UXV)
                .addOptional(GTItems.FIELD_GENERATOR_OpV);

        provider.addTag(GTTags.Items.EMITTERS)
                .addOptional(GTItems.EMITTER_UHV)
                .addOptional(GTItems.EMITTER_UEV)
                .addOptional(GTItems.EMITTER_UIV)
                .addOptional(GTItems.EMITTER_UXV)
                .addOptional(GTItems.EMITTER_OpV);

        provider.addTag(GTTags.Items.SENSORS)
                .addOptional(GTItems.SENSOR_UHV)
                .addOptional(GTItems.SENSOR_UEV)
                .addOptional(GTItems.SENSOR_UIV)
                .addOptional(GTItems.SENSOR_UXV)
                .addOptional(GTItems.SENSOR_OpV);

        provider.addTag(GTTags.Items.TOOLS_IGNITER)
                .addTag(ItemTags.CREEPER_IGNITERS);
        // add these as "empty" tags so we can add them to `#forge:tools` easily
        provider.addTag(GTTags.Items.TOOLS_BUTCHERY_KNIVES);
        provider.addTag(GTTags.Items.TOOLS_BUZZSAWS);
        provider.addTag(GTTags.Items.TOOLS_CHAINSAWS);
        provider.addTag(GTTags.Items.TOOLS_CROWBARS);
        provider.addTag(GTTags.Items.TOOLS_DRILLS);
        provider.addTag(GTTags.Items.TOOLS_FILES);
        provider.addTag(GTTags.Items.TOOLS_HAMMERS);
        provider.addTag(GTTags.Items.TOOLS_KNIVES);
        provider.addTag(GTTags.Items.TOOLS_MALLETS);
        provider.addTag(GTTags.Items.TOOLS_MINING_HAMMERS);
        provider.addTag(GTTags.Items.TOOLS_MORTARS);
        provider.addTag(GTTags.Items.TOOLS_PLUNGERS);
        provider.addTag(GTTags.Items.TOOLS_SAWS);
        provider.addTag(GTTags.Items.TOOLS_SCREWDRIVERS);
        provider.addTag(GTTags.Items.TOOLS_SCYTHES);
        provider.addTag(GTTags.Items.TOOLS_SHEARS).addTag(Tags.Items.SHEARS);
        provider.addTag(GTTags.Items.TOOLS_SPADES);
        provider.addTag(GTTags.Items.TOOLS_WIRE_CUTTERS);
        provider.addTag(GTTags.Items.TOOLS_WRENCHES);
        // mod compat
        provider.addTag(GTTags.Items.TOOLS_WRENCH).addTag(GTTags.Items.TOOLS_WRENCHES);
        provider.addTag(Tags.Items.TOOLS)
                .addTags(GTTags.Items.TOOLS_BUTCHERY_KNIVES, GTTags.Items.TOOLS_BUZZSAWS, GTTags.Items.TOOLS_CHAINSAWS,
                        GTTags.Items.TOOLS_CROWBARS, GTTags.Items.TOOLS_DRILLS, GTTags.Items.TOOLS_FILES,
                        GTTags.Items.TOOLS_HAMMERS, GTTags.Items.TOOLS_KNIVES, GTTags.Items.TOOLS_MALLETS,
                        GTTags.Items.TOOLS_MINING_HAMMERS, GTTags.Items.TOOLS_MORTARS, GTTags.Items.TOOLS_PLUNGERS,
                        GTTags.Items.TOOLS_SAWS, GTTags.Items.TOOLS_SCREWDRIVERS, GTTags.Items.TOOLS_SCYTHES,
                        GTTags.Items.TOOLS_SHEARS, GTTags.Items.TOOLS_SPADES, GTTags.Items.TOOLS_WIRE_CUTTERS,
                        GTTags.Items.TOOLS_WRENCHES);

        // Add sodalite and lazurite as enchanting fuels
        provider.addTag(Tags.Items.ENCHANTING_FUELS)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(gem, Lazurite))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(gem, Sodalite));
    }
}
