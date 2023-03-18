package com.gregtechceu.gtceu.api.item.component;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote ICustomRenderer
 */
public interface ICustomRenderer extends IItemComponent {
    @Nonnull
    IRenderer getRenderer();
}
