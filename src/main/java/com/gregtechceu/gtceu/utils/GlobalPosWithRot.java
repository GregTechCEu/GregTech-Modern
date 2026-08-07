package com.gregtechceu.gtceu.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

public record GlobalPosWithRot(BlockPos pos, Direction side, ResourceKey<Level> dimension) {

    // spotless:off
    public static final Codec<GlobalPosWithRot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPosWithRot::pos),
            Direction.CODEC.fieldOf("side").forGetter(GlobalPosWithRot::side),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPosWithRot::dimension)
    ).apply(instance, GlobalPosWithRot::new));
    public static final StreamCodec<ByteBuf, GlobalPosWithRot> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, GlobalPosWithRot::pos,
            Direction.STREAM_CODEC, GlobalPosWithRot::side,
            ResourceKey.streamCodec(Registries.DIMENSION), GlobalPosWithRot::dimension,
            GlobalPosWithRot::new
    );
    // spotless:on
}
