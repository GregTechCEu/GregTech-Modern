package com.gregtechceu.gtceu.utils.data;

import net.minecraft.nbt.*;

import com.google.gson.*;

import java.util.Set;

public class NBTToJsonConverter {

    public static JsonElement getObject(Tag tag) {
        JsonElement jsonRoot;
        if (tag instanceof CompoundTag compoundTag) {
            Set<String> keys = compoundTag.getAllKeys();
            jsonRoot = new JsonObject();
            for (String key : keys) {
                Tag nbt = compoundTag.get(key);
                ((JsonObject) jsonRoot).add(key, getObject(nbt));
            }
        } else if (tag instanceof NumericTag numericTag) {
            jsonRoot = new JsonPrimitive(numericTag.getAsNumber());
        } else if (tag instanceof StringTag) {
            jsonRoot = new JsonPrimitive(tag.getAsString());
        } else {
            JsonArray array;
            if (tag instanceof ListTag tagList) {
                array = new JsonArray();
                for (Tag value : tagList) {
                    array.add(getObject(value));
                }
                jsonRoot = array;
            } else if (tag instanceof IntArrayTag intArray) {
                array = new JsonArray();
                for (int i : intArray.getAsIntArray()) {
                    array.add(new JsonPrimitive(i));
                }
                jsonRoot = array;
            } else if (tag instanceof ByteArrayTag intArray) {
                array = new JsonArray();
                for (byte i : intArray.getAsByteArray()) {
                    array.add(new JsonPrimitive(i));
                }
                jsonRoot = array;
            } else if (tag instanceof LongArrayTag intArray) {
                array = new JsonArray();
                for (long i : intArray.getAsLongArray()) {
                    array.add(new JsonPrimitive(i));
                }
                jsonRoot = array;
            } else {
                throw new JsonParseException(
                        "NBT to JSON converter doesn't support the nbt tag: " + tag.getType() + ", tag: " + tag);
            }
        }

        return jsonRoot;
    }
}
