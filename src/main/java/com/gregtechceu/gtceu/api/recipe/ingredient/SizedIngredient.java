package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Stream;

public class SizedIngredient extends Ingredient {
    public static final ResourceLocation TYPE = GTCEu.id("sized");

    @Getter
    protected final int amount;
    @Getter
    protected final Ingredient inner;
    protected ItemStack[] itemStacks = null;

    protected SizedIngredient(Ingredient inner, int amount) {
        super(Stream.empty());
        this.amount = amount;
        this.inner = inner;
    }

    protected SizedIngredient(@NotNull TagKey<Item> tag, int amount) {
        this(Ingredient.of(tag), amount);
    }

    protected SizedIngredient(ItemStack itemStack) {
        this((itemStack.hasTag() || itemStack.getDamageValue() > 0) ? NBTIngredient.createNBTIngredient(itemStack) : Ingredient.of(itemStack), itemStack.getCount());
    }

    public static SizedIngredient create(ItemStack inner) {
        return new SizedIngredient(inner);
    }

    public static SizedIngredient create(Ingredient inner, int amount) {
        return new SizedIngredient(inner, amount);
    }

    public static SizedIngredient create(Ingredient inner) {
        return new SizedIngredient(inner, 1);
    }

    public static SizedIngredient create(TagKey<Item> tag, int amount) {
        return new SizedIngredient(tag, amount);
    }

    public static SizedIngredient copy(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            var copied = SizedIngredient.create(sizedIngredient.inner, sizedIngredient.amount);
            if (sizedIngredient.itemStacks != null) {
                copied.itemStacks = Arrays.stream(sizedIngredient.itemStacks).map(ItemStack::copy).toArray(ItemStack[]::new);
            }
            return copied;
        }
        return SizedIngredient.create(ingredient);
    }

    @Override
    @Nonnull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static SizedIngredient fromJson(JsonObject json) {
        return SERIALIZER.parse(json);
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("count", amount);
        json.add("ingredient", inner.toJson());
        return json;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return inner.test(stack);
    }

    @Override
    public ItemStack @NotNull [] getItems() {
        if (itemStacks == null)
            itemStacks = Arrays.stream(inner.getItems()).map(i -> {
                ItemStack ic = i.copy();
                ic.setCount(amount);
                return ic;
            }).toArray(ItemStack[]::new);
        return itemStacks;
    }

    @Override
    public @NotNull IntList getStackingIds() {
        return inner.getStackingIds();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    public static final IIngredientSerializer<SizedIngredient> SERIALIZER = new IIngredientSerializer<>() {
        @Override
        public @NotNull SizedIngredient parse(FriendlyByteBuf buffer) {
            int amount = buffer.readVarInt();
            return new SizedIngredient(Ingredient.fromNetwork(buffer), amount);
        }

        @Override
        public @NotNull SizedIngredient parse(JsonObject json) {
            int amount = json.get("count").getAsInt();
            Ingredient inner = Ingredient.fromJson(json.get("ingredient"));
            return new SizedIngredient(inner, amount);
        }

        @Override
        public void write(FriendlyByteBuf buffer, SizedIngredient ingredient) {
            buffer.writeVarInt(ingredient.getAmount());
            ingredient.inner.toNetwork(buffer);
        }
    };
}
