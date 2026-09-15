package com.gregtechceu.gtceu.common.recipe.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;


public class EquipmentFoundryRecipe implements Recipe<RecipeWrapper> {

    @Getter
    private final ResourceLocation id;
    @Getter
    private final Ingredient equipment;
    @Getter
    private final boolean isTiered;
    @Getter
    private final @Nullable Ingredient [] moduleIngredients;
    @Getter
    private final @Nullable ItemModuleType<?>[] modules;

    public EquipmentFoundryRecipe(ResourceLocation id, Ingredient equipment, Ingredient[] moduleIngredients, ItemModuleType<?>[] modules) {
        if (moduleIngredients.length != modules.length) throw new IllegalArgumentException("Ingredient and module array length must match");
        Validate.noNullElements(moduleIngredients, "Ingredients array cannot have null elements");
        Validate.noNullElements(modules, "Modules array cannot have null elements");

        this.id = id;
        this.equipment = equipment;
        this.moduleIngredients = moduleIngredients;
        this.modules = modules;
        this.isTiered = moduleIngredients.length == 1;
    }

    @Override
    public boolean matches(RecipeWrapper container, Level level) {
        return matches(container.getItem(0), container.getItem(1));
    }

    public boolean matches(ItemStack equipmentItem, ItemStack itemToApply) {
        if (!equipment.test(equipmentItem)) return false;

        for (int i = 0; i< moduleIngredients.length; i++) {
            Ingredient ingredient = moduleIngredients[i];
            if (ingredient == null) continue;
            if (ingredient.test(itemToApply)) {
                var moduleToApply = modules[i];
                if (moduleToApply == null) continue;
                IModularItem modularItem = GTCapabilityHelper.getModularItem(equipmentItem);
                if (modularItem == null) continue;
                if (modularItem.attach(moduleToApply, itemToApply, true) != null) return true;

            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(RecipeWrapper container, RegistryAccess registryAccess) {
        ItemStack applyTo = container.getItem(0);
        ItemStack ingredient = container.getItem(1);
        applyToItem(applyTo, ingredient, 0);
        return applyTo;
    }

    public void applyToItem(ItemStack equipmentItem, ItemStack itemToApply, int slot) {
        if (!equipment.test(equipmentItem)) return;

        for (int i = 0; i< moduleIngredients.length; i++) {
            Ingredient ingredient = moduleIngredients[i];
            if (ingredient == null) continue;
            if (ingredient.test(itemToApply)) {
                var moduleToApply = modules[i];
                if (moduleToApply == null) continue;
                IModularItem modularItem = GTCapabilityHelper.getModularItem(equipmentItem);
                if (modularItem == null) continue;
                modularItem.attach(moduleToApply, itemToApply, slot, false);
                return;
            }
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return GTItems.QUANTUM_CHESTPLATE_ADVANCED.asStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return GTRecipeTypes.EQUIPMENT_FOUNDRY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get();
    }

    public static class Serializer implements RecipeSerializer<EquipmentFoundryRecipe> {

        public EquipmentFoundryRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient equipment = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "equipment"), false);

            JsonArray ingredientArr = json.getAsJsonArray("ingredients");
            JsonArray arr = json.getAsJsonArray("modules");

            Ingredient[] ingredients = new Ingredient[ingredientArr.size()];
            ItemModuleType<?>[] modules = new ItemModuleType<?>[arr.size()];

            for (int i = 0; i < ingredientArr.size(); i++) {
                ingredients[i] = Ingredient.fromJson(ingredientArr.get(i).getAsJsonObject());
            }

            for (int i = 0; i < arr.size(); i++) {
                modules[i] = GTRegistries.ITEM_MODULES.get(ResourceLocation.parse(arr.get(i).getAsString()));
            }

            return new EquipmentFoundryRecipe(recipeId, equipment, ingredients, modules);
        }

        public EquipmentFoundryRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient equipment = Ingredient.fromNetwork(buffer);

            var arrSize = buffer.readVarInt();
            Ingredient[] ingredients = new Ingredient[arrSize];
            ItemModuleType<?>[] modules = new ItemModuleType<?>[arrSize];
            for (int i = 0; i < arrSize; i++) {
                ingredients[i] = Ingredient.fromNetwork(buffer);
                modules[i] = GTRegistries.ITEM_MODULES.get(buffer.readResourceLocation());
            }
            return new EquipmentFoundryRecipe(recipeId, equipment, ingredients, modules);
        }

        public void toNetwork(FriendlyByteBuf buffer, EquipmentFoundryRecipe recipe) {
            recipe.equipment.toNetwork(buffer);

            buffer.writeVarInt(recipe.moduleIngredients.length);
            for (int i = 0; i<recipe.moduleIngredients.length; i++) {
                recipe.moduleIngredients[i].toNetwork(buffer);
                buffer.writeResourceLocation(recipe.modules[i].id());
            }
        }
    }
}
