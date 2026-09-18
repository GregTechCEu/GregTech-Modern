package brachy.modularui.value.sync;

import brachy.modularui.utils.EqualityTest;
import brachy.modularui.utils.ICopy;
import brachy.modularui.utils.serialization.network.IByteBufAdapter;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GenericCollectionSyncHandler<B extends ByteBuf, T, C extends Collection<T>, S extends GenericCollectionSyncHandler<B, T, C, S>> extends ValueSyncHandler<B, C, S> {

    private final Supplier<C> getter;
    private final Consumer<C> setter;
    private final StreamDecoder<B, T> deserializer;
    private final StreamEncoder<B, T> serializer;
    private final EqualityTest<T> equals;
    private final ICopy<T> copy;

    protected GenericCollectionSyncHandler(@NotNull Supplier<C> getter,
                                           @Nullable Consumer<C> setter,
                                           @NotNull StreamDecoder<B, T> deserializer,
                                           @NotNull StreamEncoder<B, T> serializer,
                                           @Nullable EqualityTest<T> equals,
                                           @Nullable ICopy<T> copy) {
        this.getter = getter;
        this.setter = setter;
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.equals = equals != null ? EqualityTest.wrapNullSafe(equals) : Objects::equals;
        this.copy = copy != null ? copy : ICopy.ofSerializer(serializer, deserializer);
    }

    @Override
    public void setValue(C value, boolean setSource, boolean sync) {
        setCache(value);
        onSetCache(setSource, sync);
    }

    protected abstract void setCache(C value);

    protected void onSetCache(boolean setSource, boolean sync) {
        if (setSource && this.setter != null) {
            this.setter.accept(getValue());
        }
        onValueChanged();
        if (sync) sync();
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        C c = this.getter.get();
        if (isFirstSync || didValuesChange(c)) {
            setValue(c, false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.getter.get(), false, true);
    }

    protected abstract boolean didValuesChange(C newValues);

    @Override
    public void write(B buffer) {
        C c = getValue();
        VarInt.write(buffer, c.size());
        for (T t : c) {
            this.serializer.encode(buffer, t);
        }
    }

    @Override
    public abstract C getValue();

    public boolean areValuesEqual(T a, T b) {
        return this.equals.areEqual(a, b);
    }

    protected T deserializeValue(B buffer) {
        return this.deserializer.decode(buffer);
    }

    protected T copyValue(T value) {
        return this.copy.createDeepCopy(value);
    }

    public static class Builder<B extends ByteBuf, T, C extends Collection<T>, S extends Builder<B, T, C, S>> {

        protected Supplier<C> getter;
        protected Consumer<C> setter;
        protected StreamDecoder<B, T> deserializer;
        protected StreamEncoder<B, T> serializer;
        protected EqualityTest<T> equals;
        protected ICopy<T> copy;

        public S getter(Supplier<C> getter) {
            this.getter = getter;
            return getSelf();
        }

        public S setter(Consumer<C> setter) {
            this.setter = setter;
            return getSelf();
        }

        public S deserializer(StreamDecoder<B, T> deserializer) {
            this.deserializer = deserializer;
            return getSelf();
        }

        public S serializer(StreamEncoder<B, T> serializer) {
            this.serializer = serializer;
            return getSelf();
        }

        // protected, because for sets the objects equals and hash code is used
        protected S equals(EqualityTest<T> equals) {
            this.equals = equals;
            return getSelf();
        }

        public S adapter(IByteBufAdapter<B, T> adapter) {
            return deserializer(adapter).serializer(adapter).equals(adapter);
        }

        public S copy(ICopy<T> copy) {
            this.copy = copy;
            return getSelf();
        }

        public S immutableCopy() {
            return copy(ICopy.immutable());
        }

        @SuppressWarnings("unchecked")
        protected S getSelf() {
            return (S) this;
        }
    }
}
