package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.function.Function;

public class SerializerBlockState implements IContentSerializer<BlockState> {

    public static SerializerBlockState INSTANCE = new SerializerBlockState();

    private SerializerBlockState() {}

    @SuppressWarnings("deprecation")
    @Override
    public void toNetwork(FriendlyByteBuf buf, BlockState content) {
        buf.writeId(Block.BLOCK_STATE_REGISTRY, content);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState fromNetwork(FriendlyByteBuf buf) {
        return buf.readById(Block.BLOCK_STATE_REGISTRY);
    }

    @Override
    public BlockState fromJson(JsonElement json) {
        return BlockState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public JsonElement toJson(BlockState content) {
        return BlockState.CODEC.encodeStart(JsonOps.INSTANCE, content).get().map(Function.identity(),
                partial -> JsonNull.INSTANCE);
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
