package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FisherMachine extends TieredEnergyMachine
                           implements IMuiMachine, IWorkable {

    @SaveField
    protected final NotifiableItemStackHandler cache;
    @Getter
    @Setter
    @SaveField
    protected boolean allowInputFromOutputSideItems;
    @SaveField
    protected final NotifiableItemStackHandler baitHandler;

    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    @Nullable
    protected TickableSubscription batterySubs, fishingSubs;
    @Nullable
    protected ISubscription energySubs, baitSubs;
    private final long energyPerTick;

    private final int inventorySize;

    @Getter
    public final int maxProgress;

    @Getter
    @SaveField
    private int progress = 0;

    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;

    @Getter
    @SaveField
    private boolean active = false;
    public static final int WATER_CHECK_SIZE = 5;
    private static final ItemStack fishingRod = new ItemStack(Items.FISHING_ROD);
    private boolean hasWater = false;

    @Getter
    @SaveField
    @SyncToClient
    protected boolean junkEnabled = true;
    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public FisherMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.maxProgress = calcMaxProgress(tier);
        this.energyPerTick = GTValues.V[tier - 1];
        this.cache = new NotifiableItemStackHandler(this, inventorySize, IO.BOTH, IO.OUT);

        this.baitHandler = new NotifiableItemStackHandler(this, 1, IO.BOTH, IO.IN);
        baitHandler.setFilter(item -> item.is(Items.STRING));

        this.chargerInventory = new CustomItemStackHandler();
        chargerInventory.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));

        autoOutput = AutoOutputTrait.ofItems(this, cache);
        environmentalExplosionTrait.setEnableEnvironmentalExplosions(false);
    }

    public void setWorkingEnabled(boolean enabled) {
        isWorkingEnabled = enabled;
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
    }

    public void setJunkEnabled(boolean enabled) {
        junkEnabled = enabled;
        syncDataHolder.markClientSyncFieldDirty("junkEnabled");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        energySubs = energyContainer.addChangedListener(() -> {
            this.updateBatterySubscription();
            this.updateFishingUpdateSubscription();
        });
        baitSubs = baitHandler.addChangedListener(this::updateFishingUpdateSubscription);
        chargerInventory.setOnContentsChanged(this::updateBatterySubscription);
        this.updateFishingUpdateSubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
        if (baitSubs != null) {
            baitSubs.unsubscribe();
            baitSubs = null;
        }
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        chargerInventory.dropInventoryInWorld(getLevel(), getBlockPos());
        baitHandler.dropInventoryInWorld();
        cache.dropInventoryInWorld();
    }

    public static int calcMaxProgress(int tier) {
        return (int) (800.0 - 170 * ((double) tier - 1.0) + (((double) Math.max(0, tier - 4) / 0.012)));
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////

    public void updateFishingUpdateSubscription() {
        if (drainEnergy(true) && this.baitHandler.getStackInSlot(0).is(Items.STRING) && isWorkingEnabled) {
            fishingSubs = subscribeServerTick(fishingSubs, this::fishingUpdate);
            active = true;
            return;
        } else if (fishingSubs != null) {
            fishingSubs.unsubscribe();
            fishingSubs = null;
            active = false;
        }
        progress = 0;
    }

    private void updateHasWater() {
        for (int x = 0; x < WATER_CHECK_SIZE; x++)
            for (int z = 0; z < WATER_CHECK_SIZE; z++) {
                BlockPos waterCheckPos = getBlockPos().below().offset(x - WATER_CHECK_SIZE / 2, 0,
                        z - WATER_CHECK_SIZE / 2);
                if (!getLevel().getBlockState(waterCheckPos).getFluidState().is(Fluids.WATER)) {
                    hasWater = false;
                    return;
                }
            }
        hasWater = true;
    }

    public void fishingUpdate() {
        if (this.getOffsetTimer() % maxProgress == 0L)
            updateHasWater();

        if (!hasWater) return;

        drainEnergy(false);
        if (progress >= maxProgress) {
            LootTable lootTable = getLevel().getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
            if (!this.junkEnabled) {
                lootTable = getLevel().getServer().getLootData().getLootTable(BuiltInLootTables.FISHING_FISH);
            }

            FishingHook simulatedHook = new FishingHook(EntityType.FISHING_BOBBER, getLevel()) {

                public boolean isOpenWaterFishing() {
                    return true;
                }
            };

            LootParams lootContext = new LootParams.Builder((ServerLevel) getLevel())
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, simulatedHook)
                    .withParameter(LootContextParams.TOOL, fishingRod)
                    .withParameter(LootContextParams.ORIGIN,
                            new Vec3(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()))
                    .create(LootContextParamSets.FISHING);

            NonNullList<ItemStack> generatedLoot = NonNullList.create();
            generatedLoot.addAll(lootTable.getRandomItems(lootContext));

            boolean useBait = false;
            for (ItemStack itemStack : generatedLoot)
                useBait |= tryFillCache(itemStack);

            if (useBait && junkEnabled)
                this.baitHandler.storage.extractItem(0, 1, false);
            else if (useBait)
                this.baitHandler.storage.extractItem(0, 2, false);
            updateFishingUpdateSubscription();
            progress = -1;
        }
        progress++;
    }

    private boolean tryFillCache(ItemStack stack) {
        for (int i = 0; i < cache.getSlots(); i++) {
            if (cache.insertItemInternal(i, stack, false).getCount() < stack.getCount()) {
                return true;
            }
        }
        return false;
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
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false))
            updateBatterySubscription();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = new ModularPanel(getDefinition().getName());

        var outputItemGrid = GTMuiWidgets.createGrid(cache.getSize(), (int) Math.sqrt(cache.getSize()), true, 'i');

        int inputWidth = 18;
        int outputWidth = 18 * outputItemGrid.length;

        int slotHeight = outputItemGrid.length;

        int topMargin = 0;
        if (slotHeight == 2) {
            topMargin = 9;
        } else if (slotHeight > 2) {
            topMargin = 18;
        }

        // input slots + centering gap + output slots

        /**
         * 1 -> 1.5
         * 2 -> 1
         * 3 -> .5
         * 36 - (inputWidth / 2)
         *
         * 1:1 -> 18 + 18 + 36
         * 1:2 -> 18 + 36 + 27
         * 1:3 -> 18 + 54 + 3
         * 2 - input + 2 - output
         */
        int fullWidth = (inputWidth + outputWidth) + (77 - ((inputWidth + outputWidth) / 2));

        int inputShift = switch (tier) {
            case 1 -> 27;
            case 2 -> 27;
            case 3 -> 18;
            case 4 -> 0;
            case 5 -> 0;
            case 6 -> -2;
            default -> 0;
        };

        int padding = switch (tier) {
            case 1 -> 10;
            case 2 -> 7;
            case 3 -> 7;
            case 4 -> 10;
            case 5 -> 5;
            case 6 -> 2;
            default -> 2;
        };

        BooleanSyncValue power = new BooleanSyncValue(() -> active,
                (b) -> active = b);
        syncManager.syncValue("working_enabled", power);

        ItemSlotSyncHandler battery = new ItemSlotSyncHandler(new ModularSlot(getChargerInventory(), 0));
        syncManager.syncValue("battery", battery);

        panel.size(176, 124 + Math.max(36, 18 * slotHeight));

        panel.child(GTMuiWidgets.createTitleBar(getDefinition(), 176, GTGuiTextures.BACKGROUND))
                .child(new Row()
                        .coverChildrenHeight()
                        .width(fullWidth + 16 - inputShift)
                        .left(7 + inputShift)
                        .childPadding(padding)
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .child(new Column()
                                .coverChildrenWidth()
                                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                                .child(new ItemSlot().slot(new ModularSlot(baitHandler, 0))
                                        .background(GTGuiTextures.SLOT, GTGuiTextures.STRING_SLOT_OVERLAY)))
                        .child(new ProgressWidget()
                                .alignY(Alignment.Center)
                                .texture(GTGuiTextures.PROGRESS_BAR_ARROW, 16)
                                .progress(() -> progress / (double) maxProgress))
                        .child(new Column()
                                .coverChildrenWidth()
                                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                                .childIf(!(outputItemGrid.length == 0),
                                        GTMuiMachineUtil.createSlotGroupFromInventory(cache,
                                                "output_item_inv", cache.getSize(), 'i',
                                                syncManager, outputItemGrid)
                                                .alignX(Alignment.CenterRight))
                                .align(Alignment.CenterRight))
                        .top(30 - topMargin))

                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(new Column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(new ToggleButton()
                                .value(new BoolValue.Dynamic(power::getBoolValue, power::setBoolValue))
                                .selectedBackground(GTGuiTextures.BUTTON_POWER[1])
                                .background(GTGuiTextures.BUTTON_POWER[0])
                                .tooltipAutoUpdate(true)
                                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable(
                                        active ? "behaviour.soft_hammer.enabled" :
                                                "behaviour.soft_hammer.disabled")))))
                        .child(new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT,
                                GTGuiTextures.CHARGER_OVERLAY))
                        .child(new ToggleButton()
                                .value(new BoolValue.Dynamic(this::isJunkEnabled, this::setJunkEnabled))
                                .overlay(new ItemDrawable(Items.NAME_TAG))
                                .tooltipAutoUpdate(true)
                                .tooltipBuilder((r) -> {
                                    var lines = LangHandler.getMultiLang("gtceu.gui.fisher_mode.tooltip",
                                            GTValues.VNF[getTier()], GTValues.VNF[getTier()]);
                                    for (var line : lines) {
                                        r.addLine(line);
                                    }
                                })))
                /*
                 * .child(new Column()
                 * .coverChildren()
                 * .rightRel(1.0f)
                 * .reverseLayout(true)
                 * .padding(0, 8, 4, 4)
                 * .bottom(16)
                 * .background(GTGuiTextures.BACKGROUND.getSubArea(0f, 0f, 0.75f, 1.0f))
                 * .childIf(ghostCircuit,
                 * GTMuiWidgets.createCircuitSlotPanel(simpleTieredMachine, panel, syncManager)))
                 */
                .child(GTMuiWidgets.createGTLogo()
                        .right(7).bottom(7 + 78));

        return panel;
    }
}
