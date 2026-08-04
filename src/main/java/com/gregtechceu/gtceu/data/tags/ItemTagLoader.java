package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateItemTagsProvider;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class ItemTagLoader {

    @SuppressWarnings({ "DataFlowIssue", "unchecked" })
    public static void init(RegistrateItemTagsProvider provider) {
        provider.addTag(CustomTags.DOUGHS).addTag(CustomTags.WHEAT_DOUGHS);
        provider.addTag(CustomTags.WHEAT_GRAINS).addOptional(GTMaterialItems.MATERIAL_ITEMS.get(dust, Wheat));
        provider.addTag(CustomTags.GRAINS).addTag(CustomTags.WHEAT_GRAINS);

        provider.copy(CustomTags.CONCRETE_BLOCK, CustomTags.CONCRETE_ITEM);
        provider.copy(CustomTags.CONCRETE_POWDER_BLOCK, CustomTags.CONCRETE_POWDER_ITEM);

        // spotless:off
        // the coral blocks: alive, dead, both
        provider.addTag(CustomTags.ALIVE_CORAL_BLOCK_ITEMS)
                .add(Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK, Items.TUBE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK);
        provider.addTag(CustomTags.DEAD_CORAL_BLOCK_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL_BLOCK, Items.DEAD_BUBBLE_CORAL_BLOCK, Items.DEAD_FIRE_CORAL_BLOCK, Items.DEAD_TUBE_CORAL_BLOCK, Items.DEAD_HORN_CORAL_BLOCK);
        provider.addTag(CustomTags.CORAL_BLOCK_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_BLOCK_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_BLOCK_ITEMS);

        // the coral plants (the V-shaped pointy ones)
        provider.addTag(CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                        .add(Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL, Items.TUBE_CORAL, Items.HORN_CORAL);
        provider.addTag(CustomTags.DEAD_CORAL_PLANT_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL, Items.DEAD_BUBBLE_CORAL, Items.DEAD_FIRE_CORAL, Items.DEAD_TUBE_CORAL, Items.DEAD_HORN_CORAL);
        provider.addTag(CustomTags.CORAL_PLANT_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_PLANT_ITEMS);

        // the coral fans (the flat circular ones)
        provider.addTag(CustomTags.ALIVE_CORAL_FAN_ITEMS)
                .add(Items.BRAIN_CORAL_FAN, Items.BUBBLE_CORAL_FAN, Items.FIRE_CORAL_FAN, Items.TUBE_CORAL_FAN, Items.HORN_CORAL_FAN);
        provider.addTag(CustomTags.DEAD_CORAL_FAN_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL_FAN, Items.DEAD_BUBBLE_CORAL_FAN, Items.DEAD_FIRE_CORAL_FAN, Items.DEAD_TUBE_CORAL_FAN, Items.DEAD_HORN_CORAL_FAN);
        provider.addTag(CustomTags.CORAL_FAN_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_FAN_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_FAN_ITEMS);

        // all the coral items (except the blocks)
        provider.addTag(CustomTags.ALIVE_CORAL_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_FAN_ITEMS);
        provider.addTag(CustomTags.DEAD_CORAL_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_FAN_ITEMS);
        provider.addTag(CustomTags.CORAL_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_ITEMS);
        // spotless:on

        provider.addTag(CustomTags.LENSES).addTags(CustomTags.LENSES_ARRAY)
                .addTag(CustomTags.LENSES_GLASS);

        provider.addTag(CustomTags.LENSES_WHITE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Glass))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, NetherStar));
        provider.addTag(CustomTags.LENSES_LIGHT_BLUE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Diamond));
        provider.addTag(CustomTags.LENSES_RED)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Ruby));
        provider.addTag(CustomTags.LENSES_GREEN)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Emerald));
        provider.addTag(CustomTags.LENSES_BLUE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Sapphire));
        provider.addTag(CustomTags.LENSES_PURPLE)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(lens, Amethyst));

        provider.addTag(CustomTags.PISTONS).add(Items.PISTON, Items.STICKY_PISTON);

        // add treated wood stick to vanilla sticks tag
        // noinspection DataFlowIssue ChemicalHelper#getTag can't return null with treated wood rod
        provider.addTag(Tags.Items.RODS_WOODEN)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.rod, TreatedWood));

        // add treated and untreated wood plates to vanilla planks tag
        provider.addTag(ItemTags.PLANKS)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(plate, TreatedWood))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(plate, Wood));

        provider.addTag(CustomTags.CIRCUITS)
                .addTag(CustomTags.ULV_CIRCUITS)
                .addTag(CustomTags.LV_CIRCUITS)
                .addTag(CustomTags.MV_CIRCUITS)
                .addTag(CustomTags.HV_CIRCUITS)
                .addTag(CustomTags.EV_CIRCUITS)
                .addTag(CustomTags.IV_CIRCUITS)
                .addTag(CustomTags.LuV_CIRCUITS)
                .addTag(CustomTags.ZPM_CIRCUITS)
                .addTag(CustomTags.UV_CIRCUITS)
                .addTag(CustomTags.UHV_CIRCUITS)
                .addOptionalTag(CustomTags.UEV_CIRCUITS.location())
                .addOptionalTag(CustomTags.UIV_CIRCUITS.location())
                .addOptionalTag(CustomTags.UXV_CIRCUITS.location())
                .addOptionalTag(CustomTags.OpV_CIRCUITS.location())
                .addOptionalTag(CustomTags.MAX_CIRCUITS.location());

        provider.addTag(CustomTags.BATTERIES)
                .addTag(CustomTags.ULV_BATTERIES)
                .addTag(CustomTags.LV_BATTERIES)
                .addTag(CustomTags.MV_BATTERIES)
                .addTag(CustomTags.HV_BATTERIES)
                .addTag(CustomTags.EV_BATTERIES)
                .addTag(CustomTags.IV_BATTERIES)
                .addTag(CustomTags.LuV_BATTERIES)
                .addTag(CustomTags.ZPM_BATTERIES)
                .addTag(CustomTags.UV_BATTERIES)
                .addTag(CustomTags.UHV_BATTERIES);

        // Add highTierContent items as optional entries so it doesn't error
        provider.addTag(CustomTags.ELECTRIC_MOTORS)
                .addOptional(GTItems.ELECTRIC_MOTOR_UHV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UEV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UIV)
                .addOptional(GTItems.ELECTRIC_MOTOR_UXV)
                .addOptional(GTItems.ELECTRIC_MOTOR_OpV);

        provider.addTag(CustomTags.ELECTRIC_PUMPS)
                .addOptional(GTItems.ELECTRIC_PUMP_UHV)
                .addOptional(GTItems.ELECTRIC_PUMP_UEV)
                .addOptional(GTItems.ELECTRIC_PUMP_UIV)
                .addOptional(GTItems.ELECTRIC_PUMP_UXV)
                .addOptional(GTItems.ELECTRIC_PUMP_OpV);

        provider.addTag(CustomTags.FLUID_REGULATORS)
                .addOptional(GTItems.FLUID_REGULATOR_UHV)
                .addOptional(GTItems.FLUID_REGULATOR_UEV)
                .addOptional(GTItems.FLUID_REGULATOR_UIV)
                .addOptional(GTItems.FLUID_REGULATOR_UXV)
                .addOptional(GTItems.FLUID_REGULATOR_OpV);

        provider.addTag(CustomTags.CONVEYOR_MODULES)
                .addOptional(GTItems.CONVEYOR_MODULE_UHV)
                .addOptional(GTItems.CONVEYOR_MODULE_UEV)
                .addOptional(GTItems.CONVEYOR_MODULE_UIV)
                .addOptional(GTItems.CONVEYOR_MODULE_UXV)
                .addOptional(GTItems.CONVEYOR_MODULE_OpV);

        provider.addTag(CustomTags.ELECTRIC_PISTONS)
                .addOptional(GTItems.ELECTRIC_PISTON_UHV)
                .addOptional(GTItems.ELECTRIC_PISTON_UEV)
                .addOptional(GTItems.ELECTRIC_PISTON_UIV)
                .addOptional(GTItems.ELECTRIC_PISTON_UXV)
                .addOptional(GTItems.ELECTRIC_PISTON_OpV);

        provider.addTag(CustomTags.ROBOT_ARMS)
                .addOptional(GTItems.ROBOT_ARM_UHV)
                .addOptional(GTItems.ROBOT_ARM_UEV)
                .addOptional(GTItems.ROBOT_ARM_UIV)
                .addOptional(GTItems.ROBOT_ARM_UXV)
                .addOptional(GTItems.ROBOT_ARM_OpV);

        provider.addTag(CustomTags.FIELD_GENERATORS)
                .addOptional(GTItems.FIELD_GENERATOR_UHV)
                .addOptional(GTItems.FIELD_GENERATOR_UEV)
                .addOptional(GTItems.FIELD_GENERATOR_UIV)
                .addOptional(GTItems.FIELD_GENERATOR_UXV)
                .addOptional(GTItems.FIELD_GENERATOR_OpV);

        provider.addTag(CustomTags.EMITTERS)
                .addOptional(GTItems.EMITTER_UHV)
                .addOptional(GTItems.EMITTER_UEV)
                .addOptional(GTItems.EMITTER_UIV)
                .addOptional(GTItems.EMITTER_UXV)
                .addOptional(GTItems.EMITTER_OpV);

        provider.addTag(CustomTags.SENSORS)
                .addOptional(GTItems.SENSOR_UHV)
                .addOptional(GTItems.SENSOR_UEV)
                .addOptional(GTItems.SENSOR_UIV)
                .addOptional(GTItems.SENSOR_UXV)
                .addOptional(GTItems.SENSOR_OpV);

        provider.addTag(CustomTags.TOOLS_IGNITER)
                .addTag(ItemTags.CREEPER_IGNITERS);
        // add these as "empty" tags so we can add them to `#forge:tools` easily
        provider.addTag(CustomTags.BUTCHERY_KNIVES);
        provider.addTag(CustomTags.BUZZSAWS);
        provider.addTag(CustomTags.CHAINSAWS);
        provider.addTag(CustomTags.CROWBARS);
        provider.addTag(CustomTags.DRILLS);
        provider.addTag(CustomTags.FILES);
        provider.addTag(CustomTags.HAMMERS);
        provider.addTag(CustomTags.KNIVES);
        provider.addTag(CustomTags.MALLETS);
        provider.addTag(CustomTags.MINING_HAMMERS);
        provider.addTag(CustomTags.MORTARS);
        provider.addTag(CustomTags.PLUNGERS);
        provider.addTag(CustomTags.SAWS);
        provider.addTag(CustomTags.SCREWDRIVERS);
        provider.addTag(CustomTags.SCYTHES);
        provider.addTag(CustomTags.SHEARS);
        provider.addTag(CustomTags.SPADES);
        provider.addTag(CustomTags.WIRE_CUTTERS);
        provider.addTag(CustomTags.WRENCHES);
        // mod compat
        provider.addTag(CustomTags.WRENCH).addTag(CustomTags.WRENCHES);
        provider.addTag(Tags.Items.TOOLS)
                .addTags(CustomTags.BUTCHERY_KNIVES, CustomTags.BUZZSAWS, CustomTags.CHAINSAWS,
                        CustomTags.CROWBARS, CustomTags.DRILLS, CustomTags.FILES, CustomTags.HAMMERS,
                        CustomTags.KNIVES, CustomTags.MALLETS, CustomTags.MINING_HAMMERS, CustomTags.MORTARS,
                        CustomTags.PLUNGERS, CustomTags.SAWS, CustomTags.SCREWDRIVERS, CustomTags.SCYTHES,
                        CustomTags.SHEARS, CustomTags.SPADES, CustomTags.WIRE_CUTTERS, CustomTags.WRENCHES);

        // Add sodalite and lazurite as enchanting fuels
        provider.addTag(Tags.Items.ENCHANTING_FUELS)
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(gem, Lazurite))
                .addOptional(GTMaterialItems.MATERIAL_ITEMS.get(gem, Sodalite));
    }
}
