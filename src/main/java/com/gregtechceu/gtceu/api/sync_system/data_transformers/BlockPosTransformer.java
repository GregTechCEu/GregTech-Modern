package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Nullable;

public class BlockPosTransformer implements ValueTransformer<BlockPos> {

    @Override
    public Tag serializeNBT(BlockPos value, TransformerContext<BlockPos> context) {
        return BlockPos.CODEC.encodeStart(context.nbtOps(), value).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public @Nullable BlockPos deserializeNBT(Tag tag, TransformerContext<BlockPos> context) {
        if (tag instanceof CompoundTag compoundTag) {
            return NbtUtils.readBlockPos(compoundTag);
        }
        return BlockPos.CODEC.parse(context.nbtOps(), tag).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, BlockPos value, TransformerContext<BlockPos> context) {
        buf.writeBlockPos(value);
    }

    @Override
    public @Nullable BlockPos readFromPacket(FriendlyByteBuf buf, TransformerContext<BlockPos> context) {
        return buf.readBlockPos();
    }
}
