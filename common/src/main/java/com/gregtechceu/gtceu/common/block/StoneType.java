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
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Supplier;

@SuppressWarnings("NonFinalFieldInEnum")
public enum StoneType implements IStrataLayer {

    RED_GRANITE("red_granite", MaterialColor.COLOR_RED, true, () -> GTBlocks.RED_GRANITE::getDefaultState, GTMaterials.RedGranite, Blocks.STONE.defaultBlockState()) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(0);
        }

        @Override
        public IntProvider getVerticalSize() {
            return UniformInt.of(8, 14);
        }
    },
    MARBLE("marble", MaterialColor.QUARTZ, true, () -> GTBlocks.MARBLE::getDefaultState, GTMaterials.Marble, Blocks.STONE.defaultBlockState()) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(32);
        }

        @Override
        public IntProvider getVerticalSize() {
            return UniformInt.of(6, 14);
        }
    },
    BASALT("basalt", MaterialColor.TERRACOTTA_BLACK, true, () -> Blocks.BASALT::defaultBlockState, GTMaterials.Basalt, Blocks.STONE.defaultBlockState(), false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.aboveBottom(32);
        }

        @Override
        public IntProvider getVerticalSize() {
            return UniformInt.of(6, 12);
        }
    },
    DEEPSLATE("deepslate", MaterialColor.DEEPSLATE, true, () -> Blocks.DEEPSLATE::defaultBlockState, GTMaterials.Deepslate, Blocks.DEEPSLATE.defaultBlockState(), false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.BOTTOM;
        }

        @Override
        public IntProvider getVerticalSize() {
            return UniformInt.of(10, 16);
        }
    },
    ANDESITE("andesite", MaterialColor.STONE, true, () -> Blocks.ANDESITE::defaultBlockState, GTMaterials.Andesite, Blocks.STONE.defaultBlockState(), false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.belowTop(128);
        }

        @Override
        public IntProvider getVerticalSize() {
            return UniformInt.of(8, 12);
        }
    },
    GRANITE("granite", MaterialColor.DIRT, true, () -> Blocks.GRANITE::defaultBlockState, GTMaterials.Granite, Blocks.STONE.defaultBlockState(), false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(64);
        }

        @Override
        public IntProvider getVerticalSize() {
            return UniformInt.of(8, 12);
        }
    },
    DIORITE("diorite", MaterialColor.QUARTZ, true, () -> Blocks.DIORITE::defaultBlockState, GTMaterials.Diorite, Blocks.STONE.defaultBlockState(), false) {
        @Override
        public VerticalAnchor getHeight() {
            return VerticalAnchor.absolute(96);
        }

        @Override
        public IntProvider getVerticalSize() {
            return UniformInt.of(8, 12);
        }
    },
    CONCRETE_LIGHT("light_concrete", MaterialColor.STONE, false, () -> GTBlocks.LIGHT_CONCRETE::getDefaultState, GTMaterials.Concrete) {
        @Override
        public VerticalAnchor getHeight() {
            return null;
        }

        @Override
        public IntProvider getVerticalSize() {
            return null;
        }
    },
    CONCRETE_DARK("dark_concrete", MaterialColor.STONE, false, () -> GTBlocks.DARK_CONCRETE::getDefaultState, GTMaterials.Concrete) {
        @Override
        public VerticalAnchor getHeight() {
            return null;
        }

        @Override
        public IntProvider getVerticalSize() {
            return null;
        }
    };

    private final String name;
    public final MaterialColor mapColor;
    @Getter @Setter
    public boolean natural;
    @Getter @Setter
    public Supplier<Supplier<BlockState>> state;
    @Getter
    public final Material material;

    public final boolean generateBlocks;

    StoneType(@Nonnull String name, @Nonnull MaterialColor mapColor, boolean natural, Supplier<Supplier<BlockState>> state, Material material) {
        this(name, mapColor, natural, state, material, null, true);
    }

    StoneType(@Nonnull String name, @Nonnull MaterialColor mapColor, boolean natural, Supplier<Supplier<BlockState>> state, Material material, @Nullable BlockState originalState) {
        this(name, mapColor, natural, state, material, originalState, true);
    }

    StoneType(@Nonnull String name, @Nonnull MaterialColor mapColor, boolean natural, Supplier<Supplier<BlockState>> state, Material material, @Nullable BlockState originalState, boolean generateBlocks) {
        this.name = name;
        this.mapColor = mapColor;
        this.natural = natural;
        this.state = state;
        this.material = material;
        this.generateBlocks = generateBlocks;
        WorldGeneratorUtils.STRATA_LAYERS.put(this.name, this);
        WorldGeneratorUtils.STRATA_LAYER_BLOCK_MAP.computeIfAbsent(originalState, (bs) -> new ArrayList<>()).add(this);
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