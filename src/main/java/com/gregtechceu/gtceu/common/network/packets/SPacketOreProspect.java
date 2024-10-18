package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.integration.map.cache.GridPos;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SPacketOreProspect implements IPacket {

    private final List<ResourceKey<Level>> dimList;
    private final List<Integer> gridXList;
    private final List<Integer> gridZList;
    private final List<GeneratedVeinMetadata> nameList;

    public SPacketOreProspect() {
        this.dimList = new ArrayList<>();
        this.gridXList = new ArrayList<>();
        this.gridZList = new ArrayList<>();
        this.nameList = new ArrayList<>();
    }

    public SPacketOreProspect(ResourceKey<Level> dim, int gridX, int gridZ, GeneratedVeinMetadata vein) {
        this.dimList = Collections.singletonList(dim);
        this.gridXList = Collections.singletonList(gridX);
        this.gridZList = Collections.singletonList(gridZ);
        this.nameList = Collections.singletonList(vein);
    }

    public SPacketOreProspect(List<ResourceKey<Level>> dimList, List<Integer> gridXList, List<Integer> gridZList,
                              List<GeneratedVeinMetadata> nameList) {
        this.dimList = dimList;
        this.gridXList = gridXList;
        this.gridZList = gridZList;
        this.nameList = nameList;
    }

    public SPacketOreProspect(ResourceKey<Level> dim, List<GeneratedVeinMetadata> veins) {
        this();
        for (GeneratedVeinMetadata vein : veins) {
            dimList.add(dim);
            gridXList.add(GridPos.blockToGridCoords(vein.center().getX()));
            gridZList.add(GridPos.blockToGridCoords(vein.center().getZ()));
            nameList.add(vein);
        }
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (handler.isClient()) {
            int newVeins = 0;
            for (int i = 0; i < dimList.size(); i++) {
                if (GTClientCache.instance.addVein(dimList.get(i), gridXList.get(i), gridZList.get(i),
                        nameList.get(i))) {
                    newVeins++;
                }
            }
            GTClientCache.instance.notifyNewVeins(newVeins);
        }
    }

    @Override
    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(dimList.size());
        for (int i = 0; i < dimList.size(); i++) {
            packetBuffer.writeResourceKey(dimList.get(i));
            packetBuffer.writeInt(gridXList.get(i));
            packetBuffer.writeInt(gridZList.get(i));
            nameList.get(i).writeToPacket(packetBuffer);
        }
    }

    @Override
    public void decode(FriendlyByteBuf packetBuffer) {
        int size = packetBuffer.readInt();
        for (int i = 0; i < size; i++) {
            dimList.add(packetBuffer.readResourceKey(Registries.DIMENSION));
            gridXList.add(packetBuffer.readInt());
            gridZList.add(packetBuffer.readInt());
            nameList.add(GeneratedVeinMetadata.readFromPacket(packetBuffer));
        }
    }
}
