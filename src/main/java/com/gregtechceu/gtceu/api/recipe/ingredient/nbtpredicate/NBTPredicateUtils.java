package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class NBTPredicateUtils {

    public static JsonElement toJson(Tag tag) {
        try {
            return JsonParser.parseString(tag.getAsString());
        } catch (Exception ignored) {
            return new JsonPrimitive(tag.getAsString());
        }
    }

    public static Tag fromJson(JsonElement e) {
        if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isBoolean()) return ByteTag.valueOf(p.getAsBoolean());
            if (p.isNumber()) return DoubleTag.valueOf(p.getAsDouble());
            return StringTag.valueOf(p.getAsString());
        } else if (e.isJsonObject() || e.isJsonArray()) {
            try {
                return TagParser.parseTag(e.toString());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid NBT value: " + e, ex);
            }
        }
        return null;
    }

    public static Tag getNestedTag(CompoundTag tag, String path) {
        String[] parts = path.split("\\.");
        Tag current = tag;
        for (String p : parts) {
            if (!(current instanceof CompoundTag compound) || !compound.contains(p)) {
                return null;
            }
            current = compound.get(p);
        }
        return current;
    }
}
