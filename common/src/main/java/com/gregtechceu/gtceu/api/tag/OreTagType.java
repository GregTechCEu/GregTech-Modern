package com.gregtechceu.gtceu.api.tag;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import lombok.Getter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OreTagType extends TagPrefix {

    private static final List<OreTagType> ORES = new ArrayList<>();

    // Intended for "standard" ores with normal processing
    // ex: "gold_ores"
    private static final String COMMON_ORE_TAG = "%s_ores";
    // Intended for "special" ores with different processing,
    // like Netherrack and Endstone having doubled outputs.
    // ex: "netherrack_gold_ores"
    private static final String CUSTOM_ORE_TAG = "%s_%s_ores";

    public static final OreTagType ore = new OreTagType("stone", Blocks.STONE::defaultBlockState)
            .langValue("%s Ore");
    public static final OreTagType oreGranite = new OreTagType("granite", Blocks.GRANITE::defaultBlockState)
            .langValue("Granite %s Ore");
    public static final OreTagType oreDiorite = new OreTagType("diorite", Blocks.DIORITE::defaultBlockState)
            .langValue("Diorite %s Ore");
    public static final OreTagType oreAndesite = new OreTagType("andesite", Blocks.ANDESITE::defaultBlockState)
            .langValue("Andesite %s Ore");
    public static final OreTagType oreBasalt = new OreTagType("basalt", Blocks.BASALT::defaultBlockState)
            .langValue("Basalt %s Ore");
    public static final OreTagType oreDeepslate = new OreTagType("deepslate", Blocks.DEEPSLATE::defaultBlockState)
            .langValue("Deepslate %s Ore");
    public static final OreTagType oreSand = new OreTagType("sand", Blocks.SAND::defaultBlockState)
            .langValue("Sand %s Ore");
    public static final OreTagType oreRedSand = new OreTagType("red_sand", Blocks.RED_SAND::defaultBlockState)
            .langValue("Red Sand %s Ore");

    public static final OreTagType oreNetherrack = new OreTagType("netherrack", Blocks.NETHERRACK::defaultBlockState, false)
            .langValue("Netherrack %s Ore")
            .oreMultiplier(2);
    public static final OreTagType oreEndstone = new OreTagType("endstone", Blocks.END_STONE::defaultBlockState, false)
            .langValue("Endstone %s Ore")
            .oreMultiplier(2);

    @Getter
    private final boolean useCommonTag;
    @Getter
    private final Supplier<BlockState> stoneType;
    @Getter
    private int oreMultiplier = 1;

    protected OreTagType(String name, Supplier<BlockState> stoneType) {
        this(name, stoneType, true);
    }

    protected OreTagType(String name, Supplier<BlockState> stoneType, boolean useCommonTag) {
        super("ore_" + name);
        this.stoneType = stoneType;
        this.useCommonTag = useCommonTag;
        this.generationCondition(TagPrefix.Conditions.hasOreProperty);
        this.materialIconType(MaterialIconType.ore);
        this.unificationEnabled(true);
        ORES.add(this);
    }

    public static List<OreTagType> getAllOreTypes() {
        return ImmutableList.copyOf(ORES);
    }

    // convenience override
    @Override
    public OreTagType langValue(String langValue) {
        this.langValue = langValue;
        return this;
    }

    public OreTagType oreMultiplier(int oreMultiplier) {
        this.oreMultiplier = oreMultiplier;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TagKey<Item>[] getItemTags() {
        return new TagKey[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public TagKey<Item>[] getSubItemTags(String path) {
        return new TagKey[] {
                TagUtil.createItemTag(useCommonTag ?
                        COMMON_ORE_TAG.formatted(path) :
                        CUSTOM_ORE_TAG.formatted(name.split("_", 2)[1], path))
        };
    }
}
