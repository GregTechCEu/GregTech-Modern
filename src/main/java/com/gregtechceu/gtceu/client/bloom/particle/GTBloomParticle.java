package com.gregtechceu.gtceu.client.bloom.particle;

import com.gregtechceu.gtceu.client.bloom.BloomHandler;
import com.gregtechceu.gtceu.client.bloom.IBloomEffect;
import com.gregtechceu.gtceu.client.bloom.IRenderSetup;
import com.gregtechceu.gtceu.client.particle.GTParticle;

import org.jetbrains.annotations.Nullable;

public abstract class GTBloomParticle extends GTParticle implements IBloomEffect {

    public GTBloomParticle(double posX, double posY, double posZ) {
        super(posX, posY, posZ);
        BloomHandler.registerBloomRender(getBloomRenderSetup(), this, this);
    }

    protected abstract @Nullable IRenderSetup getBloomRenderSetup();
}
