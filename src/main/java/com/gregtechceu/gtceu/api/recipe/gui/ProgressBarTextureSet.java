package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.mui.SteamTextureSet;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.ProgressDrawable;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Getter
@Accessors(fluent = true)
public class ProgressBarTextureSet extends SteamTextureSet {

    private final int progressSize;
    private final ProgressDrawable.Direction fillDirection;

    public ProgressBarTextureSet(int progressSize, ProgressDrawable.Direction fillDirection, UITexture main,
                                 @Nullable UITexture bronze, @Nullable UITexture steel) {
        super(main, bronze, steel);
        this.progressSize = progressSize;
        this.fillDirection = fillDirection;
    }

    public ProgressBarTextureSet(UITexture main, UITexture bronze, UITexture steel) {
        this(20, ProgressDrawable.Direction.RIGHT, main, bronze, steel);
    }

    public ProgressBarTextureSet(UITexture main) {
        this(20, ProgressDrawable.Direction.RIGHT, main);
    }

    public ProgressBarTextureSet(int progressSize, ProgressDrawable.Direction fillDirection, UITexture main) {
        this(progressSize, fillDirection, main, null, null);
    }
}
