package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.KeyDispatchCodec;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CodecUtil {

    public static <T> T unboxEither(Either<? extends T, ? extends T> either) {
        return either.map(Function.identity(), Function.identity());
    }

    public static <A> Codec<A> nullDecoder() {
        return nullDecoder(() -> null);
    }

    public static <A> Codec<A> nullDecoder(A decodedNull) {
        return nullDecoder(() -> decodedNull);
    }

    public static <A> Codec<A> nullDecoder(Supplier<A> decoder) {
        return nullCodec(decoder, a -> false);
    }

    public static <A> Codec<A> nullCodec() {
        return nullCodec(() -> null, Objects::isNull);
    }

    public static <A> Codec<A> nullCodec(A decodedNull) {
        return nullCodec(() -> decodedNull);
    }

    public static <A> Codec<A> nullCodec(Supplier<A> decoder) {
        return nullCodec(decoder, Objects::isNull);
    }

    public static <A> Codec<A> nullCodec(Supplier<A> decoder, Predicate<A> nullTester) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                if (input == null || Objects.equals(ops.empty(), input)) {
                    return DataResult.success(new Pair<>(decoder.get(), ops.empty()));
                }
                return DataResult.error(() -> "Not null");
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                if (nullTester.test(input)) {
                    return DataResult.success(ops.empty());
                }
                return DataResult.error(() -> "Not null");
            }
        };
    }

    public static <A> Codec<A> wrapNullsafe(Codec<A> codec) {
        return chainedCodec(nullCodec(), codec);
    }

    public static <A> Codec<A> decodeNullsafe(Codec<A> codec) {
        return chainedCodec(nullDecoder(), codec);
    }

    @SafeVarargs
    public static <A> Codec<A> chainedCodec(Codec<A>... codecs) {
        return Codec.of(chainedEncoder(codecs), chainedDecoder(codecs));
    }

    @SafeVarargs
    public static <A> Decoder<A> chainedDecoder(Decoder<A>... decoder) {
        if (decoder == null || decoder.length == 0) throw new NullPointerException();
        if (decoder.length == 1) return decoder[0];
        return new Decoder<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                StringBuilder message = new StringBuilder();
                DataResult<Pair<A, T>> last;
                for (var codec : decoder) {
                    last = codec.decode(ops, input);
                    if (last.result().isPresent()) return last;
                    message.append(last.error().orElseThrow().message()).append("; ");
                }
                return DataResult.error(() -> message.substring(0, message.length() - 2));
            }
        };
    }

    @SafeVarargs
    public static <A> Encoder<A> chainedEncoder(Encoder<A>... encoder) {
        if (encoder == null || encoder.length == 0) throw new NullPointerException();
        if (encoder.length == 1) return encoder[0];
        return new Encoder<>() {
            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                StringBuilder message = new StringBuilder();
                DataResult<T> last = null;
                for (var codec : encoder) {
                    last = codec.encode(input, ops, prefix);
                    if (last.result().isPresent()) return last;
                    message.append(last.error().orElseThrow().message()).append("; ");
                }
                return last.mapError(s -> message.substring(0, message.length() - 2));
            }
        };
    }

    @SafeVarargs
    public static <A> MapCodec<A> chainedMapCodec(MapCodec<A>... codecs) {
        if (codecs == null || codecs.length == 0) throw new NullPointerException();
        if (codecs.length == 1) return codecs[0];
        // I hate this
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                Stream<T> s = Stream.empty();
                for (MapCodec<A> mapCodec : codecs) {
                    s = Stream.concat(s, mapCodec.keys(ops));
                }
                return s;
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                StringBuilder message = new StringBuilder();
                DataResult<A> last;
                for (var codec : codecs) {
                    last = codec.decode(ops, input);
                    if (last.result().isPresent()) return last;
                    message.append(last.error().orElseThrow().message()).append("; ");
                }
                return DataResult.error(() -> message.substring(0, message.length() - 2));
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                StringBuilder message = new StringBuilder();
                DataResult<T> last;
                for (var codec : codecs) {
                    var builder = ops.mapBuilder();
                    builder = codec.encode(input, ops, builder);
                    last = builder.build((T) null);
                    if (last.result().isPresent()) {
                        return codec.encode(input, ops, prefix);
                    }
                    message.append(last.error().orElseThrow().message()).append("; ");
                }
                return prefix.withErrorsFrom(DataResult.error(() -> message.substring(0, message.length() - 2)));
            }
        };
    }

    public static Decoder<Object> optionsDecoder(Decoder<?>... codecs) {
        if (codecs == null || codecs.length == 0) throw new NullPointerException();
        if (codecs.length == 1) return (Codec<Object>) codecs[0];
        return new Decoder<>() {
            @Override
            public <T> DataResult<Pair<Object, T>> decode(DynamicOps<T> ops, T input) {
                StringBuilder message = new StringBuilder();
                for (var codec : codecs) {
                    var d = codec.decode(ops, input);
                    var res = d.result();
                    if (res.isPresent()) return DataResult.success(new Pair<>(res.get().getFirst(), res.get().getSecond()));
                    message.append(d.error().orElseThrow().message()).append("; ");
                }
                return DataResult.error(() -> message.substring(0, message.length() - 2));
            }
        };
    }

    public static <A, J> DataResult<A> ifMap(DynamicOps<J> ops, J input, Function<MapLike<J>, DataResult<A>> map) {
        var d = ops.getMap(input);
        var res = d.result();
        if (res.isEmpty()) return DataResult.error(() -> d.error().orElseThrow().message());
        return map.apply(res.get());
    }

    @SafeVarargs
    public static <A> Codec<A> codecOf(Encoder<A> encoder, Decoder<A>... decoder) {
        return Codec.of(encoder, chainedDecoder(decoder));
    }

    public static <E, A> MapCodec<E> dispatchNullable(Codec<A> keyCodec, Function<? super E, ? extends
            A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return dispatchNullable("type", keyCodec, type, codec);
    }

    /**
     * Creates a dispatch codec, but with nullable type and codec functions.
     * If the functions return null, an error data result is returned instead of crashing.
     */
    public static <K, V> MapCodec<V> dispatchNullable(String key, Codec<K> keyCodec,
                                                      Function<? super V, ? extends K> type,
                                                      Function<? super K, ? extends MapCodec<? extends V>> codec) {
        return partialDispatchMap(key, keyCodec, v -> {
            K k = type.apply(v);
            return k == null ? DataResult.error(() -> "No key found") : DataResult.success(k);
        }, k -> {
            MapCodec<? extends V> e = codec.apply(k);
            return e == null ? DataResult.error(() -> "No codec found for key " + k) : DataResult.success(e);
        });
    }

    public static <K, V> MapCodec<V> partialDispatchMap(String key, Codec<K> keyCodec,
                                                        Function<? super V, ? extends DataResult<? extends K>> type,
                                                        Function<? super K, ? extends DataResult<? extends MapCodec<? extends V>>> codec) {
        return new KeyDispatchCodec<>(key, keyCodec, type, codec);

    }

    public static <A> Encoder<A> checked(Encoder<A> codec, Predicate<A> test) {
        return new Encoder<>() {
            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return test.test(input) ? codec.encode(input, ops, prefix) : DataResult.error(() -> "Codec " + codec + " can't handle value " + input);
            }
        };
    }

    public static <A> Codec<A> checkedEncoder(Codec<A> codec, Predicate<A> test) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return codec.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return test.test(input) ? codec.encode(input, ops, prefix) : DataResult.error(() -> "Codec " + codec + " can't handle value " + input);
            }

            @Override
            public String toString() {
                return super.toString() + "[Checked encoder]";
            }
        };
    }

    public static <A> Codec<Set<A>> setOf(Codec<A> codec) {
        return codec.listOf().xmap(ObjectOpenHashSet::new, ArrayList::new);
    }

    /**
     * Creates a codec that accepts either a data list or a single element and turns it into a list.
     */
    public static <A> Codec<List<A>> listLike(Codec<A> codec) {
        return chainedCodec(codec.flatComapMap(Collections::singletonList, list -> {
            if (list.size() != 1) return DataResult.error(() -> "List must contain exactly one element");
            return DataResult.success(list.getFirst());
        }), codec.listOf());
    }

    /**
     * Creates a codec that accepts either a data list or a single element and turns it into a set.
     */
    public static <A> Codec<Set<A>> setLike(Codec<A> codec) {
        return chainedCodec(codec.flatComapMap(Collections::singleton, list -> {
            if (list.size() != 1) return DataResult.error(() -> "List must contain exactly one element");
            return DataResult.success(list.iterator().next());
        }), setOf(codec));
    }
}
