package com.gregtechceu.gtceu.integration.map.layer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.gregtechceu.gtceu.integration.map.ButtonState;

import java.util.*;
import java.util.function.Function;

public class Layers {
    private static final BiMap<String, Function<String, ? extends MapRenderLayer>> layers = HashBiMap.create();

    public static void registerLayer(Function<String, ? extends MapRenderLayer> clazz, String key) {
        layers.put(key, clazz);
        ButtonState.Button.makeButton(key);
    }

    public static void addLayersTo(List<MapRenderLayer> layers) {
        for (var layer : Layers.layers.entrySet()) {
            layers.add(layer.getValue().apply(layer.getKey()));
        }
    }

    public static Collection<String> allKeys() {
        return layers.keySet();
    }
}
