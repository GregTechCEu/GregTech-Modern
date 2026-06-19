package com.gregtechceu.gtceu.integration.map.cache;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GridCache {

    @Getter
    private final List<GeneratedVeinMetadata> veins = new ArrayList<>();

    public boolean addVein(GeneratedVeinMetadata vein) {
        if (veins.contains(vein)) return false;
        veins.add(vein);
        return true;
    }

    public ListTag toNBT(HolderLookup.Provider registries) {
        ListTag result = new ListTag();
        for (GeneratedVeinMetadata pos : veins) {
            result.add(GeneratedVeinMetadata.CODEC
                    .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), pos)
                    .getOrThrow());
        }
        return result;
    }

    public void fromNBT(ListTag tag, HolderLookup.Provider provider) {
        for (Tag veinTag : tag) {
            GeneratedVeinMetadata vein = GeneratedVeinMetadata.CODEC
                    .parse(provider.createSerializationContext(NbtOps.INSTANCE), veinTag)
                    .getOrThrow();
            if (!veins.contains(vein)) {
                veins.add(vein);
            }
        }
    }

    public List<GeneratedVeinMetadata> getVeinsMatching(Predicate<GeneratedVeinMetadata> predicate) {
        return veins.stream().filter(predicate).collect(Collectors.toList());
    }

    public void removeVeinsMatching(Predicate<GeneratedVeinMetadata> predicate) {
        for (int i = 0; i < veins.size(); i++) {
            if (predicate.test(veins.get(i))) {
                veins.remove(i);
                i--;
            }
        }
    }
}
