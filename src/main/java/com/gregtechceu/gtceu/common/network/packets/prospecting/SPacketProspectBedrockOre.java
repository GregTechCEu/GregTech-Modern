package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class SPacketProspectBedrockOre extends SPacketProspect<ProspectorMode.BedrockOreInfo> {

    public static final ResourceLocation ID = GTCEu.id("prospect_bedrock_ore");
    public static final Type<SPacketProspectBedrockOre> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketProspectBedrockOre> CODEC = StreamCodec
            .ofMember(SPacketProspectBedrockOre::encode, SPacketProspectBedrockOre::new);

    public SPacketProspectBedrockOre(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void encodeData(RegistryFriendlyByteBuf buf, ProspectorMode.BedrockOreInfo data) {
        ProspectorMode.BEDROCK_ORE.serialize(data, buf);
    }

    @Override
    public ProspectorMode.BedrockOreInfo decodeData(RegistryFriendlyByteBuf buf) {
        return ProspectorMode.BEDROCK_ORE.deserialize(buf);
    }

    @Override
    public void execute(IPayloadContext context) {
        // todo: add cache for bedrock ore veins
    }

    @Override
    public @NotNull Type<SPacketProspectBedrockOre> type() {
        return TYPE;
    }
}
