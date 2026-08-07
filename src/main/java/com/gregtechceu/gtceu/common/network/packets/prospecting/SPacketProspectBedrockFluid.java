package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class SPacketProspectBedrockFluid extends SPacketProspect<ProspectorMode.FluidInfo> {

    public static final ResourceLocation ID = GTCEu.id("prospect_bedrock_fluid");
    public static final Type<SPacketProspectBedrockFluid> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketProspectBedrockFluid> CODEC = StreamCodec
            .ofMember(SPacketProspectBedrockFluid::encode, SPacketProspectBedrockFluid::new);

    public SPacketProspectBedrockFluid(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    public SPacketProspectBedrockFluid(ResourceKey<Level> key, BlockPos pos, ProspectorMode.FluidInfo vein) {
        super(key, pos, vein);
    }

    @Override
    public void encodeData(RegistryFriendlyByteBuf buf, ProspectorMode.FluidInfo data) {
        ProspectorMode.FLUID.serialize(data, buf);
    }

    @Override
    public ProspectorMode.FluidInfo decodeData(RegistryFriendlyByteBuf buf) {
        return ProspectorMode.FLUID.deserialize(buf);
    }

    @Override
    public void execute(IPayloadContext context) {
        data.rowMap().forEach((level, fluids) -> fluids
                .forEach((blockPos, fluid) -> GTClientCache.instance.addFluid(level,
                        blockPos.getX() >> 4, blockPos.getZ() >> 4, fluid)));
    }

    @Override
    public @NotNull Type<SPacketProspectBedrockFluid> type() {
        return TYPE;
    }
}
