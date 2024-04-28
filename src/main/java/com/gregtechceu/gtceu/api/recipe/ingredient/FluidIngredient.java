package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FluidIngredient implements Predicate<FluidStack> {
    public static final FluidIngredient EMPTY = new FluidIngredient(Stream.empty(), 0, (PatchedDataComponentMap) null);
    public static final Codec<FluidIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Value.CODEC.listOf().fieldOf("values").forGetter(ing -> Arrays.stream(ing.values).toList()),
        ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 0).forGetter(ing -> ing.amount),
        DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ing -> ing.components.asPatch())
    ).apply(instance, (values, amount, tag) -> new FluidIngredient(values.stream(), amount, tag)));

    public FluidIngredient.Value[] values;
    @Nullable
    public FluidStack[] stacks;
    @Getter
    private int amount;
    @Getter
    private PatchedDataComponentMap components;
    private boolean changed = true;

    public FluidIngredient(Stream<? extends FluidIngredient.Value> empty, int amount, @Nullable PatchedDataComponentMap components) {
        this.values = empty.toArray(Value[]::new);
        this.amount = amount;
        this.components = components == null ? new PatchedDataComponentMap(DataComponentMap.EMPTY) : components;
    }
    public FluidIngredient(Stream<? extends FluidIngredient.Value> empty, int amount, Optional<PatchedDataComponentMap> components) {
        this.values = empty.toArray(Value[]::new);
        this.amount = amount;
        this.components = components.orElse(new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }
    public FluidIngredient(Stream<? extends FluidIngredient.Value> empty, int amount, DataComponentPatch patch) {
        this.values = empty.toArray(Value[]::new);
        this.amount = amount;
        this.components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch);
    }

    public static FluidIngredient fromValues(Stream<? extends FluidIngredient.Value> stream, int amount, @Nullable DataComponentPatch components) {
        FluidIngredient ingredient = new FluidIngredient(stream, amount, components);
        return ingredient.isEmpty() ? EMPTY : ingredient;
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        buffer.writeCollection(Arrays.asList(this.getStacks()), (buf, stack) -> FluidStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, stack));
        buffer.writeVarLong(amount);
        DataComponentPatch.STREAM_CODEC.encode(buffer, components.asPatch());
    }

    public FluidIngredient copy() {
        return new FluidIngredient(Arrays.stream(this.values).map(Value::copy), this.amount, this.components == null ? null : this.components.copy());
    }

    @Override
    public boolean test(@Nullable FluidStack stack) {
        if (stack == null) {
            return false;
        }
        if (this.isEmpty()) {
            return stack.isEmpty();
        }
        if (this.components != null && !this.components.equals(stack.getComponents())) {
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

        if (!Objects.equals(this.components, other.components)) return false;
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

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    public FluidStack[] getStacks() {
        if (changed || this.stacks == null) {
            List<FluidStack> fluidStacks = new ObjectArrayList<>(1);
            List<Holder<Fluid>> found = new ObjectArrayList<>(1);
            for (Value value : this.values) {
                for (Holder<Fluid> fluid : value.getFluids()) {
                    if (found.contains(fluid)) continue;
                    found.add(fluid);

                    fluidStacks.add(new FluidStack(fluid, this.amount, this.components.asPatch()));
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

    public void setComponents(DataComponentPatch components) {
        this.components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, components);
        this.changed = true;
    }

    public static FluidIngredient of() {
        return EMPTY;
    }

    public static FluidIngredient of(int amount, Fluid... items) {
        return FluidIngredient.of(Arrays.stream(items), amount, null);
    }

    public static FluidIngredient of(FluidStack... stacks) {
        return FluidIngredient.of(Arrays.stream(stacks).map(FluidStack::getFluid), stacks.length == 0 ? 0 : stacks[0].getAmount(), stacks.length == 0 ? null : stacks[0].getComponents().asPatch());
    }

    public static FluidIngredient of(Stream<Fluid> stacks, int amount, DataComponentPatch nbt) {
        return FluidIngredient.fromValues(stacks.filter(stack -> stack != null && !stack.isSame(Fluids.EMPTY)).map(Fluid::builtInRegistryHolder).map(FluidValue::new), amount, nbt);
    }

    /**
     * {@return a new ingredient which accepts items which are in the given tag}
     *
     * @param tag the tag key
     */
    public static FluidIngredient of(TagKey<Fluid> tag, int amount) {
        return FluidIngredient.fromValues(Stream.of(new FluidIngredient.TagValue(tag)), amount, null);
    }

    public static FluidIngredient of(TagKey<Fluid> tag, int amount, DataComponentPatch nbt) {
        return FluidIngredient.fromValues(Stream.of(new FluidIngredient.TagValue(tag)), amount, nbt);
    }

    public static FluidIngredient fromNetwork(RegistryFriendlyByteBuf buffer) {
        return FluidIngredient.fromValues(buffer.readList((buf) -> FluidStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf)).stream().map(stack -> new FluidValue(stack.getFluidHolder())), buffer.readVarInt(), DataComponentPatch.STREAM_CODEC.decode(buffer));
    }

    public static FluidIngredient fromJson(@Nullable JsonElement json) {
        return FluidIngredient.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    public static interface Value {
        Codec<Value> CODEC = Codec.xor(FluidValue.CODEC, TagValue.CODEC)
                .xmap(either -> either.map(fluidValue -> fluidValue, tagValue -> tagValue), value -> {
                    if (value instanceof TagValue tagValue) {
                        return Either.right(tagValue);
                    } else if (value instanceof FluidValue fluidValue) {
                        return Either.left(fluidValue);
                    } else {
                        throw new UnsupportedOperationException("This is neither a fluid value nor a tag value.");
                    }
                });

        public Collection<Holder<Fluid>> getFluids();

        public Value copy();
    }

    public static class TagValue implements Value {
        static final Codec<TagValue> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(value -> value.tag))
                        .apply(instance, TagValue::new)
        );

        private final TagKey<Fluid> tag;

        public TagValue(TagKey<Fluid> tag) {
            this.tag = tag;
        }

        @Override
        public Collection<Holder<Fluid>> getFluids() {
            ArrayList<Holder<Fluid>> list = Lists.newArrayList();
            for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(this.tag)) {
                list.add(holder);
            }
            return list;
        }

        @Override
        public Value copy() {
            return new TagValue(this.tag);
        }
    }

    public static class FluidValue implements Value {
        static final Codec<FluidValue> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fluid").forGetter(value -> value.fluid))
                    .apply(instance, FluidValue::new)
        );
        private final Holder<Fluid> fluid;

        public FluidValue(Holder<Fluid> item) {
            this.fluid = item;
        }

        @Override
        public Collection<Holder<Fluid>> getFluids() {
            return Collections.singleton(this.fluid);
        }

        @Override
        public Value copy() {
            return new FluidValue(this.fluid);
        }
    }
}
