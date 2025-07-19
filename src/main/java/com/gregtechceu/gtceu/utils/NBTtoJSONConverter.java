package com.gregtechceu.gtceu.utils;

import net.minecraft.nbt.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class NBTtoJSONConverter {

    public static JsonElement nbtToJSON(Tag nbt) {
        if (nbt instanceof NumericTag numTag) {
            return new JsonPrimitive(numTag.getAsNumber());
        } else if (nbt instanceof StringTag strTag) {
            return new JsonPrimitive(strTag.getAsString());
        } else if (nbt instanceof IntArrayTag intArrTag) {
            var arr = new JsonArray();
            intArrTag.forEach(v -> arr.add(v.getAsInt()));
            return arr;
        } else if (nbt instanceof LongArrayTag longArrTag) {
            var arr = new JsonArray();
            longArrTag.forEach(v -> arr.add(v.getAsLong()));
            return arr;
        } else if (nbt instanceof ByteArrayTag byteArrTag) {
            var arr = new JsonArray();
            byteArrTag.forEach(v -> arr.add(v.getAsByte()));
            return arr;
        } else if (nbt instanceof ListTag listTag) {
            var arr = new JsonArray();
            listTag.forEach(v -> arr.add(nbtToJSON(v)));
            return arr;
        } else if (nbt instanceof CompoundTag compoundTag) {
            var elem = new JsonObject();
            for (String key : compoundTag.getAllKeys()) {
                elem.add(key, nbtToJSON(compoundTag.get(key)));
            }
            return elem;
        }
        throw new IllegalArgumentException("Cannot convert NBT tag %s into JSON".formatted(nbt.getType().getName()));
    }
}
