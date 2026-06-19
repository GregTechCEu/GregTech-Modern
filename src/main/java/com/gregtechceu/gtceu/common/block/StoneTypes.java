package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import lombok.Getter;

import java.util.function.Supplier;

public enum StoneTypes implements StringRepresentable {

    // spotless:off
    STONE("stone", MapColor.STONE, true, () -> Blocks.STONE::defaultBlockState, () -> GTMaterials.Stone, false),
    DEEPSLATE("deepslate", MapColor.DEEPSLATE, true, () -> Blocks.DEEPSLATE::defaultBlockState, () -> GTMaterials.Deepslate, false),
    RED_GRANITE("red_granite", MapColor.COLOR_RED, true, () -> GTBlocks.RED_GRANITE::getDefaultState, () -> GTMaterials.RedGranite),
    MARBLE("marble", MapColor.QUARTZ, true, () -> GTBlocks.MARBLE::getDefaultState, () -> GTMaterials.Marble),
    ANDESITE("andesite", MapColor.STONE, true, () -> Blocks.ANDESITE::defaultBlockState, () -> GTMaterials.Andesite, false),
    GRANITE("granite", MapColor.DIRT, true, () -> Blocks.GRANITE::defaultBlockState, () -> GTMaterials.Granite, false),
    DIORITE("diorite", MapColor.QUARTZ, true, () -> Blocks.DIORITE::defaultBlockState, () -> GTMaterials.Diorite, false),
    BASALT("basalt", MapColor.COLOR_BLACK, true, () -> Blocks.BASALT::defaultBlockState, () -> GTMaterials.Basalt, false),
    BLACKSTONE("blackstone", MapColor.COLOR_BLACK, true, () -> Blocks.BLACKSTONE::defaultBlockState, () -> GTMaterials.Blackstone, false),
    CONCRETE_LIGHT("light_concrete", MapColor.STONE, false, () -> GTBlocks.LIGHT_CONCRETE::getDefaultState, () -> GTMaterials.Concrete),
    CONCRETE_DARK("dark_concrete", MapColor.STONE, false, () -> GTBlocks.DARK_CONCRETE::getDefaultState, () -> GTMaterials.Concrete),
            ;
    // spotless:on

    private final String name;
    public final MapColor mapColor;
    @Getter
    public final boolean natural;
    @Getter
    public final Supplier<Supplier<BlockState>> state;
    public final Supplier<Material> material;

    public final boolean generateBlocks;

    StoneTypes(String name, MapColor mapColor, boolean natural, Supplier<Supplier<BlockState>> state,
               Supplier<Material> material) {
        this(name, mapColor, natural, state, material, true);
    }

    StoneTypes(String name, MapColor mapColor, boolean natural, Supplier<Supplier<BlockState>> state,
               Supplier<Material> material, boolean generateBlocks) {
        this.name = name;
        this.mapColor = mapColor;
        this.natural = natural;
        this.state = state;
        this.material = material;
        this.generateBlocks = generateBlocks;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public TagPrefix getTagPrefix() {
        // disabled, always return block
        if (false && this.natural) {
            return TagPrefix.rock;
        } else {
            return TagPrefix.block;
        }
    }

    public TagPrefix getOreBaseTagPrefix() {
        return switch (this) {
            case STONE -> TagPrefix.ore;
            case DEEPSLATE -> TagPrefix.oreDeepslate;
            case RED_GRANITE -> TagPrefix.oreRedGranite;
            case MARBLE -> TagPrefix.oreMarble;
            case ANDESITE -> TagPrefix.oreAndesite;
            case GRANITE -> TagPrefix.oreGranite;
            case DIORITE -> TagPrefix.oreDiorite;
            case BASALT -> TagPrefix.oreBasalt;
            default -> {
                if (this.natural) yield TagPrefix.ore;
                else yield TagPrefix.NULL_PREFIX;
            }
        };
    }

    public Material getMaterial() {
        return material.get();
    }

    public static void init() {}
}
