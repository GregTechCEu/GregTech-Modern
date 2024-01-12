package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@SuppressWarnings("NonFinalFieldInEnum")
public enum StoneTypes implements IStrataLayer {

    STONE("stone", MapColor.STONE, true, () -> Blocks.STONE::defaultBlockState, -0.5, 1.0, GTMaterials.Stone, false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(64);
        }

        @Override
        public IntProvider getSize() {
            return UniformInt.of(48, 100);
        }
    },
    DEEPSLATE("deepslate", MapColor.DEEPSLATE, true, () -> Blocks.DEEPSLATE::defaultBlockState, -0.1, 0.4, GTMaterials.Deepslate, false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.BOTTOM;
        }

        @Override
        public IntProvider getSize() {
            return UniformInt.of(40, 64);
        }
    },
    RED_GRANITE("red_granite", MapColor.COLOR_RED, true, () -> GTBlocks.RED_GRANITE::getDefaultState, -0.5, 1.0, GTMaterials.RedGranite) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(0);
        }

        @Override
        public IntProvider getSize() {
            return UniformInt.of(32, 56);
        }
    },
    MARBLE("marble", MapColor.QUARTZ, true, () -> GTBlocks.MARBLE::getDefaultState, -0.5, 1.0, GTMaterials.Marble) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(32);
        }

        @Override
        public IntProvider getSize() {
            return UniformInt.of(24, 56);
        }
    },
    ANDESITE("andesite", MapColor.STONE, true, () -> Blocks.ANDESITE::defaultBlockState, -0.5, 1.0, GTMaterials.Andesite, false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.belowTop(128);
        }

        @Override
        public IntProvider getSize() {
            return UniformInt.of(32, 48);
        }
    },
    GRANITE("granite", MapColor.DIRT, true, () -> Blocks.GRANITE::defaultBlockState, -0.5, 1.0, GTMaterials.Granite, false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(64);
        }

        @Override
        public IntProvider getSize() {
            return UniformInt.of(32, 48);
        }
    },
    DIORITE("diorite", MapColor.QUARTZ, true, () -> Blocks.DIORITE::defaultBlockState, -0.5, 1.0, GTMaterials.Diorite, false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(96);
        }

        @Override
        public IntProvider getSize() {
            return UniformInt.of(32, 48);
        }
    },
    /*
    BASALT("basalt", MapColor.TERRACOTTA_BLACK, true, () -> Blocks.BASALT::defaultBlockState, -0.5, 1.0, GTMaterials.Basalt, false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.aboveBottom(32);
        }

        @Override
        public IntProvider getVerticalSize() {
            return UniformInt.of(6, 12);
        }
    },
     */
    CONCRETE_LIGHT("light_concrete", MapColor.STONE, false, () -> GTBlocks.LIGHT_CONCRETE::getDefaultState, 0.0, 0.0, GTMaterials.Concrete),
    CONCRETE_DARK("dark_concrete", MapColor.STONE, false, () -> GTBlocks.DARK_CONCRETE::getDefaultState, 0.0, 0.0, GTMaterials.Concrete),
    ;

    private final String name;
    public final MapColor mapColor;
    @Getter @Setter
    public boolean natural;
    @Getter @Setter
    public Supplier<Supplier<BlockState>> state;
    @Getter
    public final Material material;
    @Getter
    public final double minSpawnTreshold, maxSpawnTreshold;


    public final boolean generateBlocks;

    StoneTypes(@Nonnull String name, @Nonnull MapColor mapColor, boolean natural, Supplier<Supplier<BlockState>> state, double minSpawnTreshold, double maxSpawnTreshold, Material material) {
        this(name, mapColor, natural, state, minSpawnTreshold, maxSpawnTreshold, material, true);
    }

    StoneTypes(@Nonnull String name, @Nonnull MapColor mapColor, boolean natural, Supplier<Supplier<BlockState>> state, double minSpawnTreshold, double maxSpawnTreshold, Material material, boolean generateBlocks) {
        this.name = name;
        this.mapColor = mapColor;
        this.natural = natural;
        this.state = state;
        this.material = material;
        this.generateBlocks = generateBlocks;
        this.minSpawnTreshold = minSpawnTreshold;
        this.maxSpawnTreshold = maxSpawnTreshold;

        WorldGeneratorUtils.STRATA_LAYERS.put(this.name, this);
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return this.name;
    }

    public TagPrefix getTagPrefix() {
        return switch (this) {
            //case RED_GRANITE, MARBLE ->
            //        TagPrefix.ore;
            case CONCRETE_LIGHT, CONCRETE_DARK ->
                    TagPrefix.block;
            default -> TagPrefix.block;
        };
    }

    public static void init() {

    }
}