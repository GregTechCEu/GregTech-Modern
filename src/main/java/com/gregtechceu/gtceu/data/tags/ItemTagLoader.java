package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.TagUtil;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateItemTagsProvider;

import java.util.Objects;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

@SuppressWarnings({ "SameParameterValue", "DataFlowIssue" })
public class ItemTagLoader {

    public static void init(RegistrateItemTagsProvider provider) {
        tag(provider, CustomTags.DOUGHS).addTag(CustomTags.WHEAT_DOUGHS);
        tag(provider, CustomTags.WHEAT_GRAINS).add(GTMaterialItems.MATERIAL_ITEMS.get(dust, Wheat).get());
        tag(provider, CustomTags.GRAINS).addTag(CustomTags.WHEAT_GRAINS);

        tag(provider, TagUtil.createItemTag("foods/dough")).addTag(CustomTags.DOUGHS);

        // spotless:off
        // the coral blocks: alive, dead, both
        tag(provider, CustomTags.ALIVE_CORAL_BLOCK_ITEMS)
                .add(Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK, Items.TUBE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK);
        tag(provider, CustomTags.DEAD_CORAL_BLOCK_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL_BLOCK, Items.DEAD_BUBBLE_CORAL_BLOCK, Items.DEAD_FIRE_CORAL_BLOCK, Items.DEAD_TUBE_CORAL_BLOCK, Items.DEAD_HORN_CORAL_BLOCK);
        tag(provider, CustomTags.CORAL_BLOCK_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_BLOCK_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_BLOCK_ITEMS);

        // the coral plants (the V-shaped pointy ones)
        tag(provider, CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                .add(Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL, Items.TUBE_CORAL, Items.HORN_CORAL);
        tag(provider, CustomTags.DEAD_CORAL_PLANT_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL, Items.DEAD_BUBBLE_CORAL, Items.DEAD_FIRE_CORAL, Items.DEAD_TUBE_CORAL, Items.DEAD_HORN_CORAL);
        tag(provider, CustomTags.CORAL_PLANT_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_PLANT_ITEMS);

        // the coral fans (the flat circular ones)
        tag(provider, CustomTags.ALIVE_CORAL_FAN_ITEMS)
                .add(Items.BRAIN_CORAL_FAN, Items.BUBBLE_CORAL_FAN, Items.FIRE_CORAL_FAN, Items.TUBE_CORAL_FAN, Items.HORN_CORAL_FAN);
        tag(provider, CustomTags.DEAD_CORAL_FAN_ITEMS)
                .add(Items.DEAD_BRAIN_CORAL_FAN, Items.DEAD_BUBBLE_CORAL_FAN, Items.DEAD_FIRE_CORAL_FAN, Items.DEAD_TUBE_CORAL_FAN, Items.DEAD_HORN_CORAL_FAN);
        tag(provider, CustomTags.CORAL_FAN_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_FAN_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_FAN_ITEMS);

        // all the coral items (except the blocks)
        tag(provider, CustomTags.ALIVE_CORAL_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_FAN_ITEMS);
        tag(provider, CustomTags.DEAD_CORAL_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_PLANT_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_FAN_ITEMS);
        tag(provider, CustomTags.CORAL_ITEMS)
                .addTag(CustomTags.ALIVE_CORAL_ITEMS)
                .addTag(CustomTags.DEAD_CORAL_ITEMS);
        // spotless:on

        addTag(provider, lens, Color.White)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Glass).get())
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, NetherStar).get());
        addTag(provider, lens, Color.LightBlue)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Diamond).get());
        addTag(provider, lens, Color.Red)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Ruby).get());
        addTag(provider, lens, Color.Green)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Emerald).get());
        addTag(provider, lens, Color.Blue)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Sapphire).get());
        addTag(provider, lens, Color.Purple)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(lens, Amethyst).get());

        tag(provider, CustomTags.PISTONS).add(Items.PISTON, Items.STICKY_PISTON);

        // add treated wood stick to vanilla sticks tag
        // noinspection DataFlowIssue ChemicalHelper#getTag can't return null with treated wood rod
        tag(provider, Tags.Items.RODS_WOODEN)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.rod, TreatedWood).get());

        // add treated and untreated wood plates to vanilla planks tag
        tag(provider, ItemTags.PLANKS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(plate, TreatedWood).get())
                .add(GTMaterialItems.MATERIAL_ITEMS.get(plate, Wood).get());

        tag(provider, CustomTags.CIRCUITS)
                .addTag(CustomTags.ULV_CIRCUITS)
                .addTag(CustomTags.LV_CIRCUITS)
                .addTag(CustomTags.MV_CIRCUITS)
                .addTag(CustomTags.HV_CIRCUITS)
                .addTag(CustomTags.EV_CIRCUITS)
                .addTag(CustomTags.IV_CIRCUITS)
                .addTag(CustomTags.LuV_CIRCUITS)
                .addTag(CustomTags.ZPM_CIRCUITS)
                .addTag(CustomTags.UV_CIRCUITS)
                .addTag(CustomTags.UHV_CIRCUITS);
        var circuits = provider.rawBuilder(CustomTags.CIRCUITS);
        circuits.addOptionalTag(CustomTags.UEV_CIRCUITS.location());
        circuits.addOptionalTag(CustomTags.UIV_CIRCUITS.location());
        circuits.addOptionalTag(CustomTags.UXV_CIRCUITS.location());
        circuits.addOptionalTag(CustomTags.OpV_CIRCUITS.location());
        circuits.addOptionalTag(CustomTags.MAX_CIRCUITS.location());
        createOptionalEmptyTag(provider, CustomTags.UEV_CIRCUITS);
        createOptionalEmptyTag(provider, CustomTags.UIV_CIRCUITS);
        createOptionalEmptyTag(provider, CustomTags.UXV_CIRCUITS);
        createOptionalEmptyTag(provider, CustomTags.OpV_CIRCUITS);
        createOptionalEmptyTag(provider, CustomTags.MAX_CIRCUITS);

        tag(provider, CustomTags.BATTERIES)
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
        addOptionalItems(provider, CustomTags.ELECTRIC_MOTORS,
                GTItems.ELECTRIC_MOTOR_UHV.getId(),
                GTItems.ELECTRIC_MOTOR_UEV.getId(),
                GTItems.ELECTRIC_MOTOR_UIV.getId(),
                GTItems.ELECTRIC_MOTOR_UXV.getId(),
                GTItems.ELECTRIC_MOTOR_OpV.getId());

        addOptionalItems(provider, CustomTags.ELECTRIC_PUMPS,
                GTItems.ELECTRIC_PUMP_UHV.getId(),
                GTItems.ELECTRIC_PUMP_UEV.getId(),
                GTItems.ELECTRIC_PUMP_UIV.getId(),
                GTItems.ELECTRIC_PUMP_UXV.getId(),
                GTItems.ELECTRIC_PUMP_OpV.getId());

        addOptionalItems(provider, CustomTags.FLUID_REGULATORS,
                GTItems.FLUID_REGULATOR_UHV.getId(),
                GTItems.FLUID_REGULATOR_UEV.getId(),
                GTItems.FLUID_REGULATOR_UIV.getId(),
                GTItems.FLUID_REGULATOR_UXV.getId(),
                GTItems.FLUID_REGULATOR_OpV.getId());

        addOptionalItems(provider, CustomTags.CONVEYOR_MODULES,
                GTItems.CONVEYOR_MODULE_UHV.getId(),
                GTItems.CONVEYOR_MODULE_UEV.getId(),
                GTItems.CONVEYOR_MODULE_UIV.getId(),
                GTItems.CONVEYOR_MODULE_UXV.getId(),
                GTItems.CONVEYOR_MODULE_OpV.getId());

        addOptionalItems(provider, CustomTags.ELECTRIC_PISTONS,
                GTItems.ELECTRIC_PISTON_UHV.getId(),
                GTItems.ELECTRIC_PISTON_UEV.getId(),
                GTItems.ELECTRIC_PISTON_UIV.getId(),
                GTItems.ELECTRIC_PISTON_UXV.getId(),
                GTItems.ELECTRIC_PISTON_OpV.getId());

        addOptionalItems(provider, CustomTags.ROBOT_ARMS,
                GTItems.ROBOT_ARM_UHV.getId(),
                GTItems.ROBOT_ARM_UEV.getId(),
                GTItems.ROBOT_ARM_UIV.getId(),
                GTItems.ROBOT_ARM_UXV.getId(),
                GTItems.ROBOT_ARM_OpV.getId());

        addOptionalItems(provider, CustomTags.FIELD_GENERATORS,
                GTItems.FIELD_GENERATOR_UHV.getId(),
                GTItems.FIELD_GENERATOR_UEV.getId(),
                GTItems.FIELD_GENERATOR_UIV.getId(),
                GTItems.FIELD_GENERATOR_UXV.getId(),
                GTItems.FIELD_GENERATOR_OpV.getId());

        addOptionalItems(provider, CustomTags.EMITTERS,
                GTItems.EMITTER_UHV.getId(),
                GTItems.EMITTER_UEV.getId(),
                GTItems.EMITTER_UIV.getId(),
                GTItems.EMITTER_UXV.getId(),
                GTItems.EMITTER_OpV.getId());

        addOptionalItems(provider, CustomTags.SENSORS,
                GTItems.SENSOR_UHV.getId(),
                GTItems.SENSOR_UEV.getId(),
                GTItems.SENSOR_UIV.getId(),
                GTItems.SENSOR_UXV.getId(),
                GTItems.SENSOR_OpV.getId());

        tag(provider, CustomTags.TOOLS_IGNITER)
                .addTag(ItemTags.CREEPER_IGNITERS);

        // Add sodalite and lazurite as enchanting fuels
        tag(provider, Tags.Items.ENCHANTING_FUELS)
                .add(GTMaterialItems.MATERIAL_ITEMS.get(gem, Lazurite).get())
                .add(GTMaterialItems.MATERIAL_ITEMS.get(gem, Sodalite).get());
    }

    private static TagAppender<Item, Item> tag(RegistrateItemTagsProvider provider, TagKey<Item> tagKey) {
        return provider.tag(tagKey);
    }

    private static void createOptionalEmptyTag(RegistrateItemTagsProvider provider, TagKey<Item> tagKey) {
        Identifier tagId = tagKey.location();
        provider.rawBuilder(tagKey)
                .addOptionalElement(Identifier.fromNamespaceAndPath(tagId.getNamespace(),
                        "_empty_tag/" + tagId.getPath()));
    }

    private static TagAppender<Item, Item> addTag(RegistrateItemTagsProvider provider, TagPrefix prefix,
                                                  Material material) {
        return tag(provider, Objects.requireNonNull(ChemicalHelper.getTag(prefix, material),
                "%s/%s doesn't have any tags!".formatted(prefix, material)));
    }

    private static void addOptionalItems(RegistrateItemTagsProvider provider, TagKey<Item> tagKey,
                                         net.minecraft.resources.Identifier... ids) {
        tag(provider, tagKey);
        var builder = provider.rawBuilder(tagKey);
        for (var id : ids) {
            builder.addOptionalElement(id);
        }
    }
}
