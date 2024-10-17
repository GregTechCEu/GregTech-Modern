package com.gregtechceu.gtceu.integration.map.cache.client;

import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collection;

/**
 * A client-side cache. Can have dimension-specific files and non-dimension-specific files.
 */
public interface IClientCache {

    /**
     * Call {@link #addDimFiles(String)} and {@link #addSingleFile(String)} in this method, as needed
     */
    void setupCacheFiles();

    /**
     * Clear the internal cache.
     */
    void clear();

    /**
     * @param prefix The prefix of the dimension files being queried
     * @return A collection of dimension ids that should have data saved to disk
     */
    Collection<ResourceKey<Level>> getExistingDimensions(String prefix);

    /**
     * Save a dimension-specific file.
     * 
     * @param prefix The prefix of the file
     * @param dim    The dimension id to be saved
     * @return The NBT to be written to disk, or <code>null</code> to save nothing
     */
    CompoundTag saveDimFile(String prefix, ResourceKey<Level> dim);

    /**
     * Save a non-dimension-specific file.
     * 
     * @param name The name of the file
     * @return The NBT to be written to disk, or <code>null</code> to save nothing
     */
    CompoundTag saveSingleFile(String name);

    /**
     * Read data from a dimension-specific file into the cache.
     * 
     * @param prefix The prefix of the file
     * @param dim    The dimension the data belongs to
     * @param data   The NBT data contained in the file
     */
    void readDimFile(String prefix, ResourceKey<Level> dim, CompoundTag data);

    /**
     * Read data from a non-dimension-specific file into the cache.
     * 
     * @param name The name of the file
     * @param data The NBT data contained in the file
     */
    void readSingleFile(String name, CompoundTag data);

    /**
     * Equivalent to <code>addDimFiles("")</code>
     */
    default void addDimFiles() {
        addDimFiles("");
    }

    /**
     * Register a set of per-dimension files for your cache.
     * If you only call this once, you may ignore the <code>prefix</code> argument in dimension-specific methods.
     * 
     * @param prefix Files will be named <code>prefix + "DIM" + dimensionID</code>
     */
    default void addDimFiles(String prefix) {
        ClientCacheManager.addDimFiles(this, prefix);
    }

    /**
     * Register a single file for your cache.
     * If you only call this once, you may ignore the <code>name</code> argument in non-dimension-specific methods.
     * 
     * @param name The name of the file
     */
    default void addSingleFile(String name) {
        ClientCacheManager.addSingleFile(this, name);
    }
}
