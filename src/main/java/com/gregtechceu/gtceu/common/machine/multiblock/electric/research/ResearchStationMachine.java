package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.data.mui.GTMultiblockPanelUtil;
import com.gregtechceu.gtceu.common.data.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResearchStationMachine extends WorkableElectricMultiblockMachine
                                    implements IOpticalComputationReceiver {

    @Getter
    private IOpticalComputationProvider computationProvider;
    @Getter
    private ObjectHolderMachine objectHolder;

    public ResearchStationMachine(BlockEntityCreationInfo info) {
        super(info, (m) -> new ResearchStationRecipeLogic((ResearchStationMachine) m));
    }

    @Override
    public ResearchStationRecipeLogic getRecipeLogic() {
        return (ResearchStationRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            if (part instanceof ObjectHolderMachine objectHolder) {
                if (objectHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
                    onStructureInvalid();
                    return;
                }
                this.objectHolder = objectHolder;
            }

            part.self()
                    .getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER)
                    .ifPresent(provider -> this.computationProvider = provider);
        }

        // should never happen, but would rather do this than have an obscure NPE
        if (computationProvider == null || objectHolder == null) {
            onStructureInvalid();
        }
    }

    @Override
    public boolean checkPattern() {
        boolean isFormed = super.checkPattern();
        if (isFormed && objectHolder != null && objectHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
            onStructureInvalid();
        }
        return isFormed;
    }

    @Override
    public void onStructureInvalid() {
        computationProvider = null;
        // recheck the ability to make sure it wasn't the one broken
        for (IMultiPart part : getParts()) {
            if (part instanceof ObjectHolderMachine holder) {
                if (holder == objectHolder) {
                    objectHolder.setLocked(false);
                }
            }
        }
        objectHolder = null;
        super.onStructureInvalid();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 164);

        var panelUtil = new GTMultiblockPanelUtil(this);

        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(new ParentWidget<>()
                        .widthRel(0.95f)
                        .heightRel(.45f)
                        .margin(4, 0)
                        .left(3).top(5)
                        .child(new Row()
                                .child(getMainTextPanel(syncManager, 170, 70))))
                .child(new Column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(this, syncManager))
                        .child(GTMuiWidgets.createVoidingButton(this, syncManager)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));

        return panel;
    }

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager, int width, int height) {
        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>()
                .width(width - 6)
                .height(height - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .alignX(Alignment.CenterLeft);
        parentWidget.size(width, height)
                .background(GTGuiTextures.MUI_DISPLAY);

        listWidget.child(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager,
                () -> Component.translatable("gtceu.multiblock.work_paused").withStyle(ChatFormatting.GOLD),
                () -> Component.translatable("gtceu.multiblock.research_station.researching")
                        .withStyle(ChatFormatting.GREEN),
                () -> Component.translatable("gtceu.multiblock.idling").withStyle(ChatFormatting.GRAY)));
        listWidget.child(GTMultiblockTextUtil.addEnergyTierLine(this, syncManager));
        listWidget.child(GTMultiblockTextUtil.addEnergyUsageLine(this, syncManager));
        listWidget.child(GTMultiblockTextUtil.addOutputLines(this, syncManager));
        listWidget.child(GTMultiblockTextUtil.addProgressLinePercentOnly(this, syncManager));
        parentWidget.child(listWidget.left(3).top(3));
        return parentWidget;
    }

    public static class ResearchStationRecipeLogic extends RecipeLogic {

        public ResearchStationRecipeLogic(ResearchStationMachine metaTileEntity) {
            super(metaTileEntity);
        }

        @NotNull
        @Override
        public ResearchStationMachine getMachine() {
            return (ResearchStationMachine) super.getMachine();
        }

        // skip "can fit" checks, it can always fit
        @Override
        protected ActionResult matchRecipe(GTRecipe recipe) {
            var match = matchRecipeNoOutput(recipe);
            if (!match.isSuccess()) return match;

            return matchTickRecipeNoOutput(recipe);
        }

        @Override
        public boolean checkMatchedRecipeAvailable(GTRecipe match) {
            var modified = machine.fullModifyRecipe(match);
            if (modified != null) {
                // What is the point of this
                if (!modified.inputs.containsKey(CWURecipeCapability.CAP) &&
                        !modified.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                    return true;
                }
                var recipeMatch = checkRecipe(modified);
                if (recipeMatch.isSuccess()) {
                    setupRecipe(modified);
                } else {
                    setWaiting(recipeMatch.reason());
                }
                if (lastRecipe != null && getStatus() == Status.WORKING) {
                    lastOriginRecipe = match;
                    lastFailedMatches = null;
                    return true;
                }
            }
            return false;
        }

        protected ActionResult matchRecipeNoOutput(GTRecipe recipe) {
            if (!machine.hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;
            return RecipeHelper.handleRecipe(machine, recipe, IO.IN, recipe.inputs, Collections.emptyMap(), false,
                    true);
        }

        protected ActionResult matchTickRecipeNoOutput(GTRecipe recipe) {
            if (recipe.hasTick()) {
                if (!machine.hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;
                return RecipeHelper.handleRecipe(machine, recipe, IO.IN, recipe.tickInputs, Collections.emptyMap(),
                        false, true);
            }
            return ActionResult.SUCCESS;
        }

        // Handle RecipeIO manually
        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN) {
                // lock the object holder on recipe start
                ObjectHolderMachine holder = getMachine().getObjectHolder();
                holder.setLocked(true);
                return ActionResult.SUCCESS;
            }

            // "replace" the items in the slots rather than outputting elsewhere
            // unlock the object holder
            ObjectHolderMachine holder = getMachine().getObjectHolder();
            if (lastRecipe == null) {
                holder.setLocked(false);
                return ActionResult.SUCCESS;
            }

            holder.setHeldItem(ItemStack.EMPTY);
            ItemStack outputItem = ItemStack.EMPTY;
            var contents = lastRecipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!contents.isEmpty()) {
                outputItem = ItemRecipeCapability.CAP.of(contents.get(0).content).getItems()[0];
            }
            if (!outputItem.isEmpty()) {
                holder.setDataItem(outputItem.copy());
            }
            holder.setLocked(false);
            return ActionResult.SUCCESS;
        }

        @Override
        protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT) {
                return super.handleTickRecipeIO(recipe, io);
            }
            return ActionResult.SUCCESS;
        }
    }
}
