package com.gregtechceu.gtceu.common.recipe.type;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@RequiredArgsConstructor
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EquipmentFoundryRecipe implements Recipe<RecipeWrapper> {

    @Getter
    private final ResourceLocation id;
    @Getter
    private final Ingredient equipment;
    @Getter
    private final Ingredient ingredient;
    @Getter
    private final ItemModule[] modules;

    public @Nullable ItemModule getModule(int tier) {
        int lowestTier = (modules[0] instanceof ITieredItemModule tieredModule) ? tieredModule.getTier() :
                GTValues.ULV;
        if (modules.length == 1) return modules[0];
        if (tier < lowestTier) return null;
        if (tier - lowestTier >= modules.length) return null;
        return modules[tier - lowestTier];
    }

    public @Nullable ItemModule getModule(ItemStack ingredient) {
        int tier = GTUtil.getTier(ingredient.getItem());
        return getModule(tier);
    }

    @Override
    public boolean matches(RecipeWrapper container, Level level) {
        return matches(container, -1);
    }

    public boolean matches(RecipeWrapper container, int slot) {
        ItemStack foundItem = null, foundIngredient = null;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (equipment.test(stack) && foundItem == null) {
                    foundItem = stack;
                } else if (ingredient.test(stack)) {
                    foundIngredient = stack;
                }
            }
        }
        if (foundIngredient == null || foundItem == null) return false;
        ItemModule module = getModule(foundIngredient);
        IModularItem modularItem = GTCapabilityHelper.getModularItem(foundItem);
        if (module == null || modularItem == null) return false;
        return (slot == -1 ? modularItem.attach(module, true) : modularItem.attach(module, slot, true)) != null;
    }

    @Override
    public ItemStack assemble(RecipeWrapper container, RegistryAccess registryAccess) {
        return assemble(container, -1);
    }

    public ItemStack assemble(RecipeWrapper container, int slot) {
        ItemStack result = ItemStack.EMPTY;
        ItemStack foundIngredient = null;

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (equipment.test(stack) && result.isEmpty()) {
                result = stack.copy();
            } else if (ingredient.test(stack)) {
                foundIngredient = stack;
                break;
            }
        }

        if (foundIngredient == null || result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemModule module = getModule(foundIngredient);
        IModularItem modularItem = GTCapabilityHelper.getModularItem(result);
        if (modularItem == null || module == null) return ItemStack.EMPTY;
        if (!module.canApplyTo(result)) return ItemStack.EMPTY;
        AppliedItemModule attachedModule = slot == -1 ? modularItem.attach(module, false) :
                modularItem.attach(module, slot, false);
        if (attachedModule != null) {
            attachedModule.setModuleItem(foundIngredient);
            return result;
        }
        return ItemStack.EMPTY;
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
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"), false);
            JsonArray arr = json.getAsJsonArray("modifier");
            ItemModule[] modifier = new ItemModule[arr.size()];
            for (int i = 0; i < arr.size(); i++)
                modifier[i] = ItemModule.getModuleById(new ResourceLocation(arr.get(i).getAsString()));

            return new EquipmentFoundryRecipe(recipeId, equipment, ingredient, modifier);
        }

        public EquipmentFoundryRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient equipment = Ingredient.fromNetwork(buffer);
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            int length = buffer.readInt();
            ItemModule[] modifier = new ItemModule[length];
            for (int i = 0; i < length; i++) modifier[i] = ItemModule.getModuleById(buffer.readResourceLocation());
            return new EquipmentFoundryRecipe(recipeId, equipment, ingredient, modifier);
        }

        public void toNetwork(FriendlyByteBuf buffer, EquipmentFoundryRecipe recipe) {
            recipe.equipment.toNetwork(buffer);
            recipe.ingredient.toNetwork(buffer);
            buffer.writeInt(recipe.modules.length);
            for (ItemModule module : recipe.modules) buffer.writeResourceLocation(module.getId());
        }
    }
}
