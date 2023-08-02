package com.gregtechceu.gtceu.api.recipe.ingredient.forge;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote SizedIngredientImpl
 */
public class SizedIngredientImpl extends SizedIngredient {

    protected SizedIngredientImpl(Ingredient inner, int amount) {
        super(inner, amount);
    }

    protected SizedIngredientImpl(String tag, int amount) {
        super(tag, amount);
    }

    protected SizedIngredientImpl(TagKey<Item> tag, int amount) {
        super(tag, amount);
    }

    protected SizedIngredientImpl(ItemStack inner) {
        super(inner);
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

    public static SizedIngredient create(ItemStack inner) {
        return new SizedIngredientImpl(inner);
    }

    @Override
    @Nonnull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static SizedIngredient fromJson(JsonObject json) {
        return SERIALIZER.parse(json);
    }

    public static final IIngredientSerializer<SizedIngredientImpl> SERIALIZER = new IIngredientSerializer<>() {
        @Override
        public @NotNull SizedIngredientImpl parse(FriendlyByteBuf buffer) {
            int amount = buffer.readVarInt();
            if (buffer.readBoolean()) {
                return new SizedIngredientImpl(buffer.readUtf(), amount);
            } else {
                return new SizedIngredientImpl(Ingredient.fromNetwork(buffer), amount);
            }
        }

        @Override
        public @NotNull SizedIngredientImpl parse(JsonObject json) {
            int amount = json.get("count").getAsInt();
            if (json.has("tag")) {
                return new SizedIngredientImpl(json.get("tag").getAsString(), amount);
            } else {
                Ingredient inner = Ingredient.fromJson(json.get("ingredient"));
                return new SizedIngredientImpl(inner, amount);
            }
        }

        @Override
        public void write(FriendlyByteBuf buffer, SizedIngredientImpl ingredient) {
            buffer.writeVarInt(ingredient.getAmount());
            if (ingredient.tag != null) {
                buffer.writeBoolean(true);
                buffer.writeUtf(ingredient.tag.location().toString());
            } else {
                buffer.writeBoolean(false);
                ingredient.inner.toNetwork(buffer);
            }
        }
    };
}
