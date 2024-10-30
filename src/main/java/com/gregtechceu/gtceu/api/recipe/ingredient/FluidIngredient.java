package com.gregtechceu.gtceu.api.recipe.ingredient;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FluidIngredient implements Predicate<FluidStack> {

    public static final Codec<FluidIngredient> CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> FluidIngredient.fromJson(dynamic.convert(JsonOps.INSTANCE).getValue()),
            ingredient -> new Dynamic<>(JsonOps.INSTANCE, ingredient.toJson()));

    public static final FluidIngredient EMPTY = new FluidIngredient(Stream.empty(), 0, null);
    public FluidIngredient.Value[] values;
    @Nullable
    public FluidStack[] stacks;
    @Getter
    private int amount;
    @Getter
    private CompoundTag nbt;
    private boolean changed = true;

    public FluidIngredient(Stream<? extends FluidIngredient.Value> empty, int amount, @Nullable CompoundTag nbt) {
        this.values = empty.toArray(Value[]::new);
        this.amount = amount;
        this.nbt = nbt;
    }

    public static FluidIngredient fromValues(Stream<? extends FluidIngredient.Value> stream, int amount,
                                             @Nullable CompoundTag nbt) {
        FluidIngredient ingredient = new FluidIngredient(stream, amount, nbt);
        return ingredient.isEmpty() ? EMPTY : ingredient;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeCollection(Arrays.asList(this.getStacks()), (buf, stack) -> stack.writeToPacket(buf));
        buffer.writeVarInt(amount);
        buffer.writeNbt(nbt);
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", this.amount);
        if (this.nbt != null) {
            jsonObject.addProperty("nbt", this.nbt.getAsString());
        }
        if (this.values.length == 1) {
            jsonObject.add("value", this.values[0].serialize());
        }
        JsonArray jsonArray = new JsonArray();
        for (FluidIngredient.Value value : this.values) {
            jsonArray.add(value.serialize());
        }
        jsonObject.add("value", jsonArray);
        return jsonObject;
    }

    public FluidIngredient copy() {
        return new FluidIngredient(Arrays.stream(this.values).map(Value::copy), this.amount,
                this.nbt == null ? null : this.nbt.copy());
    }

    @Override
    public boolean test(@Nullable FluidStack stack) {
        if (stack == null) {
            return false;
        }
        if (this.isEmpty()) {
            return stack.isEmpty();
        }
        if (this.nbt != null && !this.nbt.equals(stack.getTag())) {
            return false;
        }
        for (FluidStack fluidStack : this.getStacks()) {
            if (fluidStack.getFluid() != stack.getFluid()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FluidIngredient other)) {
            return false;
        }

        if (!Objects.equals(this.nbt, other.nbt)) return false;
        if (this.values.length != other.values.length) return false;
        for (Value value1 : this.values) {
            for (Value value2 : other.values) {
                if (value1 instanceof TagValue tagValue) {
                    if (!(value2 instanceof TagValue tagValue1)) {
                        return false;
                    }
                    if (tagValue.tag != tagValue1.tag) {
                        return false;
                    }
                } else if (value1 instanceof FluidValue) {
                    if (!(value2 instanceof FluidValue)) {
                        return false;
                    }
                    if (!value1.getFluids().containsAll(value2.getFluids())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(values);
        result = 31 * result + Integer.hashCode(amount);
        result = 31 * result + Objects.hashCode(nbt);
        return result;
    }

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    public FluidStack[] getStacks() {
        if (changed || this.stacks == null) {
            List<FluidStack> fluidStacks = new ObjectArrayList<>(1);
            List<Fluid> found = new ObjectArrayList<>(1);
            for (Value value : this.values) {
                for (Fluid fluid : value.getFluids()) {
                    if (found.contains(fluid)) continue;
                    found.add(fluid);

                    fluidStacks.add(new FluidStack(fluid, this.amount, this.nbt));
                }
            }
            this.stacks = fluidStacks.toArray(FluidStack[]::new);
            this.changed = false;
        }
        return this.stacks;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.changed = true;
    }

    public void shrink(int amount) {
        setAmount(this.amount - amount);
    }

    public void setNbt(CompoundTag nbt) {
        this.nbt = nbt;
        this.changed = true;
    }

    public static FluidIngredient of() {
        return EMPTY;
    }

    public static FluidIngredient of(int amount, Fluid... items) {
        return FluidIngredient.of(Arrays.stream(items), amount, null);
    }

    public static FluidIngredient of(FluidStack... stacks) {
        return FluidIngredient.of(Arrays.stream(stacks).map(FluidStack::getFluid),
                stacks.length == 0 ? 0 : stacks[0].getAmount(), stacks.length == 0 ? null : stacks[0].getTag());
    }

    public static FluidIngredient of(Stream<Fluid> stacks, int amount, CompoundTag nbt) {
        return FluidIngredient.fromValues(
                stacks.filter(stack -> stack != null && !stack.isSame(Fluids.EMPTY)).map(FluidValue::new), amount, nbt);
    }

    /**
     * {@return a new ingredient which accepts items which are in the given tag}
     *
     * @param tag the tag key
     */
    public static FluidIngredient of(TagKey<Fluid> tag, int amount) {
        return FluidIngredient.fromValues(Stream.of(new FluidIngredient.TagValue(tag)), amount, null);
    }

    public static FluidIngredient of(TagKey<Fluid> tag, int amount, CompoundTag nbt) {
        return FluidIngredient.fromValues(Stream.of(new FluidIngredient.TagValue(tag)), amount, nbt);
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf buffer) {
        return FluidIngredient.fromValues(
                buffer.readList(FluidStack::readFromPacket).stream().map(stack -> new FluidValue(stack.getFluid())),
                buffer.readVarInt(), buffer.readNbt());
    }

    public static FluidIngredient fromJson(@Nullable JsonElement json) {
        return FluidIngredient.fromJson(json, true);
    }

    public static FluidIngredient fromJson(@Nullable JsonElement json, boolean allowAir) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Fluid ingredient cannot be null");
        }
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected fluid ingredient to be object");
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(json, "ingredient");
        int amount = GsonHelper.getAsInt(jsonObject, "amount", 0);
        CompoundTag nbt = jsonObject.has("nbt") ? CraftingHelper.getNBT(jsonObject.get("nbt")) : null;
        if (GsonHelper.isObjectNode(jsonObject, "value")) {
            return FluidIngredient.fromValues(
                    Stream.of(FluidIngredient.valueFromJson(GsonHelper.getAsJsonObject(jsonObject, "value"))), amount,
                    nbt);
        } else if (GsonHelper.isArrayNode(jsonObject, "value")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "value");
            if (jsonArray.isEmpty() && !allowAir) {
                throw new JsonSyntaxException("Fluid array cannot be empty, at least one item must be defined");
            }
            return FluidIngredient
                    .fromValues(
                            StreamSupport.stream(jsonArray.spliterator(), false)
                                    .map(jsonElement -> FluidIngredient
                                            .valueFromJson(GsonHelper.convertToJsonObject(jsonElement, "fluid"))),
                            amount, nbt);
        }
        throw new JsonSyntaxException("expected value to be either object or array.");
    }

    private static FluidIngredient.Value valueFromJson(JsonObject json) {
        if (json.has("fluid") && json.has("tag")) {
            throw new JsonParseException("A fluid ingredient entry is either a tag or a fluid, not both");
        }
        if (json.has("fluid")) {
            Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(GsonHelper.getAsString(json, "fluid")));
            return new FluidIngredient.FluidValue(fluid);
        }
        if (json.has("tag")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, resourceLocation);
            return new FluidIngredient.TagValue(tagKey);
        }
        throw new JsonParseException("A fluid ingredient entry needs either a tag or a fluid");
    }

    public static interface Value {

        public Collection<Fluid> getFluids();

        public JsonObject serialize();

        public Value copy();
    }

    public static class TagValue implements Value {

        @Getter
        private final TagKey<Fluid> tag;

        public TagValue(TagKey<Fluid> tag) {
            this.tag = tag;
        }

        @Override
        public Collection<Fluid> getFluids() {
            ArrayList<Fluid> list = Lists.newArrayList();
            for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(this.tag)) {

                list.add(holder.value());
            }
            return list;
        }

        @Override
        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("tag", this.tag.location().toString());
            return jsonObject;
        }

        @Override
        public Value copy() {
            return new TagValue(this.tag);
        }

        @Override
        public int hashCode() {
            return tag.hashCode();
        }
    }

    public static class FluidValue implements Value {

        private final Fluid fluid;

        public FluidValue(Fluid item) {
            this.fluid = item;
        }

        @Override
        public Collection<Fluid> getFluids() {
            return Collections.singleton(this.fluid);
        }

        @Override
        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("fluid", BuiltInRegistries.FLUID.getKey(this.fluid).toString());
            return jsonObject;
        }

        @Override
        public Value copy() {
            return new FluidValue(this.fluid);
        }

        @Override
        public int hashCode() {
            return fluid.hashCode();
        }
    }
}
