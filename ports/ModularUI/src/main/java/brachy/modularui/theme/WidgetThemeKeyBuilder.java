package brachy.modularui.theme;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.utils.serialization.codec.FieldReader;

import com.mojang.serialization.Codec;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WidgetThemeKeyBuilder<T extends WidgetTheme> {

    private final String id;
    private final Class<T> type;
    private T defaultTheme;
    private T defaultHoverTheme;
    private final List<WidgetThemeField<T, ?>> fields = new ArrayList<>();
    private WidgetThemeKey<?> parser;

    public WidgetThemeKeyBuilder(String id, Class<T> type) {
        this.id = id;
        this.type = type;
    }

    public WidgetThemeKeyBuilder<T> defaultTheme(T defaultTheme) {
        this.defaultTheme = defaultTheme;
        return this;
    }

    public WidgetThemeKeyBuilder<T> defaultHoverTheme(T defaultHoverTheme) {
        this.defaultHoverTheme = defaultHoverTheme;
        return this;
    }

    /**
     * Adds all fields of another widget theme at index 0 of the list.
     * This means new fields added in these widget theme are at the end.
     * The widget theme constructor must have the parameters in this order too.
     */
    public WidgetThemeKeyBuilder<T> fieldsOf(WidgetThemeKey<?> key) {
        this.parser = key;
        return this;
    }

    public <V> WidgetThemeKeyBuilder<T> field(String name, Class<V> type, Codec<V> codec, FieldReader<T, V> fieldReader) {
        return field(name, type, codec, fieldReader, false);
    }

    public <V> WidgetThemeKeyBuilder<T> fallbackField(String name, Class<V> type, Codec<V> codec, FieldReader<T, V> fieldReader) {
        return field(name, type, codec, fieldReader, true);
    }

    /**
     * Adds a field to the codec. The widget theme must have a public constructor with all the fields in the same order they were added.
     * The constructor is found and used via reflection.
     *
     * @param name        name of the field, used to read and write in JSON
     * @param type        type of the field in the constructor, must be exact
     * @param codec       codec for this field
     * @param fieldReader reads the field from a widget theme
     * @param canFallback determines if values are inherited from parent or fallback first, if this is true fallback is used first,
     *                    but can be disabled individually via the "inherited" property in JSON
     */
    public <V> WidgetThemeKeyBuilder<T> field(String name, Class<V> type, Codec<V> codec, FieldReader<T, V> fieldReader, boolean canFallback) {
        this.fields.add(new WidgetThemeField<>(name, type, codec, fieldReader, canFallback));
        return this;
    }

    @SuppressWarnings("unchecked")
    public WidgetThemeKey<T> register() {
        Objects.requireNonNull(this.id, "Id for widget theme must not be null");
        Objects.requireNonNull(this.defaultTheme,
                "Default theme for widget theme must not be null, but is null for id '" + this.id + "'.");
        WidgetThemeMerger<T> merger = null;
        WidgetThemeCodec<T> codec = null;
        if (this.parser != null) {
            if (!this.fields.isEmpty()) {
                int i = 0;
                for (WidgetThemeField<?, ?> f : this.parser.getCodec().getFields()) {
                    this.fields.add(i++, (WidgetThemeField<T, ?>) f);
                }
            } else {
                if (this.type == this.parser.getType()) {
                    codec = (WidgetThemeCodec<T>) this.parser.getCodec();
                }
                merger = (WidgetThemeMerger<T>) this.parser.getMerger();
            }
        }
        if (this.fields.isEmpty() && (merger == null || codec == null)) throw new IllegalArgumentException("No fields for codec provided!");
        if (merger == null) merger = new WidgetThemeMerger<>(this.fields);
        if (codec == null) {
            Constructor<T> ctor;
            Class<?>[] types = this.fields.stream().map(WidgetThemeField::type).toArray(Class[]::new);
            try {
                ctor = this.type.getDeclaredConstructor(this.fields.stream().map(WidgetThemeField::type).toArray(Class[]::new));
            } catch (NoSuchMethodException e) {
                String sTypes = Arrays.stream(types).map(Class::getSimpleName).reduce("", (a, b) -> a + b + ", ");
                throw new IllegalArgumentException("No constructor found for the fields with the given types in that order. Types: " + sTypes);
            }
            codec = new WidgetThemeCodec<>(this.type, this.fields, ctor);
        }
        if (defaultHoverTheme == null) {
            WidgetTheme hover = this.defaultTheme.withNoHoverBackground();
            if (hover.getClass() != this.defaultTheme.getClass()) {
                throw new IllegalArgumentException(
                        "Tried to create a default hover theme, but method withNoHoverBackground()" +
                                "is not overridden to create its type");
            }
            defaultHoverTheme = (T) hover;
        }
        return IThemeApi.get().registerWidgetTheme(this.id, this.defaultTheme, this.defaultHoverTheme, merger, codec);
    }
}
