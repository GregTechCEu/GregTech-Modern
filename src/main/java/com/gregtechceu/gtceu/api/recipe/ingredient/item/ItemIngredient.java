package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.ingredient.NBTPredicateIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.ItemValueAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.function.Supplier;

public abstract class ItemIngredient {

    public static final Codec<ItemIngredient> CODEC = ExtraCodecs.JSON.xmap(ItemIngredient::fromJson,
            ItemIngredient::toJson);

    private static final byte TYPE_SIMPLE_ITEM = 0;
    private static final byte TYPE_SIMPLE_TAG = 1;
    private static final byte TYPE_VANILLA = 2;
    private static final byte TYPE_CHANCED = 3;
    private static final byte TYPE_RANGED = 4;
    private static final byte TYPE_CIRCUIT = 5;

    protected final int count;

    private int hashCode;

    protected ItemIngredient(int count) {
        this.count = count;
    }

    public abstract int hash();

    @Override
    public final int hashCode() {
        if (hashCode == 0) {
            hashCode = hash();
        }
        return hashCode;
    };

    public int getCount() {
        return count;
    }

    public ItemIngredient getInner() {
        return this;
    }

    public boolean isChanced() {
        return false;
    }

    public int getChance() {
        return ChancedItemIngredient.MAX_CHANCE;
    }

    public boolean isRanged() {
        return false;
    }

    // for simulate
    public abstract ItemStack[] getItems();

    // for execute
    public ItemStack toStack() {
        return getItems()[0];
    };

    public abstract List<AbstractMapIngredient> getMapIngredients();

    public abstract boolean test(ItemStack itemStack);

    public abstract ItemIngredient copy();

    public abstract ItemIngredient copyWithCount(int count);

    public abstract ItemIngredient copyWithMultiplier(int multiplier);

    public abstract ItemIngredient copyWithChance(int chance);

    public static ItemIngredient of(ItemIngredient ingredient) {
        return ingredient;
    }

    public static ItemIngredient of(ItemStack stack) {
        return of(stack, stack.getCount());
    }

    public static ItemIngredient of(ItemStack stack, int count) {
        if (stack.hasTag()) {
            return of(StrictNBTIngredient.of(stack), count);
        }
        return new SimpleItemIngredient(stack.getItem(), count);
    }

    public static ItemIngredient of(ItemLike itemLike) {
        return of(itemLike, 1);
    }

    public static ItemIngredient of(ItemLike itemLike, int count) {
        return new SimpleItemIngredient(itemLike.asItem(), count);
    }

    public static ItemIngredient of(Supplier<? extends ItemLike> supplier) {
        return of(supplier.get());
    }

    public static ItemIngredient of(Supplier<? extends ItemLike> supplier, int count) {
        return of(supplier.get(), count);
    }

    public static ItemIngredient of(TagKey<Item> tag) {
        return of(tag, 1);
    }

    public static ItemIngredient of(TagKey<Item> tag, int count) {
        return new SimpleTagIngredient(tag, count);
    }

    public static ItemIngredient of(Ingredient ingredient) {
        return of(ingredient, 1);
    }

    public static ItemIngredient of(Ingredient ingredient, int count) {
        if (ingredient.getClass() == Ingredient.class) {
            var values = ((IngredientAccessor) ingredient).getValues();
            if (values.length == 1) {
                var value = values[0];
                if (value instanceof ItemValueAccessor itemValue) {
                    return new SimpleItemIngredient(itemValue.getItem().getItem(), count);
                } else if (value instanceof TagValueAccessor tagValue) {
                    return new SimpleTagIngredient(tagValue.getTag(), 1);
                }
            }
        }
        return new VanillaIngredient(ingredient, count);
    }

    public static ItemIngredient of(TagPrefix tagPrefix, Material material) {
        return of(tagPrefix, material, 1);
    }

    public static ItemIngredient of(TagPrefix tagPrefix, Material material, int count) {
        TagKey<Item> tag = ChemicalHelper.getTag(tagPrefix, material);
        if (tag != null) {
            return of(tag, count);
        }
        return of(ChemicalHelper.get(tagPrefix, material, count));
    }

