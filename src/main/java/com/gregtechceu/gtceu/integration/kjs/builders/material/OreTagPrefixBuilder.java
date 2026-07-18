package com.gregtechceu.gtceu.integration.kjs.builders.material;

import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasOreProperty;

@Accessors(fluent = true, chain = true)
public class OreTagPrefixBuilder extends TagPrefixBuilder {

    @Setter
    public transient Supplier<BlockState> stateSupplier;
    @Setter
    public transient MaterialEntry material;
    @Setter
    public transient ResourceLocation baseModelLocation;
    @Setter
    public transient Supplier<BlockBehaviour.Properties> templateProperties;
    @Setter
    public transient boolean doubleDrops = false;
    @Setter
    public transient boolean isSand = false;
    @Setter
    public transient boolean shouldDropAsItem = false;

    public OreTagPrefixBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public TagPrefix create(ResourceLocation id) {
        return new TagPrefix(id)
                .defaultTagPath("ores/%s")
                .prefixOnlyTagPath("ores_in_ground/%s")
                .unformattedTagPath("ores")
                .materialIconType(MaterialIconType.ore)
                .unificationEnabled(true)
                .blockConstructor(OreBlock::new)
                .generationCondition(hasOreProperty);
    }

    @Override
    public TagPrefix createObject() {
        return base.registerOre(stateSupplier, material, templateProperties, baseModelLocation,
                doubleDrops, isSand, shouldDropAsItem);
    }
}
