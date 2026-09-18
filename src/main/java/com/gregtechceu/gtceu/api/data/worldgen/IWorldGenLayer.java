package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.Set;

public interface IWorldGenLayer extends StringRepresentable {

    boolean isApplicableForLevel(Identifier level);

    Set<Identifier> getLevels();

    RuleTest getTarget();

    @FunctionalInterface
    interface RuleTestSupplier {

        RuleTest get();
    }

    IWorldGenLayer NOWHERE = new IWorldGenLayer() {

        @Override
        public boolean isApplicableForLevel(Identifier level) {
            return false;
        }

        @Override
        public Set<Identifier> getLevels() {
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
