package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;

public class SPacketProspectOre extends SPacketProspect<GeneratedVeinMetadata> {

    @SuppressWarnings("unused")
    public SPacketProspectOre() {
        super();
    }

    public SPacketProspectOre(FriendlyByteBuf buf) {
        super(buf);
    }

    public SPacketProspectOre(ResourceKey<Level> key, Collection<GeneratedVeinMetadata> veins) {
        super(key, veins.stream().map(GeneratedVeinMetadata::center).toList(), veins);
    }

    @Override
    public void encodeData(FriendlyByteBuf buf, GeneratedVeinMetadata data) {
        data.writeToPacket(buf);
    }

    @Override
    public GeneratedVeinMetadata decodeData(FriendlyByteBuf buf) {
        return GeneratedVeinMetadata.readFromPacket(buf);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        data.rowMap().forEach((level, ores) -> ores
                .forEach((blockPos, vein) -> GTClientCache.instance.addVein(level,
                        blockPos.getX() >> 4, blockPos.getZ() >> 4, vein)));
    }
}
