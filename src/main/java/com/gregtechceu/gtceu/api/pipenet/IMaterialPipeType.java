package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

public interface IMaterialPipeType<NodeDataType> extends IPipeType<NodeDataType> {

    /**
     * Determines tag prefix used for this pipe type, which gives pipe tag key
     * when combined with pipe's material
     *
     * @return tag prefix used for this pipe type
     */
    TagPrefix getTagPrefix();
}
