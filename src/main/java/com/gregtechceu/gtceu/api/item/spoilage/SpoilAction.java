package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A spoil action is an action that is applied when an item spoils.
 */
public abstract class SpoilAction {

    public static final Codec<SpoilAction> CODEC = GTRegistries.SPOIL_ACTION_SERIALIZERS.byNameCodec()
            .dispatch(SpoilAction::codec, Function.identity());

    public abstract ItemStack getSpoilResult(@NotNull ItemStack stack, @NotNull SpoilContext spoilContext, boolean simulate);

    public abstract Component getTooltip();

    public abstract MapCodec<? extends SpoilAction> codec();
}
