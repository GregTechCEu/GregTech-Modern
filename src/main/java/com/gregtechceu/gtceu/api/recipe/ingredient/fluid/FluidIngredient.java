package com.gregtechceu.gtceu.api.recipe.ingredient.fluid;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.ingredient.IChancedIngredient;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FluidIngredient {

    public static final Codec<FluidIngredient> CODEC = ExtraCodecs.JSON.xmap(FluidIngredient::fromJson,
            FluidIngredient::toJson);

    private static final byte TYPE_VALUE = 0;
    private static final byte TYPE_CHANCED = 1;
    private static final byte TYPE_RANGED = 2;

    protected final int amount;
    @Getter
    protected final Value value;
    private FluidStack[] stacks;
    private int hashCode;

    protected FluidIngredient(Value value) {
        this(value.amount(), value);
    }

    protected FluidIngredient(int amount, Value value) {
        this.amount = amount;
        this.value = value;
    }

    protected FluidIngredient(int amount) {
        this.amount = amount;
        this.value = null;
    }

    public int hash() {
        return value.hash();
    }

    @Override
    public final int hashCode() {
        if (hashCode == 0) {
            hashCode = hash();
        }
        return hashCode;
    }

    public int getAmount() {
        return amount;
    }

    public FluidIngredient getInner() {
        return this;
    }

    public boolean isChanced() {
        return false;
    }

    public int getChance() {
        return IChancedIngredient.MAX_CHANCE;
    }

    public boolean isRanged() {
        return false;
    }

    public FluidStack[] getFluids() {
        if (stacks == null) {
            stacks = value.copyWithAmount(amount).getStacks().stream().map(FluidStack::copy).toArray(FluidStack[]::new);
        }
        return stacks;
    }

    public FluidStack toStack() {
        FluidStack[] stacks = getFluids();
        return stacks.length == 0 ? FluidStack.EMPTY : stacks[0].copy();
    }

    public boolean test(FluidStack fluidStack) {
        return value.test(fluidStack);
    }

    public FluidIngredient copy() {
        return new FluidIngredient(amount, value.copy());
    }

    public FluidIngredient copyWithAmount(int amount) {
        return new FluidIngredient(amount, value.copy());
    }

    public FluidIngredient copyWithMultiplier(int multiplier) {
        return new FluidIngredient(amount * multiplier, value.copy());
    }

    public FluidIngredient copyWithChance(int chance) {
        return new ChancedFluidIngredient(copy(), chance);
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf buf) {
        byte type = buf.readByte();
        return switch (type) {
            case TYPE_VALUE -> new FluidIngredient(Value.fromNetwork(buf));
            case TYPE_CHANCED -> new ChancedFluidIngredient(fromNetwork(buf), buf.readVarInt(), buf.readVarInt());
            case TYPE_RANGED -> new RangedFluidIngredient(fromNetwork(buf), buf.readVarInt(), buf.readVarInt());
            default -> throw new IllegalArgumentException("Unknown FluidIngredient network type: " + type);
        };
    }

    public void toNetwork(FriendlyByteBuf buf) {
        if (this instanceof ChancedFluidIngredient ingredient) {
            buf.writeByte(TYPE_CHANCED);
            ingredient.getInner().toNetwork(buf);
            buf.writeVarInt(ingredient.getChance());
            buf.writeVarInt(ingredient.getMultiplier());
        } else if (this instanceof RangedFluidIngredient ingredient) {
            buf.writeByte(TYPE_RANGED);
            ingredient.getInner().toNetwork(buf);
            buf.writeVarInt(ingredient.getMinAmount());
            buf.writeVarInt(ingredient.getAmount());
        } else {
            buf.writeByte(TYPE_VALUE);
            value.copyWithAmount(amount).toNetwork(buf);
        }
    }

    public static FluidIngredient of(FluidIngredient ingredient) {
        return ingredient;
    }

    public static FluidIngredient of(FluidStack stack) {
        return new FluidIngredient(new FluidValue(stack));
    }

    public static FluidIngredient of(Fluid fluid, int amount) {
        return new FluidIngredient(new FluidValue(fluid, amount, null));
    }

    public static FluidIngredient of(Fluid fluid, int amount, @Nullable CompoundTag nbt) {
        return new FluidIngredient(new FluidValue(fluid, amount, nbt));
    }

    public static FluidIngredient of(TagKey<Fluid> tag, int amount) {
        return new FluidIngredient(new TagValue(tag, amount, null));
    }

    public static FluidIngredient of(TagKey<Fluid> tag, int amount, @Nullable CompoundTag nbt) {
        return new FluidIngredient(new TagValue(tag, amount, nbt));
    }

    public static FluidIngredient of(Material material, int amount) {
        return of(material.getFluid(amount));
    }

    public static FluidIngredient ranged(FluidStack stack, int minAmount, int maxAmount) {
        FluidStack inner = stack.copy();
        inner.setAmount(1);
        return new RangedFluidIngredient(of(inner), minAmount, maxAmount);
    }

    public static FluidIngredient ranged(Fluid fluid, int minAmount, int maxAmount) {
        return new RangedFluidIngredient(of(fluid, 1), minAmount, maxAmount);
    }

    public static FluidIngredient ranged(TagKey<Fluid> tag, int minAmount, int maxAmount) {
        return new RangedFluidIngredient(of(tag, 1), minAmount, maxAmount);
    }

    public static FluidIngredient fromJson(JsonElement json) {
        JsonObject object = GsonHelper.convertToJsonObject(json, "fluid ingredient");
        int amount = GsonHelper.getAsInt(object, "amount", 0);

        if (object.has("chance")) {
            FluidIngredient inner = fromJson(GsonHelper.getAsJsonObject(object, "ingredient"));
            int chance = GsonHelper.getAsInt(object, "chance");
            int multiplier = GsonHelper.getAsInt(object, "multiplier", 1);
            return new ChancedFluidIngredient(inner, chance, multiplier);
        }

        if (object.has("min_amount") || object.has("max_amount")) {
            FluidIngredient inner = fromJson(GsonHelper.getAsJsonObject(object, "ingredient"));
            int minAmount = GsonHelper.getAsInt(object, "min_amount", 0);
            int maxAmount = GsonHelper.getAsInt(object, "max_amount", amount);
            return new RangedFluidIngredient(inner, minAmount, maxAmount);
        }

        return new FluidIngredient(Value.fromJson(object));
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("amount", amount);

        if (this instanceof ChancedFluidIngredient ingredient) {
            json.add("ingredient", ingredient.getInner().toJson());
            json.addProperty("chance", ingredient.getChance());
            json.addProperty("multiplier", ingredient.getMultiplier());
        } else if (this instanceof RangedFluidIngredient ingredient) {
            json.add("ingredient", ingredient.getInner().toJson());
            json.addProperty("min_amount", ingredient.getMinAmount());
            json.addProperty("max_amount", ingredient.getAmount());
        } else {
            json = value.copyWithAmount(amount).toJson();
        }

        return json;
    }

    public abstract static class Value {

        protected static final byte TYPE_FLUID = 0;
        protected static final byte TYPE_TAG = 1;

        protected final int amount;
        @Nullable
        protected final CompoundTag nbt;

        protected Value(int amount, @Nullable CompoundTag nbt) {
            this.amount = amount;
            this.nbt = nbt == null ? null : nbt.copy();
        }

        public int amount() {
            return amount;
        }

        @Nullable
        public CompoundTag nbt() {
            return nbt;
        }

        public abstract Collection<FluidStack> getStacks();

        public abstract boolean test(FluidStack fluidStack);

        public abstract Value copy();

        public abstract Value copyWithAmount(int amount);

        public abstract int hash();

        public abstract JsonObject toJson();

        protected abstract byte networkType();

        protected abstract void toNetworkInner(FriendlyByteBuf buf);

        public final void toNetwork(FriendlyByteBuf buf) {
            buf.writeByte(networkType());
            buf.writeVarInt(amount);
            buf.writeNbt(nbt);
            toNetworkInner(buf);
        }

        public static Value fromNetwork(FriendlyByteBuf buf) {
            byte type = buf.readByte();
            int amount = buf.readVarInt();
            CompoundTag nbt = buf.readNbt();
            return switch (type) {
                case TYPE_FLUID -> new FluidValue(buf.readById(BuiltInRegistries.FLUID), amount, nbt);
                case TYPE_TAG -> new TagValue(TagKey.create(Registries.FLUID, buf.readResourceLocation()), amount, nbt);
                default -> throw new IllegalArgumentException("Unknown FluidIngredient value network type: " + type);
            };
        }

        public static Value fromJson(JsonObject json) {
            int amount = GsonHelper.getAsInt(json, "amount", 0);
            CompoundTag nbt = json.has("nbt") ? CraftingHelper.getNBT(json.get("nbt")) : null;
            if (json.has("fluid")) {
                ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
                Fluid fluid = BuiltInRegistries.FLUID.getOptional(id)
                        .orElseThrow(() -> new JsonParseException("Unknown fluid '" + id + "'"));
                return new FluidValue(fluid, amount, nbt);
            }
            if (json.has("tag")) {
                ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
                return new TagValue(TagKey.create(Registries.FLUID, id), amount, nbt);
            }
            throw new JsonParseException("A fluid ingredient value needs either a fluid or tag");
        }
    }

    public static final class FluidValue extends Value {

        @Getter
        private final Fluid fluid;

        public FluidValue(FluidStack stack) {
            this(stack.getFluid(), stack.getAmount(), stack.getTag());
        }

        public FluidValue(Fluid fluid, int amount, @Nullable CompoundTag nbt) {
            super(amount, nbt);
            this.fluid = fluid;
        }

        @Override
        public Collection<FluidStack> getStacks() {
            return List.of(new FluidStack(fluid, amount, nbt));
        }

        @Override
        public boolean test(FluidStack fluidStack) {
            return fluidStack.getFluid() == fluid && (nbt == null || nbt.equals(fluidStack.getTag()));
        }

        @Override
        public FluidValue copy() {
            return new FluidValue(fluid, amount, nbt);
        }

        @Override
        public FluidValue copyWithAmount(int amount) {
            return new FluidValue(fluid, amount, nbt);
        }

        @Override
        public int hash() {
            int result = fluid.hashCode();
            result = 31 * result + (nbt == null ? 0 : nbt.hashCode());
            return result;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
            json.addProperty("amount", amount);
            if (nbt != null) {
                json.addProperty("nbt", nbt.getAsString());
            }
            return json;
        }

        @Override
        protected byte networkType() {
            return TYPE_FLUID;
        }

        @Override
        protected void toNetworkInner(FriendlyByteBuf buf) {
            buf.writeId(BuiltInRegistries.FLUID, fluid);
        }
    }

    public static class TagValue extends Value {

        @Getter
        private final TagKey<Fluid> tag;

        public TagValue(TagKey<Fluid> tag, int amount, @Nullable CompoundTag nbt) {
            super(amount, nbt);
            this.tag = tag;
        }

        @Override
        public Collection<FluidStack> getStacks() {
            List<FluidStack> stacks = new ArrayList<>();
            for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(tag)) {
                stacks.add(new FluidStack(holder.value(), amount, nbt));
            }
            return stacks;
        }

        @Override
        public boolean test(FluidStack fluidStack) {
            return fluidStack.getFluid().is(tag) && (nbt == null || nbt.equals(fluidStack.getTag()));
        }

        @Override
        public TagValue copy() {
            return new TagValue(tag, amount, nbt);
        }

        @Override
        public TagValue copyWithAmount(int amount) {
            return new TagValue(tag, amount, nbt);
        }

        @Override
        public int hash() {
            int result = tag.hashCode();
            result = 31 * result + (nbt == null ? 0 : nbt.hashCode());
            return result;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("tag", tag.location().toString());
            json.addProperty("amount", amount);
            if (nbt != null) {
                json.addProperty("nbt", nbt.getAsString());
            }
            return json;
        }

        @Override
        protected byte networkType() {
            return TYPE_TAG;
        }

        @Override
        protected void toNetworkInner(FriendlyByteBuf buf) {
            buf.writeResourceLocation(tag.location());
        }
    }
}
