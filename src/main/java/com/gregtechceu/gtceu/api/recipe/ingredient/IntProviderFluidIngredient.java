package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.Setter;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import java.util.Arrays;
import java.util.Objects;

public class IntProviderFluidIngredient extends FluidIngredient{

    public static final ResourceLocation TYPE = GTCEu.id("int_provider");

    @Getter
    private final IntProvider countProvider;
    @Getter
    private final FluidIngredient inner;
    @Setter
    protected int sampledCount = -1;
    @Setter
    protected FluidStack[] fluidStacks = null;

    protected IntProviderFluidIngredient(FluidIngredient inner, IntProvider provider){
        super(Stream.empty(),provider.getMaxValue(),null);
        this.inner = inner;
        this.amount=inner.amount;
        this.values = inner.values;
        this.countProvider = provider;
    }

    protected IntProviderFluidIngredient(Stream<? extends FluidIngredient.Value> ingredient, @Nullable CompoundTag nbt,
                           IntProvider provider) {
        super(Stream.empty(), provider.getMaxValue(), nbt);
        this.inner = FluidIngredient.fromValues(ingredient, provider.getMaxValue(), nbt);
        this.amount=inner.amount;
        this.values = inner.values;
        this.countProvider = provider;
    }

    protected IntProviderFluidIngredient(Stream<? extends FluidIngredient.Value> ingredient, int amount, @Nullable CompoundTag nbt,
                                         IntProvider provider) {
        super(Stream.empty(), amount, nbt);
        this.inner = FluidIngredient.fromValues(ingredient, provider.getMaxValue(), nbt);
        this.amount=inner.amount;
        this.values = inner.values;
        this.countProvider = provider;
    }

    protected IntProviderFluidIngredient(IntProviderFluidIngredient original){
        super(Arrays.stream(original.inner.values).map(Value::copy), original.amount, original.nbt == null ? null : original.nbt.copy());
        this.inner=original.inner;
        this.amount=inner.amount;
        this.values = inner.values;
        this.countProvider= original.countProvider;
        this.sampledCount=original.sampledCount;
        this.fluidStacks=original.fluidStacks;
    }

    protected IntProviderFluidIngredient(IntProviderFluidIngredient original, boolean roll){
        super(Arrays.stream(original.inner.values).map(Value::copy), original.amount, original.nbt == null ? null : original.nbt.copy());
        this.inner=original.inner;
        this.amount=inner.amount;
        this.values = inner.values;
        this.countProvider= original.countProvider;
        if (roll){
            this.sampledCount = -1;
            this.fluidStacks=null;
            this.fluidStacks=this.getStacks();
        }
        else{
            this.sampledCount=original.sampledCount;
            this.fluidStacks=original.fluidStacks;
        }
    }

    public static IntProviderFluidIngredient fromValues(Stream<? extends Value> stream,
                                             int amount, @Nullable CompoundTag nbt, @Nullable IntProvider countProvider) {
        if (countProvider != null) {
            Preconditions.checkArgument(countProvider.getMinValue() >= 0,
                    "IntProviderFluidIngredient must have a min value of at least 0.");
        }

        return          new IntProviderFluidIngredient(stream, amount, nbt, countProvider);
    }

    @Override
    public IntProviderFluidIngredient copy() {
        return new IntProviderFluidIngredient(this);
    }

    public IntProviderFluidIngredient replicate (){
        return new IntProviderFluidIngredient(this, true);
    }

    @Override
    public int getAmount(){
        if (amount == -1) {
            return getSampledCount(GTValues.RNG);
        }
        return amount;
    }

    @Override
    public void shrink (int amount){
        setAmount(this.amount - amount);
        inner.setAmount(inner.getAmount() - amount);
        this.changed=true;
    }

    @Override
    public FluidStack[] getStacks() {
        if (changed || fluidStacks == null) {
            inner.setAmount(getSampledCount(GTValues.RNG));
            this.fluidStacks = inner.getStacks();
            this.changed=false;
        }
        return fluidStacks;
    }

    public int getSampledCount(@NotNull RandomSource random){
        if (sampledCount == -1){
            sampledCount = countProvider.sample(random);
            this.amount = sampledCount;
            this.changed = true;
        }
        return sampledCount;
    }


    //TODO: rewrite the entire `of` stack
    public static IntProviderFluidIngredient of(FluidIngredient inner, IntProvider provider) {
        return new IntProviderFluidIngredient(inner.copy(), provider);
    }

    public static IntProviderFluidIngredient of(FluidStack stack, IntProvider provider) {
        return IntProviderFluidIngredient.of(FluidIngredient.of(stack), provider);
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
//    public static IntProviderFluidIngredient of(IntProvider countProvider, FluidStack... stacks) {
//        return IntProviderFluidIngredient.of(Arrays.stream(stacks).map(FluidStack::getFluid),
//                countProvider, stacks.length == 0 ? null : stacks[0].getTag());
//    }
//
//    public static IntProviderFluidIngredient of(Stream<Fluid> stacks,  CompoundTag nbt) {
//        return IntProviderFluidIngredient.fromValues(
//                stacks.filter(stack -> stack != null && !stack.isSame(Fluids.EMPTY)).map(FluidValue::new), 1000, nbt,
//                null);
//    }

    public static IntProviderFluidIngredient of(TagKey<Fluid> tag, IntProvider countProvider) {
        return IntProviderFluidIngredient.fromValues(Stream.of(new IntProviderFluidIngredient.TagValue(tag)), 1000, null, countProvider);
    }

    public static IntProviderFluidIngredient of(TagKey<Fluid> tag, int min, int max) {
        return IntProviderFluidIngredient.fromValues(Stream.of(new IntProviderFluidIngredient.TagValue(tag)), 1000, null, UniformInt.of(min, max));
    }

    public static IntProviderFluidIngredient of(TagKey<Fluid> tag, IntProvider countProvider, CompoundTag nbt) {
        return IntProviderFluidIngredient.fromValues(Stream.of(new IntProviderFluidIngredient.TagValue(tag)), 1000, nbt, countProvider);
    }

    public static IntProviderFluidIngredient of(TagKey<Fluid> tag, int min, int max, CompoundTag nbt) {
        return IntProviderFluidIngredient.fromValues(Stream.of(new IntProviderFluidIngredient.TagValue(tag)), 1000, nbt, UniformInt.of(min, max));
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
        if (changed){
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
                    Stream.of(IntProviderFluidIngredient.valueFromJson(GsonHelper.getAsJsonObject(jsonObject, "value"))),
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
