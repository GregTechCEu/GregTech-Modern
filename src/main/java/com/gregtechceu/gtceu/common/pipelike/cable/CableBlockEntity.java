package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.particle.GTOverheatParticle;
import com.gregtechceu.gtceu.client.particle.GTParticleManager;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.pipelike.cable.*;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import brachy.modularui.drawable.UITexture;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CableBlockEntity extends PipeBlockEntity<CableSegmentProperties> implements IDataInfoProvider {

    @OnlyIn(Dist.CLIENT)
    private @Nullable GTOverheatParticle particle;
    @Getter
    private static final int meltTemp = 3000;

    private final PerTickLongCounter maxVoltageCounter = new PerTickLongCounter();
    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter();
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter();

    private int heatQueue;
    @Getter
    @SaveField
    @SyncToClient
    private int temperature = getDefaultTemp();
    private @Nullable TickableSubscription heatSubs;

    public CableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, GTPipeNetworks.ENERGY);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_ENERGY_CONTAINER) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canPipesConnect(Direction side, PipeBlockEntity<CableSegmentProperties> other) {
        return other instanceof CableBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(Direction side, Block block, @Nullable BlockEntity blockEntity) {
        return blockEntity != null &&
                blockEntity.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, side.getOpposite()).isPresent();
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    @Override
    public CableVariant getPipeType() {
        return (CableVariant)super.getPipeType();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
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
        if (level == null) return;
        particle = new GTOverheatParticle(this, meltTemp, getPipeType().isCable);
        GTParticleManager.INSTANCE.addEffect(particle);
    }

    @OnlyIn(Dist.CLIENT)
    public void killParticle() {
        if (particle != null && isParticleAlive()) {
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
            getLevel().setBlockAndUpdate(worldPosition, Blocks.FIRE.defaultBlockState());
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

        int index = getPipeType().insulationLevel;
        CableBlock newBlock = GTMaterialBlocks.CABLE_BLOCKS
                .get(CableVariant.values()[index].tagPrefix, getPipeBlock().material)
                .get();
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
        getLevel().getLightEngine().checkBlock(worldPosition);
    }

    @ClientFieldChangeListener(fieldName = "temperature")
    public void onTemperatureUpdated() {
        if (particle == null) return;
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

            getLevel().addParticle(ParticleTypes.SMOKE,
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
    public UITexture getPipeTexture(boolean isBlock) {
        return isBlock ? GTGuiTextures.TOOL_WIRE_CONNECT : GTGuiTextures.TOOL_WIRE_BLOCK;
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
