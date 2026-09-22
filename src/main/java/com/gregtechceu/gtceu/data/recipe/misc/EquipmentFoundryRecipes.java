package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.EquipmentFoundryRecipeHelper;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class EquipmentFoundryRecipes {

    public static void init(RecipeOutput provider) {
        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("speed"),
                Ingredient.of(ItemTags.LEG_ARMOR),
                GTItems.ELECTRIC_MOTORS, GTItemModules.SPEED);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("energy_shield"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.FIELD_GENERATORS, GTItemModules.DAMAGE_BLOCK);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("attack_speed"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.ELECTRIC_MOTORS, GTItemModules.ATTACK_SPEED);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("attack_damage"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.ELECTRIC_PISTONS, GTItemModules.ATTACK_DAMAGE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("block_reach"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.ROBOT_ARMS, GTItemModules.BLOCK_REACH);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("sneak_speed"),
                Ingredient.of(ItemTags.LEG_ARMOR),
                GTItems.CONVEYOR_MODULES, GTItemModules.SNEAK_SPEED);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("speed_attribute"),
                Ingredient.of(ItemTags.FOOT_ARMOR),
                GTItems.ELECTRIC_MOTORS, GTItemModules.MOVEMENT_SPEED_ATTR);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("respiration"),
                Ingredient.of(ItemTags.HEAD_ARMOR),
                CustomTags.ELECTRIC_PUMPS, GTItemModules.AIR_SUPPLIER);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("autoeat"),
                Ingredient.of(ItemTags.HEAD_ARMOR),
                CustomTags.ROBOT_ARMS, GTItemModules.AUTO_EAT);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("swim_speed"),
                Ingredient.of(ItemTags.FOOT_ARMOR),
                GTItems.ELECTRIC_PUMPS, GTItemModules.SWIM_SPEED);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("step_height"),
                Ingredient.of(ItemTags.FOOT_ARMOR),
                GTItems.ELECTRIC_PISTONS, GTItemModules.STEP_HEIGHT);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("jump_boost"),
                Ingredient.of(ItemTags.LEG_ARMOR),
                GTItems.ELECTRIC_PISTONS, GTItemModules.JUMP_BOOST);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("battery_modifier"),
                Ingredient.fromValues(Stream.of(new Ingredient.TagValue(ItemTags.HEAD_ARMOR),
                        new Ingredient.TagValue(ItemTags.CHEST_ARMOR),
                        new Ingredient.TagValue(ItemTags.LEG_ARMOR),
                        new Ingredient.TagValue(ItemTags.FOOT_ARMOR))),
                CustomTags.BATTERIES, GTItemModules.BATTERY);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("night_vision"),
                Ingredient.of(ItemTags.HEAD_ARMOR),
                GTItems.NIGHTVISION_GOGGLES, GTItemModules.NIGHT_VISION);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("ppe_helmet"),
                Ingredient.of(ItemTags.HEAD_ARMOR),
                GTItems.MASK_FILTER,
                GTItemModules.PPE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("ppe_chestplate"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                ChemicalHelper.get(plate, PolyvinylChloride),
                GTItemModules.PPE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("ppe_leggings"),
                Ingredient.of(ItemTags.LEG_ARMOR),
                ChemicalHelper.get(plate, PolyvinylChloride),
                GTItemModules.PPE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("ppe_boots"),
                Ingredient.of(ItemTags.FOOT_ARMOR),
                ChemicalHelper.get(plate, PolyvinylChloride),
                GTItemModules.PPE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("sensor"),
                Ingredient.of(ItemTags.HEAD_ARMOR),
                GTItems.SENSORS, GTItemModules.SENSOR);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("wireless_charger"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.SENSORS, GTItemModules.WIRELESS_CHARGER);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("jetpack"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.ELECTRIC_JETPACK, GTItemModules.JETPACK);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("advanced_jetpack"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.ELECTRIC_JETPACK_ADVANCED, GTItemModules.ADVANCED_JETPACK);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("liquid_fuel_jetpack"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.LIQUID_FUEL_JETPACK, GTItemModules.LIQUID_FUEL_JETPACK);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("creative_flight"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                GTItems.CREATIVE_FLIGHT_MODULE, GTItemModules.CREATIVE_FLIGHT);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("fluid_storage"),
                Ingredient.of(ItemTags.CHEST_ARMOR),
                Ingredient.of(CustomTags.FLUID_CELLS), GTItemModules.FLUID_STORAGE);
    }
}
