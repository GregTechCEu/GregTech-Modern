package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FluidIngredient implements Predicate<FluidStack> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final FluidIngredient EMPTY = new FluidIngredient(Stream.empty(), 0, null);
    public FluidIngredient.Value[] values;
    @Nullable
    public FluidStack[] stacks;
    @Getter
    private long amount;
    @Getter
    private CompoundTag nbt;
    private boolean changed = true;

    public FluidIngredient(Stream<? extends FluidIngredient.Value> empty, long amount, @Nullable CompoundTag nbt) {
        this.values = empty.toArray(Value[]::new);
        this.amount = amount;
        this.nbt = nbt;
    }

    public static FluidIngredient fromValues(Stream<? extends FluidIngredient.Value> stream, long amount, @Nullable CompoundTag nbt) {
        FluidIngredient ingredient = new FluidIngredient(stream, amount, nbt);
        return ingredient.isEmpty() ? EMPTY : ingredient;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeCollection(Arrays.asList(this.getStacks()), (buf, stack) -> stack.writeToBuf(buf));
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", this.amount * FluidHelper.getBucket() / 1000);
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
        return new FluidIngredient(Arrays.stream(this.values).map(Value::copy), this.amount, this.nbt == null ? null : this.nbt.copy());
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
    
    public boolean isEmpty() {
        return this.values.length == 0;
    }

    public FluidStack[] getStacks() {
        if (changed || this.stacks == null) {
            this.stacks = Arrays.stream(this.values).flatMap(entry -> entry.getStacks().stream()).distinct().map(fluid -> FluidStack.create(fluid, this.amount, this.nbt)).toArray(FluidStack[]::new);
            this.changed = false;
        }
        return this.stacks;
    }

    public void setAmount(long amount) {
        this.amount = amount;
        this.changed = true;
    }

    public void setNbt(CompoundTag nbt) {
        this.nbt = nbt;
        this.changed = true;
    }

    public static FluidIngredient of() {
        return EMPTY;
    }

    public static FluidIngredient of(long amount, Fluid... items) {
        return FluidIngredient.of(Arrays.stream(items), amount, null);
    }

    public static FluidIngredient of(FluidStack... stacks) {
        return FluidIngredient.of(Arrays.stream(stacks).map(FluidStack::getFluid), stacks.length == 0 ? 0 : stacks[0].getAmount(), stacks.length == 0 ? null : stacks[0].getTag());
    }

    public static FluidIngredient of(Stream<Fluid> stacks, long amount, CompoundTag nbt) {
        return FluidIngredient.fromValues(stacks.filter(stack -> stack != null && !stack.isSame(Fluids.EMPTY)).map(FluidValue::new), amount, nbt);
    }

    /**
     * {@return a new ingredient which accepts items which are in the given tag}
     *
     * @param tag the tag key
     */
    public static FluidIngredient of(TagKey<Fluid> tag, long amount) {
        return FluidIngredient.fromValues(Stream.of(new FluidIngredient.TagValue(tag)), amount, null);
    }

    public static FluidIngredient of(TagKey<Fluid> tag, long amount, CompoundTag nbt) {
        return FluidIngredient.fromValues(Stream.of(new FluidIngredient.TagValue(tag)), amount, nbt);
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf buffer) {
        return FluidIngredient.fromValues(buffer.readList(FluidStack::readFromBuf).stream().map(stack -> new FluidValue(stack.getFluid())), buffer.readVarLong(), buffer.readNbt());
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
        long amount = GsonHelper.getAsLong(jsonObject, "amount", 0);
        CompoundTag nbt = jsonObject.has("nbt") ? getNBT(jsonObject.get("nbt")) : null;
        if (GsonHelper.isObjectNode(jsonObject, "value")) {
            return FluidIngredient.fromValues(Stream.of(FluidIngredient.valueFromJson(GsonHelper.getAsJsonObject(jsonObject, "value"))), amount, nbt);
        } else if (GsonHelper.isArrayNode(jsonObject, "value")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "value");
            if (jsonArray.size() == 0 && !allowAir) {
                throw new JsonSyntaxException("Fluid array cannot be empty, at least one item must be defined");
            }
            return FluidIngredient.fromValues(StreamSupport.stream(jsonArray.spliterator(), false).map(jsonElement -> FluidIngredient.valueFromJson(GsonHelper.convertToJsonObject(jsonElement, "fluid"))), amount, nbt);
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

    public static CompoundTag getNBT(JsonElement element)
    {
        try
        {
            if (element.isJsonObject())
                return TagParser.parseTag(GSON.toJson(element));
            else
                return TagParser.parseTag(GsonHelper.convertToString(element, "nbt"));
        }
        catch (CommandSyntaxException e)
        {
            throw new JsonSyntaxException("Invalid NBT Entry: " + e);
        }
    }

    public static interface Value {
        public Collection<Fluid> getStacks();

        public JsonObject serialize();
        public Value copy();
    }

    public static class TagValue
            implements Value {
        private final TagKey<Fluid> tag;

        public TagValue(TagKey<Fluid> tag) {
            this.tag = tag;
        }

        @Override
        public Collection<Fluid> getStacks() {
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
    }

    public static class FluidValue
            implements Value {
        private final Fluid fluid;

        public FluidValue(Fluid item) {
            this.fluid = item;
        }

        @Override
        public Collection<Fluid> getStacks() {
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
    }
}
