package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.TrueNBTPredicate;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class NBTPredicateIngredient extends AbstractIngredient {

    public static final ResourceLocation TYPE = GTCEu.id("nbt_predicate");
    public static final NBTPredicate ALWAYS_TRUE = new TrueNBTPredicate();
    private final NBTPredicate predicate;
    private final ItemStack stack;

    public NBTPredicateIngredient(ItemStack stack, NBTPredicate predicate) {
        super(Stream.of(new Ingredient.ItemValue(stack)));
        this.stack = stack;
        this.predicate = predicate;
    }

    protected NBTPredicateIngredient(ItemStack stack) {
        this(stack, ALWAYS_TRUE);
    }

    public static NBTPredicateIngredient of(ItemStack stack, NBTPredicate predicate) {
        return new NBTPredicateIngredient(stack, predicate);
    }

    public static NBTPredicateIngredient of(ItemStack stack) {
        return NBTPredicateIngredient.of(stack, ALWAYS_TRUE);
    }

    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        } else {
            return this.stack.getItem() == input.getItem() &&
                    predicate.test(input.getOrCreateTag());
        }
    }

    public boolean isSimple() {
        return false;
    }

    public @NotNull IIngredientSerializer<? extends Ingredient> getSerializer() {
        return NBTPredicateIngredient.Serializer.INSTANCE;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("item", ForgeRegistries.ITEMS.getKey(this.stack.getItem()).toString());
        json.addProperty("count", this.stack.getCount());
        if (this.stack.hasTag()) {
            json.addProperty("nbt", this.stack.getTag().toString());
        }
        json.add("predicate", predicate.toJson());
        return json;
    }

    public static class Serializer implements IIngredientSerializer<NBTPredicateIngredient> {

        public static final NBTPredicateIngredient.Serializer INSTANCE = new NBTPredicateIngredient.Serializer();

        public @NotNull NBTPredicateIngredient parse(FriendlyByteBuf buffer) {
            var stack = buffer.readItem();
            var json = buffer.readUtf();
            var predicate = NBTPredicates.fromJson(GsonHelper.parse(json));
            return new NBTPredicateIngredient(stack, predicate);
        }

        public @NotNull NBTPredicateIngredient parse(@NotNull JsonObject json) {
            var stack = CraftingHelper.getItemStack(json, true);
            var predicate = NBTPredicates.fromJson(GsonHelper.getAsJsonObject(json, "predicate"));

            return new NBTPredicateIngredient(stack, predicate);
        }

        public void write(FriendlyByteBuf buffer, NBTPredicateIngredient ingredient) {
            buffer.writeItem(ingredient.stack);
            buffer.writeUtf(ingredient.toJson().toString());
        }
    }
}
