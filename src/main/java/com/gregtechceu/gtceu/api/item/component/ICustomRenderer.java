package com.gregtechceu.gtceu.api.item.component;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import org.jetbrains.annotations.NotNull;

public interface ICustomRenderer extends IItemComponent {

    @NotNull
    IRenderer getRenderer();
}
