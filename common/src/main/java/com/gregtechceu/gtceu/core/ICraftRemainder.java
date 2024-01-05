package com.gregtechceu.gtceu.core;

import net.minecraft.world.entity.player.Player;

public interface ICraftRemainder {

    ThreadLocal<Player> craftingPlayer = new ThreadLocal<>();
}
