package com.gregtechceu.gtceu.integration.kjs.built;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagType;
import lombok.experimental.Accessors;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasOreProperty;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.LoaderType.FABRIC;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.LoaderType.FORGE;

@Accessors(fluent = true, chain = true)
public class KJSTagPrefix extends TagPrefix {

    public KJSTagPrefix(String name) {
        super(name);
    }

    public static KJSTagPrefix oreTagPrefix(String name) {
        return new KJSTagPrefix(name)
            .prefixTagPath(FORGE, "ores/%s/%s")
            .defaultTagPath(FORGE, "ores/%s")
            .prefixOnlyTagPath(FORGE, "ores_in_ground/%s")
            .unformattedTagPath(FORGE, "ores")
            .prefixTagPath(FABRIC, "%s_%s_ores")
            .defaultTagPath(FABRIC, "%s_ores")
            .prefixOnlyTagPath(FABRIC, "%s_ores_in_ground")
            .unformattedTagPath(FABRIC, "ores")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    }

    @Override
    public KJSTagPrefix defaultTagPath(LoaderType loader, String path) {
        loader.apply(this, TagType.withDefaultFormatter(path));
        return this;
    }

    @Override
    public KJSTagPrefix prefixTagPath(LoaderType loader, String path) {
        loader.apply(this, TagType.withPrefixFormatter(path));
        return this;
    }

    @Override
    public KJSTagPrefix prefixOnlyTagPath(LoaderType loader, String path) {
        loader.apply(this, TagType.withPrefixOnlyFormatter(path));
        return this;
    }

    @Override
    public KJSTagPrefix unformattedTagPath(LoaderType loader, String path) {
        loader.apply(this, TagType.withNoFormatter(path));
        return this;
    }

    @Override
    public KJSTagPrefix customTagPath(LoaderType loader, String path, BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        loader.apply(this, TagType.withCustomFormatter(path, formatter));
        return this;
    }

    public KJSTagPrefix materialIconType(MaterialIconType type) {
        super.materialIconType(type);
        return this;
    }

    public KJSTagPrefix unificationEnabled(boolean unificationEnabled) {
        super.unificationEnabled(unificationEnabled);
        return this;
    }

    public KJSTagPrefix generationCondition(Predicate<Material> condition) {
        super.generationCondition(condition);
        return this;
    }
}
