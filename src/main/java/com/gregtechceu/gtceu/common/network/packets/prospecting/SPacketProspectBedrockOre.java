package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SPacketProspectBedrockOre extends SPacketProspect<ProspectorMode.OreInfo> {

    @SuppressWarnings("unused")
    public SPacketProspectBedrockOre() {
        super();
    }

    public SPacketProspectBedrockOre(FriendlyByteBuf buf) {
        super(buf);
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
    public void execute(NetworkEvent.Context context) {
        // todo: add cache for bedrock ore veins
    }
}
