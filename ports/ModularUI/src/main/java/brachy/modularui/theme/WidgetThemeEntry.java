package brachy.modularui.theme;

import brachy.modularui.api.IThemeApi;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;

public record WidgetThemeEntry<T extends WidgetTheme>(WidgetThemeKey<T> key, T theme, T hoverTheme) {

    public WidgetThemeEntry(WidgetThemeKey<T> key, T theme) {
        this(key, theme, theme);
    }

    public T getTheme(boolean hover) {
        return hover ? this.hoverTheme : this.theme;
    }

    @SuppressWarnings("unchecked")
    public <F extends WidgetTheme> WidgetThemeEntry<F> expectType(Class<F> expectedType) {
        if (this.key.isOfType(expectedType)) {
            return (WidgetThemeEntry<F>) this;
        }
        throw new IllegalStateException(
                String.format("Got widget theme with invalid type. Got type '%s', but expected type '%s'",
                        this.key.getWidgetThemeType().getSimpleName(), expectedType.getSimpleName()));
    }

    public <J> void encode(DynamicOps<J> ops, RecordBuilder<J> mapBuilder, boolean fallback) {
        if (fallback) {
            this.key.getCodec().encode(this.theme, ops, mapBuilder);
        } else {
            mapBuilder.add(this.key.getFullName(), this.key.getCodec().codec().encodeStart(ops, this.theme));
        }
        if (this.theme == this.hoverTheme) return;
        mapBuilder.add(this.key.getFullName() + IThemeApi.HOVER_SUFFIX, this.key.getCodec().codec().encodeStart(ops, this.hoverTheme));
    }
}
