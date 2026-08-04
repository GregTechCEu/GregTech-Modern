package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateItemTagsProvider;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class ItemTagLoader {

    @SuppressWarnings({ "DataFlowIssue", "unchecked" })
    public static void init(RegistrateItemTagsProvider provider) {
        provider.addTag(CustomTags.Items.DOUGHS).addTag(CustomTags.Items.DOUGHS_WHEAT);
        provider.addTag(CustomTags.Items.GRAINS_WHEAT).addOptional(GTMaterialItems.MATERIAL_ITEMS.get(dust, Wheat));
        provider.addTag(CustomTags.Items.GRAINS).addTag(CustomTags.Items.GRAINS_WHEAT);

        provider.copy(CustomTags.Blocks.CONCRETES, CustomTags.Items.CONCRETES);
        provider.copy(CustomTags.Blocks.CONCRETE_POWDERS, CustomTags.Items.CONCRETE_POWDERS);

        // spotless:off
        // the coral blocks: alive, dead, both
        provider.addTag(CustomTags.Items.CORAL_BLOCKS_ALIVE)
                .add(Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK, Items.TUBE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK);
        provider.addTag(CustomTags.Items.CORAL_BLOCKS_DEAD)
                .add(Items.DEAD_BRAIN_CORAL_BLOCK, Items.DEAD_BUBBLE_CORAL_BLOCK, Items.DEAD_FIRE_CORAL_BLOCK, Items.DEAD_TUBE_CORAL_BLOCK, Items.DEAD_HORN_CORAL_BLOCK);
        provider.addTag(CustomTags.Items.CORAL_BLOCKS)
                .addTag(CustomTags.Items.CORAL_BLOCKS_ALIVE)
                .addTag(CustomTags.Items.CORAL_BLOCKS_DEAD);

        // the coral plants (the V-shaped pointy ones)
        provider.addTag(CustomTags.Items.CORAL_PLANTS_ALIVE)
                        .add(Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL, Items.TUBE_CORAL, Items.HORN_CORAL);
        provider.addTag(CustomTags.Items.CORAL_PLANTS_DEAD)
                .add(Items.DEAD_BRAIN_CORAL, Items.DEAD_BUBBLE_CORAL, Items.DEAD_FIRE_CORAL, Items.DEAD_TUBE_CORAL, Items.DEAD_HORN_CORAL);
        provider.addTag(CustomTags.Items.CORAL_PLANTS)
                .addTag(CustomTags.Items.CORAL_PLANTS_ALIVE)
                .addTag(CustomTags.Items.CORAL_PLANTS_DEAD);

        // the coral fans (the flat circular ones)
        provider.addTag(CustomTags.Items.CORAL_FANS_ALIVE)
                .add(Items.BRAIN_CORAL_FAN, Items.BUBBLE_CORAL_FAN, Items.FIRE_CORAL_FAN, Items.TUBE_CORAL_FAN, Items.HORN_CORAL_FAN);
        provider.addTag(CustomTags.Items.CORAL_FANS_DEAD)
                .add(Items.DEAD_BRAIN_CORAL_FAN, Items.DEAD_BUBBLE_CORAL_FAN, Items.DEAD_FIRE_CORAL_FAN, Items.DEAD_TUBE_CORAL_FAN, Items.DEAD_HORN_CORAL_FAN);
        provider.addTag(CustomTags.Items.CORAL_FANS)
                .addTag(CustomTags.Items.CORAL_FANS_ALIVE)
                .addTag(CustomTags.Items.CORAL_FANS_DEAD);

        // all the coral items (except the blocks)
        provider.addTag(CustomTags.Items.CORALS_ALIVE)
                .addTag(CustomTags.Items.CORAL_PLANTS_ALIVE)
                .addTag(CustomTags.Items.CORAL_FANS_ALIVE);
        provider.addTag(CustomTags.Items.CORALS_DEAD)
                .addTag(CustomTags.Items.CORAL_PLANTS_DEAD)
                .addTag(CustomTags.Items.CORAL_FANS_DEAD);
        provider.addTag(CustomTags.Items.CORALS)
                .addTag(CustomTags.Items.CORALS_ALIVE)
                .addTag(CustomTags.Items.CORALS_DEAD);
        // spotless:on

        provider.addTag(CustomTags.Items.LENSES).addTags(CustomTags.Items.LENSES_ARRAY)
                .addTag(CustomTags.Items.LENSES_GLASS);

        provider.addTag(CustomTags.Items.LENSES_WHITE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Glass))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, NetherStar));
        provider.addTag(CustomTags.Items.LENSES_LIGHT_BLUE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Diamond));
        provider.addTag(CustomTags.Items.LENSES_RED)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Ruby));
        provider.addTag(CustomTags.Items.LENSES_GREEN)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Emerald));
        provider.addTag(CustomTags.Items.LENSES_BLUE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Sapphire));
        provider.addTag(CustomTags.Items.LENSES_PURPLE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Amethyst));

        provider.addTag(CustomTags.Items.PISTONS).add(Items.PISTON, Items.STICKY_PISTON);

        // add treated wood stick to vanilla sticks tag
        // noinspection DataFlowIssue ChemicalHelper#getTag can't return null with treated wood rod
        provider.addTag(Tags.Items.RODS_WOODEN)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.rod, TreatedWood));

        // add treated and untreated wood plates to vanilla planks tag
        provider.addTag(ItemTags.PLANKS)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(plate, TreatedWood))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(plate, Wood));

        // add (likely empty) tags for the high tier circuits so we can add them to the `gtceu:circuits` tag easily
        provider.addTag(CustomTags.Items.UEV_CIRCUITS);
        provider.addTag(CustomTags.Items.UIV_CIRCUITS);
        provider.addTag(CustomTags.Items.UXV_CIRCUITS);
        provider.addTag(CustomTags.Items.OpV_CIRCUITS);
        provider.addTag(CustomTags.Items.MAX_CIRCUITS);
        provider.addTag(CustomTags.Items.CIRCUITS).addTags(CustomTags.Items.CIRCUITS_ARRAY);

        provider.addTag(CustomTags.Items.BATTERIES).addTags(CustomTags.Items.BATTERIES_ARRAY);

        // Add highTierContent items as optional entries so it doesn't error
        provider.addTag(CustomTags.Items.ELECTRIC_MOTORS)
                .addOptional(GTItems.ELECTRIC_MOTOR_UHV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UEV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UIV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UXV)
                .addOptional(GTItems.ELECTRIC_MOTOR_OpV);

        provider.addTag(CustomTags.Items.ELECTRIC_PUMPS)
                .addOptional(GTItems.ELECTRIC_PUMP_UHV)
                .addOptional(GTItems.ELECTRIC_PUMP_UEV)
                .addOptional(GTItems.ELECTRIC_PUMP_UIV)
                .addOptional(GTItems.ELECTRIC_PUMP_UXV)
                .addOptional(GTItems.ELECTRIC_PUMP_OpV);

        provider.addTag(CustomTags.Items.FLUID_REGULATORS)
                .addOptional(GTItems.FLUID_REGULATOR_UHV)
                .addOptional(GTItems.FLUID_REGULATOR_UEV)
                .addOptional(GTItems.FLUID_REGULATOR_UIV)
                .addOptional(GTItems.FLUID_REGULATOR_UXV)
                .addOptional(GTItems.FLUID_REGULATOR_OpV);

        provider.addTag(CustomTags.Items.CONVEYOR_MODULES)
                .addOptional(GTItems.CONVEYOR_MODULE_UHV)
                .addOptional(GTItems.CONVEYOR_MODULE_UEV)
                .addOptional(GTItems.CONVEYOR_MODULE_UIV)
                .addOptional(GTItems.CONVEYOR_MODULE_UXV)
                .addOptional(GTItems.CONVEYOR_MODULE_OpV);

        provider.addTag(CustomTags.Items.ELECTRIC_PISTONS)
                .addOptional(GTItems.ELECTRIC_PISTON_UHV)
                .addOptional(GTItems.ELECTRIC_PISTON_UEV)
                .addOptional(GTItems.ELECTRIC_PISTON_UIV)
                .addOptional(GTItems.ELECTRIC_PISTON_UXV)
                .addOptional(GTItems.ELECTRIC_PISTON_OpV);

        provider.addTag(CustomTags.Items.ROBOT_ARMS)
                .addOptional(GTItems.ROBOT_ARM_UHV)
                .addOptional(GTItems.ROBOT_ARM_UEV)
                .addOptional(GTItems.ROBOT_ARM_UIV)
                .addOptional(GTItems.ROBOT_ARM_UXV)
                .addOptional(GTItems.ROBOT_ARM_OpV);

        provider.addTag(CustomTags.Items.FIELD_GENERATORS)
                .addOptional(GTItems.FIELD_GENERATOR_UHV)
                .addOptional(GTItems.FIELD_GENERATOR_UEV)
                .addOptional(GTItems.FIELD_GENERATOR_UIV)
                .addOptional(GTItems.FIELD_GENERATOR_UXV)
                .addOptional(GTItems.FIELD_GENERATOR_OpV);

        provider.addTag(CustomTags.Items.EMITTERS)
                .addOptional(GTItems.EMITTER_UHV)
                .addOptional(GTItems.EMITTER_UEV)
                .addOptional(GTItems.EMITTER_UIV)
                .addOptional(GTItems.EMITTER_UXV)
                .addOptional(GTItems.EMITTER_OpV);

        provider.addTag(CustomTags.Items.SENSORS)
                .addOptional(GTItems.SENSOR_UHV)
                .addOptional(GTItems.SENSOR_UEV)
                .addOptional(GTItems.SENSOR_UIV)
                .addOptional(GTItems.SENSOR_UXV)
                .addOptional(GTItems.SENSOR_OpV);

        provider.addTag(CustomTags.Items.TOOLS_IGNITER)
                .addTag(ItemTags.CREEPER_IGNITERS);
        // add these as "empty" tags so we can add them to `#forge:tools` easily
        provider.addTag(CustomTags.Items.BUTCHERY_KNIVES);
        provider.addTag(CustomTags.Items.BUZZSAWS);
        provider.addTag(CustomTags.Items.CHAINSAWS);
        provider.addTag(CustomTags.Items.CROWBARS);
        provider.addTag(CustomTags.Items.DRILLS);
        provider.addTag(CustomTags.Items.FILES);
        provider.addTag(CustomTags.Items.HAMMERS);
        provider.addTag(CustomTags.Items.KNIVES);
        provider.addTag(CustomTags.Items.MALLETS);
        provider.addTag(CustomTags.Items.MINING_HAMMERS);
        provider.addTag(CustomTags.Items.MORTARS);
        provider.addTag(CustomTags.Items.PLUNGERS);
        provider.addTag(CustomTags.Items.SAWS);
        provider.addTag(CustomTags.Items.SCREWDRIVERS);
        provider.addTag(CustomTags.Items.SCYTHES);
        provider.addTag(CustomTags.Items.SHEARS);
        provider.addTag(CustomTags.Items.SPADES);
        provider.addTag(CustomTags.Items.WIRE_CUTTERS);
        provider.addTag(CustomTags.Items.WRENCHES);
        // mod compat
        provider.addTag(CustomTags.Items.WRENCH).addTag(CustomTags.Items.WRENCHES);
        provider.addTag(Tags.Items.TOOLS)
                .addTags(CustomTags.Items.BUTCHERY_KNIVES, CustomTags.Items.BUZZSAWS, CustomTags.Items.CHAINSAWS,
                        CustomTags.Items.CROWBARS, CustomTags.Items.DRILLS, CustomTags.Items.FILES, CustomTags.Items.HAMMERS,
                        CustomTags.Items.KNIVES, CustomTags.Items.MALLETS, CustomTags.Items.MINING_HAMMERS, CustomTags.Items.MORTARS,
                        CustomTags.Items.PLUNGERS, CustomTags.Items.SAWS, CustomTags.Items.SCREWDRIVERS, CustomTags.Items.SCYTHES,
                        CustomTags.Items.SHEARS, CustomTags.Items.SPADES, CustomTags.Items.WIRE_CUTTERS, CustomTags.Items.WRENCHES);

        // Add sodalite and lazurite as enchanting fuels
        provider.addTag(Tags.Items.ENCHANTING_FUELS)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(gem, Lazurite))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(gem, Sodalite));
    }
}
