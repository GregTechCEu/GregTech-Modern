package brachy.modularui.theme;

import brachy.modularui.utils.serialization.codec.CodecUtil;

import net.minecraft.util.ExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class WidgetThemeKey<T extends WidgetTheme> implements Comparable<WidgetThemeKey<?>> {

    private static final Map<String, WidgetThemeKey<?>> KEYS = new Object2ReferenceOpenHashMap<>();
    public static final Codec<WidgetThemeKey<?>> CODEC = CodecUtil.chainedCodec(CodecUtil.nullCodec(), Codec.stringResolver(WidgetThemeKey::getFullName, KEYS::get));

    @Nullable
    public static WidgetThemeKey<?> getFromFullName(String key) {
        return KEYS.get(key);
    }

    @Nullable
    @Getter
    private final WidgetThemeKey<T> parent;
    @Getter private final Class<T> type;
    @Getter private final String name;
    @Nullable
    @Getter
    private final String subName;
    @Getter private final T defaultValue;
    @Getter private final T defaultHoverValue;
    @Getter private final WidgetThemeMerger<T> merger;
    @Getter private final WidgetThemeCodec<T> codec;

    WidgetThemeKey(Class<T> type, String name, T defaultValue, WidgetThemeMerger<T> merger, WidgetThemeCodec<T> codec) {
        this(type, name, defaultValue, defaultValue, merger, codec);
    }

    WidgetThemeKey(Class<T> type, String name, T defaultValue, T defaultHoverValue, WidgetThemeMerger<T> merger, WidgetThemeCodec<T> codec) {
        this(null, type, name, null, defaultValue, defaultHoverValue, merger, codec);
    }

    WidgetThemeKey(@Nullable WidgetThemeKey<T> parent, Class<T> type, String name, @Nullable String subName,
                   T defaultValue, T defaultHoverValue, WidgetThemeMerger<T> merger, WidgetThemeCodec<T> codec) {
        this.parent = parent;
        this.type = type;
        this.name = name;
        this.subName = subName;
        this.defaultValue = defaultValue;
        this.defaultHoverValue = defaultHoverValue;
        this.merger = merger;
        this.codec = codec;
        KEYS.put(getFullName(), this);
        ThemeAPI.INSTANCE.registerWidgetThemeKey(this);
    }

    public WidgetThemeKey<T> createSubKey(String subName) {
        return createSubKey(subName, null, null);
    }

    public WidgetThemeKey<T> createSubKey(String subName, @Nullable T defaultValue, @Nullable T defaultHoverValue) {
        WidgetThemeKey<?> existing = KEYS.get(getName() + ":" + subName);
        if (existing != null) {
            if (existing.type == type) {
                return (WidgetThemeKey<T>) existing;
            }
            throw new IllegalStateException("A widget theme key for id " + getName() + ":" + subName +
                    " already exists, but with different types '" + existing.type.getSimpleName() + "' and '" +
                    type.getSimpleName() + "'.");
        }
        return new WidgetThemeKey<>(this, type, name, subName,
                defaultValue != null ? defaultValue : getDefaultValue(),
                defaultHoverValue != null ? defaultHoverValue : getDefaultHoverValue(),
                this.merger, this.codec);
    }

    public T parseJson(JsonObject json) {
        return getCodec().codec().parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    public JsonObject encodeJson(T theme) {
        return getCodec().codec().encodeStart(JsonOps.INSTANCE, theme).getOrThrow().getAsJsonObject();
    }

    public Class<T> getWidgetThemeType() {
        return type;
    }

    public String getFullName() {
        if (subName != null) {
            return name + ":" + subName;
        }
        return name;
    }

    public boolean isSubWidgetTheme() {
        return parent != null;
    }

    public boolean isCompatible(WidgetTheme theme) {
        return type.isInstance(theme);
    }

    public boolean isExactType(WidgetTheme theme) {
        return theme != null && type == theme.getClass();
    }

    public boolean isOfType(Class<? extends WidgetTheme> type) {
        return type.isAssignableFrom(this.type);
    }

    public T cast(WidgetTheme theme) {
        return type.cast(theme);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WidgetThemeKey<?> that = (WidgetThemeKey<?>) obj;
        return Objects.equals(name, that.name) && Objects.equals(subName, that.subName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subName);
    }

    @Override
    public int compareTo(@NotNull WidgetThemeKey<?> o) {
        if (o == this) return 0;
        int i = Boolean.compare(isSubWidgetTheme(), o.isSubWidgetTheme());
        if (i != 0) return i;
        i = isAncestor(o);
        if (i != 0) return i;
        return name.compareTo(o.name);
    }

    private int isAncestor(WidgetThemeKey<?> other) {
        WidgetThemeKey<?> parent = getParent();
        while (parent != null) {
            if (parent == other) return 1;
            parent = parent.getParent();
        }
        parent = other.getParent();
        while (parent != null) {
            if (parent == other) return -1;
            parent = parent.getParent();
        }
        return 0;
    }
}
