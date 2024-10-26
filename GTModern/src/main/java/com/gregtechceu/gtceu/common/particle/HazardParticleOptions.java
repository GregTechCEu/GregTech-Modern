package com.gregtechceu.gtceu.common.particle;

import com.gregtechceu.gtceu.common.data.GTParticleTypes;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record HazardParticleOptions(int color, float scale) implements ParticleOptions {

    public static final Codec<HazardParticleOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("color").forGetter(particle -> particle.color),
            Codec.FLOAT.fieldOf("scale").forGetter(particle -> particle.scale))
            .apply(instance, HazardParticleOptions::new));
    @SuppressWarnings("deprecation")
    public static final Deserializer<HazardParticleOptions> DESERIALIZER = new Deserializer<>() {

        public HazardParticleOptions fromCommand(ParticleType<HazardParticleOptions> arg,
                                                 StringReader stringReader) throws CommandSyntaxException {
            int color = stringReader.readInt();
            stringReader.expect(' ');
            float scale = stringReader.readFloat();
            return new HazardParticleOptions(color, scale);
        }

        public HazardParticleOptions fromNetwork(ParticleType<HazardParticleOptions> arg, FriendlyByteBuf arg2) {
            return new HazardParticleOptions(arg2.readVarInt(), arg2.readFloat());
        }
    };

    @Override
    public ParticleType<?> getType() {
        return GTParticleTypes.HAZARD_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.color);
        buffer.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(
                Locale.ROOT, "%s %d %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.color,
                this.scale);
    }
}
