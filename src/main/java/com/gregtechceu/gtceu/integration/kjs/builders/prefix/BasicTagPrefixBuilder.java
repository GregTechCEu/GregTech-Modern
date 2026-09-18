package com.gregtechceu.gtceu.integration.kjs.builders.prefix;

import com.gregtechceu.gtceu.integration.kjs.built.KJSTagPrefix;

import net.minecraft.resources.Identifier;

public class BasicTagPrefixBuilder extends TagPrefixBuilder {

    public BasicTagPrefixBuilder(Identifier id) {
        super(id);
    }

    @Override
    public KJSTagPrefix create(String id) {
        return new KJSTagPrefix(id);
    }
}
