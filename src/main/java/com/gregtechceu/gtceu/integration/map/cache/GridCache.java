package com.gregtechceu.gtceu.integration.map.cache;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;

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

    public ListTag toNBT(boolean isClient) {
        ListTag result = new ListTag();
        for (GeneratedVeinMetadata pos : veins) {
            result.add((isClient ? GeneratedVeinMetadata.CLIENT_CODEC : GeneratedVeinMetadata.CODEC)
                    .encodeStart(NbtOps.INSTANCE, pos)
                    .getOrThrow(false, GTCEu.LOGGER::error));
        }
        return result;
    }

    public void fromNBT(ListTag tag, boolean isClient) {
        for (Tag veinTag : tag) {
            GeneratedVeinMetadata vein = (isClient ? GeneratedVeinMetadata.CLIENT_CODEC : GeneratedVeinMetadata.CODEC)
                    .parse(NbtOps.INSTANCE, veinTag)
                    .getOrThrow(false, GTCEu.LOGGER::error);
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
