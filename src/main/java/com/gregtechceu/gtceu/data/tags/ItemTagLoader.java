package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateItemTagsProvider;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class ItemTagLoader {

    @SuppressWarnings("DataFlowIssue")
    public static void init(RegistrateItemTagsProvider provider) {
        provider.tag(CustomTags.DOUGHS).addTag(CustomTags.WHEAT_DOUGHS);
        provider.tag(CustomTags.WHEAT_GRAINS).add(GTMaterialItems.MATERIAL_ITEMS.get(dust, Wheat).get().builtInRegistryHolder().key());
        provider.tag(CustomTags.GRAINS).addTag(CustomTags.WHEAT_GRAINS);

        provider.copy(CustomTags.CONCRETE_BLOCK, CustomTags.CONCRETE_ITEM);
        provider.copy(CustomTags.CONCRETE_POWDER_BLOCK, CustomTags.CONCRETE_POWDER_ITEM);

        // spotless:off
        // the coral blocks: alive, dead, both
        provider.tag(CustomTags.ALIVE_CORAL_BLOCK_ITEMS)
                .add(Items.BRAIN_CORAL_BLOCK.builtInRegistryHolder().key(), Items.BUBBLE_CORAL_BLOCK.builtInRegistryHolder().key(), Items.FIRE_CORAL_BLOCK.builtInRegistryHolder().key(), Items.TUBE_CORAL_BLOCK.builtInRegistryHolder().key(), Items.HORN_CORAL_BLOCK.builtInRegistryHolder().key());
        provider.tag(CustomTags.DEAD_CORAL_BLOCK_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL_BLOCK.builtInRegistryHolder().key(), Items.DEAD_BUBBLE_CORAL_BLOCK.builtInRegistryHolder().key(), Items.DEAD_FIRE_CORAL_BLOCK.builtInRegistryHolder().key(), Items.DEAD_TUBE_CORAL_BLOCK.builtInRegistryHolder().key(), Items.DEAD_HORN_CORAL_BLOCK.builtInRegistryHolder().key());
        provider.tag(CustomTags.CORAL_BLOCK_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_BLOCK_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_BLOCK_ITEMS);

        // the coral plants (the V-shaped pointy ones)
        provider.tag(CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                        .add(Items.BRAIN_CORAL.builtInRegistryHolder().key(), Items.BUBBLE_CORAL.builtInRegistryHolder().key(), Items.FIRE_CORAL.builtInRegistryHolder().key(), Items.TUBE_CORAL.builtInRegistryHolder().key(), Items.HORN_CORAL.builtInRegistryHolder().key());
        provider.tag(CustomTags.DEAD_CORAL_PLANT_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL.builtInRegistryHolder().key(), Items.DEAD_BUBBLE_CORAL.builtInRegistryHolder().key(), Items.DEAD_FIRE_CORAL.builtInRegistryHolder().key(), Items.DEAD_TUBE_CORAL.builtInRegistryHolder().key(), Items.DEAD_HORN_CORAL.builtInRegistryHolder().key());
        provider.tag(CustomTags.CORAL_PLANT_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_PLANT_ITEMS);

        // the coral fans (the flat circular ones)
        provider.tag(CustomTags.ALIVE_CORAL_FAN_ITEMS)
                .add(Items.BRAIN_CORAL_FAN.builtInRegistryHolder().key(), Items.BUBBLE_CORAL_FAN.builtInRegistryHolder().key(), Items.FIRE_CORAL_FAN.builtInRegistryHolder().key(), Items.TUBE_CORAL_FAN.builtInRegistryHolder().key(), Items.HORN_CORAL_FAN.builtInRegistryHolder().key());
        provider.tag(CustomTags.DEAD_CORAL_FAN_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL_FAN.builtInRegistryHolder().key(), Items.DEAD_BUBBLE_CORAL_FAN.builtInRegistryHolder().key(), Items.DEAD_FIRE_CORAL_FAN.builtInRegistryHolder().key(), Items.DEAD_TUBE_CORAL_FAN.builtInRegistryHolder().key(), Items.DEAD_HORN_CORAL_FAN.builtInRegistryHolder().key());
        provider.tag(CustomTags.CORAL_FAN_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_FAN_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_FAN_ITEMS);

        // all the coral items (except the blocks)
        provider.tag(CustomTags.ALIVE_CORAL_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_FAN_ITEMS);
        provider.tag(CustomTags.DEAD_CORAL_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_FAN_ITEMS);
        provider.tag(CustomTags.CORAL_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_ITEMS);
        // spotless:on

        provider.tag(CustomTags.WHITE_LENS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Glass).get().builtInRegistryHolder().key())
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, NetherStar).get().builtInRegistryHolder().key());
        provider.tag(CustomTags.LIGHT_BLUE_LENS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Diamond).get().builtInRegistryHolder().key());
        provider.tag(CustomTags.RED_LENS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Ruby).get().builtInRegistryHolder().key());
        provider.tag(CustomTags.GREEN_LENS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Emerald).get().builtInRegistryHolder().key());
        provider.tag(CustomTags.BLUE_LENS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Sapphire).get().builtInRegistryHolder().key());
        provider.tag(CustomTags.PURPLE_LENS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Amethyst).get().builtInRegistryHolder().key());

        provider.tag(CustomTags.PISTONS).add(Items.PISTON.builtInRegistryHolder().key(), Items.STICKY_PISTON.builtInRegistryHolder().key());

        // add treated wood stick to vanilla sticks tag
        // noinspection DataFlowIssue ChemicalHelper#getTag can't return null with treated wood rod
        provider.tag(Tags.Items.RODS_WOODEN)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.rod, TreatedWood).get().builtInRegistryHolder().key());

        // add treated and untreated wood plates to vanilla planks tag
        provider.tag(ItemTags.PLANKS)
                .add(TagEntry.element(GTMaterialItems.MATERIAL_ITEMS.get(plate, TreatedWood).getId()))
                .add(TagEntry.element(GTMaterialItems.MATERIAL_ITEMS.get(plate, Wood).getId()));

        provider.tag(CustomTags.CIRCUITS)
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
                .addOptionalTag(CustomTags.UEV_CIRCUITS)
                .addOptionalTag(CustomTags.UIV_CIRCUITS)
                .addOptionalTag(CustomTags.UXV_CIRCUITS)
                .addOptionalTag(CustomTags.OpV_CIRCUITS)
                .addOptionalTag(CustomTags.MAX_CIRCUITS);

        provider.tag(CustomTags.BATTERIES)
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
        provider.tag(CustomTags.ELECTRIC_MOTORS)
                .addOptional(GTItems.ELECTRIC_MOTOR_UHV.getKey())
                .addOptional(GTItems.ELECTRIC_MOTOR_UEV.getKey())
                .addOptional(GTItems.ELECTRIC_MOTOR_UIV.getKey())
                .addOptional(GTItems.ELECTRIC_MOTOR_UXV.getKey())
                .addOptional(GTItems.ELECTRIC_MOTOR_OpV.getKey());

        provider.tag(CustomTags.ELECTRIC_PUMPS)
                .addOptional(GTItems.ELECTRIC_PUMP_UHV.getKey())
                .addOptional(GTItems.ELECTRIC_PUMP_UEV.getKey())
                .addOptional(GTItems.ELECTRIC_PUMP_UIV.getKey())
                .addOptional(GTItems.ELECTRIC_PUMP_UXV.getKey())
                .addOptional(GTItems.ELECTRIC_PUMP_OpV.getKey());

        provider.tag(CustomTags.FLUID_REGULATORS)
                .addOptional(GTItems.FLUID_REGULATOR_UHV.getKey())
                .addOptional(GTItems.FLUID_REGULATOR_UEV.getKey())
                .addOptional(GTItems.FLUID_REGULATOR_UIV.getKey())
                .addOptional(GTItems.FLUID_REGULATOR_UXV.getKey())
                .addOptional(GTItems.FLUID_REGULATOR_OpV.getKey());

        provider.tag(CustomTags.CONVEYOR_MODULES)
                .addOptional(GTItems.CONVEYOR_MODULE_UHV.getKey())
                .addOptional(GTItems.CONVEYOR_MODULE_UEV.getKey())
                .addOptional(GTItems.CONVEYOR_MODULE_UIV.getKey())
                .addOptional(GTItems.CONVEYOR_MODULE_UXV.getKey())
                .addOptional(GTItems.CONVEYOR_MODULE_OpV.getKey());

        provider.tag(CustomTags.ELECTRIC_PISTONS)
                .addOptional(GTItems.ELECTRIC_PISTON_UHV.getKey())
                .addOptional(GTItems.ELECTRIC_PISTON_UEV.getKey())
                .addOptional(GTItems.ELECTRIC_PISTON_UIV.getKey())
                .addOptional(GTItems.ELECTRIC_PISTON_UXV.getKey())
                .addOptional(GTItems.ELECTRIC_PISTON_OpV.getKey());

        provider.tag(CustomTags.ROBOT_ARMS)
                .addOptional(GTItems.ROBOT_ARM_UHV.getKey())
                .addOptional(GTItems.ROBOT_ARM_UEV.getKey())
                .addOptional(GTItems.ROBOT_ARM_UIV.getKey())
                .addOptional(GTItems.ROBOT_ARM_UXV.getKey())
                .addOptional(GTItems.ROBOT_ARM_OpV.getKey());

        provider.tag(CustomTags.FIELD_GENERATORS)
                .addOptional(GTItems.FIELD_GENERATOR_UHV.getKey())
                .addOptional(GTItems.FIELD_GENERATOR_UEV.getKey())
                .addOptional(GTItems.FIELD_GENERATOR_UIV.getKey())
                .addOptional(GTItems.FIELD_GENERATOR_UXV.getKey())
                .addOptional(GTItems.FIELD_GENERATOR_OpV.getKey());

        provider.tag(CustomTags.EMITTERS)
                .addOptional(GTItems.EMITTER_UHV.getKey())
                .addOptional(GTItems.EMITTER_UEV.getKey())
                .addOptional(GTItems.EMITTER_UIV.getKey())
                .addOptional(GTItems.EMITTER_UXV.getKey())
                .addOptional(GTItems.EMITTER_OpV.getKey());

        provider.tag(CustomTags.SENSORS)
                .addOptional(GTItems.SENSOR_UHV.getKey())
                .addOptional(GTItems.SENSOR_UEV.getKey())
                .addOptional(GTItems.SENSOR_UIV.getKey())
                .addOptional(GTItems.SENSOR_UXV.getKey())
                .addOptional(GTItems.SENSOR_OpV.getKey());

        provider.tag(CustomTags.TOOLS_IGNITER)
                .addTag(ItemTags.CREEPER_IGNITERS);

        // Add sodalite and lazurite as enchanting fuels
        provider.tag(Tags.Items.ENCHANTING_FUELS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(gem, Lazurite).get().builtInRegistryHolder().key())
                .add(GTMaterialItems.MATERIAL_ITEMS.get(gem, Sodalite).get().builtInRegistryHolder().key());
    }
}
