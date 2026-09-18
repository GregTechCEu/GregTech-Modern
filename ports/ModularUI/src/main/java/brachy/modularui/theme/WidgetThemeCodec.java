package brachy.modularui.theme;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WidgetThemeCodec<T extends WidgetTheme> extends MapCodec<T> {

    @Getter
    private final Class<T> type;
    @Getter(AccessLevel.PACKAGE)
    private final List<WidgetThemeField<T, ?>> fields;
    private final Constructor<T> ctor;

    public WidgetThemeCodec(Class<T> type, List<WidgetThemeField<T, ?>> fields, Constructor<T> ctor) {
        this.type = type;
        this.fields = fields;
        this.ctor = ctor;
    }

    @Override
    public <J> Stream<J> keys(DynamicOps<J> ops) {
        return this.fields.stream().map(WidgetThemeField::name).map(ops::createString);
    }

    @Override
    public <J> DataResult<T> decode(DynamicOps<J> ops, MapLike<J> input) {
        var map = new Object2ObjectOpenHashMap<String, J>();
        input.entries().forEach(p -> map.put(ops.getStringValue(p.getFirst()).result().orElseThrow(), p.getSecond()));
        List<String> errors = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        this.fields.forEach(f -> args.add(f.decode(ops, map, errors)));
        if (!errors.isEmpty()) {
            return DataResult.error(() ->
                    String.format("Errors while decoding widget theme: %s", errors));
        }
        try {
            return DataResult.success(this.ctor.newInstance(args.toArray(Object[]::new)));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public <J> RecordBuilder<J> encode(T input, DynamicOps<J> ops, RecordBuilder<J> prefix) {
        this.fields.forEach(f -> f.encode(input, ops, prefix));
        return prefix;
    }
}
