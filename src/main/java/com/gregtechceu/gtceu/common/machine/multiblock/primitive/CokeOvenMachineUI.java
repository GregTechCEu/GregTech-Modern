package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;

import net.minecraft.world.entity.player.Player;

final class CokeOvenMachineUI {

    private CokeOvenMachineUI() {}

    static ModularUI createUI(CokeOvenMachine machine, Player entityPlayer) {
        return new ModularUI(176, 166, machine, entityPlayer)
                .background(GuiTextures.PRIMITIVE_BACKGROUND)
                .widget(new LabelWidget(5, 5, machine.getBlockState().getBlock().getDescriptionId()))
                .widget(new SlotWidget(machine.importItems.storage, 0, 52, 30, true, true)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY)))
                .widget(new ProgressWidget(machine.recipeLogic::getProgressPercent, 76, 32, 20, 15,
                        GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR))
                .widget(new SlotWidget(machine.exportItems.storage, 0, 103, 30, true, false)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY)))
                .widget(new TankWidget(machine.exportFluids.getStorages()[0], 134, 13, 20, 58, true, false)
                        .setBackground(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK)
                        .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP)
                        .setShowAmountOverlay(false)
                        .setOverlay(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK_OVERLAY))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.PRIMITIVE_SLOT, 7, 84,
                        true));
    }
}
