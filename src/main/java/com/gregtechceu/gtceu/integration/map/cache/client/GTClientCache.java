package com.gregtechceu.gtceu.integration.map.cache.client;

import com.gregtechceu.gtceu.integration.map.cache.DimensionCache;
import com.gregtechceu.gtceu.integration.map.cache.WorldCache;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class GTClientCache extends WorldCache implements IClientCache {

    public static final GTClientCache instance = new GTClientCache();

    public void notifyNewVeins(int amount) {
        if (amount <= 0) return;
        Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("message.gtceu.new_veins", amount));
    }

    @Override
    public Collection<ResourceKey<Level>> getExistingDimensions(String prefix) {
        return cache.keySet();
    }

    @Override
    public CompoundTag saveDimFile(String prefix, ResourceKey<Level> dim) {
        if (!cache.containsKey(dim)) return null;
        return cache.get(dim).toNBT(true);
    }

    @Override
    public CompoundTag saveSingleFile(String name) {
        return null;
    }

    @Override
    public void readDimFile(String prefix, ResourceKey<Level> dim, CompoundTag data) {
        if (!cache.containsKey(dim)) {
            cache.put(dim, new DimensionCache());
        }
        cache.get(dim).fromNBT(data);
    }

    @Override
    public void readSingleFile(String name, CompoundTag data) {}

    @Override
    public void setupCacheFiles() {
        addDimFiles();
    }
}
