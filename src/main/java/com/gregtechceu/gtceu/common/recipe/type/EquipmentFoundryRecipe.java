package com.gregtechceu.gtceu.common.recipe.type;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class EquipmentFoundryRecipe implements Recipe<RecipeWrapper> {

    @Getter
    private final ResourceLocation id;
    @Getter
    private final Ingredient equipment;
    @Getter
    private final boolean isTiered;
    @Getter
    private final List<TierEntry> entries;

    public EquipmentFoundryRecipe(ResourceLocation id, Ingredient equipment, List<TierEntry> entries) {
        if (entries.isEmpty())
            throw new IllegalArgumentException("Equipment foundry recipe must have at least one entry");

        this.id = id;
        this.entries = entries;
        this.equipment = equipment;
        this.isTiered = entries.size() == 1;
    }

    public EquipmentFoundryRecipe(ResourceLocation id, Ingredient equipment, @Nullable Ingredient[] moduleIngredients,
                                  @Nullable ItemModule[] modules) {
        if (moduleIngredients.length != modules.length)
            throw new IllegalArgumentException("Ingredient and module array length must match");

        List<TierEntry> entries = new ArrayList<>();
        for (int i = 0; i < moduleIngredients.length; i++) {
            var ingredient = moduleIngredients[i];
            var module = modules[i];
            if (ingredient == null || module == null) continue;
            entries.add(new TierEntry(ingredient, module));
        }

        this.id = id;
        this.equipment = equipment;
        this.entries = entries;
        this.isTiered = moduleIngredients.length == 1;
    }

    @Override
    public boolean matches(RecipeWrapper container, Level level) {
        return matches(container.getItem(0), container.getItem(1));
    }

    public boolean matches(ItemStack equipmentItem, ItemStack itemToApply) {
        if (!equipment.test(equipmentItem)) return false;

        for (var entry : entries) {
            Ingredient ingredient = entry.ingredient();
            ItemModule moduleToApply = entry.moduleForTier();
            if (ingredient.test(itemToApply)) {
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
        for (var entry : entries) {
            Ingredient ingredient = entry.ingredient();
            ItemModule moduleToApply = entry.moduleForTier();
            if (ingredient.test(itemToApply)) {
                IModularItem modularItem = GTCapabilityHelper.getModularItem(equipmentItem);
                if (modularItem == null) continue;
                if (modularItem.attach(moduleToApply, itemToApply, slot, false) != null) return;

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

    public record TierEntry(Ingredient ingredient, ItemModule moduleForTier) {}

    public static class Serializer implements RecipeSerializer<EquipmentFoundryRecipe> {

        public static void toJson(JsonObject json, EquipmentFoundryRecipe recipe) {
            json.add("equipment", recipe.equipment.toJson());

            JsonArray entryArr = new JsonArray();
            for (var entry : recipe.entries) {
                var obj = new JsonObject();
                obj.add("ingredient", entry.ingredient.toJson());
                obj.add("module", new JsonPrimitive(entry.moduleForTier.getId().toString()));
                entryArr.add(obj);
            }
            json.add("entries", entryArr);
        }

        public EquipmentFoundryRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient equipment = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "equipment"), false);

            JsonArray entryArr = json.getAsJsonArray("entries");
            List<TierEntry> entries = new ArrayList<>();

            for (JsonElement entry : entryArr) {
                var obj = entry.getAsJsonObject();
                Ingredient ing = Ingredient.fromJson(obj.getAsJsonObject("ingredient"));
                ItemModule module = Objects.requireNonNull(GTRegistries.ITEM_MODULES.get(ResourceLocation.parse(obj.getAsJsonPrimitive("module").getAsString())));
                entries.add(new TierEntry(ing, module));
            }

            entries.sort(Comparator.comparingInt(
                    v -> v.moduleForTier() instanceof ITieredItemModule tieredItemModule ? tieredItemModule.getTier() :
                            -1));

            return new EquipmentFoundryRecipe(recipeId, equipment, entries);
        }

        public EquipmentFoundryRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient equipment = Ingredient.fromNetwork(buffer);
            List<TierEntry> entries = buffer.readList(b -> {
                Ingredient ing = Ingredient.fromNetwork(b);
                ItemModule moduleType = GTRegistries.ITEM_MODULES.get(buffer.readResourceLocation());
                return new TierEntry(ing, Objects.requireNonNull(moduleType));
            });
            entries.sort(Comparator.comparingInt(
                    v -> v.moduleForTier() instanceof ITieredItemModule tieredItemModule ? tieredItemModule.getTier() :
                            -1));
            return new EquipmentFoundryRecipe(recipeId, equipment, entries);
        }

        public void toNetwork(FriendlyByteBuf buffer, EquipmentFoundryRecipe recipe) {
            recipe.equipment.toNetwork(buffer);
            buffer.writeCollection(recipe.entries, (b, v) -> {
                v.ingredient.toNetwork(b);
                b.writeResourceLocation(v.moduleForTier.getId());
            });
        }
    }
}
