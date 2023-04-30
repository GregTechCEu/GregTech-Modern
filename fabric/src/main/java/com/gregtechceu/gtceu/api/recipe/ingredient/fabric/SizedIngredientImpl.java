package com.gregtechceu.gtceu.api.recipe.ingredient.fabric;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
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
public class SizedIngredientImpl extends SizedIngredient implements FabricIngredient {

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

    public static SizedIngredient create(Ingredient inner, int amount) {
        return new SizedIngredientImpl(inner, amount);
    }

    public static SizedIngredient create(TagKey<Item> tag, int amount) {
        return new SizedIngredientImpl(tag, amount);
    }

    public static SizedIngredient create(String tag, int amount) {
        return new SizedIngredientImpl(tag, amount);
    }

    @Override
    public @Nullable CustomSizedIngredient getCustomIngredient() {
        return new CustomSizedIngredient(this);
    }

    public static class CustomSizedIngredient implements CustomIngredient {

        public final SizedIngredientImpl vanilla;

        public CustomSizedIngredient(SizedIngredientImpl vanilla) {
            this.vanilla = vanilla;
        }

        public CustomSizedIngredient(Ingredient inner, int amount) {
            this.vanilla = new SizedIngredientImpl(inner, amount);
        }

        public CustomSizedIngredient(String tag, int amount) {
            this.vanilla = new SizedIngredientImpl(tag, amount);
        }

        @Override
        public boolean test(ItemStack stack) {
            return vanilla.test(stack);
        }

        @Override
        public List<ItemStack> getMatchingStacks() {
            return Arrays.stream(vanilla.getItems()).toList();
        }

        @Override
        public boolean requiresTesting() {
            return true;
        }

        @Override
        public CustomIngredientSerializer<?> getSerializer() {
            return Serializer.INSTANCE;
        }

        public int getAmount() {
            return vanilla.amount;
        }

        public Ingredient getInner() {
            return vanilla.inner;
        }

        public String getTag() {
            return vanilla.tag;
        }

        /*@Override
        public SizedIngredientImpl toVanilla() {
            return vanilla;
        }*/
    }


    public static class Serializer implements CustomIngredientSerializer<CustomSizedIngredient> {

        public static Serializer INSTANCE = new Serializer();

        @Override
        public ResourceLocation getIdentifier() {
            return SizedIngredient.TYPE;
        }

        @Override
        public CustomSizedIngredient read(JsonObject json) {
            int amount = json.get("count").getAsInt();
            if (json.has("tag")) {
                return new CustomSizedIngredient(json.get("tag").getAsString(), amount);
            } else {
                Ingredient inner = Ingredient.fromJson(json.get("ingredient"));
                return new CustomSizedIngredient(inner, amount);
                //return new SizedIngredientImpl(inner, amount).getCustomIngredient();
            }
        }

        @Override
        public void write(JsonObject json, CustomSizedIngredient ingredient) {
            var vanilla = ingredient.toVanilla();
            json.addProperty("count", ingredient.getAmount());
            if (ingredient.getTag() != null) {
                json.addProperty("tag", ingredient.getTag());
            } else {
                json.add("ingredient", ingredient.getInner().toJson());
            }
        }

        @Override
        public CustomSizedIngredient read(FriendlyByteBuf buffer) {
            int amount = buffer.readVarInt();
            if (buffer.readBoolean()) {
                return new SizedIngredientImpl(buffer.readUtf(), amount).getCustomIngredient();
            } else {
                return new SizedIngredientImpl(Ingredient.fromNetwork(buffer), amount).getCustomIngredient();
            }
        }

        @Override
        public void write(FriendlyByteBuf buffer, CustomSizedIngredient ingredient) {
            var vanilla = ingredient.toVanilla();
            buffer.writeVarInt(ingredient.getAmount());
            if (ingredient.getTag() != null) {
                buffer.writeBoolean(true);
                buffer.writeUtf(ingredient.getTag());
            } else {
                buffer.writeBoolean(false);
                //vanilla.inner.toNetwork(buffer);
                ingredient.getInner().toNetwork(buffer);
            }
        }
    }

}
