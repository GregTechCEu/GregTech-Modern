package com.gregtechceu.gtceu.common.particle;

import com.gregtechceu.gtceu.data.particle.GTParticleTypes;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record HazardParticleOptions(int color, float scale) implements ParticleOptions {

    public static final MapCodec<HazardParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("color").forGetter(particle -> particle.color),
            Codec.FLOAT.fieldOf("scale").forGetter(particle -> particle.scale))
            .apply(instance, HazardParticleOptions::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HazardParticleOptions> STREAM_CODEC = StreamCodec
            .of((buffer, var) -> {
                buffer.writeVarInt(var.color);
                buffer.writeFloat(var.scale);
            }, (buffer) -> new HazardParticleOptions(buffer.readVarInt(), buffer.readFloat()));

    @Override
    public ParticleType<?> getType() {
        return GTParticleTypes.HAZARD_PARTICLE.get();
    }
}
