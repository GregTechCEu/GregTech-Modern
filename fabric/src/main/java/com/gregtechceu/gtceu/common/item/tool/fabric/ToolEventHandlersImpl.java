package com.gregtechceu.gtceu.common.item.tool.fabric;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public class ToolEventHandlersImpl {
    // fabric doesn't have an item pickup event. FFS.
    public static int fireItemPickupEvent(ItemEntity drop, Player player) {
        return 0;
    }
}
