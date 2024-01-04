package com.gregtechceu.gtceu.core.fabric;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.Objects;

public class RecipeHelpers {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Item getItem(String itemName, boolean disallowsAirInRecipe) {
        Item item = tryGetItem(itemName, disallowsAirInRecipe);
        if (item == null) {
            if (!BuiltInRegistries.ITEM.containsKey(new ResourceLocation(itemName)))
                throw new JsonSyntaxException("Unknown item '" + itemName + "'");
            if (disallowsAirInRecipe && (item == Items.AIR || item == null))
                throw new JsonSyntaxException("Invalid item: " + itemName);
        }
        return Objects.requireNonNull(item);
    }

    @Nullable
    public static Item tryGetItem(String itemName, boolean disallowsAirInRecipe) {
        ResourceLocation itemKey = new ResourceLocation(itemName);
        if (!BuiltInRegistries.ITEM.containsKey(itemKey))
            return null;

        Item item = BuiltInRegistries.ITEM.get(itemKey);
        if (disallowsAirInRecipe && item == Items.AIR)
            return null;
        return item;
    }

    public static CompoundTag getNBT(JsonElement element) {
        try {
            if (element.isJsonObject())
                return TagParser.parseTag(GSON.toJson(element));
            else
                return TagParser.parseTag(GsonHelper.convertToString(element, "nbt"));
        } catch (
            CommandSyntaxException e) {
            throw new JsonSyntaxException("Invalid NBT Entry: " + e);
        }
    }

    public static ItemStack getItemStack(JsonObject json, boolean readNBT, boolean disallowsAirInRecipe) {
        String itemName = GsonHelper.getAsString(json, "item");
        Item item = getItem(itemName, disallowsAirInRecipe);
        if (readNBT && json.has("nbt")) {
            CompoundTag nbt = getNBT(json.get("nbt"));
            CompoundTag tmp = new CompoundTag();

            tmp.put("tag", nbt);
            tmp.putString("id", itemName);
            tmp.putInt("Count", GsonHelper.getAsInt(json, "count", 1));

            return ItemStack.of(tmp);
        }

        return new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
    }
}
