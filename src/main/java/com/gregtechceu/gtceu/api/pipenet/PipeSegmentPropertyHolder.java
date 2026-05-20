package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.pipenet.property.*;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

public class PipeSegmentPropertyHolder {

    @Getter
    private final Object2ObjectOpenHashMap<SegmentPropertyType<?>, PipeSegmentProperty<?>> properties = new Object2ObjectOpenHashMap<>();

    public PipeSegmentPropertyHolder setProperty(SegmentPropertyType<?> prop, PipeSegmentProperty<?> value) {
        properties.put(prop, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends PipeSegmentProperty<?>> T getProperty(SegmentPropertyType<T> prop) {
        return (T) properties.get(prop);
    }

    public <T> T getPropertyValue(SegmentPropertyType<? extends PipeSegmentProperty<T>> prop) {
        return getProperty(prop).getValue();
    }

    public PipeSegmentPropertyHolder copy() {
        PipeSegmentPropertyHolder newHolder = new PipeSegmentPropertyHolder();
        for (var p : properties.entrySet()) {
            newHolder.setProperty(p.getKey(), p.getValue().copy());
        }
        return newHolder;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PipeSegmentPropertyHolder other)) return false;
        for (var prop : properties.keySet()) {
            if (!other.properties.containsKey(prop)) return false;
            if (!getProperties().get(prop).equals(other.getProperties().get(prop))) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "PipeSegmentPropertyHolder{";
        for (var p : properties.entrySet()) {
            str = str.concat(p.getKey().getId().toString() + "=" + p.getValue().toString() + ",");
        }

        str = str.concat("}");
        return str;
    }
}
