package com.gregtechceu.gtceu.api.machine.trait.feature;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.datafixers.util.Pair;

import java.util.Set;

/**
 * A machine trait that provides special interaction behaviour.
 */
public interface IInteractionTrait extends ITraitFeature {

    default Pair<GTToolType, InteractionResult> onToolClick(Set<GTToolType> toolType,
                                                            Player player, InteractionHand hand, Direction gridSide,
                                                            BlockHitResult hitResult) {
        return Pair.of(null, InteractionResult.PASS);
    }
}
