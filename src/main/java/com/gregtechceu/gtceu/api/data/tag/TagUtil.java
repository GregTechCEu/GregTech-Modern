package com.gregtechceu.gtceu.api.data.tag;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
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

    public static <T> TagKey<T> optionalTag(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id) {
        return TagKey.create(registryKey, id);
    }

    public static <T> TagKey<T> createTag(Registry<T> registry, String path, boolean vanilla) {
        if (vanilla) return optionalTag(registry, new ResourceLocation("minecraft", path));
        return optionalTag(registry, new ResourceLocation("forge", path)); // TODO change "forge" to "common" in 1.21
    }

    public static <T> TagKey<T> createTag(ResourceKey<? extends Registry<T>> registryKey, String path, boolean vanilla) {
        if (vanilla) return optionalTag(registryKey, new ResourceLocation("minecraft", path));
        return optionalTag(registryKey, new ResourceLocation("forge", path)); // TODO change "forge" to "common" in 1.21
    }

    public static <T> TagKey<T> createModTag(Registry<T> registry, String path) {
        return optionalTag(registry, GTCEu.id(path));
    }

    public static <T> TagKey<T> createModTag(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return TagKey.create(registryKey, GTCEu.id(path));
    }

    public static TagKey<Block> createBlockTag(String path) {
        return createTag(Registry.BLOCK, path, false);
    }

    public static TagKey<Block> createBlockTag(String path, boolean vanilla) {
        return createTag(Registry.BLOCK, path, vanilla);
    }

    public static TagKey<Block> createModBlockTag(String path) {
        return createModTag(Registry.BLOCK, path);
    }

    public static TagKey<Item> createItemTag(String path) {
        return createTag(Registry.ITEM, path, false);
    }

    public static TagKey<Item> createItemTag(String path, boolean vanilla) {
        return createTag(Registry.ITEM, path, vanilla);
    }

    public static TagKey<Item> createModItemTag(String path) {
        return createModTag(Registry.ITEM, path);
    }

    public static TagKey<Fluid> createFluidTag(String path) {
        return createTag(Registry.FLUID, path, false);
    }

}
