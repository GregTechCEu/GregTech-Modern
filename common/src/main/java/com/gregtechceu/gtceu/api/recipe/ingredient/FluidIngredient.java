package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FluidIngredient implements Predicate<FluidStack> {
    public static final FluidIngredient EMPTY = new FluidIngredient(Stream.empty());
    public FluidIngredient.Value[] values;
    @Nullable
    public FluidStack[] stacks;

    public FluidIngredient(Stream<? extends FluidIngredient.Value> empty) {
        this.values = empty.toArray(Value[]::new);
    }

    public static FluidIngredient fromValues(Stream<? extends FluidIngredient.Value> stream) {
        FluidIngredient ingredient = new FluidIngredient(stream);
        return ingredient.isEmpty() ? EMPTY : ingredient;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeCollection(Arrays.asList(this.getStacks()), (buf, stack) -> stack.writeToBuf(buf));
    }

    public JsonElement toJson() {
        if (this.values.length == 1) {
            return this.values[0].serialize();
        }
        JsonArray jsonArray = new JsonArray();
        for (FluidIngredient.Value value : this.values) {
            jsonArray.add(value.serialize());
        }
        return jsonArray;
    }

    public FluidIngredient copy() {
        return new FluidIngredient(Arrays.stream(this.values).map(Value::copy));
    }
    
    @Override
    public boolean test(@Nullable FluidStack stack) {
        if (stack == null) {
            return false;
        }
        if (this.isEmpty()) {
            return stack.isEmpty();
        }
        for (FluidStack fluidStack : this.getStacks()) {
            if (!fluidStack.isFluidEqual(stack) || stack.getAmount() > fluidStack.getAmount()) continue;
            return true;
        }
        return false;
    }
    
    public boolean isEmpty() {
        return this.values.length == 0;
    }

    public FluidStack[] getStacks() {
        if (this.stacks == null) {
            this.stacks = Arrays.stream(this.values).flatMap(entry -> entry.getStacks().stream()).distinct().toArray(FluidStack[]::new);
        }
        return this.stacks;
    }

    public void setAmount(long amount) {
        this.stacks = Arrays.stream(this.getStacks()).map(stack -> FluidStack.create(stack.getFluid(), amount)).toArray(FluidStack[]::new);
    }

    public long getAmount() {
        return this.getStacks().length > 0 ? this.getStacks()[0].getAmount() : 0;
    }

    public static FluidIngredient of() {
        return EMPTY;
    }

    public static FluidIngredient of(long amount, Fluid... items) {
        return FluidIngredient.of(Arrays.stream(items).map(fluid -> FluidStack.create(fluid, amount)));
    }

    public static FluidIngredient of(FluidStack... stacks) {
        return FluidIngredient.of(Arrays.stream(stacks));
    }

    public static FluidIngredient of(Stream<FluidStack> stacks) {
        return FluidIngredient.fromValues(stacks.filter(stack -> !stack.isEmpty()).map(FluidValue::new));
    }

    /**
     * {@return a new ingredient which accepts items which are in the given tag}
     *
     * @param tag the tag key
     */
    public static FluidIngredient of(TagKey<Fluid> tag, long amount) {
        return FluidIngredient.fromValues(Stream.of(new FluidIngredient.TagValue(tag, amount)));
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf buffer) {
        return FluidIngredient.fromValues(buffer.readList(FluidStack::readFromBuf).stream().map(FluidValue::new));
    }

    public static FluidIngredient fromJson(@Nullable JsonElement json) {
        return FluidIngredient.fromJson(json, true);
    }

    public static FluidIngredient fromJson(@Nullable JsonElement json, boolean allowAir) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Fluid ingredient cannot be null");
        }
        if (json.isJsonObject()) {
            return FluidIngredient.fromValues(Stream.of(FluidIngredient.valueFromJson(json.getAsJsonObject())));
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            if (jsonArray.size() == 0 && !allowAir) {
                throw new JsonSyntaxException("Fluid array cannot be empty, at least one item must be defined");
            }
            return FluidIngredient.fromValues(StreamSupport.stream(jsonArray.spliterator(), false).map(jsonElement -> FluidIngredient.valueFromJson(GsonHelper.convertToJsonObject(jsonElement, "fluid"))));
        }
        throw new JsonSyntaxException("Expected fluid ingredient to be object or array of objects");
    }

    private static FluidIngredient.Value valueFromJson(JsonObject json) {
        long amount = 0;
        if (json.has("amount")) {
            amount = GsonHelper.getAsLong(json, "amount", 0);
        }
        if (json.has("fluid") && json.has("tag")) {
            throw new JsonParseException("A fluid ingredient entry is either a tag or a fluid, not both");
        }
        if (json.has("fluid")) {
            Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(GsonHelper.getAsString(json, "fluid")));
            return new FluidIngredient.FluidValue(FluidStack.create(fluid, amount));
        }
        if (json.has("tag")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, resourceLocation);
            return new FluidIngredient.TagValue(tagKey, amount);
        }
        throw new JsonParseException("A fluid ingredient entry needs either a tag or a fluid");
    }

    public static interface Value {
        public Collection<FluidStack> getStacks();
        public long getAmount();

        public JsonObject serialize();
        public Value copy();
    }

    public static class TagValue
            implements Value {
        private final TagKey<Fluid> tag;
        @Getter
        private final long amount;

        public TagValue(TagKey<Fluid> tag, long amount) {
            this.tag = tag;
            this.amount = amount;
        }

        @Override
        public Collection<FluidStack> getStacks() {
            ArrayList<FluidStack> list = Lists.newArrayList();
            for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(this.tag)) {
                list.add(FluidStack.create(holder.value(), amount));
            }
            return list;
        }

        @Override
        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("tag", this.tag.location().toString());
            jsonObject.addProperty("amount", this.amount);
            return jsonObject;
        }

        @Override
        public Value copy() {
            return new TagValue(this.tag, this.amount);
        }
    }

    public static class FluidValue
            implements Value {
        private final FluidStack stack;

        public FluidValue(FluidStack item) {
            this.stack = item;
        }

        @Override
        public Collection<FluidStack> getStacks() {
            return Collections.singleton(this.stack);
        }

        @Override
        public long getAmount() {
            return stack.getAmount();
        }

        @Override
        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("fluid", BuiltInRegistries.FLUID.getKey(this.stack.getFluid()).toString());
            jsonObject.addProperty("amount", this.getAmount());
            return jsonObject;
        }

        @Override
        public Value copy() {
            return new FluidValue(this.stack.copy());
        }
    }
}
