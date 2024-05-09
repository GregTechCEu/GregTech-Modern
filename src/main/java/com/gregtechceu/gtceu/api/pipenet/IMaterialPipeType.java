package com.gregtechceu.gtceu.api.pipenet;


import com.gregtechceu.gtceu.api.tags.TagPrefix;

public interface IMaterialPipeType<NodeDataType> extends IPipeType<NodeDataType> {

    /**
     * Determines ore prefix used for this pipe type, which gives pipe ore dictionary key
     * when combined with pipe's material
     *
     * @return ore prefix used for this pipe type
     */
    TagPrefix getTagPrefix();
}
