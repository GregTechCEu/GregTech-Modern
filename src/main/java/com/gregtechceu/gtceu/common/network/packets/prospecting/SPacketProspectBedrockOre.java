package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SPacketProspectBedrockOre extends SPacketProspect<ProspectorMode.BedrockOreInfo> {

    @SuppressWarnings("unused")
    public SPacketProspectBedrockOre() {
        super();
    }

    public SPacketProspectBedrockOre(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void encodeData(FriendlyByteBuf buf, ProspectorMode.BedrockOreInfo data) {
        ProspectorMode.BEDROCK_ORE.serialize(data, buf);
    }

    @Override
    public ProspectorMode.BedrockOreInfo decodeData(FriendlyByteBuf buf) {
        return ProspectorMode.BEDROCK_ORE.deserialize(buf);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        // todo: add cache for bedrock ore veins
    }
}
