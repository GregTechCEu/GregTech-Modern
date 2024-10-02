package com.gregtechceu.gtceu.client.particle;

import com.gregtechceu.gtceu.client.renderer.IRenderSetup;
import com.gregtechceu.gtceu.client.shader.post.BloomType;
import com.gregtechceu.gtceu.client.util.BloomEffectUtil;
import com.gregtechceu.gtceu.client.util.IBloomEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GTBloomParticle extends GTParticle implements IBloomEffect {

    public GTBloomParticle(double posX, double posY, double posZ) {
        super(posX, posY, posZ);
        BloomEffectUtil.registerBloomRender(getBloomRenderSetup(), getBloomType(), this, this);
    }

    @Nullable
    protected abstract IRenderSetup getBloomRenderSetup();

    @NotNull
    protected abstract BloomType getBloomType();
}
