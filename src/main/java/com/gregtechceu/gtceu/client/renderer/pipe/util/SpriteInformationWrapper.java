package com.gregtechceu.gtceu.client.renderer.pipe.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpriteInformationWrapper implements Supplier<SpriteInformation>, Consumer<SpriteInformation>,
        BiConsumer<TextureAtlasSprite, Integer> {

    private SpriteInformation sprite;

    @Override
    public void accept(TextureAtlasSprite sprite, Integer colorID) {
        accept(new SpriteInformation(sprite, colorID));
    }

    @Override
    public void accept(SpriteInformation spriteInformation) {
        this.sprite = spriteInformation;
    }

    @Override
    public SpriteInformation get() {
        return this.sprite;
    }

    public static SpriteInformationWrapper[] array(int size) {
        SpriteInformationWrapper[] array = new SpriteInformationWrapper[size];
        for (int i = 0; i < size; i++) {
            array[i] = new SpriteInformationWrapper();
        }
        return array;
    }
}
