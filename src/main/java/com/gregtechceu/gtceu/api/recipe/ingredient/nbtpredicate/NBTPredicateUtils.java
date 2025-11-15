package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public final class NBTPredicateUtils {

    private NBTPredicateUtils(){};

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

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            int bracketIndex = part.indexOf('[');

            if (bracketIndex == -1) {
                // simple compound key
                if (!(current instanceof CompoundTag compound) || !compound.contains(part)) {
                    return null;
                }
                current = compound.get(part);
            } else {
                // compound key with array index
                String key = part.substring(0, bracketIndex);
                String indexSection = part.substring(bracketIndex); // e.g. "[4][2]"
                if (!(current instanceof CompoundTag compound) || !compound.contains(key)) {
                    return null;
                }
                Tag arrayTag = compound.get(key);
                if (!(arrayTag instanceof ListTag list)) {
                    return null;
                }

                // There can be multiple nested indices like arr[1][3]
                Tag element = arrayTag;
                int from = 0;
                while (true) {
                    int open = indexSection.indexOf('[', from);
                    int close = indexSection.indexOf(']', from);
                    if (open == -1 || close == -1) break;
                    String numStr = indexSection.substring(open + 1, close);
                    int index;
                    try {
                        index = Integer.parseInt(numStr);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    if (!(element instanceof ListTag listTag)) return null;
                    if (index < 0 || index >= listTag.size()) return null;
                    element = listTag.get(index);
                    from = close + 1;
                }
                current = element;
            }
        }

        return current;
    }
}
