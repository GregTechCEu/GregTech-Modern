package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.editor.GTUIEditor;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class GTUIEditorFactory extends UIFactory<GTUIEditorFactory> implements IUIHolder {

    public static final GTUIEditorFactory INSTANCE = new GTUIEditorFactory();

    private GTUIEditorFactory() {
        super(LDLib.location("gt_ui_editor"));
    }

    @Override
    protected ModularUI createUITemplate(GTUIEditorFactory holder, Player entityPlayer) {
        return createUI(entityPlayer);
    }

    @Override
    protected GTUIEditorFactory readHolderFromSyncData(FriendlyByteBuf syncData) {
        return this;
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, GTUIEditorFactory holder) {}

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(this, entityPlayer).widget(new GTUIEditor());
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return GTCEu.isClientThread();
    }

    @Override
    public void markAsDirty() {}
}
