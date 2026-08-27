package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

/**
 * A spoil action is an action that is applied when an item stack spoils.
 */
public abstract class SpoilAction {

    public static final Codec<SpoilAction> CODEC = GTRegistries.SPOIL_ACTION_SERIALIZERS.byNameCodec()
            .dispatch(SpoilAction::codec, Function.identity());

    /**
     * Called when an item stack spoils.
     * @param itemResults A modifiable list containing the item results to be produced by this item stack spoiling.
     * @param stack The item stack which has spoiled.
     * @param spoilContext The spoil context.
     * @param simulate If side effects should be performed (e.g. spawning an entity should only be performed when simulate = false).
     */
    public abstract void getSpoilResult(List<ItemStack> itemResults, ItemStack stack, SpoilContext spoilContext, boolean simulate);

    public abstract void appendTooltip(List<Component> tooltip, ItemStack stack);

    public abstract MapCodec<? extends SpoilAction> codec();
}
