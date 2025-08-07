package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.ItemValueAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

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
    /**
     * This array's elements must be treated as immutable.
     */
    protected ItemStack[] itemStacks = null;
    private boolean changed = true;
    @Getter
    private final boolean isEmpty;
    private final Value value;

    protected SizedIngredient(Ingredient inner, int amount) {
        super(Stream.empty());
        this.amount = amount;
        this.inner = inner;
        this.isEmpty = inner.isEmpty();
        if (isEmpty || inner.getClass() != Ingredient.class) {
            this.value = null;
        } else {
            var values = ((IngredientAccessor) inner).getValues();
            this.value = values.length == 1 ? values[0] : null;
        }
    }

    protected SizedIngredient(@NotNull TagKey<Item> tag, int amount) {
        this(Ingredient.of(tag), amount);
    }

    protected SizedIngredient(ItemStack itemStack) {
        this(itemStack.hasTag() ? StrictNBTIngredient.of(itemStack) : Ingredient.of(itemStack), itemStack.getCount());
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

            return SizedIngredient.create(sizedIngredient.inner, sizedIngredient.amount);
        } else if (ingredient instanceof IntCircuitIngredient circuit) {
            return circuit;
        } else if (ingredient instanceof IntProviderIngredient intProviderIngredient) {
            var copied = IntProviderIngredient.of(intProviderIngredient.inner, intProviderIngredient.countProvider);
            if (intProviderIngredient.itemStacks != null) {
                copied.itemStacks = Arrays.stream(intProviderIngredient.itemStacks).map(ItemStack::copy)
                        .toArray(ItemStack[]::new);
            }
            if (intProviderIngredient.sampledCount != -1) {
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
        if (stack == null) return false;
        if (this.isEmpty) return stack.isEmpty();

        if (this.value instanceof TagValueAccessor tagValue) {
            return stack.is(tagValue.getTag());
        } else if (this.value instanceof ItemValueAccessor itemValue) {
            return ItemStack.isSameItem(stack, itemValue.getItem());
        }
        return inner.test(stack);
    }

    @Override
    public ItemStack @NotNull [] getItems() {
        if (getInner() instanceof IntProviderIngredient intProviderIngredient) {
            return intProviderIngredient.getItems();
        }
        if (changed || itemStacks == null) {
            var innerStacks = inner.getItems();
            this.itemStacks = new ItemStack[innerStacks.length];
            for (int i = 0; i < itemStacks.length; i++) {
                itemStacks[i] = innerStacks[i].copyWithCount(amount);
            }
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
    public int hashCode() {
        int result = amount;
        result = 31 * result + Arrays.hashCode(itemStacks);
        return result;
    }

    public static Ingredient getInner(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            return getInner(sizedIngredient.getInner());
        } else if (ingredient instanceof IntProviderIngredient intProviderIngredient) {
            return getInner(intProviderIngredient.getInner());
        }
        return ingredient;
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
