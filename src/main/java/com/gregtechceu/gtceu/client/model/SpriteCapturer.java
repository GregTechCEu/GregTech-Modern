package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SpriteCapturer implements Function<Material, TextureAtlasSprite> {

    private final Function<Material, TextureAtlasSprite> original;
    @Getter
    private final Map<String, TextureAtlasSprite> capturedMaterials = new HashMap<>();
    private final Map<Material, String> capturedNames = new HashMap<>();

    public SpriteCapturer(Function<Material, TextureAtlasSprite> original) {
        this.original = original;
    }

    public void captureMaterialName(Material material, String name) {
        this.capturedNames.putIfAbsent(material, name);
    }

    @Override
    public TextureAtlasSprite apply(Material material) {
        var sprite = original.apply(material);

        String materialName = capturedNames.get(material);
        if (materialName != null) {
            capturedMaterials.put(materialName, sprite);
            capturedNames.remove(material);
        }
        return sprite;
    }
}
