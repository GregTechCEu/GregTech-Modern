package brachy.modularui.utils;

import brachy.modularui.utils.serialization.codec.CodecUtil;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.google.common.base.CaseFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Alignment {

    private static final Map<String, Alignment> ALIGNMENT_MAP = new Object2ObjectOpenHashMap<>();

    private static final Codec<Alignment> CODEC_OF_INSTANCE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("x").forGetter(Alignment::getX),
            Codec.FLOAT.fieldOf("y").forGetter(Alignment::getY)
    ).apply(instance, Alignment::new));
    private static final Codec<Alignment> CODEC_OF_NAME = Codec.stringResolver(Alignment::getName, ALIGNMENT_MAP::get);

    public static final Codec<Alignment> CODEC = CodecUtil.chainedCodec(CODEC_OF_NAME, CODEC_OF_INSTANCE);

    @Getter public final float x, y;
    @Getter(AccessLevel.PRIVATE) private final String name;

    public static final Alignment TopLeft = new Alignment(0, 0, "TopLeft");
    public static final Alignment TopCenter = new Alignment(0.5f, 0, "TopCenter");
    public static final Alignment TopRight = new Alignment(1, 0, "TopRight");
    public static final Alignment CenterLeft = new Alignment(0, 0.5f, "CenterLeft");
    public static final Alignment Center = new Alignment(0.5f, 0.5f, "Center");
    public static final Alignment CenterRight = new Alignment(1, 0.5f, "CenterRight");
    public static final Alignment BottomLeft = new Alignment(0, 1, "BottomLeft");
    public static final Alignment BottomCenter = new Alignment(0.5f, 1, "BottomCenter");
    public static final Alignment BottomRight = new Alignment(1, 1, "BottomRight");

    public static final Alignment START = TopLeft;
    public static final Alignment CENTER = Center;
    public static final Alignment END = BottomRight;

    public static final Alignment[] ALL = {
            TopLeft, TopCenter, TopRight,
            CenterLeft, Center, CenterRight,
            BottomLeft, BottomCenter, BottomRight
    };

    public static final Alignment[] CORNERS = {
            TopLeft, TopRight,
            BottomLeft, BottomRight
    };

    public Alignment(float x, float y) {
        this(x, y, null);
    }

    private Alignment(float x, float y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
        if (name != null) {
            ALIGNMENT_MAP.put(name, this);
            ALIGNMENT_MAP.put(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), this);
            String abbrev = name.replaceAll("[a-z]", "");
            ALIGNMENT_MAP.put(abbrev, this);
            ALIGNMENT_MAP.put(abbrev.toLowerCase(), this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Alignment alignment = (Alignment) o;
        return Float.compare(x, alignment.x) == 0 && Float.compare(y, alignment.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        if (this.name != null) {
            return "Alignment{" + this.name + "}";
        }
        return "Alignment{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    /**
     * Defines how elements should be aligned on the main axis.
     * In a row this would mean the x coordinates.
     */
    public enum MainAxis implements StringRepresentable {

        /**
         * All children will be put at the start of the Flow next to each other.
         */
        START,
        /**
         * All children will be put in the center of the Flow next to each other.
         */
        CENTER,
        /**
         * All children will be put at the end of the Flow next to each other (this does not reverse children order).
         */
        END,
        /**
         * This maximizes the space between children with the given available space of the Flow. The first widget will
         * be put at the very
         * start and the last widget will be put at the very end. If the flow has exactly one child, then this behaves
         * the same as
         * {@link #CENTER}.
         */
        SPACE_BETWEEN,
        /**
         * This maximizes the space around the children with the given available space of the Flow. Contrary to
         * {@link #SPACE_BETWEEN} this
         * does not put one "space" between every widget, but rather one "space" on both sides of every widget. If the
         * flow has exactly one
         * child, then this behaves the same as {@link #CENTER}.
         */
        SPACE_AROUND;

        public static final Codec<MainAxis> CODEC = StringRepresentable.fromEnum(MainAxis::values);

        public final String name;

        MainAxis() {
            this.name = name().toLowerCase(Locale.ENGLISH);
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

    /**
     * Defines how elements should be aligned on the cross axis.
     * In a row this would mean the y coordinates.
     */
    public enum CrossAxis implements StringRepresentable {

        /**
         * All children will be put at the start of the Flow next to each other.
         */
        START,
        /**
         * All children will be put in the center of the Flow next to each other.
         */
        CENTER,
        /**
         * All children will be put at the end of the Flow next to each other (this does not reverse children order).
         */
        END;

        public static final Codec<CrossAxis> CODEC = StringRepresentable.fromEnum(CrossAxis::values);

        public final String name;

        CrossAxis() {
            this.name = name().toLowerCase(Locale.ENGLISH);
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
