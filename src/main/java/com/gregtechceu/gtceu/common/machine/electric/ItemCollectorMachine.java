package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemCollectorMachine extends TieredEnergyMachine
                                  implements IMuiMachine, IWorkable {

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
    // ******* Rendering ********//
    //////////////////////////////////////
    // TODO(Onion): fix the gui stuff for this
    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new ModularPanel(getDefinition().getName())
                .height(220)
                .child(GTMuiWidgets.createTitleBar(getDefinition(), 174))
                .bindPlayerInventory()
                .child(Flow.column()
                        .coverChildrenHeight()
                        .widthRel(1)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(2)
                                .margin(5)
                                .horizontalCenter()
                                .child(new TextWidget<>(IKey.lang("gtceu.gui.item_collector.range")))
                                .child(new TextFieldWidget()
                                        .setNumbers(1, maxRange)
                                        .value(SyncHandlers.intNumber(this::getRange, this::setRange))))
                        .child(Flow.row()
                                .coverChildrenHeight()
                                .widthRel(1)
                                .child(new ItemSlot()
                                        .slot(filterInventory, 0)
                                        .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY)
                                        .margin(7))
                                .child(GTMuiMachineUtil
                                        .createSquareSlotGroupFromInventory(output, "main_inv", syncManager)
                                        .horizontalCenter())))
                .child(new Column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .excludeAreaInXei()
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(this::isWorkingEnabled, this::setWorkingEnabled,
                                syncManager))
                        .child(GTMuiWidgets.createBatterySlot(getChargerInventory(), 0, syncManager))
                        .child(GTMuiWidgets.createAutoOutputItemButton(this.autoOutput, syncManager)));
    }
}
