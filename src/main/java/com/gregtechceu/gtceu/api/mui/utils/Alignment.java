package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import com.google.common.base.CaseFormat;
import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.lang.reflect.Type;
import java.util.Map;

public class Alignment {

    private static final Map<String, Alignment> ALIGNMENT_MAP = new Object2ObjectOpenHashMap<>();

    public final float x, y;

    public static final Alignment TopLeft = new Alignment(0, 0, "TopLeft");
    public static final Alignment TopCenter = new Alignment(0.5f, 0, "TopCenter");
    public static final Alignment TopRight = new Alignment(1, 0, "TopRight");
    public static final Alignment CenterLeft = new Alignment(0, 0.5f, "CenterLeft");
    public static final Alignment Center = new Alignment(0.5f, 0.5f, "Center");
    public static final Alignment CenterRight = new Alignment(1, 0.5f, "CenterRight");
    public static final Alignment BottomLeft = new Alignment(0, 1, "BottomLeft");
    public static final Alignment BottomCenter = new Alignment(0.5f, 1, "BottomCenter");
    public static final Alignment BottomRight = new Alignment(1, 1, "BottomRight");

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
        if (name != null) {
            ALIGNMENT_MAP.put(name, this);
            ALIGNMENT_MAP.put(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), this);
            String abbrev = name.replaceAll("[a-z]", "");
            ALIGNMENT_MAP.put(abbrev, this);
            ALIGNMENT_MAP.put(abbrev.toLowerCase(), this);
        }
    }

    /**
     * Defines how elements should be aligned on the main axis.
     * In a row this would mean the x coordinates.
     */
    public enum MainAxis {

        START,
        CENTER,
        END,
        SPACE_BETWEEN,
        SPACE_AROUND
    }

    /**
     * Defines how elements should be aligned on the cross axis.
     * In a row this would mean the y coordinates.
     */
    public enum CrossAxis {

        START,
        CENTER,
        END
    }

    public static class Json implements JsonDeserializer<Alignment>, JsonSerializer<Alignment> {

        @Override
        public Alignment deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) {
                Alignment alignment = ALIGNMENT_MAP.get(json.getAsString());
                if (alignment == null) {
                    throw new JsonParseException("Can't find alignment for " + json.getAsString());
                }
                return alignment;
            }
            float x = JsonHelper.getFloat(json.getAsJsonObject(), 0f, "x");
            float y = JsonHelper.getFloat(json.getAsJsonObject(), 0f, "y");
            return new Alignment(x, y);
        }

        @Override
        public JsonElement serialize(Alignment src, Type typeOfSrc, JsonSerializationContext context) {
            for (Map.Entry<String, Alignment> entry : ALIGNMENT_MAP.entrySet()) {
                if (entry.getValue() == src) {
                    return new JsonPrimitive(entry.getKey());
                }
            }
            return JsonHelper.makeJson(json -> {
                json.addProperty("x", src.x);
                json.addProperty("y", src.y);
            });
        }
    }
}
