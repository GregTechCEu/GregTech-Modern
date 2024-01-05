package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author h3tr
 * @date 2023/7/15
 * @implNote BlockBreakerMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockBreakerMachine extends TieredEnergyMachine implements IAutoOutputItem, IFancyUIMachine, IMachineModifyDrops {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(BlockBreakerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected Direction outputFacingItems;
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected boolean autoOutputItems;
    @Persisted
    protected final NotifiableItemStackHandler cache;
    @Getter
    @Persisted
    protected final ItemStackTransfer chargerInventory;
    @Nullable
    protected TickableSubscription autoOutputSubs, batterySubs, breakerSubs;
    @Nullable
    protected ISubscription exportItemSubs, energySubs;
    private final int inventorySize;
    @DescSynced
    private int blockBreakProgress = 0;
    private float currentHardness;
    private final long energyPerTick;
    public final float efficiencyMultiplier;

    public BlockBreakerMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.cache = createCacheItemHandler(args);
        this.chargerInventory = createChargerItemHandler();
        this.energyPerTick = GTValues.V[tier - 1];
        setOutputFacingItems(getFrontFacing().getOpposite());
        this.efficiencyMultiplier = 1.0f - getEfficiencyMultiplier(tier);

    }

    public static float getEfficiencyMultiplier(int tier) {
        float efficiencyMultiplier = 1.0f - 0.2f * (tier - 1.0f);
        //Clamp efficiencyMultiplier
        if (efficiencyMultiplier > 1.0f)
            efficiencyMultiplier = 1.0f;
        else if (efficiencyMultiplier < .1f)
            efficiencyMultiplier = .1f;
        efficiencyMultiplier = 1.0f - efficiencyMultiplier;
        return efficiencyMultiplier;
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected ItemStackTransfer createChargerItemHandler(Object... args) {
        var transfer = new ItemStackTransfer();
        transfer.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null);
        return transfer;
    }

    protected NotifiableItemStackHandler createCacheItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, inventorySize, IO.BOTH, IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
                serverLevel.getServer().tell(new TickTask(0, this::updateBreakerUpdateSubscription));
            }
            exportItemSubs = cache.addChangedListener(this::updateAutoOutputSubscription);
            energySubs = energyContainer.addChangedListener(() -> {
                this.updateBatterySubscription();
                this.updateBreakerUpdateSubscription();
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
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, chargerInventory);
        clearInventory(drops, cache.storage);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateBreakerUpdateSubscription();
        updateAutoOutputSubscription();
    }

    //////////////////////////////////////
    //*********     Logic     **********//
    //////////////////////////////////////

    public void updateBreakerUpdateSubscription() {
        if (drainEnergy(true) && !getLevel().getBlockState(getPos().relative(getFrontFacing())).isAir() && getLevel().hasNeighborSignal(getPos())) {
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

        updateBreakerUpdateSubscription();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientTick() {
        super.clientTick();
        if (blockBreakProgress > 0) {
            var pos = getPos().relative(getFrontFacing());
            var blockState = getLevel().getBlockState(pos);
            getLevel().addDestroyBlockEffect(pos, blockState);
        }
    }

    private List<ItemStack> tryDestroyBlockAndGetDrops(BlockPos pos) {
        List<ItemStack> drops = Block.getDrops(getLevel().getBlockState(pos), (ServerLevel) getLevel(), pos, null, null, ItemStack.EMPTY);
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
    //*******     Auto Output    *******//
    //////////////////////////////////////
    @Override
    public void setAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public void setAllowInputFromOutputSideItems(boolean allow) {
    }

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        updateAutoOutputSubscription();
    }

    protected void updateAutoOutputSubscription() {
        var outputFacing = getOutputFacingItems();
        if ((isAutoOutputItems() && !cache.isEmpty()) && outputFacing != null
                && ItemTransferHelper.getItemTransfer(getLevel(), getPos().relative(outputFacing), outputFacing.getOpposite()) != null)
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

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////
    public static BiFunction<ResourceLocation, Integer, EditableMachineUI> EDITABLE_UI_CREATOR = Util.memoize((path, inventorySize) -> new EditableMachineUI("misc", path, () -> {
        var template = createTemplate(inventorySize).createDefault();
        var energyBar = createEnergyBar().createDefault();
        var batterySlot = createBatterySlot().createDefault();
        var energyGroup = new WidgetGroup(0, 0, energyBar.getSize().width, energyBar.getSize().height + 20);
        batterySlot.setSelfPosition(new Position((energyBar.getSize().width - 18) / 2, energyBar.getSize().height + 1));
        energyGroup.addWidget(energyBar);
        energyGroup.addWidget(batterySlot);
        var group = new WidgetGroup(0, 0,
                Math.max(energyGroup.getSize().width + template.getSize().width + 4 + 8, 172),
                Math.max(template.getSize().height + 8, energyGroup.getSize().height + 8));
        var size = group.getSize();
        energyGroup.setSelfPosition(new Position(3, (size.height - energyGroup.getSize().height) / 2));

        template.setSelfPosition(new Position(
                (size.width - energyGroup.getSize().width - 4 - template.getSize().width) / 2 + 2 + energyGroup.getSize().width + 2,
                (size.height - template.getSize().height) / 2));

        group.addWidget(energyGroup);
        group.addWidget(template);
        return group;
    }, (template, machine) -> {
        if (machine instanceof BlockBreakerMachine blockBreakerMachine) {
            createTemplate(inventorySize).setupUI(template, blockBreakerMachine);
            createEnergyBar().setupUI(template, blockBreakerMachine);
            createBatterySlot().setupUI(template, blockBreakerMachine);
        }
    }));

    protected static EditableUI<SlotWidget, BlockBreakerMachine> createBatterySlot() {
        return new EditableUI<>("battery_slot", SlotWidget.class, () -> {
            var slotWidget = new SlotWidget();
            slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY);
            return slotWidget;
        }, (slotWidget, machine) -> {
            slotWidget.setHandlerSlot(machine.chargerInventory, 0);
            slotWidget.setCanPutItems(true);
            slotWidget.setCanTakeItems(true);
            slotWidget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip", GTValues.VNF[machine.getTier()], GTValues.VNF[machine.getTier()]).toArray(new MutableComponent[0]));
        });
    }

    protected static EditableUI<WidgetGroup, BlockBreakerMachine> createTemplate(int inventorySize) {
        return new EditableUI<>("functional_container", WidgetGroup.class, () -> {
            int rowSize = (int) Math.sqrt(inventorySize);
            WidgetGroup main = new WidgetGroup(0, 0, rowSize * 18 + 8, rowSize * 18 + 8);
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    SlotWidget slotWidget = new SlotWidget();
                    slotWidget.initTemplate();
                    slotWidget.setSelfPosition(new Position(4 + x * 18, 4 + y * 18));
                    slotWidget.setBackground(GuiTextures.SLOT);
                    slotWidget.setId("slot_" + index);
                    main.addWidget(slotWidget);
                }
            }
            main.setBackground(GuiTextures.BACKGROUND_INVERSE);
            return main;
        }, (group, machine) -> {
            WidgetUtils.widgetByIdForEach(group, "^slot_[0-9]+$", SlotWidget.class, slot -> {
                var index = WidgetUtils.widgetIdIndex(slot);
                if (index >= 0 && index < machine.cache.getSlots()) {
                    slot.setHandlerSlot(machine.cache, index);
                    slot.setCanTakeItems(true);
                    slot.setCanPutItems(false);
                }
            });
        });
    }

    //////////////////////////////////////
    //*******     Rendering     ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, Set<GTToolType> toolTypes, Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (!player.isCrouching()) {
                if (!hasFrontFacing() || side != getFrontFacing()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        }
        return super.sideTips(player, toolTypes, side);
    }

    //////////////////////////////////////
    //*******    Interactions   ********//
    //////////////////////////////////////
    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!playerIn.isCrouching() && !isRemote()) {
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

            return InteractionResult.CONSUME;
        }

        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }
}
