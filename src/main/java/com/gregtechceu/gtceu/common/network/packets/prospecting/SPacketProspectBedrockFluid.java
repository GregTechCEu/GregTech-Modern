package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;

public class SPacketProspectBedrockFluid extends SPacketProspect<ProspectorMode.FluidInfo> {

    @SuppressWarnings("unused")
    public SPacketProspectBedrockFluid() {
        super();
    }

    public SPacketProspectBedrockFluid(FriendlyByteBuf buf) {
        super(buf);
    }

    @SuppressWarnings("unused")
    public SPacketProspectBedrockFluid(ResourceKey<Level> key, Collection<BlockPos> positions,
                                       Collection<ProspectorMode.FluidInfo> prospected) {
        super(key, positions, prospected);
    }

    public SPacketProspectBedrockFluid(ResourceKey<Level> key, BlockPos pos, ProspectorMode.FluidInfo vein) {
        super(key, pos, vein);
    }

    @Override
    public void encodeData(FriendlyByteBuf buf, ProspectorMode.FluidInfo data) {
        ProspectorMode.FLUID.serialize(data, buf);
    }

    @Override
    public ProspectorMode.FluidInfo decodeData(FriendlyByteBuf buf) {
        return ProspectorMode.FLUID.deserialize(buf);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        data.rowMap().forEach((level, fluids) -> fluids
                .forEach((blockPos, fluid) -> GTClientCache.instance.addFluid(level,
                        blockPos.getX() >> 4, blockPos.getZ() >> 4, fluid)));
    }
}
