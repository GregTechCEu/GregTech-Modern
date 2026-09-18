package brachy.modularui.theme;

import brachy.modularui.utils.serialization.codec.FieldReader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;

import java.util.List;
import java.util.Map;

public record WidgetThemeField<T extends WidgetTheme, V>(String name, Class<V> type, Codec<V> codec, FieldReader<T, V> fieldReader,
                                                         boolean canFallback) {

    <J> void encode(T widgetTheme, DynamicOps<J> ops, RecordBuilder<J> mapBuilder) {
        var value = fieldReader().readField(widgetTheme);
        J data;
        if (value == null) {
            data = ops.empty();
            mapBuilder.add(name, data);
        } else {
            mapBuilder.add(name, codec().encodeStart(ops, value));
        }
    }

    <J> V decode(DynamicOps<J> ops, Map<String, J> map, List<String> errors) {
        J element = map.get(name());
        if (element == null) {
            if (!map.containsKey(name())) {
                errors.add(String.format("Field '%s' in widget theme of type %s was not found", name(), type().getSimpleName()));
            }
            return null;
        }
        var d = codec().parse(ops, element);
        var res = d.result();
        if (res.isEmpty()) {
            errors.add(d.error().orElseThrow().message());
            return null;
        }
        return res.get();
    }
}
