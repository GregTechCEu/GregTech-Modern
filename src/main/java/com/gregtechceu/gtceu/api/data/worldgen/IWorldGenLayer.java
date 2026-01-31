package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IWorldGenLayer extends StringRepresentable {

    Codec<IWorldGenLayer> CODEC = ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName,
            WorldGeneratorUtils.WORLD_GEN_LAYERS::get);

    boolean isApplicableForLevel(ResourceKey<Level> dimension);

    Set<ResourceKey<Level>> getDimensions();

    RuleTest getTarget();

    @FunctionalInterface
    interface RuleTestSupplier {

        RuleTest get();
    }

    IWorldGenLayer NOWHERE = new IWorldGenLayer() {

        @Override
        public boolean isApplicableForLevel(ResourceKey<Level> dimension) {
            return false;
        }

        @Override
        public Set<ResourceKey<Level>> getDimensions() {
            return Set.of();
        }

        @Override
        public RuleTest getTarget() {
            return AlwaysTrueTest.INSTANCE;
        }

        @Override
        public @NotNull String getSerializedName() {
            return "nowhere";
        }
    };
}
