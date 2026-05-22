package com.gregtechceu.gtceu.client.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Map;
import java.util.Objects;

public class GTModelProperties {

    public static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();

    public static final ModelProperty<Map<Direction, ModelData>> COVER_MODEL_DATA = new ModelProperty<>();

    public static final ModelProperty<Integer> PIPE_CONNECTION_MASK = new ModelProperty<>();
    public static final ModelProperty<Integer> PIPE_BLOCKED_MASK = new ModelProperty<>();

    public static final ModelProperty<ModelData> CHILD_MODEL_DATA = new ModelProperty<>(Objects::nonNull);
}
