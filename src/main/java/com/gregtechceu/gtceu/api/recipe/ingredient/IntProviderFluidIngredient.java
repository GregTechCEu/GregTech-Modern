package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Allows a {@link FluidIngredient} to be created with a ranged {@code amount}, which will be randomly rolled upon
 * recipe start (input) / completion (output).
 * Instantiated using {@link IntProviderFluidIngredient#of()}, with a {@link FluidIngredient}
 * and either an {@link IntProvider} or {@code int, int} range bounds (inclusive).
 * Functions similarly to {@link IntProviderIngredient}.
 */
public class IntProviderFluidIngredient extends FluidIngredient {

    public static final Codec<IntProviderFluidIngredient> CODEC = ExtraCodecs.JSON
            .xmap(IntProviderFluidIngredient::fromJson, IntProviderFluidIngredient::toJson);

    @Getter
    private final IntProvider countProvider;
    /**
     * The last result of {@link IntProviderFluidIngredient#getSampledCount()}. -1 if not rolled.
     */
    @Setter
    protected int sampledCount = -1;
    /**
     * The {@link FluidIngredient} to have a ranged amount.
     */
    @Getter
    private final FluidIngredient inner;
    @Setter
    protected FluidStack[] fluidStacks = null;

    protected IntProviderFluidIngredient(FluidIngredient inner, IntProvider provider) {
        super(inner.values, provider.getMaxValue(), null);
        this.inner = inner;
        this.countProvider = provider;
    }

    protected IntProviderFluidIngredient(FluidIngredient inner, IntProvider provider, int sampledCount) {
        super(inner.values, provider.getMaxValue(), null);
        this.inner = inner;
        this.countProvider = provider;
        this.sampledCount = sampledCount;
    }

    @Override
    public IntProviderFluidIngredient copy() {
        IntProviderFluidIngredient ipfi = new IntProviderFluidIngredient(this.inner, this.countProvider);
        ipfi.setSampledCount(this.sampledCount);
        return ipfi;
    }

    /**
     * An {@link IntProviderFluidIngredient} does not have an amount.
     * You probably want either {@link IntProviderFluidIngredient#getStacks()} or
     * {@link IntProviderFluidIngredient#getMaxSizeStack()}.
     */
    @Deprecated
    @Override
    public int getAmount() {
        if (ConfigHolder.INSTANCE.dev.debug) {
            throw new IllegalCallerException("An IPFI should never have getAmount() called on it!");
        }
        return -1;
    }

    /**
     * Gets a usable {@link FluidStack FluidStack[]} from this {@link IntProviderFluidIngredient}.
     * If this ingredient has not yet had its {@link IntProviderFluidIngredient#sampledCount} rolled, rolls it.
     *
     * @return a {@link FluidStack FluidStack[]} with amount {@link IntProviderFluidIngredient#sampledCount}
     */
    @Override
    public FluidStack[] getStacks() {
        if (fluidStacks == null) {
            int cachedAmount = getSampledCount(GTValues.RNG);
            if (cachedAmount == 0) {
                return EMPTY_STACK_ARRAY;
            }
            var innerStacks = inner.getStacks();
            this.fluidStacks = new FluidStack[innerStacks.length];
            for (int i = 0; i < fluidStacks.length; i++) {
                fluidStacks[i] = innerStacks[i].copy();
                fluidStacks[i].setAmount(cachedAmount);
            }
        }
        return fluidStacks;
    }

    /**
     * Gets a {@link FluidStack} containing the maximum possible output from this {@link IntProviderFluidIngredient}.
     * Mainly used for things like Recipe provider simulations to see if there is enough tank space to handle
     * the recipe output.
     *
     * @return a {@link FluidStack} with amount {@link IntProvider#getMaxValue()}
     */
    public @NotNull FluidStack getMaxSizeStack() {
        FluidStack[] in = inner.getStacks();
        if (in.length == 0) return FluidStack.EMPTY;
        return new FluidStack(in[0], countProvider.getMaxValue());
    }

    /**
     * If this ingredient has not yet had its {@link IntProviderFluidIngredient#sampledCount} rolled, rolls it and
     * returns the roll.
     * If it has, returns the existing roll.
     * Passthrough method, invokes {@link IntProviderFluidIngredient#getSampledCount(RandomSource)} using the threadsafe
     * {@link GTValues#RNG}.
     *
     * @return the amount rolled
     */
    public int getSampledCount() {
        return getSampledCount(GTValues.RNG);
    }

    /**
     * If this ingredient has not yet had its {@link IntProviderFluidIngredient#sampledCount} rolled, rolls it and
     * returns the roll.
     * If it has, returns the existing roll.
     *
     * @param random {@link RandomSource}, must be threadsafe, usually called using {@link GTValues#RNG}.
     * @return the amount rolled
     */
    public int getSampledCount(@NotNull RandomSource random) {
        if (sampledCount == -1) {
            sampledCount = countProvider.sample(random);
        }
        return sampledCount;
    }

    /**
     * @return the average roll of this ranged amount
     */
    public double getMidRoll() {
        return ((countProvider.getMaxValue() + countProvider.getMinValue()) / 2.0);
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    /**
     * Resets the random roll on this ingredient
     */
    public void reroll() {
        sampledCount = -1;
        fluidStacks = null;
    }

    /**
     * @param inner    {@link FluidIngredient}
     * @param provider usually as {@link UniformInt#of(int, int)}
     */
    public static IntProviderFluidIngredient of(FluidIngredient inner, IntProvider provider) {
        return new IntProviderFluidIngredient(inner, provider);
    }

    public static IntProviderFluidIngredient of(FluidStack inner, int min, int max) {
        return IntProviderFluidIngredient.of(FluidIngredient.of(inner), UniformInt.of(min, max));
    }

    @Override
    public boolean test(@Nullable FluidStack stack) {
        return inner.test(stack);
    }

    /**
     * Properties:
     * <ul>
     * <li>{@code count_provider}</li>
     * <li>{@code inner}</li>
     * </ul>
     */
    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.add("count_provider", IntProvider.CODEC.encodeStart(JsonOps.INSTANCE, countProvider)
                .getOrThrow(false, GTCEu.LOGGER::error));
        json.add("inner", inner.toJson());
        json.addProperty("sampledCount", sampledCount);
        return json;
    }

    /**
     * @param json containing
     *             <ul>
     *             <li>{@code count_provider}</li>
     *             <li>{@code inner}</li>
     *             </ul>
     */
    public static IntProviderFluidIngredient fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Fluid ingredient cannot be null");
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(json, "ingredient");
        IntProvider amount = IntProvider.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("count_provider"))
                .getOrThrow(false, GTCEu.LOGGER::error);
        int sampledCount = jsonObject.getAsJsonPrimitive("sampledCount").getAsInt();
        FluidIngredient inner = FluidIngredient.fromJson(jsonObject.get("inner"));
        return new IntProviderFluidIngredient(inner, amount, sampledCount);
    }

    public CompoundTag toNBT() {
        return (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, this.toJson());
    }

    public static IntProviderFluidIngredient fromNBT(CompoundTag nbt) {
        return IntProviderFluidIngredient.fromJson(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt));
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        inner.toNetwork(buffer);
        buffer.writeVarIntArray(new int[] { countProvider.getMinValue(), countProvider.getMaxValue() });
    }

    public static IntProviderFluidIngredient fromNetwork(FriendlyByteBuf buffer) {
        FluidIngredient inner = FluidIngredient.fromNetwork(buffer);
        int[] range = buffer.readVarIntArray(2);
        IntProvider provider = UniformInt.of(range[0], range[1]);
        return new IntProviderFluidIngredient(inner, provider);
    }
}
