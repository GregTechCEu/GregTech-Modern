package com.lowdragmc.gtceu.api.tag;

import com.lowdragmc.lowdraglib.Platform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote TagUtil
 */
public class TagUtil {

    public static <T> TagKey<T> optionalTag(Registry<T> registry, ResourceLocation id) {
        return TagKey.create(registry.key(), id);
    }

    public static <T> TagKey<T> createTag(Registry<T> registry, String path, boolean vanilla) {
        return optionalTag(registry, (Platform.isForge() && !vanilla) ? new ResourceLocation("forge", path) : new ResourceLocation(path));
    }

    public static TagKey<Block> createBlockTag(String path) {
        return createTag(Registry.BLOCK, path, false);
    }

    public static TagKey<Item> createBlockTag(String path, boolean vanilla) {
        return createTag(Registry.ITEM, path, vanilla);
    }

    public static TagKey<Item> createItemTag(String path) {
        return createTag(Registry.ITEM, path, false);
    }

    public static TagKey<Item> createItemTag(String path, boolean vanilla) {
        return createTag(Registry.ITEM, path, vanilla);
    }

    public static TagKey<Fluid> createFluidTag(String path) {
        return createTag(Registry.FLUID, path, false);
    }

}
