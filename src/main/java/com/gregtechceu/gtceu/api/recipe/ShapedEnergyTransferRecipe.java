package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.Map;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class ShapedEnergyTransferRecipe extends ShapedRecipe {

    public static final RecipeSerializer<ShapedEnergyTransferRecipe> SERIALIZER = new Serializer();

    @Getter
    private final Ingredient chargeIngredient;
    @Getter
    private final boolean transferMaxCharge;
    @Getter
    private final boolean overrideCharge;

    public ShapedEnergyTransferRecipe(ResourceLocation id, String group, int width, int height,
                                      Ingredient chargeIngredient, boolean overrideCharge, boolean transferMaxCharge,
                                      NonNullList<Ingredient> recipeItems, ItemStack result) {
        super(id, group, CraftingBookCategory.MISC, width, height, recipeItems, result);
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.assemble(craftingContainer, registryAccess);
        for (ItemStack chargeStack : chargeIngredient.getItems()) {
            for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
                if (ItemStack.isSameItem(craftingContainer.getItem(i), chargeStack)) {
                    ItemStack stack = craftingContainer.getItem(i);
                    IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
                    if (electricItem != null) {
                        maxCharge += electricItem.getMaxCharge();
                        charge += electricItem.getCharge();
                        resultStack.getOrCreateTag().putLong("MaxCharge", maxCharge);
                        resultStack.getOrCreateTag().putLong("Charge", charge);
                        return resultStack;
                    }
                }
            }
        }
        return resultStack;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.getResultItem(registryAccess);
        for (ItemStack chargeStack : chargeIngredient.getItems()) {
            IElectricItem electricItem = GTCapabilityHelper.getElectricItem(chargeStack);
            if (electricItem != null) {
                maxCharge += electricItem.getMaxCharge();
                charge += electricItem.getCharge();
                resultStack.getOrCreateTag().putLong("MaxCharge", maxCharge);
                resultStack.getOrCreateTag().putLong("Charge", charge);
                return resultStack;
            }
        }
        return resultStack;
    }

    public static class Serializer implements RecipeSerializer<ShapedEnergyTransferRecipe> {

        @Override
        public ShapedEnergyTransferRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            Map<String, Ingredient> key = ShapedRecipeAccessor.callKeyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            String[] pattern = ShapedRecipeAccessor.callPatternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
            int xSize = pattern[0].length();
            int ySize = pattern.length;
            NonNullList<Ingredient> dissolved = ShapedRecipeAccessor.callDissolvePattern(pattern, key, xSize, ySize);
            boolean overrideCharge = GsonHelper.getAsBoolean(json, "overrideCharge");
            boolean transferMaxCharge = GsonHelper.getAsBoolean(json, "transferMaxCharge");
            Ingredient chargeIngredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "chargeIngredient"));
            ItemStack result = ShapedEnergyTransferRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new ShapedEnergyTransferRecipe(recipeId, group, xSize, ySize, chargeIngredient, overrideCharge,
                    transferMaxCharge, dissolved, result);
        }

        @Override
        public ShapedEnergyTransferRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int xSize = buffer.readVarInt();
            int ySize = buffer.readVarInt();
            boolean overrideCharge = buffer.readBoolean();
            boolean transferMaxCharge = buffer.readBoolean();
            Ingredient chargeIngredient = Ingredient.fromNetwork(buffer);
            String group = buffer.readUtf();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(xSize * ySize, Ingredient.EMPTY);
            ingredients.replaceAll($ -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();
            return new ShapedEnergyTransferRecipe(recipeId, group, xSize, ySize, chargeIngredient, overrideCharge,
                    transferMaxCharge, ingredients, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedEnergyTransferRecipe recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());
            buffer.writeBoolean(recipe.isOverrideCharge());
            buffer.writeBoolean(recipe.isTransferMaxCharge());
            recipe.getChargeIngredient().toNetwork(buffer);
            buffer.writeUtf(recipe.getGroup());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(((ShapedRecipeAccessor) recipe).getResult());
        }
    }
}
