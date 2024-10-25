package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public class SizedIngredient extends Ingredient {

    public static final ResourceLocation TYPE = GTCEu.id("sized");

    @Getter
    protected int amount;
    @Getter
    protected final Ingredient inner;
    protected ItemStack[] itemStacks = null;
    private boolean changed = true;

    protected SizedIngredient(Ingredient inner, int amount) {
        super(Stream.empty());
        this.amount = amount;
        this.inner = inner;
    }

    protected SizedIngredient(@NotNull TagKey<Item> tag, int amount) {
        this(Ingredient.of(tag), amount);
    }

    protected SizedIngredient(ItemStack itemStack) {
        this((itemStack.hasTag() || itemStack.getDamageValue() > 0) ? NBTIngredient.createNBTIngredient(itemStack) :
                Ingredient.of(itemStack), itemStack.getCount());
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

    public static Ingredient copy(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            if (sizedIngredient.inner instanceof IntProviderIngredient intProviderIngredient) {
                return copy(intProviderIngredient);
            }

            var copied = SizedIngredient.create(sizedIngredient.inner, sizedIngredient.amount);
            if (sizedIngredient.itemStacks != null) {
                copied.itemStacks = Arrays.stream(sizedIngredient.itemStacks).map(ItemStack::copy)
                        .toArray(ItemStack[]::new);
            }
            return copied;
        } else if (ingredient instanceof IntCircuitIngredient circuit) {
            return circuit.copy();
        } else if (ingredient instanceof IntProviderIngredient intProviderIngredient) {
            var copied = new IntProviderIngredient(intProviderIngredient.inner, intProviderIngredient.countProvider);
            if (intProviderIngredient.itemStacks != null) {
                copied.itemStacks = Arrays.stream(intProviderIngredient.itemStacks).map(ItemStack::copy)
                        .toArray(ItemStack[]::new);
            }
            if (intProviderIngredient.sampledCount != null) {
                copied.sampledCount = intProviderIngredient.sampledCount;
            }
            return copied;
        }
        return SizedIngredient.create(ingredient);
    }

    @Override
    @NotNull
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
        if (getInner() instanceof IntProviderIngredient intProviderIngredient) {
            return intProviderIngredient.getItems();
        }
        if (changed || itemStacks == null) {
            itemStacks = Arrays.stream(inner.getItems()).map(i -> {
                ItemStack ic = i.copy();
                ic.setCount(amount);
                return ic;
            }).toArray(ItemStack[]::new);
            changed = false;
        }
        return itemStacks;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.changed = true;
    }

    @Override
    public @NotNull IntList getStackingIds() {
        return inner.getStackingIds();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public int hashCode() {
        int result = amount;
        result = 31 * result + Arrays.hashCode(itemStacks);
        return result;
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
