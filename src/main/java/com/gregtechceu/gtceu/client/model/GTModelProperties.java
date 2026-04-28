package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.model.data.ModelProperty;

public class GTModelProperties {

    public static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();

    public static final ModelProperty<Integer> PIPE_CONNECTION_MASK = new ModelProperty<>();
    public static final ModelProperty<Integer> PIPE_BLOCKED_MASK = new ModelProperty<>();
}
