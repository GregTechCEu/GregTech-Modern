package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilAction;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.item.spoil_actions.TransformItemSpoilAction;
import com.gregtechceu.gtceu.common.item.spoil_actions.SpawnEntitySpoilAction;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTSpoilActions {

    public static void init() {}

    public static DeferredHolder<MapCodec<? extends SpoilAction>, MapCodec<TransformItemSpoilAction>> TRANSFORM_ITEM = REGISTRATE.simple("transform_item",
            GTRegistries.Keys.SPOIL_ACTION_SERIALIZER, () -> TransformItemSpoilAction.CODEC);

    public static DeferredHolder<MapCodec<? extends SpoilAction>, MapCodec<SpawnEntitySpoilAction>> SPAWN_ENTITY = REGISTRATE.simple("spawn_entity",
            GTRegistries.Keys.SPOIL_ACTION_SERIALIZER, () -> SpawnEntitySpoilAction.CODEC);
}
