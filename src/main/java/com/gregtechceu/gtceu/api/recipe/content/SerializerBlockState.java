package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import io.netty.buffer.ByteBuf;

public class SerializerBlockState implements IContentSerializer<BlockState> {

    public static SerializerBlockState INSTANCE = new SerializerBlockState();
    private static final StreamCodec<ByteBuf, BlockState> STREAM_CODEC = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);

    private SerializerBlockState() {}

    @SuppressWarnings("deprecation")
    @Override
    public void toNetwork(FriendlyByteBuf buf, BlockState content) {
        STREAM_CODEC.encode(buf, content);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState fromNetwork(FriendlyByteBuf buf) {
        return STREAM_CODEC.decode(buf);
    }

    @Override
    public BlockState fromJson(JsonElement json) {
        return BlockState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    @Override
    public JsonElement toJson(BlockState content) {
        return BlockState.CODEC.encodeStart(JsonOps.INSTANCE, content).getOrThrow();
    }

    @Override
    public BlockState of(Object o) {
        if (o instanceof BlockState state) {
            return state;
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState defaultValue() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public Class<BlockState> contentClass() {
        return BlockState.class;
    }

    @Override
    public Codec<BlockState> codec() {
        return BlockState.CODEC;
    }
}
