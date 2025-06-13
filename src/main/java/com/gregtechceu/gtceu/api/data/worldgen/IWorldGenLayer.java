package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import com.mojang.serialization.Codec;

import java.util.Set;

public interface IWorldGenLayer extends StringRepresentable {

    Codec<IWorldGenLayer> CODEC = ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName,
            WorldGeneratorUtils.WORLD_GEN_LAYERS::get);

    boolean isApplicableForLevel(ResourceLocation level);

    Set<ResourceLocation> getLevels();

    RuleTest getTarget();

    @FunctionalInterface
    interface RuleTestSupplier {

        RuleTest get();
    }

    IWorldGenLayer NOWHERE = new IWorldGenLayer() {

        @Override
        public boolean isApplicableForLevel(ResourceLocation level) {
            return false;
        }

        @Override
        public Set<ResourceLocation> getLevels() {
            return Set.of();
        }

        @Override
        public RuleTest getTarget() {
            return AlwaysTrueTest.INSTANCE;
        }

        @Override
        public String getSerializedName() {
            return "nowhere";
        }
    };
}
