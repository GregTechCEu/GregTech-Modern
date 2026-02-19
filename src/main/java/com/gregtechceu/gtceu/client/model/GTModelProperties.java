package com.gregtechceu.gtceu.client.model;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class GTModelProperties {

    public static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();

    public static final ModelProperty<Integer> PIPE_CONNECTION_MASK = new ModelProperty<>();
    public static final ModelProperty<Integer> PIPE_BLOCKED_MASK = new ModelProperty<>();
}
