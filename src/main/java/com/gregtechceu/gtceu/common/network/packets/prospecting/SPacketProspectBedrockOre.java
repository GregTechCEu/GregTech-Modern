package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;

import net.minecraft.network.FriendlyByteBuf;

public class SPacketProspectBedrockOre extends SPacketProspect<ProspectorMode.OreInfo> {

    public SPacketProspectBedrockOre() {
        super();
    }

    @Override
    public void encodeData(FriendlyByteBuf buf, ProspectorMode.OreInfo data) {
        ProspectorMode.BEDROCK_ORE.serialize(data, buf);
    }

    @Override
    public ProspectorMode.OreInfo decodeData(FriendlyByteBuf buf) {
        return ProspectorMode.BEDROCK_ORE.deserialize(buf);
    }

    @Override
    public void execute(IHandlerContext handler) {
        // todo: add cache for bedrock ore veins
    }
}
