package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

/**
 * Allows decoded data to be written to an existing instance.
 *
 * @param <A> type of instance
 */
public interface MutableDecoder<A> extends Decoder<A>, InstanceDecoder<A> {

    <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input, A instance);

    @Override
    default <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        var d = decodeInstance(ops, input);
        var result = d.result();
        if (result.isEmpty()) return d;
        return decode(ops, input, result.get().getFirst());
    }

    default <T> DataResult<A> parse(final DynamicOps<T> ops, final T input, A instance) {
        return decode(ops, input, instance).map(Pair::getFirst);
    }

    default <T> DataResult<Pair<A, T>> decode(final Dynamic<T> input, A instance) {
        return decode(input.getOps(), input.getValue(), instance);
    }

    default <T> DataResult<A> parse(final Dynamic<T> input, A instance) {
        return decode(input, instance).map(Pair::getFirst);
    }
}
