package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import lombok.Getter;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public enum StoneTypes implements StringRepresentable {

    STONE("stone", MaterialColor.STONE, true, () -> Blocks.STONE::defaultBlockState, GTMaterials.Stone, false),
    DEEPSLATE("deepslate", MaterialColor.DEEPSLATE, true, () -> Blocks.DEEPSLATE::defaultBlockState, GTMaterials.Deepslate, false),
    RED_GRANITE("red_granite", MaterialColor.COLOR_RED, true, () -> GTBlocks.RED_GRANITE::getDefaultState, GTMaterials.GraniteRed),
    MARBLE("marble", MaterialColor.QUARTZ, true, () -> GTBlocks.MARBLE::getDefaultState, GTMaterials.Marble),
    ANDESITE("andesite", MaterialColor.STONE, true, () -> Blocks.ANDESITE::defaultBlockState, GTMaterials.Andesite, false),
    GRANITE("granite", MaterialColor.DIRT, true, () -> Blocks.GRANITE::defaultBlockState, GTMaterials.Granite, false) ,
    DIORITE("diorite", MaterialColor.QUARTZ, true, () -> Blocks.DIORITE::defaultBlockState, GTMaterials.Diorite, false),
    BASALT("basalt", MaterialColor.TERRACOTTA_BLACK, true, () -> Blocks.BASALT::defaultBlockState, GTMaterials.Basalt, false),
    CONCRETE_LIGHT("light_concrete", MaterialColor.STONE, false, () -> GTBlocks.LIGHT_CONCRETE::getDefaultState, GTMaterials.Concrete),
    CONCRETE_DARK("dark_concrete", MaterialColor.STONE, false, () -> GTBlocks.DARK_CONCRETE::getDefaultState, GTMaterials.Concrete),
    ;

    private final String name;
    public final MaterialColor mapColor;
    @Getter
    public final boolean natural;
    @Getter
    public final Supplier<Supplier<BlockState>> state;
    @Getter
    public final Material material;


    public final boolean generateBlocks;

    StoneTypes(@Nonnull String name, @Nonnull MaterialColor MaterialColor, boolean natural, Supplier<Supplier<BlockState>> state, Material material) {
        this(name, MaterialColor, natural, state, material, true);
    }

    StoneTypes(@Nonnull String name, @Nonnull MaterialColor mapColor, boolean natural, Supplier<Supplier<BlockState>> state, Material material, boolean generateBlocks) {
        this.name = name;
        this.mapColor = mapColor;
        this.natural = natural;
        this.state = state;
        this.material = material;
        this.generateBlocks = generateBlocks;
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