    public static ItemIngredient of(MaterialEntry entry) {
        return of(entry, 1);
    }

    public static ItemIngredient of(MaterialEntry entry, int count) {
        return of(entry.tagPrefix(), entry.material(), count);
    }

    public static ItemIngredient of(MachineDefinition machine) {
        return of(machine, 1);
    }

    public static ItemIngredient of(MachineDefinition machine, int count) {
        return of(machine.asStack(count));
    }

    public static ItemIngredient of(ItemStack stack, NBTPredicate predicate) {
        return of(NBTPredicateIngredient.of(stack, predicate), stack.getCount());
    }

    public static ItemIngredient circuit(int configuration) {
        return IntCircuitIngredient.of(configuration);
    }

    public static ItemIngredient ranged(ItemIngredient ingredient, int minCount, int maxCount) {
        return new RangedItemIngredient(ingredient.copyWithCount(1), minCount, maxCount);
    }

    public static ItemIngredient ranged(ItemStack stack, int minCount, int maxCount) {
        return new RangedItemIngredient(of(stack, 1), minCount, maxCount);
    }

    public static ItemIngredient ranged(ItemLike itemLike, int minCount, int maxCount) {
        return new RangedItemIngredient(of(itemLike, 1), minCount, maxCount);
    }

    public static ItemIngredient ranged(Supplier<? extends ItemLike> supplier, int minCount, int maxCount) {
        return ranged(supplier.get(), minCount, maxCount);
    }

    public static ItemIngredient ranged(TagKey<Item> tag, int minCount, int maxCount) {
        return new RangedItemIngredient(of(tag, 1), minCount, maxCount);
    }

    public static ItemIngredient ranged(TagPrefix tagPrefix, Material material, int minCount, int maxCount) {
        return new RangedItemIngredient(of(tagPrefix, material, 1), minCount, maxCount);
    }

    public static ItemIngredient ranged(MaterialEntry entry, int minCount, int maxCount) {
        return ranged(entry.tagPrefix(), entry.material(), minCount, maxCount);
    }

    public static ItemIngredient ranged(MachineDefinition machine, int minCount, int maxCount) {
        return ranged(machine.asStack(), minCount, maxCount);
    }

    public static ItemIngredient fromNetwork(FriendlyByteBuf buf) {
        byte type = buf.readByte();
        return switch (type) {
            case TYPE_SIMPLE_ITEM -> new SimpleItemIngredient(buf.readById(BuiltInRegistries.ITEM), buf.readVarInt());
            case TYPE_SIMPLE_TAG -> new SimpleTagIngredient(TagKey.create(Registries.ITEM, buf.readResourceLocation()),
                    buf.readVarInt());
            case TYPE_VANILLA -> new VanillaIngredient(Ingredient.fromNetwork(buf), buf.readVarInt());
            case TYPE_CHANCED -> new ChancedItemIngredient(fromNetwork(buf), buf.readVarInt(), buf.readVarInt());
            case TYPE_RANGED -> new RangedItemIngredient(fromNetwork(buf), buf.readVarInt(), buf.readVarInt());
            case TYPE_CIRCUIT -> IntCircuitIngredient.of(buf.readVarInt());
            default -> throw new IllegalArgumentException("Unknown ItemIngredient network type: " + type);
        };
    }

