package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagUtil {

    /**
     * Creates a tag under the {@code c} or {@code minecraft} namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of common
     * @return Tag {@code #c:path} or {@code #minecraft:path}
     */
    public static <T> TagKey<T> createTag(ResourceKey<? extends Registry<T>> registryKey, String path,
                                          boolean vanilla) {
        if (vanilla) return TagKey.create(registryKey, ResourceLocation.withDefaultNamespace(path));
        return TagKey.create(registryKey, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    /**
     * Creates a tag under the {@code gtceu} namespace
     * 
     * @return {@code #gtceu:path}
     */
    public static <T> TagKey<T> createModTag(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return TagKey.create(registryKey, GTCEu.id(path));
    }

    /**
     * Creates a block tag under the {@code c} namespace
     * 
     * @return Block tag {@code #c:path}
     */
    public static TagKey<Block> createBlockTag(String path) {
        return createTag(Registries.BLOCK, path, false);
    }

    /**
     * Creates a block tag under the {@code c} or {@code minecraft} namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of common
     * @return Block tag {@code #c:path} or {@code #minecraft:path}
     */
    public static TagKey<Block> createBlockTag(String path, boolean vanilla) {
        return createTag(Registries.BLOCK, path, vanilla);
    }

    /**
     * Creates a block tag under the {@code gtceu} namespace
     *
     * @return Block tag {@code #gtceu:path}
     */
    public static TagKey<Block> createModBlockTag(String path) {
        return createModTag(Registries.BLOCK, path);
    }

    /**
     * Creates an item tag under the {@code c} namespace
     * 
     * @return Item tag {@code #c:path}
     */
    public static TagKey<Item> createItemTag(String path) {
        return createTag(Registries.ITEM, path, false);
    }

    /**
     * Creates an item tag under the {@code c} or {@code minecraft} namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of common
     * @return Item tag {@code #c:path} or {@code #minecraft:path}
     */
    public static TagKey<Item> createItemTag(String path, boolean vanilla) {
        return createTag(Registries.ITEM, path, vanilla);
    }

    /**
     * Creates an item tag under the {@code gtceu} namespace
     * 
     * @return Item tag {@code #gtceu:path}
     */
    public static TagKey<Item> createModItemTag(String path) {
        return createModTag(Registries.ITEM, path);
    }

    /**
     * Creates a fluid tag under the {@code c} namespace
     * 
     * @return Fluid tag {@code #c:path}
     */
    public static TagKey<Fluid> createFluidTag(String path) {
        return createTag(Registries.FLUID, path, false);
    }

    /**
     * Generates fluid tag under GTM namespace
     *
     * @return Fluid tag #gtceu:path
     */
    public static TagKey<Fluid> createModFluidTag(String path) {
        return createModTag(Registries.FLUID, path);
    }

    public static <T> TagKey<T> optionalTag(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation path) {
        return TagKey.create(registryKey, path);
    }
}
