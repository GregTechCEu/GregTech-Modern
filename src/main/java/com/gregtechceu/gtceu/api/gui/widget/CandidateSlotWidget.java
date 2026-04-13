package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemEntryHandler;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.screen.EmiScreenManager;

import java.util.List;

public class CandidateSlotWidget extends SlotWidget {

    public CandidateSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                               boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public boolean showListIngredient(Object ingredient, int button) {
        List<ListEmiIngredient> ingredients = (List<ListEmiIngredient>) ingredient;
        return EmiScreenManager.stackInteraction(new EmiStackInteraction(new ListEmiIngredient(ingredients, 1)),
                bind -> bind.matchesMouse(button));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.slotReference != null && this.isMouseOverElement(mouseX, mouseY) && this.gui != null) {
            var ingredient = this.getXEICurrentIngredient();
            if (ingredient != null && LDLib.isEmiLoaded()) {
                return showListIngredient(this.getXEICurrentIngredient(), button);
            } else {
                ModularUIGuiContainer modularUIGui = this.gui.getModularUIGui();
                boolean last = modularUIGui.getQuickCrafting();
                InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(button);
                HOVER_SLOT = this.slotReference;
                this.gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
                HOVER_SLOT = null;
                if (last != modularUIGui.getQuickCrafting()) {
                    modularUIGui.dragSplittingButton = button;
                    if (button == 0) {
                        modularUIGui.dragSplittingLimit = 0;
                    } else if (button == 1) {
                        modularUIGui.dragSplittingLimit = 1;
                    } else if (Minecraft.getInstance().options.keyPickItem.matchesMouse(mouseKey.getValue())) {
                        modularUIGui.dragSplittingLimit = 2;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public Object getXEICurrentIngredient() {
        if (this.slotReference != null && !this.slotReference.getItem().isEmpty()) {
            Slot handler = this.getHandler();
            if (handler == null) {
                return null;
            } else {
                ItemStack realStack = this.getRealStack(handler.getItem());
                if (handler instanceof WidgetSlotItemHandler slotHandler) {
                    if (slotHandler.getItemHandler() instanceof CycleItemEntryHandler entryHandler) {
                        return getXEIIngredientsClickable(entryHandler, slotHandler.index);
                    }
                }
            }
        }
        return null;
    }
}
