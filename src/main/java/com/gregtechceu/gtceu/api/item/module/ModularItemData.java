package com.gregtechceu.gtceu.api.item.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Accessors(fluent = true)
public class ModularItemData {

    //spotless:off
    public static final Codec<ModularItemData> DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ModuleData.DISPATCH_CODEC).fieldOf("modules").forGetter(ModularItemData::getModules)
    ).apply(instance, ModularItemData::new));
    //spotless:on

    @Getter
    private final Int2ObjectMap<ModuleData> moduleMap;

    public ModularItemData(Int2ObjectMap<ModuleData> modules) {
        moduleMap = Int2ObjectMaps.unmodifiable(modules);
    }

    public ModularItemData(List<ModuleData> modules) {
        Int2ObjectMap<ModuleData> map = new Int2ObjectArrayMap<>();
        for (var module: modules) {
            map.put(module.getSlot(), module);
        }
        moduleMap = Int2ObjectMaps.unmodifiable(map);
    }

    public @Nullable ModuleData getModuleDataForSlot(int slot) {
        return moduleMap.get(slot);
    }

    public List<ModuleData> getModules() {
        return moduleMap.values().stream().toList();
    }

    public @Nullable ModuleData getModuleByType(ItemModule module) {
        return moduleMap.values().stream().filter(v -> v.getModule() == module).findFirst().orElse(null);
    }

    public Int2ObjectMap<ModuleData> copyData() {
        Int2ObjectMap<ModuleData> newModules = new Int2ObjectArrayMap<>();
        for (var entry: moduleMap.int2ObjectEntrySet()) {
            newModules.put(entry.getIntKey(), entry.getValue().copy());
        }
        return newModules;
    }

    public ModularItemData withModuleInSlot(int slot, ModuleData module) {
        var newData = copyData();
        newData.put(slot, module);
        return new ModularItemData(newData);
    }

    public ModularItemData withModuleRemoved(int slot) {
        var newData = copyData();
        newData.remove(slot);
        return new ModularItemData(newData);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModularItemData modularItemData) {
            return moduleMap.size() == modularItemData.moduleMap.size() && moduleMap.int2ObjectEntrySet().stream().allMatch(v -> v.getValue().equals(modularItemData.moduleMap.get(v.getIntKey())));
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (ModuleData data : moduleMap.values()) {
            hash += data.hashCode();
        }
        return hash;
    }
}
