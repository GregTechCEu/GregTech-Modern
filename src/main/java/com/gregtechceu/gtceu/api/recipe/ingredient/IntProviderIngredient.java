package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Allows an {@link Ingredient} to be created with a ranged {@code count}, which will be randomly rolled upon recipe
 * start (input) / completion (output).
 * Instantiated using {@link IntProviderIngredient#of()}, with a {@link Ingredient} or {@link ItemStack},
 * and an {@link IntProvider}.
 * Functions similarly to {@link IntProviderFluidIngredient}.
 */
public class IntProviderIngredient extends Ingredient implements IRangedIngredient<SizedIngredient> {

    public static final ResourceLocation TYPE = GTCEu.id("int_provider");
    public static final ItemStack[] EMPTY_STACK_ARRAY = new ItemStack[0];

    @Getter
    protected final IntProvider countProvider;
    /**
     * The last result of {@link IntProviderIngredient#rollSampledCount(RandomSource)}. -1 if not rolled.
     */
    @Getter
    protected int sampledCount = -1;
    /**
     * The {@link Ingredient} to have a ranged amount.
     */
    @Getter
    protected final Ingredient inner;
    @Setter
    protected ItemStack[] itemStacks = null;
    @Getter
    private int amount;
    private boolean changed = true;

    protected IntProviderIngredient(Ingredient inner, IntProvider countProvider) {
        super(Stream.empty());
        this.inner = inner;
        this.countProvider = countProvider;
        this.amount = getMaxRoll();
    }

    protected IntProviderIngredient(Ingredient inner, IntProvider countProvider, int sampledCount, int amount) {
        super(Stream.empty());
        this.inner = inner;
        this.countProvider = countProvider;
        this.sampledCount = sampledCount;
        this.amount = amount;
    }

    public IntProviderIngredient copy() {
        return new IntProviderIngredient(this.inner, this.countProvider, this.sampledCount, this.amount);
    }

    /**
     * @param inner         {@link Ingredient}
     * @param countProvider usually as {@link net.minecraft.util.valueproviders.UniformInt#of(int, int)}
     */
    public static IntProviderIngredient of(Ingredient inner, IntProvider countProvider) {
        Preconditions.checkArgument(countProvider.getMinValue() >= 0,
                "IntProviderIngredient must have a min value of at least 0.");
        return new IntProviderIngredient(inner, countProvider);
    }

    /**
     * @param stack         {@link ItemStack}
     * @param countProvider usually as {@link net.minecraft.util.valueproviders.UniformInt#of(int, int)}
     */
    public static IntProviderIngredient of(ItemStack stack, IntProvider countProvider) {
        Ingredient inner = stack.hasTag() ? StrictNBTIngredient.of(stack) : Ingredient.of(stack);
        return of(inner, countProvider);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return inner.test(stack);
    }

    /**
     * Gets a usable {@link ItemStack ItemStack[]} from this {@link IntProviderIngredient}.
     * If this ingredient has not yet had its {@link IntProviderIngredient#sampledCount} rolled, rolls it.
     * 
     * @return a {@link ItemStack ItemStack[]} with count {@link IntProviderIngredient#sampledCount}
     */
    @Override
    public ItemStack @NotNull [] getItems() {
        if (changed || itemStacks == null) {
            changed = false;
            if (!isRolled()) {
                setAmount(rollSampledCount());
                if (getAmount() == 0) {
                    itemStacks = EMPTY_STACK_ARRAY;
                    return EMPTY_STACK_ARRAY;
                }
            }
            var innerStacks = inner.getItems();
            this.itemStacks = new ItemStack[innerStacks.length];
            for (int i = 0; i < itemStacks.length; i++) {
                itemStacks[i] = innerStacks[i].copyWithCount(getAmount());
            }
        }
        return itemStacks;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.changed = true;
    }

    /**
     * Gets a {@link ItemStack} containing the maximum possible output from this {@link IntProviderIngredient}.
     * Mainly used for things like Recipe provider simulations to see if there is enough inventory space to handle
     * the recipe output.
     * 
     * @return a {@link ItemStack} with count {@link IntProvider#getMaxValue()}
     */
    public @NotNull ItemStack getMaxSizeStack() {
        if (inner.getItems().length == 0) return ItemStack.EMPTY;
        else return inner.getItems()[0].copyWithCount(countProvider.getMaxValue());
    }

    /**
     * If this ingredient has not yet had its {@link IntProviderIngredient#sampledCount} rolled, rolls it and returns
     * the roll.
     * If it has, returns the existing roll.
     * 
     * @param random {@link RandomSource}, must be threadsafe, usually called using {@link GTValues#RNG}.
     * @return the count rolled
     */
    public int rollSampledCount(@NotNull RandomSource random) {
        if (!isRolled()) {
            sampledCount = countProvider.sample(random);
            this.setAmount(sampledCount);
        }
        return sampledCount;
    }

    /**
     * Also sets the Amount of this ingredient
     */
    public void setSampledCount(int count) {
        this.sampledCount = count;
        this.setAmount(count);
    }

    @Override
    public SizedIngredient replace() {
        return SizedIngredient.create(inner, getAmount());
    }

    /**
     * Resets the random roll on this ingredient
     */
    public void reset() {
        sampledCount = -1;
        setAmount(getMaxRoll());
        itemStacks = null;
    }

    @Override
    public @NotNull IntList getStackingIds() {
        return inner.getStackingIds();
    }

    @Override
    public boolean isEmpty() {
        return this.getAmount() == 0 || inner.isEmpty();
    }

    @Override
    @NotNull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    /**
     * @param json containing
     *             <ul>
     *             <li>{@code type}</li>
     *             <li>{@code count_provider}</li>
     *             <li>{@code ingredient}</li>
     *             </ul>
     */
    public static IntProviderIngredient fromJson(JsonObject json) {
        return SERIALIZER.parse(json);
    }

    /**
     * Properties:
     * <ul>
     * <li>{@code type}</li>
     * <li>{@code count_provider}</li>
     * <li>{@code ingredient}</li>
     * </ul>
     */
    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.add("count_provider", IntProvider.CODEC.encodeStart(JsonOps.INSTANCE, countProvider)
                .getOrThrow(false, GTCEu.LOGGER::error));
        json.add("ingredient", inner.toJson());
        json.addProperty("sampledCount", sampledCount);
        json.addProperty("amount", amount);
        return json;
    }

    public static final IIngredientSerializer<IntProviderIngredient> SERIALIZER = new IIngredientSerializer<>() {

        @Override
        public @NotNull IntProviderIngredient parse(FriendlyByteBuf buffer) {
            var nbt = buffer.readNbt();
            IntProvider provider = IntProvider.CODEC.parse(NbtOps.INSTANCE, nbt.get("provider"))
                    .getOrThrow(false, GTCEu.LOGGER::error);
            int sampledCount = nbt.getInt("sampledCount");
            int amount = nbt.getInt("amount");
            return new IntProviderIngredient(Ingredient.fromNetwork(buffer), provider, sampledCount, amount);
        }

        @Override
        public @NotNull IntProviderIngredient parse(JsonObject json) {
            IntProvider provider = IntProvider.CODEC.parse(JsonOps.INSTANCE, json.get("count_provider"))
                    .getOrThrow(false, GTCEu.LOGGER::error);
            Ingredient inner = Ingredient.fromJson(json.get("ingredient"));
            int sampledCount = json.getAsJsonPrimitive("sampledCount").getAsInt();
            int amount = json.getAsJsonPrimitive("amount").getAsInt();
            return new IntProviderIngredient(inner, provider, sampledCount, amount);
        }

        @Override
        public void write(FriendlyByteBuf buffer, IntProviderIngredient ingredient) {
            CompoundTag wrapper = new CompoundTag();
            wrapper.put("provider", IntProvider.CODEC.encodeStart(NbtOps.INSTANCE, ingredient.countProvider)
                    .getOrThrow(false, GTCEu.LOGGER::error));
            wrapper.putInt("sampledCount", ingredient.sampledCount);
            wrapper.putInt("amount", ingredient.amount);
            buffer.writeNbt(wrapper);
            ingredient.inner.toNetwork(buffer);
        }
    };
}
