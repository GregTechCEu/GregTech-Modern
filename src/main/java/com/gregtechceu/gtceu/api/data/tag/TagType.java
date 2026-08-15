package com.gregtechceu.gtceu.api.data.tag;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.utils.TagUtil;

import net.minecraft.Util;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Predicate;

@ApiStatus.Internal
public final class TagType {

    @Getter
    private boolean isParentTag = false;
    // this is now memoized because creating tag keys interns them and that's slow
    private final @NotNull BiFunction<TagPrefix, Material, TagKey<Item>> formatter;
    @Nullable
    /* package-private */ Predicate<Material> filter;

    private TagType(BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        this.formatter = Util.memoize(formatter);
    }

    // spotless:off
    /**
     * Create a tag with a specified path, with the "default" formatter, meaning
     * that there is 1 "%s" format character in the path, intended for the Material name.
     */
    public static TagType withDefaultFormatter(String tagPath, boolean isVanilla) {
        return new TagType((prefix, mat) -> TagUtil.createItemTag(tagPath.formatted(mat.getName()), isVanilla));
    }

    /**
     * Create a tag with a specified path, with the "default" formatter, meaning
     * that there is 2 "%s" format characters in the path, with the first being the
     * prefix name, and the second being the material name.
     */
    public static TagType withPrefixFormatter(String tagPath) {
        return new TagType((prefix, mat) -> TagUtil.createItemTag(tagPath.formatted(prefix.name, mat.getName())));
    }

    /**
     * Create a tag with a specified path, with the "default" formatter, meaning
     * that there is 1 "%s" format character in the path, intended for the prefix name.
     */
    public static TagType withPrefixOnlyFormatter(String tagPath) {
        TagType type = new TagType((prefix, mat) -> TagUtil.createItemTag(tagPath.formatted(prefix.name)));
        type.isParentTag = true;
        return type;
    }

    public static TagType withNoFormatter(String tagPath, boolean isVanilla) {
        TagType type = new TagType((prefix, material) -> TagUtil.createItemTag(tagPath, isVanilla));
        type.isParentTag = true;
        return type;
    }

    public static TagType withCustomFormatter(BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        return new TagType(formatter);
    }
    // spotless:on

    public @Nullable TagKey<Item> getTag(TagPrefix prefix, @NotNull Material material) {
        if (this.filter != null && !material.isNull() && !this.filter.test(material)) return null;
        return this.formatter.apply(prefix, material);
    }
}
