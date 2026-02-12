package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockBreakerMachine extends TieredEnergyMachine
                                 implements IFancyUIMachine, IControllable {

    @SaveField
    protected final NotifiableItemStackHandler cache;
    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    @Nullable
    protected TickableSubscription batterySubs, breakerSubs;
    @Nullable
    protected ISubscription energySubs;
    private final int inventorySize;
    @SyncToClient
    private int blockBreakProgress = 0;
    private float currentHardness;
    private final long energyPerTick;
    public final float efficiencyMultiplier;
    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;

    public BlockBreakerMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.cache = createCacheItemHandler();
        this.chargerInventory = createChargerItemHandler();
        this.energyPerTick = GTValues.V[tier - 1];
        this.efficiencyMultiplier = 1.0f - getEfficiencyMultiplier(tier);

        this.autoOutput = AutoOutputTrait.ofItems(this, cache);
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
                serverLevel.getServer().tell(new TickTask(0, this::updateBreakerSubscription));
            }
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
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        chargerInventory.dropInventoryInWorld(getLevel(), getBlockPos());
        cache.dropInventoryInWorld();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateBreakerSubscription();
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////

    public void updateBreakerSubscription() {
        if (drainEnergy(true) && !getLevel().getBlockState(getBlockPos().relative(getFrontFacing())).isAir() &&
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
                var pos = getBlockPos().relative(getFrontFacing());
                var blockState = getLevel().getBlockState(pos);
                float hardness = blockState.getBlock().defaultDestroyTime();
                if (hardness >= 0.0f && Math.abs(hardness - currentHardness) < .5f) {
                    var drops = tryDestroyBlockAndGetDrops(pos);
                    for (ItemStack drop : drops) {
                        var remainder = tryFillCache(drop);
                        if (!remainder.isEmpty()) {
                            if (autoOutput.getItemOutputDirection() == null) {
                                Block.popResource(getLevel(), getBlockPos(), remainder);
                            } else {
                                Block.popResource(getLevel(),
                                        getBlockPos().relative(autoOutput.getItemOutputDirection()),
                                        remainder);
                            }
                        }
                    }
                }
                this.currentHardness = 0f;
            }
        }

        if (blockBreakProgress == 0) {
            var pos = getBlockPos().relative(getFrontFacing());
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
            var pos = getBlockPos().relative(getFrontFacing());
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

    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
        updateBreakerSubscription();
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
                        Math.max(template.getSize().height + 8, energyGroup.getSize().height + 8));
                var size = group.getSize();
                energyGroup.setSelfPosition(new Position(3, (size.height - energyGroup.getSize().height) / 2));

                template.setSelfPosition(new Position(
                        (size.width - 4 - template.getSize().width) / 2 + 4,
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
            slotWidget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip",
                    GTValues.VNF[machine.getTier()], GTValues.VNF[machine.getTier()]).toArray(new MutableComponent[0]));
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
}
