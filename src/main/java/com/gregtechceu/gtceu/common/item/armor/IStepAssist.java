package com.gregtechceu.gtceu.common.item.armor;

import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

/**
 * Logic from
 * <a href=
 * "https://github.com/SleepyTrousers/EnderIO/blob/d6dfb9d3964946ceb9fd72a66a3cff197a51a1fe/enderio-base/src/main/java/crazypants/enderio/base/handler/darksteel/DarkSteelController.java">EnderIO</a>
 */
public interface IStepAssist {

    float MAGIC_STEP_HEIGHT = 1.0023f;

    default void updateStepHeight(@NotNull Player player) {
        if (!player.isShiftKeyDown()) {
            if (player.maxUpStep() < MAGIC_STEP_HEIGHT) {
                player.setMaxUpStep(MAGIC_STEP_HEIGHT);
            }
        } else if (player.getStepHeight() == MAGIC_STEP_HEIGHT) {
            player.setMaxUpStep(0.6F);
        }
    }
}
