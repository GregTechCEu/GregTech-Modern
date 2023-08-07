package com.gregtechceu.gtceu.api.recipe.ingredient.fabric;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote SizedIngredientImpl
 */
public class SizedIngredientImpl extends SizedIngredient implements FabricIngredient, CustomIngredient {

    protected SizedIngredientImpl(Ingredient inner, int amount) {
        super(inner, amount);
    }

    protected SizedIngredientImpl(ItemStack inner) {
        super(inner);
    }

    protected SizedIngredientImpl(String tag, int amount) {
        super(tag, amount);
    }

    protected SizedIngredientImpl(TagKey<Item> tag, int amount) {
        super(tag, amount);
    }

    public static SizedIngredient create(ItemStack inner) {
        return new SizedIngredientImpl(inner);
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return inner.test(stack);
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        return Arrays.stream(getItems()).toList();
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public TagKey<Item> getTag() {
        return tag;
    }

    public static SizedIngredient create(Ingredient inner, int amount) {
        return new SizedIngredientImpl(inner, amount);
    }

    public static SizedIngredient create(Ingredient inner) {
        if (inner instanceof CustomIngredientImpl customIngredient && customIngredient.getCustomIngredient() instanceof SizedIngredient sizedIngredient) {
            return SizedIngredient.copy(sizedIngredient);
        }
        return new SizedIngredientImpl(inner, 1);
    }

    public static SizedIngredient create(TagKey<Item> tag, int amount) {
        return new SizedIngredientImpl(tag, amount);
    }

    public static SizedIngredient create(String tag, int amount) {
        return new SizedIngredientImpl(tag, amount);
    }

    @Override
    public @Nullable SizedIngredientImpl getCustomIngredient() {
        return this;
    }

    public static SizedIngredient fromJson(JsonObject json) {
        return Serializer.INSTANCE.read(json);
    }

    public static class Serializer implements CustomIngredientSerializer<SizedIngredientImpl> {

        public static Serializer INSTANCE = new Serializer();

        @Override
        public ResourceLocation getIdentifier() {
            return SizedIngredient.TYPE;
        }

        @Override
        public SizedIngredientImpl read(JsonObject json) {
            int amount = json.get("count").getAsInt();
            if (json.has("tag")) {
                return new SizedIngredientImpl(json.get("tag").getAsString(), amount);
            } else {
                Ingredient inner = Ingredient.fromJson(json.get("ingredient"));
                return new SizedIngredientImpl(inner, amount);
            }
        }

        @Override
        public void write(JsonObject json, SizedIngredientImpl ingredient) {
            json.addProperty("count", ingredient.getAmount());
            if (ingredient.getTag() != null) {
                json.addProperty("tag", ingredient.getTag().location().toString());
            } else {
                json.add("ingredient", ingredient.getInner().toJson());
            }
        }

        @Override
        public SizedIngredientImpl read(FriendlyByteBuf buffer) {
            int amount = buffer.readVarInt();
            if (buffer.readBoolean()) {
                return new SizedIngredientImpl(buffer.readUtf(), amount).getCustomIngredient();
            } else {
                return new SizedIngredientImpl(Ingredient.fromNetwork(buffer), amount).getCustomIngredient();
            }
        }

        @Override
        public void write(FriendlyByteBuf buffer, SizedIngredientImpl ingredient) {
            buffer.writeVarInt(ingredient.getAmount());
            if (ingredient.getTag() != null) {
                buffer.writeBoolean(true);
                buffer.writeUtf(ingredient.getTag().location().toString());
            } else {
                buffer.writeBoolean(false);
                ingredient.getInner().toNetwork(buffer);
            }
        }
    }

}
