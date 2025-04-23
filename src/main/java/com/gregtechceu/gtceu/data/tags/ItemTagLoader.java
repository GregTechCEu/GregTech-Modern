package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
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

    @SuppressWarnings("DataFlowIssue")
    public static void init(RegistrateItemTagsProvider provider) {
        provider.copy(CustomTags.CONCRETE_BLOCK, CustomTags.CONCRETE_ITEM);
        provider.copy(CustomTags.CONCRETE_POWDER_BLOCK, CustomTags.CONCRETE_POWDER_ITEM);

        provider.addTag(ChemicalHelper.getTag(lens, Color.White))
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Glass).get())
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, NetherStar).get());
        provider.addTag(ChemicalHelper.getTag(lens, Color.LightBlue))
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Diamond).get());
        provider.addTag(ChemicalHelper.getTag(lens, Color.Red))
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Ruby).get());
        provider.addTag(ChemicalHelper.getTag(lens, Color.Green))
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Emerald).get());
        provider.addTag(ChemicalHelper.getTag(lens, Color.Blue))
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Sapphire).get());
        provider.addTag(ChemicalHelper.getTag(lens, Color.Purple))
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Amethyst).get());

        provider.addTag(CustomTags.PISTONS).add(Items.PISTON, Items.STICKY_PISTON);

        // add treated wood stick to vanilla sticks tag
        // noinspection DataFlowIssue ChemicalHelper#getTag can't return null with treated wood rod
        provider.addTag(Tags.Items.RODS_WOODEN)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.rod, TreatedWood).get());

        // add treated and untreated wood plates to vanilla planks tag
        provider.addTag(ItemTags.PLANKS)
                .add(TagEntry.element(GTMaterialItems.MATERIAL_ITEMS.get(plate, TreatedWood).getId()))
                .add(TagEntry.element(GTMaterialItems.MATERIAL_ITEMS.get(plate, Wood).getId()));

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
                .addOptional(GTItems.ELECTRIC_MOTOR_UHV.getId())
                .addOptional(GTItems.ELECTRIC_MOTOR_UEV.getId())
                .addOptional(GTItems.ELECTRIC_MOTOR_UIV.getId())
                .addOptional(GTItems.ELECTRIC_MOTOR_UXV.getId())
                .addOptional(GTItems.ELECTRIC_MOTOR_OpV.getId());

        provider.addTag(CustomTags.ELECTRIC_PUMPS)
                .addOptional(GTItems.ELECTRIC_PUMP_UHV.getId())
                .addOptional(GTItems.ELECTRIC_PUMP_UEV.getId())
                .addOptional(GTItems.ELECTRIC_PUMP_UIV.getId())
                .addOptional(GTItems.ELECTRIC_PUMP_UXV.getId())
                .addOptional(GTItems.ELECTRIC_PUMP_OpV.getId());

        provider.addTag(CustomTags.FLUID_REGULATORS)
                .addOptional(GTItems.FLUID_REGULATOR_UHV.getId())
                .addOptional(GTItems.FLUID_REGULATOR_UEV.getId())
                .addOptional(GTItems.FLUID_REGULATOR_UIV.getId())
                .addOptional(GTItems.FLUID_REGULATOR_UXV.getId())
                .addOptional(GTItems.FLUID_REGULATOR_OpV.getId());

        provider.addTag(CustomTags.CONVEYOR_MODULES)
                .addOptional(GTItems.CONVEYOR_MODULE_UHV.getId())
                .addOptional(GTItems.CONVEYOR_MODULE_UEV.getId())
                .addOptional(GTItems.CONVEYOR_MODULE_UIV.getId())
                .addOptional(GTItems.CONVEYOR_MODULE_UXV.getId())
                .addOptional(GTItems.CONVEYOR_MODULE_OpV.getId());

        provider.addTag(CustomTags.ELECTRIC_PISTONS)
                .addOptional(GTItems.ELECTRIC_PISTON_UHV.getId())
                .addOptional(GTItems.ELECTRIC_PISTON_UEV.getId())
                .addOptional(GTItems.ELECTRIC_PISTON_UIV.getId())
                .addOptional(GTItems.ELECTRIC_PISTON_UXV.getId())
                .addOptional(GTItems.ELECTRIC_PISTON_OpV.getId());

        provider.addTag(CustomTags.ROBOT_ARMS)
                .addOptional(GTItems.ROBOT_ARM_UHV.getId())
                .addOptional(GTItems.ROBOT_ARM_UEV.getId())
                .addOptional(GTItems.ROBOT_ARM_UIV.getId())
                .addOptional(GTItems.ROBOT_ARM_UXV.getId())
                .addOptional(GTItems.ROBOT_ARM_OpV.getId());

        provider.addTag(CustomTags.FIELD_GENERATORS)
                .addOptional(GTItems.FIELD_GENERATOR_UHV.getId())
                .addOptional(GTItems.FIELD_GENERATOR_UEV.getId())
                .addOptional(GTItems.FIELD_GENERATOR_UIV.getId())
                .addOptional(GTItems.FIELD_GENERATOR_UXV.getId())
                .addOptional(GTItems.FIELD_GENERATOR_OpV.getId());

        provider.addTag(CustomTags.EMITTERS)
                .addOptional(GTItems.EMITTER_UHV.getId())
                .addOptional(GTItems.EMITTER_UEV.getId())
                .addOptional(GTItems.EMITTER_UIV.getId())
                .addOptional(GTItems.EMITTER_UXV.getId())
                .addOptional(GTItems.EMITTER_OpV.getId());

        provider.addTag(CustomTags.SENSORS)
                .addOptional(GTItems.SENSOR_UHV.getId())
                .addOptional(GTItems.SENSOR_UEV.getId())
                .addOptional(GTItems.SENSOR_UIV.getId())
                .addOptional(GTItems.SENSOR_UXV.getId())
                .addOptional(GTItems.SENSOR_OpV.getId());
    }
}
