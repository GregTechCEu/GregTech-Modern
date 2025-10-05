package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import lombok.Getter;

import java.util.Objects;

/**
 * This class and subclasses are holding necessary data to find the exact same GUI on client and server.
 * For example, if the GUI was opened by right-clicking a BlockEntity, then this data needs a world and a block pos.
 * <p>
 * Also see {@link PosGuiData} (useful for BlockEntities), {@link SidedPosGuiData} (useful for covers from GregTech) and
 * {@link HandGuiData} (useful for guis opened by interacting with an item in the players hand) for default
 * implementations.
 * </p>
 */
public class GuiData {

    @Getter
    private final Player player;

    public GuiData(Player player) {
        this.player = Objects.requireNonNull(player);
    }

    public Level getLevel() {
        return getPlayer().level();
    }

    public boolean isClient() {
        return NetworkUtils.isClient(this.player);
    }

    public ItemStack getMainHandItem() {
        return this.player.getMainHandItem();
    }

    public ItemStack getOffHandItem() {
        return this.player.getOffhandItem();
    }
}
