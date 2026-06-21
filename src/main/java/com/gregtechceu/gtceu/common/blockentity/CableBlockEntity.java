package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.particle.GTOverheatParticle;
import com.gregtechceu.gtceu.client.particle.GTParticleManager;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.pipelike.cable.*;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class CableBlockEntity extends PipeBlockEntity<Insulation, WireProperties> implements IDataInfoProvider {

    protected WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);

    @OnlyIn(Dist.CLIENT)
    private GTOverheatParticle particle;
    public static final int meltTemp = 3000;

    private final EnumMap<Direction, EnergyNetHandler> handlers = new EnumMap<>(Direction.class);
    private final PerTickLongCounter maxVoltageCounter = new PerTickLongCounter();
    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter();
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter();
    private EnergyNetHandler defaultHandler;
    private int heatQueue;
    @Getter
    @SaveField
    @SyncToClient
    private int temperature = getDefaultTemp();
    private TickableSubscription heatSubs;

    public CableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
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
            this.heatSubs = subscribeServerTick(this::updateHeat);
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

    public static int getDefaultTemp() {
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

    @OnlyIn(Dist.CLIENT)
    public boolean isParticleAlive() {
        return particle != null && particle.isAlive();
    }

    @OnlyIn(Dist.CLIENT)
    public void createParticle() {
        particle = new GTOverheatParticle(this, meltTemp, getPipeType().isCable());
        GTParticleManager.INSTANCE.addEffect(particle);
    }

    @OnlyIn(Dist.CLIENT)
    public void killParticle() {
        if (isParticleAlive()) {
            particle.setExpired();
            particle = null;
        }
    }

    public void applyHeat(int amount) {
        heatQueue += amount;
        if (!getLevel().isClientSide && heatSubs == null && temperature + heatQueue > getDefaultTemp()) {
            subscribeHeat();
        }
    }

    private boolean updateHeat() {
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

        if (getPipeType().isCable() && temperature >= 1500 && GTValues.RNG.nextFloat() < 0.1f) {
            // insulation melted
            uninsulate();
            return false;
        }

        if (heatQueue <= 0) {
            // otherwise cool down
            setTemperature((int) (temperature - Math.pow(temperature - getDefaultTemp(), 0.35f)));
        }
        heatQueue = 0;
        return true;
    }

    private void uninsulate() {
        int oldTemperature = temperature;
        setTemperature(getDefaultTemp());

        TagPrefix uninsulatedPrefix = getPipeType().getUninsulated().tagPrefix;
        CableBlock newBlock = GTMaterialBlocks.CABLE_BLOCKS.get(uninsulatedPrefix, getPipeBlock().material).get();
        level.setBlockAndUpdate(getBlockPos(), newBlock.defaultBlockState());

        CableBlockEntity newCable = (CableBlockEntity) level.getBlockEntity(getBlockPos());
        if (newCable != null) { // should never be null
            newCable.setTemperature(oldTemperature);
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
        syncDataHolder.markClientSyncFieldDirty("temperature");
        level.getLightEngine().checkBlock(worldPosition);
    }

    @ClientFieldChangeListener(fieldName = "temperature")
    public void onTemperatureUpdated() {
        if (temperature <= getDefaultTemp()) {
            if (isParticleAlive()) {
                particle.setExpired();
            }
        } else {
            if (!isParticleAlive()) {
                createParticle();
            }
            particle.setTemperature(temperature);
        }

        if (this.temperature >= meltTemp) {
            float xPos = Direction.UP.getStepX() * 0.76f + getBlockPos().getX() + 0.25f;
            float yPos = Direction.UP.getStepY() * 0.76f + getBlockPos().getY() + 0.25f;
            float zPos = Direction.UP.getStepZ() * 0.76f + getBlockPos().getZ() + 0.25f;

            float horizontalDirection = getLevel().random.nextFloat() * 2 * Mth.PI;
            float xSpd = Mth.sin(horizontalDirection) * 0.1f;
            float ySpd = Direction.UP.getStepY() * 0.1f + 0.2f + 0.1f * getLevel().random.nextFloat();
            float zSpd = Mth.cos(horizontalDirection) * 0.1f;

            level.addParticle(ParticleTypes.SMOKE,
                    xPos + level.random.nextFloat() * 0.5f,
                    yPos + level.random.nextFloat() * 0.5f,
                    zPos + level.random.nextFloat() * 0.5f,
                    xSpd, ySpd, zSpd);
        }
    }

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
    public boolean hasCorrectAction(ItemStack stack) {
        return stack.canPerformAction(GTItemAbilities.WIRE_CUTTER_CONNECT);
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
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
