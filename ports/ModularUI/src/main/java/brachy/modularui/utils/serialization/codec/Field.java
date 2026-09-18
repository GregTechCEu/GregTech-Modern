package brachy.modularui.utils.serialization.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public final class Field<T, V> {

    @Getter private final String name;
    @Getter private final FieldWriter<T, V> fieldWriter;
    @Getter private final FieldReader<T, V> fieldReader;
    @Getter private final Codec<V> codec;
    @Getter private final Supplier<V> defaultSupplier;
    @Getter private final boolean dynamicSupplier;
    @Getter
    @Setter
    private String[] altNames;
    @Getter
    @Setter
    private EncodeWhen encodeWhen = EncodeWhen.CHANGED;
    @Getter
    @Setter
    private boolean writeDefault = true;

    Field(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec, Supplier<V> defaultSupplier, boolean dynamicSupplier) {
        this.name = name;
        this.fieldWriter = fieldWriter;
        this.fieldReader = fieldReader;
        this.codec = codec;
        this.defaultSupplier = defaultSupplier;
        this.dynamicSupplier = dynamicSupplier;
    }

    public boolean hasDefault() {
        return this.defaultSupplier != null;
    }

    private V getDefault() {
        // could return a non-dynamic, modifiable instance
        return this.defaultSupplier.get();
    }

    public V getModifiableDefault() {
        V v = getDefault();
        if (!this.dynamicSupplier && this.codec instanceof MapCodec.MapCodecCodec<V>(MapCodec<V> mapCodec) &&
                mapCodec instanceof MutableObjectCodec<V> moc && moc.canCopy()) {
            v = moc.copy(v);
        }
        return v;
    }

    public boolean isUnencodable() {
        return this.codec == null;
    }

    public <J> void encode(T holder, DynamicOps<J> ops, RecordBuilder<J> map) {
        V value = this.fieldReader.readField(holder);
        if (isUnencodable()) {
            if (!isEmpty(value)) {
                map.withErrorsFrom(DataResult.error(() -> String.format("Field '%s' is unencodeable, but the value is not empty", this.name)));
            }
            return;
        }
        if (value == null) {
            if (!hasDefault()) {
                map.withErrorsFrom(DataResult.error(() -> String.format("Field '%s' is not optional, but is trying to encode a null value", this.name)));
                return;
            }
            if (this.encodeWhen == EncodeWhen.ALWAYS) {
                value = getModifiableDefault();
                map.add(this.name, this.codec.encodeStart(ops, value));
            }
            return;
        }
        if (shouldEncode(value)) {
            map.add(this.name, this.codec.encodeStart(ops, value));
        }
    }

    public boolean shouldEncode(V value) {
        if (this.encodeWhen == EncodeWhen.ALWAYS) return true;
        if (this.encodeWhen == EncodeWhen.NEVER) return false;
        return !hasDefault() || !Objects.equals(value, getDefault());
    }

    @SuppressWarnings("unchecked")
    public <J> @Nullable String decode(T holder, DynamicOps<J> ops, MapLike<J> map) {
        J element = map.get(this.name);
        if (element == null && this.altNames != null) {
            for (String alt : this.altNames) {
                element = map.get(alt);
                if (element != null) break;
            }
        }
        if (element == null) {
            if (this.encodeWhen == EncodeWhen.NEVER || !this.writeDefault) return null;
            if (!hasDefault()) {
                return null;//isUnencodable() ? null : String.format("Field '%s' has no value and is not optional", this.name);
            }
            this.fieldWriter.writeField(holder, getModifiableDefault());
            return null;
        }
        if (isUnencodable()) {
            return String.format("Field '%s' is unencodable, but data still contains value", this.name);
        }
        if (this.codec instanceof MapCodec.MapCodecCodec<V> mcc && mcc.codec() instanceof MutableMapDecoder<?>) {
            var d = decode(holder, ops, element, (MutableMapDecoder<V>) mcc.codec());
            var res = d.result();
            if (res.isEmpty()) return d.error().orElseThrow().message();
            this.fieldWriter.writeField(holder, res.get());
            return null;
        }
        if (this.codec instanceof MutableDecoder<?> mutableCodec) {
            var d = decode(holder, ops, element, (MutableDecoder<V>) mutableCodec);
            var res = d.result();
            if (res.isEmpty()) return d.error().orElseThrow().message();
            this.fieldWriter.writeField(holder, res.get());
            return null;
        }
        var d = this.codec.parse(ops, element);
        var res = d.result();
        if (res.isEmpty()) return d.error().orElseThrow().message();
        this.fieldWriter.writeField(holder, res.get());
        return null;
    }

    private <J> DataResult<V> decode(T holder, DynamicOps<J> ops, J element, MutableMapDecoder<V> decoder) {
        var dMap = ops.getMap(element);
        var dRes = dMap.result();
        if (dRes.isEmpty()) return DataResult.error(() -> dMap.error().orElseThrow().message());
        V value = this.fieldReader.readField(holder);
        if (value == null) {
            if (hasDefault()) {
                value = getModifiableDefault();
            }
            if (value == null && decoder.canDecodeInstance()) {
                var d = decoder.decodeInstance(ops, dRes.get());
                var res = d.result();
                if (res.isEmpty()) return d;
                value = res.get();
            }
            if (value == null) {
                return DataResult.error(() -> String.format("Field '%s' is unable to decode instance and the holder has no default value and this property has no default value", this.name));
            }
        }
        return decoder.decode(ops, dRes.get(), value);
    }

    private <J> DataResult<V> decode(T holder, DynamicOps<J> ops, J element, MutableDecoder<V> decoder) {
        V value = this.fieldReader.readField(holder);
        if (value == null) {
            if (hasDefault()) {
                value = getModifiableDefault();
            }
            if (value == null && decoder.canDecodeInstance()) {
                var d = decoder.parseInstance(ops, element);
                var res = d.result();
                if (res.isEmpty()) return d;
                value = res.get();
            }
            if (value == null) {
                return DataResult.error(() -> String.format("Field '%s' is unable to decode instance and the holder has no default value and this property has no default value", this.name));
            }
        }
        return decoder.parse(ops, element, value);
    }

    public void copyValue(T from, T to) {
        V value = this.fieldReader.readField(from);
        if (this.codec instanceof MapCodec.MapCodecCodec<V> mcc && mcc.codec() instanceof MutableObjectCodec<V> moc) {
            value = moc.copy(value);
        }
        this.fieldWriter.writeField(to, value);
    }

    public void applyDefault(T instance) {
        if (hasDefault()) {
            this.fieldWriter.writeField(instance, getModifiableDefault());
        }
    }

    public boolean isEmpty(V value) {
        return value == null;
    }

    public <O> Field<O, V> copyToType(Function<O, T> converter) {
        return copyToType(this.name, converter);
    }

    public <O> Field<O, V> copyToType(String newName, Function<O, T> converter) {
        var field = new Field<O, V>(newName, (o, v) -> this.fieldWriter.writeField(converter.apply(o), v),
                (o) -> this.fieldReader.readField(converter.apply(o)), this.codec, this.defaultSupplier, this.dynamicSupplier);
        field.altNames(this.altNames);
        field.encodeWhen(this.encodeWhen);
        field.writeDefault(this.writeDefault);
        return field;
    }

    public Field<T, V> copy(String newName) {
        if (this.name.equals(newName)) return this;
        var field = new Field<>(newName, this.fieldWriter, this.fieldReader, this.codec, this.defaultSupplier, this.dynamicSupplier);
        field.altNames(this.altNames);
        field.encodeWhen(this.encodeWhen);
        field.writeDefault(this.writeDefault);
        return field;
    }

    public void convertToString(T instance, StringBuilder b, int indent) {
        b.append(this.name)
                .append(": ");
        V value = this.fieldReader.readField(instance);
        if (this.codec instanceof MapCodec.MapCodecCodec<V>(MapCodec<V> mapCodec) && mapCodec instanceof MutableObjectCodec<V> moc) {
            b.append(moc.convertToString(value, indent));
        } else {
            b.append(value);
        }
    }

    public enum EncodeWhen {
        ALWAYS,
        NEVER,
        CHANGED
    }
}
