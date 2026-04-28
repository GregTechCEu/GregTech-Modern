package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.editor.LazyEditableMachineUI;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author h3tr
 * @date 2023/7/15
 * @implNote BlockBreakerMachine
 */
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
        environmentalExplosionTrait.setEnableEnvironmentalExplosions(false);
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
                serverLevel.getServer().executeIfPossible(this::updateBreakerSubscription);
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
    public void onNeighborChanged(net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
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
                                net.minecraft.world.level.block.Block.popResource(getLevel(), getBlockPos(), remainder);
                            } else {
                                net.minecraft.world.level.block.Block.popResource(getLevel(),
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
        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(getLevel().getBlockState(pos),
                (ServerLevel) getLevel(), pos, null, null, ItemStack.EMPTY);
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

    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
        updateBreakerSubscription();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    public static BiFunction<Identifier, Integer, Object> EDITABLE_UI_CREATOR = Util
            .memoize((path, inventorySize) -> new LazyEditableMachineUI(
                    () -> BlockBreakerMachineUI.createEditableUI(path, inventorySize)));
}
