package brachy.modularui.utils.serialization.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapLike;

public interface MutableMapDecoder<A> extends MapDecoder<A>, InstanceMapDecoder<A> {

    <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input, A instance);

    @Override
    default <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
        var d = decodeInstance(ops, input);
        var result = d.result();
        if (result.isEmpty()) return d;
        return decode(ops, input, result.get());
    }
}
