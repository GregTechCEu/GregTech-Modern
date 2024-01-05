package com.gregtechceu.gtceu.api.item.gui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * {@link UIFactory} implementation for {@link ComponentItem}s
 */
@Deprecated
public class PlayerInventoryUIFactory extends UIFactory<PlayerInventoryHolder> {

    public static final PlayerInventoryUIFactory INSTANCE = new PlayerInventoryUIFactory();

    private PlayerInventoryUIFactory() {
        super(GTCEu.id("player_inventory_factory"));
    }

    @Override
    protected ModularUI createUITemplate(PlayerInventoryHolder holder, Player entityPlayer) {
        return holder.createUI(entityPlayer);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected PlayerInventoryHolder readHolderFromSyncData(FriendlyByteBuf syncData) {
        Player entityPlayer = Minecraft.getInstance().player;
        InteractionHand enumHand = InteractionHand.values()[syncData.readByte()];
        ItemStack itemStack;
        itemStack = syncData.readItem();
        return new PlayerInventoryHolder(entityPlayer, enumHand, itemStack);
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, PlayerInventoryHolder holder) {
        syncData.writeByte(holder.hand.ordinal());
        syncData.writeItem(holder.getCurrentItem());
    }
}