package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedImageWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputBoth;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import com.gregtechceu.gtlib.gui.texture.*;
import com.gregtechceu.gtlib.gui.widget.CycleButtonWidget;
import com.gregtechceu.gtlib.gui.widget.ImageWidget;
import com.gregtechceu.gtlib.gui.widget.LabelWidget;
import com.gregtechceu.gtlib.gui.widget.SlotWidget;
import com.gregtechceu.gtlib.misc.ItemStackTransfer;
import com.gregtechceu.gtlib.side.fluid.FluidTransferHelper;
import com.gregtechceu.gtlib.side.item.ItemTransferHelper;
import com.gregtechceu.gtlib.syncdata.ISubscription;
import com.gregtechceu.gtlib.syncdata.annotation.DescSynced;
import com.gregtechceu.gtlib.syncdata.annotation.Persisted;
import com.gregtechceu.gtlib.syncdata.field.ManagedFieldHolder;
import com.gregtechceu.gtlib.utils.Position;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote SimpleMachine
 * All simple single machines are implemented here.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleTieredMachine extends WorkableTieredMachine implements IAutoOutputBoth, IUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SimpleTieredMachine.class, WorkableTieredMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced
    protected Direction outputFacingItems;
    @Persisted @DescSynced
    protected Direction outputFacingFluids;
    @Getter @Persisted @DescSynced
    protected boolean autoOutputItems;
    @Getter @Persisted @DescSynced
    protected boolean autoOutputFluids;
    @Getter @Setter @Persisted
    protected boolean allowInputFromOutputSideItems;
    @Getter @Setter @Persisted
    protected boolean allowInputFromOutputSideFluids;
    @Getter @Persisted
    protected final ItemStackTransfer chargerInventory;
    @Getter @Persisted
    protected final NotifiableItemStackHandler circuitInventory;
    @Nullable
    protected TickableSubscription autoOutputSubs, batterySubs;
    @Nullable
    protected ISubscription exportItemSubs, exportFluidSubs, energySubs;

    public SimpleTieredMachine(IMachineBlockEntity holder, int tier, Int2LongFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
        this.outputFacingItems = hasFrontFacing() ? getFrontFacing().getOpposite() : Direction.UP;
        this.outputFacingFluids = outputFacingItems;
        this.chargerInventory = createCharterItemHandler(args);
        this.circuitInventory = createCircuitItemHandler(args);
        if (isRemote()) {
            addSyncUpdateListener("outputFacingItems", this::scheduleRender);
            addSyncUpdateListener("outputFacingFluids", this::scheduleRender);
            addSyncUpdateListener("autoOutputItems", this::scheduleRender);
            addSyncUpdateListener("autoOutputFluids", this::scheduleRender);
        }
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected ItemStackTransfer createCharterItemHandler(Object... args) {
        var transfer = new ItemStackTransfer();
        transfer.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null);
        return transfer;
    }

    protected NotifiableItemStackHandler createCircuitItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 1, IO.IN).setFilter(IntCircuitBehaviour::isIntegratedCircuit);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
            }
            updateBatterySubscription();
            exportItemSubs = exportItems.addChangedListener(this::updateAutoOutputSubscription);
            exportFluidSubs = exportFluids.addChangedListener(this::updateAutoOutputSubscription);
            energySubs = energyContainer.addChangedListener(this::updateBatterySubscription);
            chargerInventory.setOnContentsChanged(this::updateBatterySubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }

        if (exportFluidSubs != null) {
            exportFluidSubs.unsubscribe();
            exportFluidSubs = null;
        }

        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
    }

    //////////////////////////////////////
    //*******     Auto Output    *******//
    //////////////////////////////////////


    @Override
    public @Nullable Direction getOutputFacingFluids() {
        if (exportFluids.getTanks() > 0) {
            return outputFacingFluids;
        }
        return null;
    }

    @Override
    public @Nullable Direction getOutputFacingItems() {
        if (exportItems.getSlots() > 0) {
            return outputFacingItems;
        }
        return null;
    }

    @Override
    public void setAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public void setAutoOutputFluids(boolean allow) {
        this.autoOutputFluids = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public void setOutputFacingFluids(@Nullable Direction outputFacing) {
        this.outputFacingFluids = outputFacing;
        updateAutoOutputSubscription();
    }

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        updateAutoOutputSubscription();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    protected void updateAutoOutputSubscription() {
        var outputFacingItems = getOutputFacingItems();
        var outputFacingFluids = getOutputFacingFluids();
        if ((isAutoOutputFluids() && !exportFluids.isEmpty()) && outputFacingItems != null
                && ItemTransferHelper.getItemTransfer(getLevel(), getPos().relative(outputFacingItems), outputFacingItems.getOpposite()) != null
                ||
                (isAutoOutputItems() && !exportItems.isEmpty()) && outputFacingFluids != null
                        && FluidTransferHelper.getFluidTransfer(getLevel(), getPos().relative(outputFacingFluids), outputFacingFluids.getOpposite()) != null) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true)) {
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        } else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false)) {
            updateBatterySubscription();
        }
    }

    protected void autoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputFluids()) {
                exportFluids.exportToNearby(getOutputFacingFluids());
            }
            if (isAutoOutputItems()) {
                exportItems.exportToNearby(getOutputFacingItems());
            }
        }
        updateAutoOutputSubscription();
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        if (facing == getOutputFacingItems() || facing == getOutputFacingFluids()) {
            return false;
        }
        return super.isFacingValid(facing);
    }

    //////////////////////////////////////
    //*******     Interaction    *******//
    //////////////////////////////////////
    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!playerIn.isCrouching() && !isRemote()) {
            var tool = playerIn.getItemInHand(hand);
            if (tool.getDamageValue() >= tool.getMaxDamage()) return InteractionResult.PASS;
            if (hasFrontFacing() && gridSide == getFrontFacing()) return InteractionResult.PASS;

            // important not to use getters here, which have different logic
            Direction itemFacing = this.outputFacingItems;
            Direction fluidFacing = this.outputFacingFluids;

            if (gridSide != itemFacing) {
                // if it is a new side, move it
                setOutputFacingItems(gridSide);
            } else {
                // remove the output facing when wrenching the current one to disable it
                setOutputFacingItems(null);
            }

            if (gridSide != fluidFacing) {
                // if it is a new side, move it
                setOutputFacingFluids(gridSide);
            } else {
                // remove the output facing when wrenching the current one to disable it
                setOutputFacingFluids(null);
            }

            return InteractionResult.CONSUME;
        }

        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!isRemote()) {
            if (gridSide == getOutputFacingItems()) {
                if (isAllowInputFromOutputSideItems()) {
                    setAllowInputFromOutputSideItems(false);
                    playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side.disallow").append(Component.translatable("gtceu.creative.chest.item")));
                } else {
                    setAllowInputFromOutputSideItems(true);
                    playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side.allow").append(Component.translatable("gtceu.creative.chest.item")));
                }
            }
            if (gridSide == getOutputFacingFluids()) {
                if (isAllowInputFromOutputSideFluids()) {
                    setAllowInputFromOutputSideFluids(false);
                    playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side.disallow").append(Component.translatable("gtceu.creative.tank.fluid")));
                } else {
                    setAllowInputFromOutputSideFluids(true);
                    playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side.allow").append(Component.translatable("gtceu.creative.tank.fluid")));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////
    @Override
    public ModularUI createUI(Player entityPlayer) {
        var group = recipeType.createUITemplate(recipeLogic::getProgressPercent, importItems.storage, exportItems.storage, importFluids.storages, exportFluids.storages);
        var size = group.getSize();
        group.setSelfPosition(new Position((176 - size.width) / 2, 20));
        var modularUI = new ModularUI(176, 128 + size.height, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(group)
                .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(new SlotWidget(chargerInventory, 0, 79, 24 + size.height, true, true)
                        .setBackground(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY)
                        .setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip", GTValues.VNF[getTier()], GTValues.VNF[getTier()]).toArray(new MutableComponent[0])))
                .widget(new PredicatedImageWidget(79, (size.height - 18) / 2 + 20, 18, 18, new ResourceTexture("gtceu:textures/gui/base/indicator_no_energy.png"))
                        .setPredicate(recipeLogic::isHasNotEnoughEnergy))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 46 + size.height, true));

        int leftButtonStartX = 7;
        if (exportItems.getSlots() > 0) {
            modularUI.widget(new ToggleButtonWidget(leftButtonStartX, 24 + size.height, 18, 18,
                    GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                    .setShouldUseBaseBackground()
                    .setTooltipText("gtceu.gui.item_auto_output.tooltip"));
            leftButtonStartX += 18;
        }
        if (exportFluids.getTanks() > 0) {
            modularUI.widget(new ToggleButtonWidget(leftButtonStartX, 24 + size.height, 18, 18,
                    GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                    .setShouldUseBaseBackground()
                    .setTooltipText("gtceu.gui.fluid_auto_output.tooltip"));
            leftButtonStartX += 18;
        }

        modularUI.widget(new CycleButtonWidget(leftButtonStartX, 24 + size.height, 18, 18, getMaxOverclockTier() + 1,
                index -> new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture(GTValues.VNF[index])), index -> {
            overclockTier = index;
            if (!isRemote()) {
                recipeLogic.markLastRecipeDirty();
            }
        }).setIndexSupplier(() -> overclockTier).setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.overclock.description").toArray(new MutableComponent[0])));

        if (exportItems.storage.getSlots() + exportFluids.storages.length <= 9) {
            SlotWidget circuitSlot = new SlotWidget(circuitInventory.storage, 0, 124, 24 + size.height, true, true)
                    .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, getCircuitSlotOverlay()));
            modularUI.widget(getCircuitSlotTooltip(circuitSlot))
                    .widget(new ImageWidget(152, 24 + size.height, 18, 18, GTValues.XMAS.get() ? GuiTextures.GREGTECH_LOGO_XMAS : GuiTextures.GREGTECH_LOGO))
                    .widget(new PredicatedButtonWidget(115, 24 + size.height, 9, 9, GuiTextures.BUTTON_INT_CIRCUIT_PLUS, clickData -> {
                        if (!clickData.isRemote) {
                            ItemStack stack = circuitInventory.storage.getStackInSlot(0).copy();
                            if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                                IntCircuitBehaviour.adjustConfiguration(stack, clickData.isShiftClick ? 5 : 1);
                                circuitInventory.storage.setStackInSlot(0, stack);
                            }
                        }
                    }).setPredicate(() -> IntCircuitBehaviour.isIntegratedCircuit(circuitInventory.storage.getStackInSlot(0))))
                    .widget(new PredicatedButtonWidget(115, 33 + size.height, 9, 9, GuiTextures.BUTTON_INT_CIRCUIT_MINUS, clickData -> {
                        if (!clickData.isRemote) {
                            ItemStack stack = circuitInventory.storage.getStackInSlot(0).copy();
                            if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                                IntCircuitBehaviour.adjustConfiguration(stack, clickData.isShiftClick ? -5 : -1);
                                circuitInventory.storage.setStackInSlot(0, stack);
                            }
                        }
                    }).setPredicate(() -> IntCircuitBehaviour.isIntegratedCircuit(circuitInventory.storage.getStackInSlot(0))));
        }
        return modularUI;
    }

    // Method provided to override
    protected SlotWidget getCircuitSlotTooltip(SlotWidget widget) {
        widget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.configurator_slot.tooltip").toArray(new MutableComponent[0]));
        return widget;
    }

    // Method provided to override
    protected IGuiTexture getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    //////////////////////////////////////
    //*******     Rendering     ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.WRENCH) {
            if (!player.isCrouching()) {
                if (!hasFrontFacing() || side != getFrontFacing()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        }
        if (toolType == GTToolType.SCREWDRIVER) {
            if (side == getOutputFacingItems() || side == getOutputFacingFluids()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
            }
        }
        return super.sideTips(player, toolType, side);
    }
}
