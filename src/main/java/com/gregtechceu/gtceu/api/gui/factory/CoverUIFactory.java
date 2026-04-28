package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CoverUIFactory extends UIFactory<CoverBehavior> {

    public static final CoverUIFactory INSTANCE = new CoverUIFactory();

    public CoverUIFactory() {
        super(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "cover"));
    }

    @Override
    protected ModularUI createUITemplate(CoverBehavior holder, Player entityPlayer) {
        if (holder instanceof IUICover cover) {
            var widget = (Widget) cover.createUIWidget();
            var size = widget.getSize();
            widget.setSelfPosition(new Position((176 - size.width) / 2, 0));
            var uiHolder = new IUIHolder() {

                @Override
                public ModularUI createUI(Player entityPlayer) {
                    return null;
                }

                @Override
                public boolean isInvalid() {
                    return cover.isInvalid();
                }

                @Override
                public boolean isRemote() {
                    return cover.isRemote();
                }

                @Override
                public void markAsDirty() {}
            };
            var modularUI = new ModularUI(176, size.height + 82, uiHolder, entityPlayer)
                    .background(GuiTextures.BACKGROUND)
                    .widget(widget)
                    .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7,
                            size.height, true));
            modularUI.registerCloseListener(cover::onUIClosed);
            return modularUI;
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected CoverBehavior readHolderFromSyncData(RegistryFriendlyByteBuf syncData) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return null;
        var pos = syncData.readBlockPos();
        var side = syncData.readEnum(Direction.class);
        var coverable = GTCapabilityHelper.getCoverable(world, pos, side);
        if (coverable != null) {
            return coverable.getCoverAtSide(side);
        }
        return null;
    }

    @Override
    protected void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, CoverBehavior holder) {
        syncData.writeBlockPos(holder.coverHolder.getBlockPos());
        syncData.writeEnum(holder.attachedSide);
    }
}
