package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSH;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import net.minecraft.world.item.ItemStack;

public class GTMuiWidgets {

    public static Flow createTitleBar(MachineDefinition definition, int panelWidth) {
        var displayItem = definition.asStack();
        String hatchName = displayItem.getHoverName().getString();
        hatchName = hatchName.replaceAll("§.", "").trim();

        int borderRadius = 5;
        int iconSize = 16;
        int minPanelWidth = (int)(panelWidth * 0.9f) - (iconSize + (borderRadius * 2));
        int textTitleWidth = TextRenderer.getFont().width(hatchName);

        int textRows = (int) Math.ceil((double) textTitleWidth / minPanelWidth);
        int textHeightPerRow = (int) (IKey.renderer.getFontHeight());
        int textHeight = textHeightPerRow * textRows + borderRadius;

        int rowWidth = Math.min((int)(0.9 * panelWidth), (iconSize + (borderRadius * 4) + textTitleWidth));

        return new Row()
                .coverChildrenHeight()
                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                .width(rowWidth)
                .top(-(textHeight + borderRadius))
                .rightRel(0.45f)
                .background(GTGuiTextures.BACKGROUND)
                .child(new ItemDrawable(displayItem)
                        .asIcon().size(iconSize)
                        .asWidget()
                        .marginLeft(borderRadius))
                .mainAxisAlignment(Alignment.MainAxis.START)
                .child(IKey.str(hatchName)
                        .asWidget()
                        .paddingTop(1)
                        .margin(borderRadius, borderRadius, borderRadius,1)
                        .size(Math.min(minPanelWidth, textTitleWidth), textHeight));
    }

    public static ToggleButton createPowerButton(IRecipeLogicMachine recipeLogicMachine, PanelSyncManager syncManager) {
        BooleanSyncValue power = new BooleanSyncValue(() -> recipeLogicMachine.getRecipeLogic().isWorkingEnabled(),
                recipeLogicMachine::setWorkingEnabled);
        syncManager.syncValue("working_enabled", power);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(power::getBoolValue, power::setBoolValue))
                .selectedBackground(GTGuiTextures.BUTTON_POWER[1])
                .background(GTGuiTextures.BUTTON_POWER[0]);
    }

    public static ProgressWidget createProgressBar(WorkableTieredMachine workableMachine, UITexture texture, int size) {
        return new ProgressWidget()
                .texture(texture, size)
                .progress(() -> workableMachine.getProgress() / (double)workableMachine.getMaxProgress());
    }

    public static ItemSlot createBatterySlot(SimpleTieredMachine tieredMachine, PanelSyncManager syncManager) {
        ItemSlotSH battery = new ItemSlotSH(new ModularSlot(tieredMachine.getChargerInventory(), 0));
        syncManager.syncValue("battery", battery);
        return new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY);
    }

    public static ToggleButton createAutoOutputItemButton(SimpleTieredMachine machine, PanelSyncManager syncManager) {
        BooleanSyncValue itemOutputs = new BooleanSyncValue(machine::isAutoOutputItems,
                machine::setAutoOutputItems);
        syncManager.syncValue("auto_output_items", itemOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(itemOutputs::getBoolValue, itemOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT);
    }

    public static ToggleButton createAutoOutputFluidButton(SimpleTieredMachine machine, PanelSyncManager syncManager) {
        BooleanSyncValue fluidOutputs = new BooleanSyncValue(machine::isAutoOutputFluids,
                machine::setAutoOutputFluids);
        syncManager.syncValue("auto_output_fluids", fluidOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(fluidOutputs::getBoolValue, fluidOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT);
    }

    public static IDrawable.DrawableWidget createGTLogo() {
        return new IDrawable.DrawableWidget(GTGuiTextures.GREGTECH_LOGO);
    }
}
