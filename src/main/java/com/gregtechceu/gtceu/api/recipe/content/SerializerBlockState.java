package com.gregtechceu.gtceu.api.recipe.content;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.gregtechceu.gtceu.GTCEu;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class SerializerBlockState implements IContentSerializer<BlockState> {

    public static SerializerBlockState INSTANCE = new SerializerBlockState();

    private SerializerBlockState() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, BlockState content) {
        buf.writeVarInt(BuiltInRegistries.BLOCK.getId(content.getBlock()));
        ImmutableMap<Property<?>, Comparable<?>> values = content.getValues();
        if (!values.isEmpty()) {
            buf.writeBoolean(true);

            for (Map.Entry<Property<?>, Comparable<?>> entry : values.entrySet()) {
                buf.writeUtf(entry.getKey().getName());
                buf.writeUtf(((Property)entry.getKey()).getName(entry.getValue()));
            }
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public BlockState fromNetwork(FriendlyByteBuf buf) {
        Block block = BuiltInRegistries.BLOCK.byId(buf.readVarInt());
        BlockState blockState = block.defaultBlockState();
        if (buf.readBoolean()) {
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            ImmutableMap<Property<?>, Comparable<?>> values = blockState.getValues();

            for (int i = 0; i < values.size(); ++i) {
                String propertyName = buf.readUtf();
                String propertyValueName = buf.readUtf();
                Property<?> property = stateDefinition.getProperty(propertyName);
                if (property != null) {
                    Optional<? extends Comparable<?>> value = property.getValue(propertyValueName);
                    value.ifPresent(comparable -> ((StateHolder) blockState).setValue(property, comparable));
                }
            }
        }
        return blockState;
    }

    @Override
    public BlockState fromJson(JsonElement json) {
        return BlockState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public JsonElement toJson(BlockState content) {
        return BlockState.CODEC.encodeStart(JsonOps.INSTANCE, content).get().map(Function.identity(), partial -> JsonNull.INSTANCE);
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
}
