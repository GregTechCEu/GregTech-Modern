package com.gregtechceu.gtceu.api.machine.trait.feature;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.world.InteractionResult;

import com.mojang.datafixers.util.Pair;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A machine trait that provides special interaction behaviour.
 */
@ParametersAreNonnullByDefault
public interface IInteractionTrait extends ITraitFeature {

    /// Called when a player interacts with a machine without an item.
    default InteractionResult onUse(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    /// Called when a player interacts with a machine with a tool.
    default Pair<GTToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        return Pair.of(null, InteractionResult.PASS);
    }
}
