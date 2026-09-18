package brachy.modularui.utils.serialization.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

public interface InstanceMapDecoder<A> {

    <T> DataResult<A> decodeInstance(final DynamicOps<T> ops, final MapLike<T> input);

    default boolean canDecodeInstance() {
        return true;
    }
}
