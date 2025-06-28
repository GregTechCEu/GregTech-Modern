package com.gregtechceu.gtceu.api.data.tag;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagUtil {

    public static <T> TagKey<T> optionalTag(Registry<T> registry, ResourceLocation id) {
        return TagKey.create(registry.key(), id);
    }

    public static <T> TagKey<T> optionalTag(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id) {
        return TagKey.create(registryKey, id);
    }

    /**
     * Generates tag under Forge namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of Forge
     * @return optional tag #forge:path or #minecraft:path
     */
    public static <T> TagKey<T> createTag(Registry<T> registry, String path, boolean vanilla) {
        if (vanilla) return optionalTag(registry, new ResourceLocation("minecraft", path));
        return optionalTag(registry, new ResourceLocation("forge", path));
    }

    /**
     * Generates tag under Forge namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of Forge
     * @return optional tag #forge:path or #minecraft:path
     */
    public static <T> TagKey<T> createTag(ResourceKey<? extends Registry<T>> registryKey, String path,
                                          boolean vanilla) {
        if (vanilla) return optionalTag(registryKey, new ResourceLocation("minecraft", path));
        return optionalTag(registryKey, new ResourceLocation("forge", path));
    }

    /**
     * Generates tag under GTM namespace
     * 
     * @return #gtceu:path
     */
    public static <T> TagKey<T> createModTag(Registry<T> registry, String path) {
        return optionalTag(registry, GTCEu.id(path));
    }

    /**
     * Generates tag under GTM namespace
     * 
     * @return #gtceu:path
     */
    public static <T> TagKey<T> createModTag(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return TagKey.create(registryKey, GTCEu.id(path));
    }

    /**
     * Generates block tag under Forge namespace
     * 
     * @return Block tag #forge:path
     */
    public static TagKey<Block> createBlockTag(String path) {
        return createTag(BuiltInRegistries.BLOCK, path, false);
    }

    /**
     * Generates block tag under Forge namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of Forge
     * @return Block tag #forge:path or #minecraft:path
     */
    public static TagKey<Block> createBlockTag(String path, boolean vanilla) {
        return createTag(BuiltInRegistries.BLOCK, path, vanilla);
    }

    public static TagKey<Block> createModBlockTag(String path) {
        return createModTag(BuiltInRegistries.BLOCK, path);
    }

    /**
     * Generates tag under Forge namespace
     * 
     * @return Tag #forge:path
     */
    public static TagKey<Item> createItemTag(String path) {
        return createTag(BuiltInRegistries.ITEM, path, false);
    }

    /**
     * Generates tag under Forge namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of Forge
     * @return Tag #forge:path or #minecraft:path
     */
    public static TagKey<Item> createItemTag(String path, boolean vanilla) {
        return createTag(BuiltInRegistries.ITEM, path, vanilla);
    }

    /**
     * Generates item tag under GTM namespace
     * 
     * @return Item tag #gtceu:path
     */
    public static TagKey<Item> createModItemTag(String path) {
        return createModTag(BuiltInRegistries.ITEM, path);
    }

    /**
     * Generates fluid tag under Forge namespace
     * 
     * @return Fluid tag #forge:path
     */
    public static TagKey<Fluid> createFluidTag(String path) {
        return createTag(BuiltInRegistries.FLUID, path, false);
    }

    /**
     * Generates fluid tag under GTM namespace
     *
     * @return Fluid tag #gtceu:path
     */
    public static TagKey<Fluid> createModFluidTag(String path) {
        return createModTag(BuiltInRegistries.FLUID, path);
    }
}
