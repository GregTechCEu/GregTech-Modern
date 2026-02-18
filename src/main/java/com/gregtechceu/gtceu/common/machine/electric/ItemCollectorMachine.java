package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.ISubscription;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemCollectorMachine extends TieredEnergyMachine
                                  implements IFancyUIMachine, IWorkable {

    @Getter
    private static final int[] INVENTORY_SIZES = { 4, 9, 16, 25, 25 };
    private static final double MOTION_MULTIPLIER = 0.04;
    private static final int BASE_EU_CONSUMPTION = 6;

    @SaveField
    protected final NotifiableItemStackHandler output;

    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    @SaveField
    protected final CustomItemStackHandler filterInventory;

    @Nullable
    protected TickableSubscription batterySubs, collectionSubs;
    @Nullable
    protected ISubscription energySubs;
    private final long energyPerTick;

    private final int inventorySize;

    private AABB aabb;

    @SaveField
    @Getter
    @SyncToClient
    private int range;

    private boolean rangeDirty = false;

    private final int maxRange;

    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;

    @SyncToClient
    @SaveField
    @Getter
    @RerenderOnChanged
    private boolean active = false;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public ItemCollectorMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.inventorySize = INVENTORY_SIZES[Mth.clamp(getTier(), 0, INVENTORY_SIZES.length - 1)];
        this.energyPerTick = (long) BASE_EU_CONSUMPTION * (1L << (tier - 1));
        this.output = createOutputItemHandler();
        this.chargerInventory = createChargerItemHandler();
        this.filterInventory = createFilterItemHandler();
        environmentalExplosionTrait.setEnableEnvironmentalExplosions(false);
        this.autoOutput = AutoOutputTrait.ofItems(this, output);
        maxRange = (int) Math.pow(2, tier + 2);
        range = maxRange;
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

    protected CustomItemStackHandler createFilterItemHandler() {
        var handler = new CustomItemStackHandler();
        handler.setFilter(
                item -> item.is(GTItems.ITEM_FILTER.asItem()) || item.is(GTItems.TAG_FILTER.asItem()));
        return handler;
    }

    protected NotifiableItemStackHandler createOutputItemHandler() {
        return new NotifiableItemStackHandler(this, inventorySize, IO.BOTH, IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;

        if (getLevel() instanceof ServerLevel serverLevel) {

            serverLevel.getServer().tell(new TickTask(0, this::updateCollectionSubscription));
        }

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
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        chargerInventory.dropInventoryInWorld(getLevel(), getBlockPos());
        output.dropInventoryInWorld();
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////

    public void updateCollectionSubscription() {
        if (drainEnergy(true) && isWorkingEnabled) {
            collectionSubs = subscribeServerTick(collectionSubs, this::update);
            setActive(true);
            active = true;
        } else if (collectionSubs != null) {
            collectionSubs.unsubscribe();
            collectionSubs = null;
            active = false;
        }
    }

    public void setActive(boolean active) {
        this.active = active;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_ACTIVE, active));
    }

    public void update() {
        if (drainEnergy(false)) {
            if (aabb == null || rangeDirty) {
                rangeDirty = false;
                BlockPos pos1 = getBlockPos().offset(-range, 0, -range);
                BlockPos pos2 = getBlockPos().offset(range, 2, range);
                this.aabb = AABB.of(BoundingBox.fromCorners(pos1, pos2));
            }
            moveItemsInRange();
            updateCollectionSubscription();
        }
    }

    public void moveItemsInRange() {
        ItemFilter filter = null;
        if (!filterInventory.getStackInSlot(0).isEmpty())
            filter = ItemFilter.loadFilter(filterInventory.getStackInSlot(0));
        BlockPos centerPos = self().getBlockPos().above();

        List<ItemEntity> itemEntities = getLevel().getEntitiesOfClass(ItemEntity.class, aabb);
        for (ItemEntity itemEntity : itemEntities) {
            if (!itemEntity.isAlive()) continue;
            if (filter != null && !filter.test(itemEntity.getItem())) continue;
            double distX = (centerPos.getX() + 0.5) - itemEntity.position().x;
            double distZ = (centerPos.getZ() + 0.5) - itemEntity.position().z;
            double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distZ, 2));
            if (dist >= 0.7f) {
                if (itemEntity.pickupDelay == 32767) continue; // INFINITE_PICKUP_DELAY = 32767
                double dirX = distX / dist;
                double dirZ = distZ / dist;
                Vec3 delta = itemEntity.getDeltaMovement();
                itemEntity.setDeltaMovement(dirX * MOTION_MULTIPLIER * tier, delta.y, dirZ * MOTION_MULTIPLIER * tier);
                itemEntity.setPickUpDelay(1);
            } else {
                ItemStack stack = itemEntity.getItem();
                if (!canFillOutput(stack)) continue;

                ItemStack remainder = fillOutput(stack);
                if (remainder.isEmpty())
                    itemEntity.kill();
                else if (stack.getCount() > remainder.getCount())
                    itemEntity.setItem(remainder);
            }
        }
    }

    private boolean canFillOutput(ItemStack stack) {
        for (int i = 0; i < output.getSlots(); i++) {
            if (output.insertItemInternal(i, stack, true).getCount() < stack.getCount())
                return true;
        }

        return false;
    }

    private ItemStack fillOutput(ItemStack stack) {
        for (int i = 0; i < output.getSlots(); i++) {
            if (output.insertItemInternal(i, stack, true).getCount() < stack.getCount())
                return output.insertItemInternal(i, stack, false);
        }

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

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true))
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false)) {
            updateBatterySubscription();
        }
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaxProgress() {
        return 0;
    }

    public void setRange(int range) {
        this.range = range;
        syncDataHolder.markClientSyncFieldDirty("range");
        rangeDirty = true;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
        updateCollectionSubscription();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    public static BiFunction<ResourceLocation, Integer, EditableMachineUI> EDITABLE_UI_CREATOR = Util
            .memoize((path, inventorySize) -> new EditableMachineUI("misc", path, () -> {
                var template = createTemplate(inventorySize).createDefault();
                var energyBar = createEnergyBar().createDefault();
                var batterySlot = createBatterySlot().createDefault();

                var energyGroup = new WidgetGroup(0, 0, energyBar.getSize().width, energyBar.getSize().height + 20);
                batterySlot.setSelfPosition(
                        new Position((energyBar.getSize().width - 18) / 2, energyBar.getSize().height + 1));
                energyGroup.addWidget(energyBar);
                energyGroup.addWidget(batterySlot);
                var group = new WidgetGroup(0, 0,
                        Math.max(energyGroup.getSize().width + template.getSize().width + 4 + 8, 172),
                        Math.max(template.getSize().height + 8 + 30, energyGroup.getSize().height + 8));
                var size = group.getSize();
                energyGroup.setSelfPosition(new Position(3, (size.height - energyGroup.getSize().height) / 2));

                template.setSelfPosition(new Position(
                        (size.width - energyGroup.getSize().width - 4 - template.getSize().width) / 2 + 2 +
                                energyGroup.getSize().width + 2,
                        (size.height - template.getSize().height) / 2 + 15));

                group.addWidget(energyGroup);
                group.addWidget(template);
                return group;
            }, (template, machine) -> {
                if (machine instanceof ItemCollectorMachine itemCollectorMachine) {
                    createTemplate(inventorySize).setupUI(template, itemCollectorMachine);
                    createEnergyBar().setupUI(template, itemCollectorMachine);
                    createBatterySlot().setupUI(template, itemCollectorMachine);
                    var rangeSelector = new IntInputWidget((template.getSize().width - 80) / 2, 5, 80, 20,
                            itemCollectorMachine::getRange, itemCollectorMachine::setRange);
                    rangeSelector.setMin(1);
                    rangeSelector.setMax(itemCollectorMachine.maxRange);
                    template.addWidget(rangeSelector);
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
            slotWidget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip",
                    GTValues.VNF[machine.getTier()], GTValues.VNF[machine.getTier()]).toArray(new MutableComponent[0]));
        });
    }

    protected static EditableUI<WidgetGroup, ItemCollectorMachine> createTemplate(int inventorySize) {
        return new EditableUI<>("functional_container", WidgetGroup.class, () -> {
            int rowSize = (int) Math.sqrt(inventorySize);
            WidgetGroup main = new WidgetGroup(0, 0, rowSize * 18 + 8 + 25, rowSize * 18 + 8);

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
            filterSlotWidget
                    .setSelfPosition(new Position(4, (main.getSize().height - filterSlotWidget.getSize().height) / 2));
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
}
