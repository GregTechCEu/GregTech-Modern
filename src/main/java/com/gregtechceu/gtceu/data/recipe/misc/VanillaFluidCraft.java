package com.gregtechceu.gtceu.data.recipe.misc;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.ShapedEnergyTransferRecipe;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VanillaFluidCraft extends ShapedRecipe {
    public static final RecipeSerializer<VanillaFluidCraft> SERIALIZER = new Serializer();

    private final FluidStack fluid;

    public VanillaFluidCraft(ResourceLocation id, String group, int width, int height,
                             NonNullList<Ingredient> recipeItems, ItemStack result, FluidStack fluid) {
        super(id, group, CraftingBookCategory.MISC, width, height, recipeItems, result);
        this.fluid = fluid;
    }

    public static class Serializer implements RecipeSerializer<VanillaFluidCraft> {

        @Override
        public VanillaFluidCraft fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            Map<String, Ingredient> key = ShapedRecipeAccessor.callKeyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            String[] pattern = ShapedRecipeAccessor.callPatternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
            int xSize = pattern[0].length();
            int ySize = pattern.length;
            NonNullList<Ingredient> dissolved = ShapedRecipeAccessor.callDissolvePattern(pattern, key, xSize, ySize);
            ItemStack result = ShapedEnergyTransferRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            JsonObject fluidJson = GsonHelper.getAsJsonObject(json, "fluid_stack");

            Fluid _fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(GsonHelper.getAsString(fluidJson, "fluid")));
            FluidStack fluid = FluidStack.create(_fluid, GsonHelper.getAsLong(fluidJson, "amount"));

            return new VanillaFluidCraft(recipeId, group, xSize, ySize, dissolved, result, fluid);
        }

        @Override
        public @Nullable VanillaFluidCraft fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int xSize = buffer.readVarInt();
            int ySize = buffer.readVarInt();
            String group = buffer.readUtf();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(xSize * ySize, Ingredient.EMPTY);
            ingredients.replaceAll($ -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();

            // Should ensure the buffer is read in proper order.
            Fluid _fluid = BuiltInRegistries.FLUID.get(buffer.readResourceLocation());
            FluidStack fluid = FluidStack.create(_fluid, buffer.readLong());

            return new VanillaFluidCraft(recipeId, group, xSize, ySize, ingredients, result, fluid);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, VanillaFluidCraft recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());
            buffer.writeUtf(recipe.getGroup());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(((ShapedRecipeAccessor) this).getResult());
            buffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.fluid.getFluid()));
            buffer.writeLong(recipe.fluid.getAmount());
        }
    }
}
