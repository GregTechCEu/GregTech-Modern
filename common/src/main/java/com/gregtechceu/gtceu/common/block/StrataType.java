package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nonnull;

public enum StrataType implements StringRepresentable {

    RED_GRANITE("red_granite", MaterialColor.COLOR_RED),
    MARBLE("marble", MaterialColor.QUARTZ),
    BASALT("basalt", MaterialColor.TERRACOTTA_BLACK),
    CONCRETE_LIGHT("light_concrete", MaterialColor.STONE),
    CONCRETE_DARK("dark_concrete", MaterialColor.STONE);

    private final String name;
    public final MaterialColor mapColor;

    StrataType(@Nonnull String name, @Nonnull MaterialColor mapColor) {
        this.name = name;
        this.mapColor = mapColor;
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

    public Material getMaterial() {
        return switch (this) {
            case RED_GRANITE ->
                    GTMaterials.GraniteRed;
            case MARBLE ->
                    GTMaterials.Marble;
            case BASALT ->
                    GTMaterials.Basalt;
            case CONCRETE_LIGHT, CONCRETE_DARK ->
                    GTMaterials.Concrete;
        };
    }
}