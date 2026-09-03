package com.gregtechceu.gtceu.integration.kjs.builders.material;

import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import com.gregtechceu.gtceu.integration.recipeviewer.widgets.GTOreByProduct;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasOreProperty;
import static com.gregtechceu.gtceu.integration.kjs.Validator.*;

@Accessors(fluent = true, chain = true)
public class OreTagPrefixBuilderJS extends TagPrefixBuilderJS {

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

    public OreTagPrefixBuilderJS(ResourceLocation id) {
        super(id);
    }

    @Override
    public TagPrefix create(ResourceLocation id) {
        defaultTagPath("ores/%s");
        prefixOnlyTagPath("ores_in_ground/%s");
        unformattedTagPath("ores");
        materialIconType(MaterialIconType.ore);
        unificationEnabled(true);
        blockConstructor(OreBlock::new);
        generationCondition(hasOreProperty);

        return super.create(id);
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

        TagPrefix newPrefix = create(id);

        newPrefix.setTags(tags);
        newPrefix.miningToolTag().addAll(miningToolTag);

        TagPrefix.ORES.put(newPrefix, new TagPrefix.OreType(stateSupplier, materialSupplier, templateProperties, baseModelLocation, doubleDrops, isSand, shouldDropAsItem));
        if (shouldDropAsItem) {
            GTOreByProduct.addOreByProductPrefix(newPrefix);
        }

        return newPrefix;
    }
}
