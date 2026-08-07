package com.gregtechceu.gtceu.api.machine.trait.feature;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A machine trait that provides special interaction behaviour.
 */
@ParametersAreNonnullByDefault
public interface IInteractionTrait {

    /**
     * Called when a machine is right clicked without an item, or if this machine was clicked with an item but no
     * item-specific interaction was performed.
     *
     * @param context The context which this interaction is being performed from.
     * @return The result of this interaction callback.
     */
    default InteractionResult onUse(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    /**
     * Called when a player clicks this machine with a GT tool
     *
     * @param context The context of this interaction.
     * @return A pair containing the type of the tool (if the interaction was successful), and the result of the
     *         interaction.
     *         {@link InteractionResult#sidedSuccess(boolean)} will play the tool sound (based on the first element of
     *         the pair) and consume
     *         durability.
     */
    default Pair<@Nullable GTToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        return Pair.of(null, InteractionResult.PASS);
    }

    /**
     * Called when a machine is left clicked.
     *
     * @param player Player that clicked
     * @param hand   Player hand
     * @param face   Clicked face
     * @return true to cancel the click event, false to continue processing
     */
    default boolean onLeftClick(Player player, InteractionHand hand, @Nullable Direction face) {
        return false;
    }
}
