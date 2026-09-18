package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A {@link Codec} that can decode mutable field to an existing instance.
 *
 * @param <A> type of instance
 */
public interface MutableCodec<A> extends Codec<A>, MutableDecoder<A> {

    default DataResult<Pair<A, JsonElement>> decodeJson(JsonObject json) {
        return decode(JsonOps.INSTANCE, json);
    }

    default DataResult<A> parseJson(JsonObject json) {
        return parse(JsonOps.INSTANCE, json);
    }

    default DataResult<Pair<A, JsonElement>> decodeJson(JsonObject json, A instance) {
        return decode(JsonOps.INSTANCE, json, instance);
    }

    default DataResult<A> parseJson(JsonObject json, A instance) {
        return parse(JsonOps.INSTANCE, json, instance);
    }

    default DataResult<JsonElement> encodeJson(A instance) {
        return encodeStart(JsonOps.INSTANCE, instance);
    }

    default DataResult<JsonElement> encodeJson(A instance, JsonElement prefix) {
        return encode(instance, JsonOps.INSTANCE, prefix);
    }
}
