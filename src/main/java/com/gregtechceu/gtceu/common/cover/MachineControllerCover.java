package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.cover.data.ControllerMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.SliderWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineControllerCover extends CoverBehavior implements IMuiCover {// IUICover {

    @SaveField
    @Getter
    private boolean isInverted = false;

    @SaveField
    @Getter
    private int minRedstoneStrength = 1;

    @SaveField
    @SyncToClient
    @Getter
    @Nullable
    private ControllerMode controllerMode = ControllerMode.MACHINE;

    @Getter
    @Accessors(fluent = true)
    @SaveField
    private boolean preventPowerFail = false;

    public MachineControllerCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && !getAllowedModes().isEmpty();
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);

        var allowedModes = getAllowedModes();
        setControllerMode(allowedModes.isEmpty() ? null : allowedModes.get(0));
    }

    @Override
    public void onRemoved() {
        super.onRemoved();

        resetCurrentControllable();
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);

        updateInput();
    }

    public void setControllerMode(@Nullable ControllerMode controllerMode) {
        resetCurrentControllable();

        this.controllerMode = controllerMode;
        syncDataHolder.markClientSyncFieldDirty("filterMode");

        updateInput();
    }

    public void setMinRedstoneStrength(int minRedstoneStrength) {
        this.minRedstoneStrength = minRedstoneStrength;
        updateInput();
    }

    public void setInverted(boolean inverted) {
        isInverted = inverted;
        updateInput();
    }

    ///////////////////////////////////////////////////
    // *********** CONTROLLER LOGIC ***********//
    ///////////////////////////////////////////////////

    @Nullable
    private IControllable getControllable(@Nullable Direction side) {
        if (side == null) {
            return GTCapabilityHelper.getControllable(coverHolder.getLevel(), coverHolder.getBlockPos(), null);
        }

        if (coverHolder.getCoverAtSide(side) instanceof IControllable cover) {
            return cover;
        } else {
            return null;
        }
    }

    private void updateInput() {
        if (controllerMode == null)
            return;

        IControllable controllable = getControllable(controllerMode.side);
        if (controllable != null) {
            controllable.setWorkingEnabled(shouldAllowWorking() && doOthersAllowWorking());
        }
    }

    private void resetCurrentControllable() {
        if (controllerMode == null)
            return;

        IControllable controllable = getControllable(controllerMode.side);
        if (controllable != null) {
            controllable.setWorkingEnabled(doOthersAllowWorking());
        }
    }

    private boolean shouldAllowWorking() {
        boolean shouldAllowWorking = getInputSignal() < minRedstoneStrength;

        return isInverted != shouldAllowWorking;
    }

    private boolean doOthersAllowWorking() {
        return coverHolder.getCovers().stream()
                .filter(cover -> this.attachedSide != cover.attachedSide)
                .filter(cover -> cover instanceof MachineControllerCover)
                .filter(cover -> ((MachineControllerCover) cover).controllerMode == this.controllerMode)
                .allMatch(cover -> ((MachineControllerCover) cover).shouldAllowWorking());
    }

    public List<ControllerMode> getAllowedModes() {
        return Arrays.stream(ControllerMode.values())
                .filter(mode -> mode.side != this.attachedSide)
                .filter(mode -> getControllable(mode.side) != null)
                .collect(Collectors.toList());
    }

    private int getInputSignal() {
        Level level = coverHolder.getLevel();
        BlockPos sourcePos = coverHolder.getBlockPos().relative(attachedSide);

        return level.getSignal(sourcePos, attachedSide);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public ModularPanel<?> buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        EnumSyncValue<ControllerMode> controllerModeValue = new EnumSyncValue<>(ControllerMode.class,
                this::getControllerMode, this::setControllerMode);

        syncManager.syncValue("controllerMode", controllerModeValue);

        return ModularPanel.defaultPanel(coverDefinition.getId().getPath(), 176, 245)
                .child(GTMuiWidgets.createTitleBar(this.self().getAttachItem(), 176, GTGuiTextures.BACKGROUND))
                .child(Flow.col().top(7).margin(7, 0)
                        .childPadding(2)
                        .widthRel(1.0f)
                        .coverChildrenHeight()

                        .child(coverUIRow()
                                .child(new ToggleButton()
                                        .size(16).left(0)
                                        .value(new BooleanSyncValue(this::isInverted, ($) -> this.setInverted(true)))
                                        .overlay(GTGuiTextures.OVERLAY_REDSTONE_ON))
                                .child(IKey.lang("cover.enable_with_redstone").asWidget()
                                        .heightRel(1.0f).left(20)))
                        .child(coverUIRow()
                                .child(new ToggleButton()
                                        .size(16).left(0)
                                        .value(new BooleanSyncValue(() -> !this.isInverted(),
                                                ($) -> this.setInverted(false)))
                                        .overlay(GTGuiTextures.OVERLAY_REDSTONE_OFF))
                                .child(IKey.lang("cover.disable_with_redstone").asWidget()
                                        .heightRel(1.0f).left(20)))
                        .child(coverUIRow()
                                .child(new ToggleButton()
                                        .size(16).left(0)
                                        .value(new BooleanSyncValue(() -> preventPowerFail,
                                                bool -> preventPowerFail = bool))
                                        .overlay(GTGuiTextures.CIRCUIT_OVERLAY))
                                .child(IKey.lang("cover.machine_controller.suspend_powerfail").asWidget()
                                        .heightRel(1.0f).left(20)))
                        .child(coverUIRow()
                                .child(IKey
                                        .dynamic(() -> Component.translatable("cover.machine_controller.redstone",
                                                redstoneSignalOutput))
                                        .asWidget()
                                        .height(16).leftRel(0f)))
                        .child(coverUIRow()
                                .child(new SliderWidget()
                                        .background(GTGuiTextures.FLUID_SLOT)
                                        .widthRel(0.9f)
                                        .height(16)
                                        .leftRel(0.5f)
                                        .bounds(0, 15)
                                        .stopper(1.0)
                                        .value(new DoubleSyncValue(() -> (double) redstoneSignalOutput,
                                                v -> redstoneSignalOutput = (int) v))))
                        // Separating line
                        .child(coverUIRow().child(new Rectangle().color(UI_TEXT_COLOR).asWidget()
                                .height(1).widthRel(0.9f).leftRel(0.5f)).margin(0, 2))

                        .child(coverUIRow().child(IKey.lang("cover.machine_controller.control").asWidget()
                                .height(16)))

                        // Controlling selector
                        .child(coverUIRow()
                                .child(modeButton(controllerModeValue, ControllerMode.MACHINE).bottom(0))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_UP, IKey.str("U")))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_DOWN, IKey.str("D")))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_NORTH, IKey.str("N")))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_SOUTH, IKey.str("S")))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_EAST, IKey.str("E")))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_WEST, IKey.str("W")))))
                .bindPlayerInventory();
    }

    private Flow modeColumn(EnumSyncValue<ControllerMode> syncValue, ControllerMode mode, IKey title) {
        return Flow.column().width(18).height(28)
                .child(title.asWidget().height(10).leftRel(0.5f))
                .child(modeButton(syncValue, mode).bottom(0));
    }

    private Widget<?> modeButton(EnumSyncValue<ControllerMode> syncValue, ControllerMode mode) {
        IControllable controllable = getControllable(mode.side);
        if (controllable == null) {
            // Nothing to control, put a placeholder widget
            // 3 states possible here:
            IKey detail;
            if (mode.side == attachedSide) {
                // our own side, we can't control ourselves
                detail = IKey.lang("cover.machine_controller.this_cover");
            } else if (mode.side != null) {
                // some potential cover that either doesn't exist or isn't controllable
                detail = IKey.lang("cover.machine_controller.cover_not_controllable");
            } else {
                // cover holder is not controllable
                detail = IKey.lang("cover.machine_controller.machine_not_controllable");
            }

            return GuiTextures.MC_BUTTON.asWidget().size(18)
                    .overlay(GTGuiTextures.BUTTON_CROSS)
                    .tooltip(t -> t.addLine(IKey.lang(mode.localeName)).addLine(detail));
        }

        ItemStack stack;
        if (controllerMode == null) {
            stack = ItemStack.EMPTY;
        } else {
            if (mode == ControllerMode.MACHINE && coverHolder instanceof MachineCoverContainer coverContainer) {
                stack = coverContainer.getMachine().getDefinition().asStack();
            } else {
                // this can't be null because we already checked IControllable, and it was not null
                // noinspection ConstantConditions
                stack = coverHolder.getCoverAtSide(mode.side).getAttachItem().copy();
            }
        }

        return new ToggleButton().size(18)
                .value(boolValueOf(syncValue, mode))
                .overlay(new ItemDrawable(stack).asIcon().size(16))
                .tooltip(t -> t.addLine(IKey.lang(mode.localeName))
                        .addLine(IKey.lang(stack.getHoverName())));
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putBoolean("inverted", isInverted);
        tag.putInt("redstoneLvl", minRedstoneStrength);
        tag.putBoolean("preventPowerfail", preventPowerFail);
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setInverted(tag.getBoolean("inverted"));
        setMinRedstoneStrength(tag.getInt("redstoneLvl"));
        preventPowerFail = tag.getBoolean("preventPowerfail");
        super.pasteConfig(player, tag);
    }
}
