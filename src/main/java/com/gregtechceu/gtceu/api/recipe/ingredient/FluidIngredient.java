package com.gregtechceu.gtceu.api.recipe.ingredient;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class FluidIngredient implements Predicate<FluidStack> {

    public static final Codec<FluidIngredient> CODEC = ExtraCodecs.JSON
            .xmap(FluidIngredient::fromJson, FluidIngredient::toJson);

    public static final FluidIngredient EMPTY = new FluidIngredient(new Value[0], 0, null);
    public static final FluidStack[] EMPTY_STACK_ARRAY = new FluidStack[0];
    public FluidIngredient.Value[] values;
    @Nullable
    public FluidStack[] stacks;
    @Getter
    protected int amount;
    @Getter
    protected CompoundTag nbt;
    protected boolean changed = true;

    protected FluidIngredient(Value[] values, int amount, @Nullable CompoundTag nbt) {
        this.values = values;
        this.amount = amount;
        this.nbt = nbt;
    }

    public static FluidIngredient fromValues(Value[] values, int amount, @Nullable CompoundTag nbt) {
        if (values.length == 0) return EMPTY;
        return new FluidIngredient(values, amount, nbt);
    }

    public static FluidIngredient fromValues(List<? extends FluidIngredient.Value> values,
                                             int amount, @Nullable CompoundTag nbt) {
        return fromValues(values.toArray(Value[]::new), amount, nbt);
    }

    public static FluidIngredient fromValue(FluidIngredient.Value value, int amount, @Nullable CompoundTag nbt) {
        return fromValues(new Value[] { value }, amount, nbt);
    }

    private List<Fluid> getFluids() {
        List<Fluid> fluids = new ArrayList<>(1);
        for (Value value : this.values) {
            fluids.addAll(value.getFluids());
        }
        return fluids;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeCollection(this.getFluids(), (buf, fluid) -> buf.writeId(BuiltInRegistries.FLUID, fluid));
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
        return new FluidIngredient(values, this.amount, this.nbt == null ? null : this.nbt.copy());
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

        Value[] myValues = this.values.clone();
        Value[] otherValues = other.values.clone();
        Arrays.parallelSort(myValues, VALUE_COMPARATOR);
        Arrays.parallelSort(otherValues, VALUE_COMPARATOR);

        for (Value value1 : myValues) {
            for (Value value2 : otherValues) {
                if (value1 instanceof TagValue first) {
                    if (!(value2 instanceof TagValue second)) {
                        return false;
                    }
                    if (first.tag != second.tag) {
                        return false;
                    }
                } else if (value1 instanceof FluidValue first) {
                    if (!(value2 instanceof FluidValue second)) {
                        return false;
                    }
                    if (first.fluid != second.fluid) {
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

    public static FluidIngredient of(Fluid fluid, int amount) {
        return of(fluid, amount, null);
    }

    public static FluidIngredient of(Fluid fluid, int amount, @Nullable CompoundTag nbt) {
        return FluidIngredient.fromValue(new FluidValue(fluid), amount, nbt);
    }

    public static FluidIngredient of(List<Fluid> fluids, int amount, @Nullable CompoundTag nbt) {
        if (fluids.isEmpty()) return FluidIngredient.EMPTY;
        if (fluids.size() == 1) return of(fluids.get(0), amount, nbt);

        List<Value> values = new ArrayList<>();
        for (Fluid fluid : fluids) {
            values.add(new FluidValue(fluid));
        }
        return FluidIngredient.fromValues(values, amount, nbt);
    }

    public static FluidIngredient of(FluidStack stack) {
        if (stack.isEmpty()) return FluidIngredient.EMPTY;
        return FluidIngredient.fromValue(new FluidValue(stack.getFluid()), stack.getAmount(), stack.getTag());
    }

    public static FluidIngredient of(List<FluidStack> stacks) {
        if (stacks.isEmpty()) return FluidIngredient.EMPTY;
        if (stacks.size() == 1) return of(stacks.get(0));

        List<Value> values = new ArrayList<>();
        CompoundTag tag = null;

        for (FluidStack stack : stacks) {
            if (!stack.isEmpty()) {
                values.add(new FluidValue(stack.getFluid()));
                if (tag == null) tag = stack.getTag();
            }
        }
        return FluidIngredient.fromValues(values, stacks.get(0).getAmount(), tag);
    }

    /**
     * {@return a new ingredient which accepts items which are in the given tag}
     *
     * @param tag the tag key
     */
    public static FluidIngredient of(TagKey<Fluid> tag, int amount) {
        return FluidIngredient.fromValue(new TagValue(tag), amount, null);
    }

    public static FluidIngredient of(TagKey<Fluid> tag, int amount, @Nullable CompoundTag nbt) {
        return FluidIngredient.fromValue(new TagValue(tag), amount, nbt);
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf buffer) {
        List<Fluid> fluids = buffer.readList(buf -> buf.readById(BuiltInRegistries.FLUID));
        return FluidIngredient.of(fluids, buffer.readVarInt(), buffer.readNbt());
    }

    public static FluidIngredient fromJson(@Nullable JsonElement json) {
        return FluidIngredient.fromJson(json, true);
    }

    public static FluidIngredient fromJson(@Nullable JsonElement json, boolean allowEmpty) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Fluid ingredient cannot be null");
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(json, "ingredient");
        if (GsonHelper.isObjectNode(jsonObject, "count_provider")) {
            return IntProviderFluidIngredient.fromJson(json);
        }

        int amount = GsonHelper.getAsInt(jsonObject, "amount", 0);
        CompoundTag nbt = jsonObject.has("nbt") ? CraftingHelper.getNBT(jsonObject.get("nbt")) : null;

        if (GsonHelper.isObjectNode(jsonObject, "value")) {
            Value value = FluidIngredient.valueFromJson(GsonHelper.getAsJsonObject(jsonObject, "value"));
            return FluidIngredient.fromValue(value, amount, nbt);
        } else if (GsonHelper.isArrayNode(jsonObject, "value")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "value");
            if (jsonArray.isEmpty() && !allowEmpty) {
                throw new JsonSyntaxException("Fluid array cannot be empty, at least one fluid must be defined");
            }
            List<Value> values = new ArrayList<>();
            for (JsonElement e : jsonArray) {
                values.add(FluidIngredient.valueFromJson(GsonHelper.convertToJsonObject(e, "fluid")));
            }
            return FluidIngredient.fromValues(values, amount, nbt);
        } else if (GsonHelper.isStringValue(jsonObject, "value")) {
            String value = GsonHelper.getAsString(jsonObject, "value");
            if (value.startsWith("#")) {
                ResourceLocation resourceLocation = new ResourceLocation(value.substring(1));
                TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, resourceLocation);
                return FluidIngredient.fromValue(new TagValue(tagKey), amount, nbt);
            } else {
                Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(value));
                return FluidIngredient.fromValue(new FluidValue(fluid), amount, nbt);
            }
        } else {
            throw new JsonSyntaxException("expected 'value' to be an object, an array or a string.");
        }
    }

    private static FluidIngredient.Value valueFromJson(JsonObject json) {
        if (json.has("fluid") && json.has("tag")) {
            throw new JsonParseException("A fluid ingredient entry is either a tag or a fluid, not both");
        }
        if (json.has("fluid")) {
            Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(GsonHelper.getAsString(json, "fluid")));
            return new FluidValue(fluid);
        }
        if (json.has("tag")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, resourceLocation);
            return new TagValue(tagKey);
        }
        throw new JsonParseException("A fluid ingredient entry needs either a tag or a fluid");
    }

    public interface Value {

        Collection<Fluid> getFluids();

        JsonObject serialize();
    }

    public record TagValue(TagKey<Fluid> tag) implements Value {

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
    }

    public record FluidValue(Fluid fluid) implements Value {

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
    }

    public static final Comparator<Fluid> FLUID_COMPARATOR = Comparator.comparing(BuiltInRegistries.FLUID::getKey);

    public static final Comparator<FluidIngredient.Value> VALUE_COMPARATOR = new Comparator<>() {

        @Override
        public int compare(FluidIngredient.Value value1, FluidIngredient.Value value2) {
            if (value1 instanceof FluidIngredient.TagValue first) {
                if (!(value2 instanceof FluidIngredient.TagValue second)) {
                    return 1;
                }
                if (first.tag() != second.tag()) {
                    return 1;
                }
            } else if (value1 instanceof FluidIngredient.FluidValue first) {
                if (!(value2 instanceof FluidIngredient.FluidValue second)) {
                    return 1;
                }
                return FLUID_COMPARATOR.compare(first.fluid, second.fluid);
            }
            return 0;
        }
    };
}
