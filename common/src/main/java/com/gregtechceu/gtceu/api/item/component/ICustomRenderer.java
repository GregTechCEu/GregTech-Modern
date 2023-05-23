package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtlib.client.renderer.IRenderer;

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
