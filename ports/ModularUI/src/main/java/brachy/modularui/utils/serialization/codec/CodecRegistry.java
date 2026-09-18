package brachy.modularui.utils.serialization.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CodecRegistry<T> {

    private final Map<String, MapCodec<? extends T>> drawableCodecs = new Object2ReferenceOpenHashMap<>();

    public @Nullable MapCodec<? extends T> getNullable(String name) {
        return this.drawableCodecs.get(name);
    }

    public @Nullable Codec<? extends T> getNullableCodec(String name) {
        var c = getNullable(name);
        return c != null ? c.codec() : null;
    }

    public MapCodec<? extends T> get(String name) {
        return Objects.requireNonNull(getNullable(name));
    }

    public Optional<MapCodec<? extends T>> getOptional(String name) {
        return Optional.ofNullable(getNullable(name));
    }

    public MapCodec<? extends T> getOrElse(String name, MapCodec<? extends T> codec) {
        return this.drawableCodecs.getOrDefault(name, codec);
    }

    public synchronized <A extends T> MapCodec<A> register(String name, MapCodec<A> codec) {
        this.drawableCodecs.put(name, Objects.requireNonNull(codec));
        return codec;
    }

    public synchronized <A extends T> MapCodec<A> register(MapCodec<A> codec, String... names) {
        for (String name : names) this.drawableCodecs.put(name, codec);
        return codec;
    }
}
