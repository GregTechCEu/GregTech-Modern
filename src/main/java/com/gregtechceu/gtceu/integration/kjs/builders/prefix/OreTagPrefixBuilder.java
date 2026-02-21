package com.gregtechceu.gtceu.integration.kjs.builders.prefix;

import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.data.block.GTBlocks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.Conditions.hasOreProperty;
import static com.gregtechceu.gtceu.integration.kjs.Validator.*;

@Accessors(fluent = true, chain = true)
public class OreTagPrefixBuilder extends TagPrefixBuilder {

    @Setter
    public transient Supplier<BlockState> stateSupplier;
    @Setter
    public transient Supplier<Material> materialSupplier;
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
    public TagPrefix create(String id) {
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
        validate(this.id,
                errorIfNull(stateSupplier, "stateSupplier"),
                onlySetDefault(templateProperties, () -> {
                    templateProperties = () -> GTBlocks.copy(stateSupplier.get().getBlock().properties(),
                            BlockBehaviour.Properties.of());
                }),
                errorIfNull(baseModelLocation, "baseModelLocation"));

        return base.registerOre(stateSupplier, materialSupplier, templateProperties, baseModelLocation,
                doubleDrops, isSand, shouldDropAsItem);
    }
}
