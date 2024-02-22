package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
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
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author h3tr
 * @date 2023/7/13
 * @implNote FisherMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemCollectorMachine extends TieredEnergyMachine implements IAutoOutputItem, IFancyUIMachine, IMachineModifyDrops {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ItemCollectorMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private static final int[] INVENTORY_SIZES = { 4, 9, 16, 25, 25 };
    private static final double MOTION_MULTIPLIER = 0.04;
    private static final int BASE_EU_CONSUMPTION = 6;

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
    protected final NotifiableItemStackHandler output;

    @Getter
    @Persisted
    protected final ItemStackTransfer chargerInventory;
    @Persisted
    protected final ItemStackTransfer filterInventory;

    @Nullable
    protected TickableSubscription autoOutputSubs, batterySubs, collectionSubs;
    @Nullable
    protected ISubscription exportItemSubs, energySubs;
    private final long energyPerTick;

    private final int inventorySize;

    private AABB aabb;
    @Persisted
    private int range; //TODO: setting range GUI


    public ItemCollectorMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        this.inventorySize = INVENTORY_SIZES[Mth.clamp(getTier(), 0, INVENTORY_SIZES.length - 1)];
        this.energyPerTick = (long) BASE_EU_CONSUMPTION * (1L << (tier - 1));
        this.output = createoutputItemHandler();
        this.chargerInventory = createChargerItemHandler();
        this.filterInventory = createFilterItemHandler();

        range = (int) Math.pow(2,tier+2);
        setOutputFacingItems(getFrontFacing());
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////

    protected ItemStackTransfer createChargerItemHandler() {
        var transfer = new ItemStackTransfer();
        transfer.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null);
        return transfer;
    }

    protected ItemStackTransfer createFilterItemHandler() {
        var transfer = new ItemStackTransfer();
       transfer.setFilter(item -> item.is(GTItems.ITEM_FILTER.asItem())||item.is(GTItems.ORE_DICTIONARY_FILTER.asItem()));
        return transfer;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableItemStackHandler createoutputItemHandler() {
        return new NotifiableItemStackHandler(this, inventorySize, IO.BOTH, IO.OUT);
    }



    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;

        if (getLevel() instanceof ServerLevel serverLevel)
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));

        exportItemSubs = output.addChangedListener(this::updateAutoOutputSubscription);

        energySubs = energyContainer.addChangedListener(() -> {
            this.updateBatterySubscription();
            this.updateCollectionSubscription();

        });
        chargerInventory.setOnContentsChanged(this::updateBatterySubscription);

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
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, chargerInventory);
        clearInventory(drops, output.storage);
    }

    //////////////////////////////////////
    //*********     Logic     **********//
    //////////////////////////////////////

    public void updateCollectionSubscription() {
        if (drainEnergy(true)) {
            collectionSubs = subscribeServerTick(collectionSubs, this::update);
        } else if (collectionSubs != null) {
            collectionSubs.unsubscribe();
            collectionSubs = null;
        }
    }

    public void update() {
        if (drainEnergy(false)) {
            if (aabb == null) {
                BlockPos pos1,pos2;
                pos1 = getPos().offset(-range,0,-range);
                pos2 = getPos().offset(range,2,range);
                this.aabb =  AABB.of(BoundingBox.fromCorners(pos1,pos2));
            }
            moveItemsInRange();
            updateCollectionSubscription();
        }
    }

    public void moveItemsInRange(){
            ItemFilter filter = null;
        if(!filterInventory.getStackInSlot(0).isEmpty())
            filter = ItemFilter.loadFilter(filterInventory.getStackInSlot(0));
        BlockPos centerPos = self().getPos().above();

        List<ItemEntity> itemEntities = getLevel().getEntitiesOfClass(ItemEntity.class, aabb);
        for(ItemEntity itemEntity: itemEntities){
            if(!itemEntity.isAlive()) continue;
            if(filter != null && !filter.test(itemEntity.getItem())) continue;
            double distX = (centerPos.getX() + 0.5) - itemEntity.position().x;
            double distZ = (centerPos.getZ() + 0.5) - itemEntity.position().z;
            double dist = Math.sqrt(Math.pow(distX,2) + Math.pow(distZ,2));
            if(dist>=.7f){
                if(itemEntity.pickupDelay==32767) continue; //INFINITE_PICKUP_DELAY = 32767
                double dirX = distX/dist;
                double dirZ = distZ/dist;
                itemEntity.kjs$setMotionX(dirX*MOTION_MULTIPLIER*tier);
                itemEntity.kjs$setMotionZ(dirZ*MOTION_MULTIPLIER*tier);
                itemEntity.setPickUpDelay(1);

            } else {
                ItemStack stack = itemEntity.getItem();
                if(!canFillOutput(stack)) continue;

                ItemStack remainder = fillOutput(stack);
                if(remainder.isEmpty())
                    itemEntity.kill();
                else if(stack.getCount()>remainder.getCount())
                    itemEntity.setItem(remainder);
            }
        }
    }

    private boolean canFillOutput(ItemStack stack) {
        for (int i = 0; i < output.getSlots(); i++)
            if (output.insertItemInternal(i, stack, true).getCount() < stack.getCount())
                return true;

        return false;
    }

    private ItemStack fillOutput(ItemStack stack) {
        for (int i = 0; i < output.getSlots(); i++)
            if (output.insertItemInternal(i, stack, true).getCount() < stack.getCount())
                return output.insertItemInternal(i, stack, false);

        return ItemStack.EMPTY;
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
    public boolean isAllowInputFromOutputSideItems() { return false;}

    @Override
    public void setAllowInputFromOutputSideItems(boolean allow) {}

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        updateAutoOutputSubscription();
    }

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true))
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void updateAutoOutputSubscription() {
        var outputFacing = getOutputFacingItems();
        if ((isAutoOutputItems() && !output.isEmpty()) && outputFacing != null
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
                output.exportToNearby(getOutputFacingItems());
            updateAutoOutputSubscription();
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false))
            updateBatterySubscription();
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        if (facing == getOutputFacingItems()) {
            return false;
        }
        return super.isFacingValid(facing);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
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
        if (machine instanceof ItemCollectorMachine itemCollectorMachine) {
            createTemplate(inventorySize).setupUI(template, itemCollectorMachine);
            createEnergyBar().setupUI(template, itemCollectorMachine);
            createBatterySlot().setupUI(template, itemCollectorMachine);

        }
    }));

    protected static EditableUI<SlotWidget, ItemCollectorMachine> createBatterySlot() {
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



    protected static EditableUI<WidgetGroup, ItemCollectorMachine> createTemplate(int inventorySize) {
        return new EditableUI<>("functional_container", WidgetGroup.class, () -> {
            int rowSize = (int) Math.sqrt(inventorySize);
            WidgetGroup main = new WidgetGroup(0, 0, rowSize * 18 + 8 + 20, rowSize * 18 + 8);

            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    SlotWidget slotWidget = new SlotWidget();
                    slotWidget.initTemplate();
                    slotWidget.setSelfPosition(new Position(24 + x * 18, 4 + y * 18));
                    slotWidget.setBackground(GuiTextures.SLOT);
                    slotWidget.setId("slot_" + index);
                    main.addWidget(slotWidget);
                }
            }

            SlotWidget filterSlotWidget = new SlotWidget();
            filterSlotWidget.initTemplate();
            filterSlotWidget.setSelfPosition(new Position(4, (main.getSize().height - filterSlotWidget.getSize().height) / 2));
            filterSlotWidget.setBackground(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY);
            filterSlotWidget.setId("filter_slot");
            main.addWidget(filterSlotWidget);
            main.setBackground(GuiTextures.BACKGROUND_INVERSE);
            return main;
        }, (group, machine) -> {
            WidgetUtils.widgetByIdForEach(group, "^slot_[0-9]+$", SlotWidget.class, slot -> {
                var index = WidgetUtils.widgetIdIndex(slot);
                if (index >= 0 && index < machine.output.getSlots()) {
                    slot.setHandlerSlot(machine.output, index);
                    slot.setCanTakeItems(true);
                    slot.setCanPutItems(false);
                }
            });
            WidgetUtils.widgetByIdForEach(group, "^filter_slot$", SlotWidget.class, slot -> {
                slot.setHandlerSlot(machine.filterInventory, 0);
                slot.setCanTakeItems(true);
                slot.setCanPutItems(true);
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
        if (toolTypes.contains(GTToolType.SCREWDRIVER)) {
            if (side == getOutputFacingItems()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
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
