package com.gregtechceu.gtceu.integration.kjs.builders.prefix;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import net.minecraft.resources.ResourceLocation;

public class BasicTagPrefixBuilder extends TagPrefixBuilder {
    public BasicTagPrefixBuilder(ResourceLocation id, Object... args) {
        super(id, args);
    }

    @Override
    public TagPrefix create(String id) {
        return new TagPrefix(id);
    }
}
