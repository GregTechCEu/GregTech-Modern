package com.gregtechceu.gtceu.common.recipe.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
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
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EquipmentFoundryRecipe implements Recipe<RecipeWrapper> {

    @Getter
    private final ResourceLocation id;
    @Getter
    private final Ingredient equipment;
    @Getter
    private final boolean isTiered;
    @Getter
    private final @Nullable Ingredient[] ingredients;
    @Getter
    private final @Nullable ItemModuleType<?>[] modules;

    public EquipmentFoundryRecipe(ResourceLocation id, Ingredient equipment, @Nullable Ingredient[] ingredients, @Nullable ItemModuleType<?>[] modules) {
        if (ingredients.length != GTValues.TIER_COUNT) throw new IllegalArgumentException("Ingredient array length must equal tier count");
        if (modules.length != GTValues.TIER_COUNT) throw new IllegalArgumentException("Module array length must equal tier count");
        this.id = id;
        this.equipment = equipment;
        this.ingredients = ingredients;
        this.modules = modules;

        int foundIngredients = 0;
        for (int i=0; i<GTValues.TIER_COUNT; i++) {
            Ingredient ingredient = ingredients[i];
            ItemModuleType<?> moduleType = modules[i];
            if (ingredient == null && moduleType == null) {
            }
            else if (ingredient != null && moduleType != null) foundIngredients++;
            else {
                throw new IllegalArgumentException("Ingredient and module must both be either null or not null: {ingredient=%s, module=%s, tier=%s}".formatted(ingredient, moduleType, i));
            }
        }
        if (foundIngredients == 0) throw new IllegalArgumentException("Recipe must have at least one set of ingredients");
        this.isTiered = foundIngredients == 1;
    }

    @Override
    public boolean matches(RecipeWrapper container, Level level) {
        return matches(container.getItem(0), container.getItem(1));
    }

    public boolean matches(ItemStack equipmentItem, ItemStack itemToApply) {
        if (!equipment.test(equipmentItem)) return false;

        for (int i=0; i<GTValues.TIER_COUNT; i++) {
            Ingredient ingredient = ingredients[i];
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

        for (int i=0; i<GTValues.TIER_COUNT; i++) {
            Ingredient ingredient = ingredients[i];
            if (ingredient == null) continue;
            if (ingredient.test(itemToApply)) {
                var moduleToApply = modules[i];
                if (moduleToApply == null) continue;
                IModularItem modularItem = GTCapabilityHelper.getModularItem(equipmentItem);
                if (modularItem == null) continue;
                modularItem.attach(moduleToApply, itemToApply, slot, true);
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

            Ingredient[] ingredients = new Ingredient[GTValues.TIER_COUNT];
            ItemModuleType<?>[] modules = new ItemModuleType<?>[GTValues.TIER_COUNT];

            JsonArray ingredientArr = json.getAsJsonArray("ingredients");
            for (int i = 0; i < ingredientArr.size(); i++) {
                if (ingredientArr.get(i).isJsonNull()) ingredients[i] = null;
                else ingredients[i] = Ingredient.fromJson(ingredientArr.get(i).getAsJsonObject());
            }

            JsonArray arr = json.getAsJsonArray("modules");
            for (int i = 0; i < arr.size(); i++)
                if (arr.get(i).isJsonNull()) modules[i] = null;
                else modules[i] = GTRegistries.ITEM_MODULES.get(ResourceLocation.parse(arr.get(i).getAsString()));

            return new EquipmentFoundryRecipe(recipeId, equipment, ingredients, modules);
        }

        public EquipmentFoundryRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient equipment = Ingredient.fromNetwork(buffer);

            Ingredient[] ingredients = new Ingredient[GTValues.TIER_COUNT];
            ItemModuleType<?>[] modules = new ItemModuleType<?>[GTValues.TIER_COUNT];

            for (int i = 0; i < GTValues.TIER_COUNT; i++) {
                if (buffer.readBoolean()) continue;
                ingredients[i] = Ingredient.fromNetwork(buffer);
            }

            for (int i = 0; i < GTValues.TIER_COUNT; i++) {
                if (buffer.readBoolean()) continue;
                modules[i] = GTRegistries.ITEM_MODULES.get(buffer.readResourceLocation());
            }
            return new EquipmentFoundryRecipe(recipeId, equipment, ingredients, modules);
        }

        public void toNetwork(FriendlyByteBuf buffer, EquipmentFoundryRecipe recipe) {
            recipe.equipment.toNetwork(buffer);

            for (var ingredient : recipe.ingredients) {
                buffer.writeBoolean(ingredient == null);
                if (ingredient == null) continue;
                ingredient.toNetwork(buffer);
            }

            for (ItemModuleType<?> module : recipe.modules) {
                buffer.writeBoolean(module == null);
                if (module == null) continue;
                buffer.writeResourceLocation(module.id());
            }
        }
    }
}
