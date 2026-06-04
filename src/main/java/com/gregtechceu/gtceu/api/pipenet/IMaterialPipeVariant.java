package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;

/**
 * A pipe variant for a material pipe block, where each pipe variant has its own tag prefix.
 * 
 * @param <NodeDataType>
 */
public interface IMaterialPipeVariant<NodeDataType extends PipeSegmentProperties> extends IPipeVariant<NodeDataType> {

    /**
     * Determines tag prefix used for this pipe type, which gives pipe tag key
     * when combined with pipe's material
     *
     * @return tag prefix used for this pipe type
     */
    TagPrefix getTagPrefix();

    @Override
    default NodeDataType createSegmentProperties(PipeBlock block) {
        if (block instanceof MaterialPipeBlock materialPipeBlock) {
            return createSegmentProperties(materialPipeBlock, materialPipeBlock.material);
        }
        throw new IllegalStateException(
                "Attempted to create material pipe segment properties for a non-material pipe block");
    }

    NodeDataType createSegmentProperties(MaterialPipeBlock block, Material material);

    @Override
    default PipeModel createPipeModel(PipeBlock block, GTBlockstateProvider provider) {
        if (block instanceof MaterialPipeBlock materialPipeBlock) {
            return createPipeModel(materialPipeBlock, materialPipeBlock.material, provider);
        }
        throw new IllegalStateException(
                "Attempted to create material pipe variant model for a non-material pipe block");
    }

    PipeModel createPipeModel(MaterialPipeBlock block, Material material, GTBlockstateProvider provider);
}
