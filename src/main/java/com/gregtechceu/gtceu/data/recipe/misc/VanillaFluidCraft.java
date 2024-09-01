package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.recipe.ShapedEnergyTransferRecipe;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;

import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VanillaFluidCraft extends ShapedRecipe {

    public static final RecipeSerializer<VanillaFluidCraft> SERIALIZER = new Serializer();

    // Issue with differences between the LDLib Fluidstack and the Forge one
    // means it's actually easier to grab the Forge Fluidstack, which absolutely
    // will have issues as Drum's storage uses the LDLib FluidStack which is
    // a long and not an int.
    private final FluidStack fluid;

    public VanillaFluidCraft(ResourceLocation id, String group, int width, int height,
                             NonNullList<Ingredient> recipeItems, ItemStack result, FluidStack fluid) {
        super(id, group, CraftingBookCategory.MISC, width, height, recipeItems, result);
        this.fluid = fluid;
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inv, @NotNull Level level) {
        if (!super.matches(inv, level)) {
            return false;
        }

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack iStack = inv.getItem(i);

            if (FluidUtil.getFluidHandler(iStack).isPresent()) {
                // This is ugly as shit. Unless I'm missing something (very possible), this is the only way
                // to handle the fact that there exist 2 different FluidStacks both with different bit-lengths.
                // Checks that the inventory stack meets the minimum required.
                if (!FluidUtil.getFluidContained(iStack).get().containsFluid(this.fluid)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer inv) {
        NonNullList<ItemStack> items = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < items.size(); i++) {
            ItemStack iStack = inv.getItem(i);

            if (FluidUtil.getFluidHandler(iStack).isPresent()) {
                // Drain then set.
                ItemStack copy = iStack.copy();
                FluidUtil.getFluidHandler(copy)
                        .map(f -> f.drain(this.fluid.getAmount(), IFluidHandler.FluidAction.EXECUTE));
                items.set(i, copy);
            } else {
                items.set(i, iStack.getCraftingRemainingItem());
            }
        }

        return items;
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

            Fluid _fluid = BuiltInRegistries.FLUID
                    .get(new ResourceLocation(GsonHelper.getAsString(fluidJson, "fluid")));
            FluidStack fluid = new FluidStack(_fluid, GsonHelper.getAsInt(fluidJson, "amount"));

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
            FluidStack fluid = new FluidStack(_fluid, buffer.readInt());

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
            buffer.writeInt(recipe.fluid.getAmount());
        }
    }
}
