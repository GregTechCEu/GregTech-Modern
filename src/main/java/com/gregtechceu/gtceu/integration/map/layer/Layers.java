package com.gregtechceu.gtceu.integration.map.layer;

import com.gregtechceu.gtceu.integration.map.ButtonState;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.*;
import java.util.function.BiFunction;

public class Layers {

    private static final BiMap<String, BiFunction<String, GenericMapRenderer, ? extends MapRenderLayer>> layers = HashBiMap
            .create();

    public static void registerLayer(BiFunction<String, GenericMapRenderer, ? extends MapRenderLayer> initFunction,
                                     String key) {
        layers.put(key, initFunction);
        ButtonState.Button.makeButton(key);
    }

    public static void addLayersTo(List<MapRenderLayer> layers, GenericMapRenderer renderer) {
        for (var layer : Layers.layers.entrySet()) {
            layers.add(layer.getValue().apply(layer.getKey(), renderer));
        }
    }

    public static Collection<String> allKeys() {
        return layers.keySet();
    }
}
