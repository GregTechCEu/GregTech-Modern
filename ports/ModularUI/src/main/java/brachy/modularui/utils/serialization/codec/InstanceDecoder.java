package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

/**
 * Creates an instance and only serializes any immutable fields.
 * For no arg constructors this can just be a supplier.
 *
 * @param <A> type of instance
 */
public interface InstanceDecoder<A> {

    <T> DataResult<Pair<A, T>> decodeInstance(final DynamicOps<T> ops, final T input);

    default <T> DataResult<A> parseInstance(final DynamicOps<T> ops, final T input) {
        return decodeInstance(ops, input).map(Pair::getFirst);
    }

    default <T> DataResult<Pair<A, T>> decodeInstance(final Dynamic<T> dynamic) {
        return decodeInstance(dynamic.getOps(), dynamic.getValue());
    }

    default <T> DataResult<A> parseInstance(final Dynamic<T> dynamic) {
        return decodeInstance(dynamic.getOps(), dynamic.getValue()).map(Pair::getFirst);
    }

    default boolean canDecodeInstance() {
        return true;
    }
}
