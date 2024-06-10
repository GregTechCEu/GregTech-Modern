package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

/**
 * Logic from
 * <a href=
 * "https://github.com/SleepyTrousers/EnderIO/blob/d6dfb9d3964946ceb9fd72a66a3cff197a51a1fe/enderio-base/src/main/java/crazypants/enderio/base/handler/darksteel/DarkSteelController.java">EnderIO</a>
 */
public interface IStepAssist {

    AttributeModifier STEP_ASSIST_MODIFIER = new AttributeModifier(GTCEu.id("step_assist"), 0.4023,
            AttributeModifier.Operation.ADD_VALUE);

    float MAGIC_STEP_HEIGHT = 1.0023f;

    default void updateStepHeight(@NotNull Player player) {
        if (!player.isShiftKeyDown()) {
            if (player.maxUpStep() < MAGIC_STEP_HEIGHT) {
                player.getAttribute(Attributes.STEP_HEIGHT).addOrUpdateTransientModifier(STEP_ASSIST_MODIFIER);
            }
        } else if (player.maxUpStep() == MAGIC_STEP_HEIGHT) {
            player.getAttribute(Attributes.STEP_HEIGHT).removeModifier(STEP_ASSIST_MODIFIER);
        }
    }
}
