package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.EquipmentFoundryRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class EquipmentFoundryRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("speed"),
                Ingredient.of(Tags.Items.ARMORS_LEGGINGS),
                GTItems.ELECTRIC_MOTORS, GTItemModules.SPEED);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("energy_shield"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.FIELD_GENERATORS, GTItemModules.DAMAGE_BLOCK);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("attack_speed"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.ELECTRIC_MOTORS, GTItemModules.ATTACK_SPEED);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("attack_damage"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.ELECTRIC_PISTONS, GTItemModules.ATTACK_DAMAGE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("block_reach"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.ROBOT_ARMS, GTItemModules.BLOCK_REACH);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("sneak_speed"),
                Ingredient.of(Tags.Items.ARMORS_LEGGINGS),
                GTItems.CONVEYOR_MODULES, GTItemModules.SNEAK_SPEED);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("speed_attribute"),
                Ingredient.of(Tags.Items.ARMORS_BOOTS),
                GTItems.ELECTRIC_MOTORS, GTItemModules.MOVEMENT_SPEED_ATTR);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("respiration"),
                Ingredient.of(Tags.Items.ARMORS_HELMETS),
                CustomTags.ELECTRIC_PUMPS, GTItemModules.AIR_SUPPLIER);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("autoeat"),
                Ingredient.of(Tags.Items.ARMORS_HELMETS),
                CustomTags.ROBOT_ARMS, GTItemModules.AUTO_EAT);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("swim_speed"),
                Ingredient.of(Tags.Items.ARMORS_BOOTS),
                GTItems.ELECTRIC_PUMPS, GTItemModules.SWIM_SPEED);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("step_height"),
                Ingredient.of(Tags.Items.ARMORS_BOOTS),
                GTItems.ELECTRIC_PISTONS, GTItemModules.STEP_HEIGHT);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("jump_boost"),
                Ingredient.of(Tags.Items.ARMORS_LEGGINGS),
                GTItems.ELECTRIC_PISTONS, GTItemModules.JUMP_BOOST);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("battery_modifier"),
                Ingredient.of(Tags.Items.ARMORS),
                CustomTags.BATTERIES, GTItemModules.BATTERY);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("night_vision"),
                Ingredient.of(Tags.Items.ARMORS_HELMETS),
                GTItems.NIGHTVISION_GOGGLES, GTItemModules.NIGHT_VISION);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("ppe_helmet"),
                Ingredient.of(Tags.Items.ARMORS_HELMETS),
                GTItems.MASK_FILTER,
                GTItemModules.PPE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("ppe_chestplate"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                ChemicalHelper.get(plate, PolyvinylChloride),
                GTItemModules.PPE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("ppe_leggings"),
                Ingredient.of(Tags.Items.ARMORS_LEGGINGS),
                ChemicalHelper.get(plate, PolyvinylChloride),
                GTItemModules.PPE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("ppe_boots"),
                Ingredient.of(Tags.Items.ARMORS_BOOTS),
                ChemicalHelper.get(plate, PolyvinylChloride),
                GTItemModules.PPE);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("sensor"),
                Ingredient.of(Tags.Items.ARMORS_HELMETS),
                GTItems.SENSORS, GTItemModules.SENSOR);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("wireless_charger"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.SENSORS, GTItemModules.WIRELESS_CHARGER);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("jetpack"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.ELECTRIC_JETPACK, GTItemModules.JETPACK);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("advanced_jetpack"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.ELECTRIC_JETPACK_ADVANCED, GTItemModules.ADVANCED_JETPACK);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("liquid_fuel_jetpack"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.LIQUID_FUEL_JETPACK, GTItemModules.LIQUID_FUEL_JETPACK);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("creative_flight"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                GTItems.CREATIVE_FLIGHT_MODULE, GTItemModules.CREATIVE_FLIGHT);

        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, GTCEu.id("fluid_storage"),
                Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                Ingredient.of(CustomTags.FLUID_CELLS), GTItemModules.FLUID_STORAGE);
    }
}
