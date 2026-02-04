package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldAcceleratorMachine extends TieredEnergyMachine implements IControllable {

    private static final Map<String, Class<?>> blacklistedClasses = new Object2ObjectOpenHashMap<>();
    private static final Object2BooleanFunction<Class<? extends BlockEntity>> blacklistCache = new Object2BooleanOpenHashMap<>();
    private static boolean gatheredClasses = false;

    // Hard-coded blacklist for blockentities
    private static final List<String> blockEntityClassNamesBlackList = new ArrayList<>();

    public static final BooleanProperty RANDOM_TICK_PROPERTY = GTMachineModelProperties.IS_RANDOM_TICK_MODE;

    private static final long blockEntityAmperage = 6;
    private static final long randomTickAmperage = 3;
    // Variables for Random Tick mode optimization
    // limit = ((tier - min) / (max - min)) * 2^tier
    private static final int[] SUCCESS_LIMITS = { 1, 8, 27, 64, 125, 216, 343, 512 };

    private final int speed;
    private final int successLimit;
    private final int randRange;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isRandomTickMode = true;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private boolean active = false;
    private TickableSubscription tickSubs;

    public WorldAcceleratorMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, (TieredEnergyMachine machine) -> {
            long tierVoltage = GTValues.V[machine.getTier()];
            return new NotifiableEnergyContainer(machine, tierVoltage * 256L, tierVoltage, 8, 0L, 0L);
        });
        this.speed = (int) Math.pow(2, tier);
        this.successLimit = SUCCESS_LIMITS[tier - 1];
        this.randRange = (getTier() << 1) + 1;
    }

    public void updateSubscription() {
        if (isWorkingEnabled && drainEnergy(true)) {
            tickSubs = subscribeServerTick(tickSubs, this::update);
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_ACTIVE, true));
            if (!active) {
                active = true;
                syncDataHolder.markClientSyncFieldDirty("active");
            }
        } else if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_ACTIVE, false));
            if (active) {
                active = false;
                syncDataHolder.markClientSyncFieldDirty("active");
            }
        }
    }

    public void update() {
        drainEnergy(false);
        // handle random tick mode
        if (isRandomTickMode) {
            BlockPos cornerPos = new BlockPos(
                    getBlockPos().getX() - getTier(),
                    getBlockPos().getY() - getTier(),
                    getBlockPos().getZ() - getTier());
            int attempts = successLimit * 3;

            for (int i = 0, j = 0; i < successLimit && j < attempts; j++) {
                BlockPos randomPos = cornerPos.offset(
                        GTValues.RNG.nextInt(randRange),
                        GTValues.RNG.nextInt(randRange),
                        GTValues.RNG.nextInt(randRange));
                if (randomPos.getY() > getLevel().getMaxBuildHeight() ||
                        randomPos.getY() < getLevel().getMinBuildHeight() || !getLevel().isLoaded(randomPos) ||
                        randomPos.equals(getBlockPos()))
                    continue;
                if (getLevel().getBlockState(randomPos).isRandomlyTicking()) {
                    getLevel().getBlockState(randomPos).randomTick((ServerLevel) this.getLevel(), randomPos,
                            GTValues.RNG);
                }
                i++;
            }
        } else {
            // else handle block entity mode
            for (Direction dir : GTUtil.DIRECTIONS) {
                BlockEntity blockEntity = this.getLevel().getBlockEntity(this.getBlockPos().relative(dir));
                if (blockEntity != null && canAccelerate(blockEntity)) {
                    tickBlockEntity(blockEntity);
                }
            }
        }
        updateSubscription();
    }

    public boolean drainEnergy(boolean simulate) {
        long toDrain = (isRandomTickMode ? randomTickAmperage : blockEntityAmperage) * GTValues.V[tier];
        long resultEnergy = energyContainer.getEnergyStored() - toDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate) {
                energyContainer.removeEnergy(toDrain);
            }
            return true;
        }
        return false;
    }

    private <T extends BlockEntity> void tickBlockEntity(@NotNull T blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        // noinspection unchecked
        BlockEntityTicker<T> blockEntityTicker = this.getLevel().getBlockState(pos).getTicker(this.getLevel(),
                (BlockEntityType<T>) blockEntity.getType());
        if (blockEntityTicker == null) return;
        for (int i = 0; i < speed - 1; i++) {
            blockEntityTicker.tick(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(),
                    blockEntity);
        }
    }

    private boolean canAccelerate(BlockEntity blockEntity) {
        if (blockEntity instanceof PipeBlockEntity || blockEntity instanceof MetaMachine) return false;

        generateWorldAcceleratorBlacklist();
        final Class<? extends BlockEntity> blockEntityClass = blockEntity.getClass();
        if (blacklistCache.containsKey(blockEntityClass)) {
            return blacklistCache.getBoolean(blockEntityClass);
        }

        for (Class<?> clazz : blacklistedClasses.values()) {
            if (clazz.isAssignableFrom(blockEntityClass)) {
                // Is a subclass, so it cannot be accelerated
                blacklistCache.put(blockEntityClass, false);
                return false;
            }
        }

        blacklistCache.put(blockEntityClass, true);
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            energyContainer.addChangedListener(this::updateSubscription);
            updateSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_WORKING_ENABLED, isWorkingEnabled));
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
        updateSubscription();
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return isWorkingEnabled ? GuiTextures.TOOL_PAUSE : GuiTextures.TOOL_START;
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }

    @Override
    protected @NotNull InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                            BlockHitResult hitResult) {
        if (!isRemote()) {
            isRandomTickMode = !isRandomTickMode;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_RANDOM_TICK_MODE, isRandomTickMode));
            syncDataHolder.markClientSyncFieldDirty("isRandomTickMode");
            playerIn.sendSystemMessage(Component.translatable(isRandomTickMode ?
                    "gtceu.machine.world_accelerator.mode_entity" : "gtceu.machine.world_accelerator.mode_tile"));
            scheduleRenderUpdate();
        }
        return InteractionResult.CONSUME;
    }

    private static void generateWorldAcceleratorBlacklist() {
        if (!gatheredClasses) {
            for (String name : ConfigHolder.INSTANCE.machines.worldAcceleratorBlacklist) {
                if (!blacklistedClasses.containsKey(name)) {
                    try {
                        blacklistedClasses.put(name, Class.forName(name));
                    } catch (ClassNotFoundException ignored) {
                        GTCEu.LOGGER.warn("Could not find class {} for World Accelerator Blacklist!", name);
                    }
                }
            }

            for (String className : blockEntityClassNamesBlackList) {
                try {
                    blacklistedClasses.put(className, Class.forName(className));
                } catch (ClassNotFoundException ignored) {}
            }

            gatheredClasses = true;
        }
    }
}
