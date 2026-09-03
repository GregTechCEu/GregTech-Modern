package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TagPrefixEntry extends RegistryEntry<TagPrefix, TagPrefix> {

    public TagPrefixEntry(AbstractRegistrate<?> owner, DeferredHolder<TagPrefix, TagPrefix> key) {
        super(owner, key);
    }
}
