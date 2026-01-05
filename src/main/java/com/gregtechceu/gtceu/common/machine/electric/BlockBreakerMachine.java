package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSH;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.syncsystem.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockBreakerMachine extends TieredEnergyMachine
                                 implements IAutoOutputItem, IMuiMachine, IMachineLife, IControllable {

    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected Direction outputFacingItems;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean autoOutputItems;
    @SaveField
    protected final NotifiableItemStackHandler cache;
    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    @Nullable
    protected TickableSubscription autoOutputSubs, batterySubs, breakerSubs;
    @Nullable
    protected ISubscription exportItemSubs, energySubs;
    private final int inventorySize;
    @SyncToClient
    private int blockBreakProgress = 0;
    private float currentHardness;
    private final long energyPerTick;
    public final float efficiencyMultiplier;

    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;

    public BlockBreakerMachine(IMachineBlockEntity holder, int tier, Object... ignoredArgs) {
        super(holder, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.cache = createCacheItemHandler();
        this.chargerInventory = createChargerItemHandler();
        this.energyPerTick = GTValues.V[tier - 1];
        setOutputFacingItems(getFrontFacing().getOpposite());
        this.efficiencyMultiplier = 1.0f - getEfficiencyMultiplier(tier);
    }

    public static float getEfficiencyMultiplier(int tier) {
        float efficiencyMultiplier = 1.0f - 0.2f * (tier - 1.0f);
        // Clamp efficiencyMultiplier
        if (efficiencyMultiplier > 1.0f)
            efficiencyMultiplier = 1.0f;
        else if (efficiencyMultiplier < .1f)
            efficiencyMultiplier = .1f;
        efficiencyMultiplier = 1.0f - efficiencyMultiplier;
        return efficiencyMultiplier;
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected CustomItemStackHandler createChargerItemHandler() {
        var handler = new CustomItemStackHandler();
        handler.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));
        return handler;
    }

    protected NotifiableItemStackHandler createCacheItemHandler() {
        return new NotifiableItemStackHandler(this, inventorySize, IO.BOTH, IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
                serverLevel.getServer().tell(new TickTask(0, this::updateBreakerSubscription));
            }
            exportItemSubs = cache.addChangedListener(this::updateAutoOutputSubscription);
            energySubs = energyContainer.addChangedListener(() -> {
                this.updateBatterySubscription();
                this.updateBreakerSubscription();
            });
            chargerInventory.setOnContentsChanged(this::updateBatterySubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(chargerInventory);
        clearInventory(cache.storage);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateBreakerSubscription();
        updateAutoOutputSubscription();
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////

    public void updateBreakerSubscription() {
        if (drainEnergy(true) && !getLevel().getBlockState(getPos().relative(getFrontFacing())).isAir() &&
                isWorkingEnabled) {
            breakerSubs = subscribeServerTick(breakerSubs, this::breakerUpdate);
        } else if (breakerSubs != null) {
            blockBreakProgress = 0;
            breakerSubs.unsubscribe();
            breakerSubs = null;
        }
    }

    public void breakerUpdate() {
        if (this.blockBreakProgress > 0) {
            --this.blockBreakProgress;
            drainEnergy(false);

            if (blockBreakProgress == 0) {
                var pos = getPos().relative(getFrontFacing());
                var blockState = getLevel().getBlockState(pos);
                float hardness = blockState.getBlock().defaultDestroyTime();
                if (hardness >= 0.0f && Math.abs(hardness - currentHardness) < .5f) {
                    var drops = tryDestroyBlockAndGetDrops(pos);
                    for (ItemStack drop : drops) {
                        var remainder = tryFillCache(drop);
                        if (!remainder.isEmpty()) {
                            if (getOutputFacingItems() == null) {
                                Block.popResource(getLevel(), getPos(), remainder);
                            } else {
                                Block.popResource(getLevel(), getPos().relative(getOutputFacingItems()), remainder);
                            }
                        }
                    }
                }
                this.currentHardness = 0f;
            }
        }

        if (blockBreakProgress == 0) {
            var pos = getPos().relative(getFrontFacing());
            var blockState = getLevel().getBlockState(pos);
            float hardness = blockState.getBlock().defaultDestroyTime();
            boolean skipBlock = blockState.isAir();
            if (hardness >= 0f && !skipBlock) {
                int ticksPerOneDurability = 5;
                int totalTicksPerBlock = (int) Math.ceil(ticksPerOneDurability * hardness);
                this.blockBreakProgress = (int) Math.ceil(totalTicksPerBlock * this.efficiencyMultiplier);
                this.currentHardness = hardness;
            }
        }

        syncDataHolder.markClientSyncFieldDirty("blockBreakProgress");
        updateBreakerSubscription();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        super.clientTick();
        if (blockBreakProgress > 0) {
            var pos = getPos().relative(getFrontFacing());
            var blockState = getLevel().getBlockState(pos);
            getLevel().addDestroyBlockEffect(pos, blockState);
        }
    }

    private List<ItemStack> tryDestroyBlockAndGetDrops(BlockPos pos) {
        List<ItemStack> drops = Block.getDrops(getLevel().getBlockState(pos), (ServerLevel) getLevel(), pos, null, null,
                ItemStack.EMPTY);
        getLevel().destroyBlock(pos, false);
        return drops;
    }

    private ItemStack tryFillCache(ItemStack stack) {
        for (int i = 0; i < cache.getSlots(); i++) {
            if (cache.insertItemInternal(i, stack, true).getCount() == stack.getCount())
                continue;
            return tryFillCache(cache.insertItemInternal(i, stack, false));
        }
        return stack;
    }

    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    //////////////////////////////////////
    // ******* Auto Output *******//
    //////////////////////////////////////
    @Override
    public void setAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        syncDataHolder.markClientSyncFieldDirty("autoOutputItems");
        updateAutoOutputSubscription();
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public void setAllowInputFromOutputSideItems(boolean allow) {}

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        syncDataHolder.markClientSyncFieldDirty("outputFacingItems");
        updateAutoOutputSubscription();
    }

    protected void updateAutoOutputSubscription() {
        var outputFacing = getOutputFacingItems();
        if ((isAutoOutputItems() && !cache.isEmpty()) && outputFacing != null &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getPos(), outputFacing))
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::checkAutoOutput);
        else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void checkAutoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputItems() && getOutputFacingItems() != null)
                cache.exportToNearby(getOutputFacingItems());
            updateAutoOutputSubscription();
        }
    }

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true))
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false))
            updateBatterySubscription();
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        if (facing == getOutputFacingItems()) {
            return false;
        }
        return super.isFacingValid(facing);
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
        updateBreakerSubscription();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    /*
     * public static BiFunction<ResourceLocation, Integer, EditableMachineUI> EDITABLE_UI_CREATOR = Util
     * .memoize((path, inventorySize) -> new EditableMachineUI("misc", path, () -> {
     * var template = createTemplate(inventorySize).createDefault();
     * var energyBar = createEnergyBar().createDefault();
     * var batterySlot = createBatterySlot().createDefault();
     * var energyGroup = new WidgetGroup(0, 0, energyBar.getSize().width, energyBar.getSize().height + 20);
     * batterySlot.setSelfPosition(
     * new Position((energyBar.getSize().width - 18) / 2, energyBar.getSize().height + 1));
     * energyGroup.addWidget(energyBar);
     * energyGroup.addWidget(batterySlot);
     * var group = new WidgetGroup(0, 0,
     * Math.max(energyGroup.getSize().width + template.getSize().width + 4 + 8, 172),
     * Math.max(template.getSize().height + 8, energyGroup.getSize().height + 8));
     * var size = group.getSize();
     * energyGroup.setSelfPosition(new Position(3, (size.height - energyGroup.getSize().height) / 2));
     *
     * template.setSelfPosition(new Position(
     * (size.width - 4 - template.getSize().width) / 2 + 4,
     * (size.height - template.getSize().height) / 2));
     *
     * group.addWidget(energyGroup);
     * group.addWidget(template);
     * return group;
     * }, (template, machine) -> {
     * if (machine instanceof BlockBreakerMachine blockBreakerMachine) {
     * createTemplate(inventorySize).setupUI(template, blockBreakerMachine);
     * createEnergyBar().setupUI(template, blockBreakerMachine);
     * createBatterySlot().setupUI(template, blockBreakerMachine);
     * }
     * }));
     *
     * protected static EditableUI<SlotWidget, BlockBreakerMachine> createBatterySlot() {
     * return new EditableUI<>("battery_slot", SlotWidget.class, () -> {
     * var slotWidget = new SlotWidget();
     * slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY);
     * return slotWidget;
     * }, (slotWidget, machine) -> {
     * slotWidget.setHandlerSlot(machine.chargerInventory, 0);
     * slotWidget.setCanPutItems(true);
     * slotWidget.setCanTakeItems(true);
     * slotWidget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip",
     * GTValues.VNF[machine.getTier()], GTValues.VNF[machine.getTier()]).toArray(new MutableComponent[0]));
     * });
     * }
     *
     * protected static EditableUI<WidgetGroup, BlockBreakerMachine> createTemplate(int inventorySize) {
     * return new EditableUI<>("functional_container", WidgetGroup.class, () -> {
     * int rowSize = (int) Math.sqrt(inventorySize);
     * WidgetGroup main = new WidgetGroup(0, 0, rowSize * 18 + 8, rowSize * 18 + 8);
     * for (int y = 0; y < rowSize; y++) {
     * for (int x = 0; x < rowSize; x++) {
     * int index = y * rowSize + x;
     * SlotWidget slotWidget = new SlotWidget();
     * slotWidget.initTemplate();
     * slotWidget.setSelfPosition(new Position(4 + x * 18, 4 + y * 18));
     * slotWidget.setBackground(GuiTextures.SLOT);
     * slotWidget.setId("slot_" + index);
     * main.addWidget(slotWidget);
     * }
     * }
     * main.setBackground(GuiTextures.BACKGROUND_INVERSE);
     * return main;
     * }, (group, machine) -> {
     * WidgetUtils.widgetByIdForEach(group, "^slot_[0-9]+$", SlotWidget.class, slot -> {
     * var index = WidgetUtils.widgetIdIndex(slot);
     * if (index >= 0 && index < machine.cache.getSlots()) {
     * slot.setHandlerSlot(machine.cache, index);
     * slot.setCanTakeItems(true);
     * slot.setCanPutItems(false);
     * }
     * });
     * });
     * }
     */

    // TODO: Needs EIO type side selection widget when that's done
    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = new ModularPanel(this.getDefinition().getName());
        var slotHeight = (int) Math.sqrt(inventorySize);
        panel
                .size(176, 104 + 18 * slotHeight)
                .child(GTMuiWidgets.createTitleBar(this.getDefinition(), 190))
                .child(new Column()
                        .coverChildren()
                        .child(GTMuiMachineUtil.createSquareSlotGroupFromInventory(this.cache, "output_cache",
                                syncManager))
                        .alignX(Alignment.CENTER)
                        .top(10))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(new Column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(this::isWorkingEnabled, this::setWorkingEnabled,
                                syncManager))
                        .child(createBatterySlot(syncManager))
                        .child(createAutoOutputItemButton(syncManager))
                        .excludeAreaInXei());
        return panel;
    }

    public ToggleButton createAutoOutputItemButton(PanelSyncManager syncManager) {
        BooleanSyncValue itemOutputs = new BooleanSyncValue(this::isAutoOutputItems,
                this::setAutoOutputItems);
        syncManager.syncValue("auto_output_items", itemOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(itemOutputs::getBoolValue, itemOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.item_auto_output",
                        Component.translatable(itemOutputs.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    public ItemSlot createBatterySlot(PanelSyncManager syncManager) {
        ItemSlotSH battery = new ItemSlotSH(new ModularSlot(this.chargerInventory, 0));
        syncManager.syncValue("battery", battery);
        return new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY);
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////
    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (!player.isShiftKeyDown()) {
                if (!hasFrontFacing() || side != getFrontFacing()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        } else if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return isWorkingEnabled ? GuiTextures.TOOL_PAUSE : GuiTextures.TOOL_START;
        } else if (toolTypes.contains(GTToolType.SCREWDRIVER)) {
            if (side == getOutputFacingItems()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
            }
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }

    //////////////////////////////////////
    // ******* Interactions ********//
    //////////////////////////////////////
    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                              BlockHitResult hitResult) {
        if (!playerIn.isShiftKeyDown() && !isRemote()) {
            var tool = playerIn.getItemInHand(hand);
            if (tool.getDamageValue() >= tool.getMaxDamage()) return InteractionResult.PASS;
            if (hasFrontFacing() && gridSide == getFrontFacing()) return InteractionResult.PASS;

            // important not to use getters here, which have different logic
            Direction itemFacing = this.outputFacingItems;

            if (gridSide != itemFacing) {
                // if it is a new side, move it
                setOutputFacingItems(gridSide);
            } else {
                // remove the output facing when wrenching the current one to disable it
                setOutputFacingItems(null);
            }
            return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
        }

        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    protected InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                  BlockHitResult hitResult) {
        var controllable = GTCapabilityHelper.getControllable(getLevel(), getPos(), gridSide);
        if (controllable != null) {
            if (!isRemote()) {
                controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
                playerIn.sendSystemMessage(Component.translatable(controllable.isWorkingEnabled() ?
                        "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"));
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
