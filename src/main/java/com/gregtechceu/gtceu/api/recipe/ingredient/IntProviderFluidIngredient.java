package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IntProviderFluidIngredient extends FluidIngredient {

    public static final ResourceLocation TYPE = GTCEu.id("int_provider");

    @Getter
    private final IntProvider countProvider;
    @Setter
    protected int sampledCount = -1;
    @Getter
    private final FluidIngredient inner;
    @Setter
    protected FluidStack[] fluidStacks = null;

    protected IntProviderFluidIngredient(FluidIngredient inner, IntProvider provider) {
        super(Stream.empty(), provider.getMaxValue(), null);
        this.inner = inner;
        this.countProvider = provider;
    }

    @Override
    public IntProviderFluidIngredient copy() {
        IntProviderFluidIngredient ipfi = new IntProviderFluidIngredient(this, this.countProvider);
        ipfi.setSampledCount(this.sampledCount);
        return ipfi;
    }

    public IntProviderFluidIngredient replicate() {
        IntProviderFluidIngredient ipfi = new IntProviderFluidIngredient(this, this.countProvider);
        return ipfi;
    }

    @Override
    public int getAmount() {
        return -1;
    }

    @Override
    public FluidStack[] getStacks() {
        if (fluidStacks == null) {
            inner.setAmount(getSampledCount(GTValues.RNG));
            fluidStacks = inner.getStacks();
        }
        return fluidStacks;
    }

    public int getSampledCount(@NotNull RandomSource random) {
        if (sampledCount == -1) {
            sampledCount = countProvider.sample(random);
        }
        return sampledCount;
    }

    @Override
    public boolean isEmpty() { return inner.isEmpty(); }

    // TODO: rewrite the entire `of` stack
    public static IntProviderFluidIngredient of(FluidIngredient inner, IntProvider provider) {
        return new IntProviderFluidIngredient(inner.copy(), provider);
    }

    public static IntProviderFluidIngredient of(FluidStack stack, IntProvider provider) {
        return IntProviderFluidIngredient.of(FluidIngredient.of(stack), provider);
    }

    public static IntProviderFluidIngredient of(FluidStack stack, int min, int max) {
        return IntProviderFluidIngredient.of(FluidIngredient.of(stack), UniformInt.of(min, max));
    }

    public static IntProviderFluidIngredient of(IntProvider countProvider, Fluid... fluids) {
        return IntProviderFluidIngredient.of(Arrays.stream(fluids), countProvider, null);
    }

    private static IntProviderFluidIngredient of(Stream<Fluid> stacks, IntProvider countProvider, CompoundTag nbt) {
        return IntProviderFluidIngredient.fromValues(
                stacks.filter(stack -> stack != null && !stack.isSame(Fluids.EMPTY)).map(FluidValue::new), 1000, nbt,
                countProvider);
    }
    //
    // public static IntProviderFluidIngredient of(IntProvider countProvider, FluidStack... stacks) {
    // return IntProviderFluidIngredient.of(Arrays.stream(stacks).map(FluidStack::getFluid),
    // countProvider, stacks.length == 0 ? null : stacks[0].getTag());
    // }
    //
    // public static IntProviderFluidIngredient of(Stream<Fluid> stacks, CompoundTag nbt) {
    // return IntProviderFluidIngredient.fromValues(
    // stacks.filter(stack -> stack != null && !stack.isSame(Fluids.EMPTY)).map(FluidValue::new), 1000, nbt,
    // null);
    // }

    public static IntProviderFluidIngredient of(TagKey<Fluid> tag, IntProvider countProvider) {
        return IntProviderFluidIngredient.fromValues(Stream.of(new IntProviderFluidIngredient.TagValue(tag)), 1000,
                null, countProvider);
    }

    public static IntProviderFluidIngredient of(TagKey<Fluid> tag, int min, int max) {
        return IntProviderFluidIngredient.fromValues(Stream.of(new IntProviderFluidIngredient.TagValue(tag)), 1000,
                null, UniformInt.of(min, max));
    }

    public static IntProviderFluidIngredient of(TagKey<Fluid> tag, IntProvider countProvider, CompoundTag nbt) {
        return IntProviderFluidIngredient.fromValues(Stream.of(new IntProviderFluidIngredient.TagValue(tag)), 1000, nbt,
                countProvider);
    }

    public static IntProviderFluidIngredient of(TagKey<Fluid> tag, int min, int max, CompoundTag nbt) {
        return IntProviderFluidIngredient.fromValues(Stream.of(new IntProviderFluidIngredient.TagValue(tag)), 1000, nbt,
                UniformInt.of(min, max));
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putString("FluidName", ForgeRegistries.FLUIDS.getKey(this.inner.getStacks()[0].getFluid()).toString());
        nbt.putInt("Amount", this.amount);
        if (this.nbt != null) {
            nbt.put("Tag", this.nbt);
        }
        nbt.put("count_provider", IntProvider.CODEC.encodeStart(NbtOps.INSTANCE, countProvider)
                .getOrThrow(false, GTCEu.LOGGER::error));
        return nbt;
    }

    public static FluidIngredient loadFluidIngredientFromNBT(CompoundTag nbt) {
        if (nbt == null) {
            return EMPTY;
        } else if (!nbt.contains("FluidName", 8)) {
            return EMPTY;
        } else {
            if (nbt.contains("Minimum")) {
                ResourceLocation fluidName = new ResourceLocation(nbt.getString("FluidName"));
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
                if (fluid == null) {
                    return EMPTY;
                } else {
                    int max = nbt.getInt("Maximum");
                    FluidStack stack = new FluidStack(fluid, max);
                    if (nbt.contains("Tag", 10)) {
                        stack.setTag(nbt.getCompound("Tag"));
                    }
                    int min = nbt.getInt("Minimum");
                    return IntProviderFluidIngredient.of(stack, min, max);
                }
            } else {
                return FluidIngredient.CODEC.parse(NbtOps.INSTANCE, nbt);
            }
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeCollection(Arrays.asList(this.getStacks()), (buf, stack) -> stack.writeToPacket(buf));
        buffer.writeInt(amount);
        buffer.writeNbt(nbt);
        CompoundTag providerTag = (CompoundTag) IntProvider.CODEC
                .encodeStart(NbtOps.INSTANCE, countProvider == null ? UniformInt.of(0, 0) : countProvider)
                .getOrThrow(false, GTCEu.LOGGER::error);
        buffer.writeNbt(providerTag);
    }

    public static IntProviderFluidIngredient fromNetwork(FriendlyByteBuf buffer) {
        return IntProviderFluidIngredient.fromValues(
                buffer.readList(FluidStack::readFromPacket).stream().map(stack -> new FluidValue(stack.getFluid())),
                buffer.readInt(),
                buffer.readNbt(),
                IntProvider.CODEC.parse(NbtOps.INSTANCE, Objects.requireNonNull(buffer.readNbt()))
                        .getOrThrow(false, GTCEu.LOGGER::error));
    }

    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", this.sampledCount);
        if (this.nbt != null) {
            jsonObject.addProperty("nbt", this.nbt.getAsString());
        }
        if (changed) {
            getStacks();
        }
        if (this.values.length == 1) {
            jsonObject.add("value", this.values[0].serialize());
        }
        JsonArray jsonArray = new JsonArray();
        for (FluidIngredient.Value value : this.values) {
            jsonArray.add(value.serialize());
        }
        jsonObject.add("value", jsonArray);
        jsonObject.add("count_provider",
                IntProvider.CODEC
                        .encodeStart(JsonOps.INSTANCE,
                                countProvider == null ? UniformInt.of(0, 0) : countProvider)
                        .getOrThrow(false, GTCEu.LOGGER::error));
        return jsonObject;
    }

    public static IntProviderFluidIngredient fromJson(@Nullable JsonElement json, boolean allowAir) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Fluid ingredient cannot be null");
        }
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected fluid ingredient to be object");
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(json, "ingredient");
        int amount = GsonHelper.getAsInt(jsonObject, "amount", 0);
        IntProvider countProvider = IntProvider.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("count_provider"))
                .getOrThrow(false, GTCEu.LOGGER::error);
        CompoundTag nbt = jsonObject.has("nbt") ? CraftingHelper.getNBT(jsonObject.get("nbt")) : null;
        if (GsonHelper.isObjectNode(jsonObject, "value")) {
            return IntProviderFluidIngredient.fromValues(
                    Stream.of(
                            IntProviderFluidIngredient.valueFromJson(GsonHelper.getAsJsonObject(jsonObject, "value"))),
                    amount, nbt, countProvider);
        } else if (GsonHelper.isArrayNode(jsonObject, "value")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "value");
            if (jsonArray.isEmpty() && !allowAir) {
                throw new JsonSyntaxException("Fluid array cannot be empty, at least one item must be defined");
            }
            return IntProviderFluidIngredient
                    .fromValues(
                            StreamSupport.stream(jsonArray.spliterator(), false)
                                    .map(jsonElement -> IntProviderFluidIngredient
                                            .valueFromJson(GsonHelper.convertToJsonObject(jsonElement, "fluid"))),
                            amount, nbt, countProvider);
        }
        throw new JsonSyntaxException("expected value to be either object or array.");
    }

    private static IntProviderFluidIngredient.Value valueFromJson(JsonObject json) {
        if (json.has("fluid") && json.has("tag")) {
            throw new JsonParseException("A fluid ingredient entry is either a tag or a fluid, not both");
        }
        if (json.has("fluid")) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluid")));
            return new IntProviderFluidIngredient.FluidValue(fluid);
        }
        if (json.has("tag")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, resourceLocation);
            return new IntProviderFluidIngredient.TagValue(tagKey);
        }
        throw new JsonParseException("A fluid ingredient entry needs either a tag or a fluid");
    }
}
