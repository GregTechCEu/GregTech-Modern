package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.pipelike.cable.CableSegmentProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a variant of a specific pipe type (e.g. sizes of item pipe)
 */
public interface IPipeVariant<NodeDataType extends PipeSegmentProperties> {

    /**
     * The thickness of the pipe, used for rendering/collisions.
     * 
     * @return Pipe thickness
     */
    float getThickness();

    /**
     * Creates the default segment properties for a specific pipe block.<br>
     * NOTE: This should always create a new object and never pass a reference to an existing object.
     */
    NodeDataType createSegmentProperties(PipeBlock<NodeDataType> block);

    /**
     * Used for datagen, creates the model for pipe blocks of this variant.
     * 
     * @param block    The block to create the model for
     * @param provider Datagen blockstate provider
     * @return Pipe model
     */
    PipeModel createPipeModel(PipeBlock<?> block, GTBlockstateProvider provider);
}