    public void toNetwork(FriendlyByteBuf buf) {
        if (this instanceof ChancedItemIngredient ingredient) {
            buf.writeByte(TYPE_CHANCED);
            ingredient.getInner().toNetwork(buf);
            buf.writeVarInt(ingredient.getChance());
            buf.writeVarInt(ingredient.getMultiplier());
        } else if (this instanceof RangedItemIngredient ingredient) {
            buf.writeByte(TYPE_RANGED);
            ingredient.getInner().toNetwork(buf);
            buf.writeVarInt(ingredient.getMinCount());
            buf.writeVarInt(ingredient.getCount());
        } else if (this instanceof IntCircuitIngredient ingredient) {
            buf.writeByte(TYPE_CIRCUIT);
            buf.writeVarInt(ingredient.getConfiguration());
        } else if (this instanceof SimpleItemIngredient ingredient) {
            buf.writeByte(TYPE_SIMPLE_ITEM);
            buf.writeId(BuiltInRegistries.ITEM, ingredient.getItem());
            buf.writeVarInt(ingredient.getCount());
        } else if (this instanceof SimpleTagIngredient ingredient) {
            buf.writeByte(TYPE_SIMPLE_TAG);
            buf.writeResourceLocation(ingredient.getTag().location());
            buf.writeVarInt(ingredient.getCount());
        } else if (this instanceof VanillaIngredient ingredient) {
            buf.writeByte(TYPE_VANILLA);
            ingredient.getIngredient().toNetwork(buf);
            buf.writeVarInt(ingredient.getCount());
        } else {
            throw new IllegalArgumentException("Unsupported ItemIngredient implementation: " + getClass().getName());
        }
    }

    @SuppressWarnings("removal")
    public static ItemIngredient fromJson(JsonElement json) {
        if (!json.isJsonObject()) {
            return new VanillaIngredient(Ingredient.fromJson(json), 1);
        }

        JsonObject object = GsonHelper.convertToJsonObject(json, "item ingredient");
        int count = GsonHelper.getAsInt(object, "count", 1);

        if (object.has("configuration")) {
            return IntCircuitIngredient.of(GsonHelper.getAsInt(object, "configuration"));
        }

        if (object.has("chance")) {
            ItemIngredient inner = fromJson(GsonHelper.getAsJsonObject(object, "ingredient"));
            int chance = GsonHelper.getAsInt(object, "chance");
            int multiplier = GsonHelper.getAsInt(object, "multiplier", 1);
            return new ChancedItemIngredient(inner, chance, multiplier);
        }

        if (object.has("min_count") || object.has("max_count")) {
            ItemIngredient inner = fromJson(GsonHelper.getAsJsonObject(object, "ingredient"));
            int minCount = GsonHelper.getAsInt(object, "min_count", 0);
            int maxCount = GsonHelper.getAsInt(object, "max_count", count);
            return new RangedItemIngredient(inner, minCount, maxCount);
        }

        if (object.has("item")) {
            ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(object, "item"));
            Item item = BuiltInRegistries.ITEM.getOptional(id)
                    .orElseThrow(() -> new JsonParseException("Unknown item '" + id + "'"));
            return new SimpleItemIngredient(item, count);
        }

        if (object.has("tag")) {
            ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(object, "tag"));
            return new SimpleTagIngredient(TagKey.create(Registries.ITEM, id), count);
        }

        if (object.has("ingredient")) {
            return new VanillaIngredient(Ingredient.fromJson(object.get("ingredient")), count);
        }

        return new VanillaIngredient(Ingredient.fromJson(json), count);
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("count", count);

        if (this instanceof ChancedItemIngredient ingredient) {
            json.add("ingredient", ingredient.getInner().toJson());
            json.addProperty("chance", ingredient.getChance());
            json.addProperty("multiplier", ingredient.getMultiplier());
        } else if (this instanceof IntCircuitIngredient ingredient) {
            json.addProperty("configuration", ingredient.getConfiguration());
        } else if (this instanceof RangedItemIngredient ingredient) {
            json.add("ingredient", ingredient.getInner().toJson());
            json.addProperty("min_count", ingredient.getMinCount());
            json.addProperty("max_count", ingredient.getCount());
        } else if (this instanceof SimpleItemIngredient ingredient) {
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString());
        } else if (this instanceof SimpleTagIngredient ingredient) {
            json.addProperty("tag", ingredient.getTag().location().toString());
        } else if (this instanceof VanillaIngredient ingredient) {
            json.add("ingredient", ingredient.getIngredient().toJson());
        } else {
            throw new JsonParseException("Unsupported ItemIngredient implementation: " + getClass().getName());
        }

        return json;
    }
}
