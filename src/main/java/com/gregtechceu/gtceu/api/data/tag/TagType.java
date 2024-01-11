package com.gregtechceu.gtceu.api.data.tag;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import lombok.Getter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.BiFunction;

public class TagType {

    private final String tagPath;
    @Getter
    private boolean isParentTag = false;
    private BiFunction<TagPrefix, Material, TagKey<Item>> formatter;

    private TagType(String tagPath) {
        this.tagPath = tagPath;
    }

    /**
     * Create a tag with a specified path, with the "default" formatter, meaning
     * that there is 1 "%s" format character in the path, intended for the Material name.
     */
    public static TagType withDefaultFormatter(String tagPath) {
        TagType type = new TagType(tagPath);
        type.formatter = (prefix, mat) -> TagUtil.createItemTag(type.tagPath.formatted(mat.getName()));
        return type;
    }

    /**
     * Create a tag with a specified path, with the "default" formatter, meaning
     * that there is 2 "%s" format characters in the path, with the first being the
     * prefix name, and the second being the material name.
     */
    public static TagType withPrefixFormatter(String tagPath) {
        TagType type = new TagType(tagPath);
        type.formatter = (prefix, mat) -> TagUtil.createItemTag(type.tagPath.formatted(FormattingUtil.toLowerCaseUnderscore(prefix.name), mat.getName()));
        return type;
    }

    /**
     * Create a tag with a specified path, with the "default" formatter, meaning
     * that there is 1 "%s" format character in the path, intended for the prefix name.
     */
    public static TagType withPrefixOnlyFormatter(String tagPath) {
        TagType type = new TagType(tagPath);
        type.formatter = (prefix, mat) -> TagUtil.createItemTag(type.tagPath.formatted(FormattingUtil.toLowerCaseUnderscore(prefix.name)));
        type.isParentTag = true;
        return type;
    }

    public static TagType withNoFormatter(String tagPath) {
        TagType type = new TagType(tagPath);
        type.formatter = (prefix, material) -> TagUtil.createItemTag(type.tagPath);
        type.isParentTag = true;
        return type;
    }

    public static TagType withCustomFormatter(String tagPath, BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        TagType type = new TagType(tagPath);
        type.formatter = formatter;
        return type;
    }

    public TagKey<Item> getTag(TagPrefix prefix, Material material) {
        return formatter.apply(prefix, material);
    }
}
