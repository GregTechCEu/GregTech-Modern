package com.gregtechceu.gtceu.data.loader.forge;

import com.gregtechceu.gtceu.data.loader.BedrockOreLoader;
import com.gregtechceu.gtceu.data.loader.FluidVeinLoader;

public class BedrockOreLoaderImpl extends BedrockOreLoader {

    public BedrockOreLoaderImpl() {
        super();
        BedrockOreLoader.INSTANCE = this;
    }
}
