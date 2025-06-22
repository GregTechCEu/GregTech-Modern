package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.pipelike.cable.*;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CableBlockEntity extends PipeBlockEntity<Insulation, WireProperties> implements IDataInfoProvider {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CableBlockEntity.class,
            PipeBlockEntity.MANAGED_FIELD_HOLDER);

    protected WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);

    private static final int meltTemp = 3000;

    private final EnumMap<Direction, EnergyNetHandler> handlers = new EnumMap<>(Direction.class);
    private final PerTickLongCounter maxVoltageCounter = new PerTickLongCounter();
    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter();
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter();
    private EnergyNetHandler defaultHandler;
    private int heatQueue;
    @Getter
    @Persisted
    @DescSynced
    private int temperature = getDefaultTemp();
    private TickableSubscription heatSubs;

    public CableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static CableBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new CableBlockEntity(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_ENERGY_CONTAINER) {
            var container = getEnergyContainer(side);
            if (container != null) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> container));
            }
        } else if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(this::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof CableBlockEntity) {
                return false;
            }
            return GTCapabilityHelper.getEnergyContainer(level, getBlockPos().relative(side), side.getOpposite()) !=
                    null;
        }
        return false;
    }

    @Nullable
    private EnergyNet getEnergyNet() {
        if (!(level instanceof ServerLevel serverLevel))
            return null;
        EnergyNet currentEnergyNet = this.currentEnergyNet.get();
        if (currentEnergyNet != null && currentEnergyNet.isValid() &&
                currentEnergyNet.containsNode(getBlockPos()))
            return currentEnergyNet; // return current net if it is still valid
        LevelEnergyNet worldENet = LevelEnergyNet.getOrCreate(serverLevel);
        currentEnergyNet = worldENet.getNetFromPos(getBlockPos());
        if (currentEnergyNet != null) {
            this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
        }
        return currentEnergyNet;
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            EnergyNet current = getEnergyNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (EnergyNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    @Nullable
    public IEnergyContainer getEnergyContainer(@Nullable Direction side) {
        if (side != null && !isConnected(side)) return null;
        // the EnergyNetHandler can only be created on the server, so we have an empty placeholder for the client
        if (isRemote()) return IEnergyContainer.DEFAULT;
        if (handlers.isEmpty())
            initHandlers();
        checkNetwork();
        return handlers.getOrDefault(side, defaultHandler);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    private void initHandlers() {
        EnergyNet net = getEnergyNet();
        if (net == null) {
            return;
        }
        for (Direction facing : GTUtil.DIRECTIONS) {
            handlers.put(facing, new EnergyNetHandler(net, this, facing));
        }
        defaultHandler = new EnergyNetHandler(net, this, null);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            setTemperature(temperature);
            if (temperature > getDefaultTemp()) {
                subscribeHeat();
            }
        }
    }

    private void subscribeHeat() {
        if (this.heatSubs == null) {
            this.heatSubs = subscribeServerTick(this::update);
        }
    }

    private void unsubscribeHeat() {
        if (this.heatSubs != null) {
            this.unsubscribe(this.heatSubs);
            this.heatSubs = null;
        }
    }

    public CableBlock getPipeBlock() {
        return (CableBlock) super.getPipeBlock();
    }

    public double getAverageAmperage() {
        return averageAmperageCounter.getAverage(getLevel());
    }

    public long getCurrentMaxVoltage() {
        return maxVoltageCounter.get(getLevel());
    }

    public double getAverageVoltage() {
        return averageVoltageCounter.getAverage(getLevel());
    }

    public long getMaxAmperage() {
        return getNodeData().getAmperage();
    }

    public long getMaxVoltage() {
        return getNodeData().getVoltage();
    }

    public int getDefaultTemp() {
        return 293;
    }

    public static int getMeltTemp() {
        return meltTemp;
    }

    /**
     * Should only be called internally
     *
     * @return if the cable should be destroyed
     */
    public boolean incrementAmperage(long amps, long voltage) {
        if (voltage > maxVoltageCounter.get(getLevel())) {
            maxVoltageCounter.set(getLevel(), voltage);
        }
        averageVoltageCounter.increment(getLevel(), voltage * amps);
        averageAmperageCounter.increment(getLevel(), amps);

        int dif = GTMath.saturatedCast(averageAmperageCounter.getLast(getLevel()) - getMaxAmperage());
        if (dif > 0) {
            applyHeat(dif * 40);
            return true;
        }

        return false;
    }

    public void applyHeat(int amount) {
        heatQueue += amount;
        if (!level.isClientSide && heatSubs == null && temperature + heatQueue > getDefaultTemp()) {
            subscribeHeat();
        }
    }

    private boolean update() {
        if (heatQueue > 0) {
            // if received heat from overvolting or overamping, add heat
            setTemperature(temperature + heatQueue);
        }

        if (temperature >= meltTemp) {
            // cable melted
            level.setBlockAndUpdate(worldPosition, Blocks.FIRE.defaultBlockState());
            return false;
        }

        if (temperature <= getDefaultTemp()) {
            unsubscribeHeat();
            return false;
        }

        if (getPipeType().insulationLevel >= 0 && temperature >= 1500 && GTValues.RNG.nextFloat() < 0.1) {
            // insulation melted
            uninsulate();
            return false;
        }

        if (heatQueue == 0) {
            // otherwise cool down
            setTemperature((int) (temperature - Math.pow(temperature - getDefaultTemp(), 0.35)));
        } else {
            heatQueue = 0;
        }
        return true;
    }

    private void uninsulate() {
        int temp = temperature;
        setTemperature(getDefaultTemp());
        int index = getPipeType().insulationLevel;
        CableBlock newBlock = GTMaterialBlocks.CABLE_BLOCKS
                .get(Insulation.values()[index].tagPrefix, getPipeBlock().material)
                .get();
        level.setBlockAndUpdate(getBlockPos(), newBlock.defaultBlockState());
        CableBlockEntity newCable = (CableBlockEntity) level.getBlockEntity(getBlockPos());
        if (newCable != null) { // should never be null
            newCable.setTemperature(temp);
            newCable.subscribeHeat();
            for (Direction facing : GTUtil.DIRECTIONS) {
                if (isConnected(facing)) {
                    newCable.setConnection(facing, true, true);
                }
            }
            newCable.setChanged();
            // force a block rerender
            newCable.scheduleRenderUpdate();
        }
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        level.getLightEngine().checkBlock(worldPosition);
        if (!level.isClientSide && temperature >= meltTemp) {
            var facing = Direction.UP;
            float xPos = facing.getStepX() * 0.76F + worldPosition.getX() + 0.25F;
            float yPos = facing.getStepY() * 0.76F + worldPosition.getY() + 0.25F;
            float zPos = facing.getStepZ() * 0.76F + worldPosition.getZ() + 0.25F;

            float ySpd = facing.getStepY() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
            float temp = GTValues.RNG.nextFloat() * 2 * (float) Math.PI;
            float xSpd = (float) Math.sin(temp) * 0.1F;
            float zSpd = (float) Math.cos(temp) * 0.1F;

            ((ServerLevel) level).sendParticles(ParticleTypes.SMOKE,
                    xPos + GTValues.RNG.nextFloat() * 0.5F,
                    yPos + GTValues.RNG.nextFloat() * 0.5F,
                    zPos + GTValues.RNG.nextFloat() * 0.5F,
                    0,
                    xSpd, ySpd, zSpd, 1);
        }
    }

    public static void onBlockEntityRegister(BlockEntityType<CableBlockEntity> cableBlockEntityBlockEntityType) {}

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    @Override
    public ResourceTexture getPipeTexture(boolean isBlock) {
        return isBlock ? GuiTextures.TOOL_WIRE_CONNECT : GuiTextures.TOOL_WIRE_BLOCK;
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public @NotNull List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        List<Component> list = new ArrayList<>();

        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            list.add(Component.translatable("behavior.portable_scanner.eu_per_sec",
                    Component.translatable(FormattingUtil.formatNumbers(getAverageVoltage()))
                            .withStyle(ChatFormatting.RED)));
            list.add(Component.translatable("behavior.portable_scanner.amp_per_sec",
                    Component.translatable(FormattingUtil.formatNumbers(getAverageAmperage()))
                            .withStyle(ChatFormatting.RED)));
        }

        return list;
    }
}
