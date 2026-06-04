package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;

/**
 * Represents a variant of a specific pipe type (e.g. sizes of item pipe)
 */
public interface IPipeVariant<NodeDataType> {

    /**
     * The thickness of the pipe, used for rendering/collisions.
     * 
     * @return Pipe thickness
     */
    float getThickness();

    /**
     * modify the node data by the pipe type.
     */
    NodeDataType modifyProperties(NodeDataType baseProperties);

    /**
     * Used for datagen, creates the model for pipe blocks of this variant.
     * 
     * @param block    The block to create the model for
     * @param provider Datagen blockstate provider
     * @return Pipe model
     */
    PipeModel createPipeModel(PipeBlock<?> block, GTBlockstateProvider provider);
}
