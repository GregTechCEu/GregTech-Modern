package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class TagPrefixTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<TagPrefix> provider) {
        provider.addTag(CustomTags.INHALATION_TRIGGER_PREFIXES)
                .add(TagPrefix.dust.getKey(),
                        TagPrefix.dustSmall.getKey(),
                        TagPrefix.dustTiny.getKey(),
                        TagPrefix.dustPure.getKey(),
                        TagPrefix.dustImpure.getKey());

        provider.addTag(CustomTags.SKIN_CONTACT_TRIGGER_PREFIXES)
                .add(TagPrefix.dust.getKey(),
                        TagPrefix.dustSmall.getKey(),
                        TagPrefix.dustTiny.getKey());
    }
}
