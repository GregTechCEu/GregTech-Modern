package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;

// Custom registrate data provider types
public class GTProviderTypes {

    public static final ProviderType<RegistrateTagsProvider.IntrinsicImpl<Material>> MATERIAL_TAGS = ProviderType
            .registerIntrinsicTag(
                    "tags/material", "materials", GTRegistries.Keys.MATERIAL,
                    Material::getKey);

    public static final ProviderType<RegistrateTagsProvider.IntrinsicImpl<TagPrefix>> TAG_PREFIX_TAGS = ProviderType
            .registerIntrinsicTag(
                    "tags/tag_prefix", "tag_prefixes", GTRegistries.Keys.TAG_PREFIX,
                    TagPrefix::getKey);
}
