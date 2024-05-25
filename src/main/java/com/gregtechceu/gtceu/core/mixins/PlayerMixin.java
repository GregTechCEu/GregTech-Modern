package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IGTPlayer;
import com.gregtechceu.gtceu.utils.PlayerInventoryListener;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerMixin implements IGTPlayer {
    @Unique
    private PlayerInventoryListener gtceu$inventoryChangeListener;

    @Override
    public PlayerInventoryListener gtceu$getInventoryListener() {
        if (gtceu$inventoryChangeListener == null) {
            gtceu$inventoryChangeListener = new PlayerInventoryListener((Player) (Object) this);
        }
        return gtceu$inventoryChangeListener;
    }
}
