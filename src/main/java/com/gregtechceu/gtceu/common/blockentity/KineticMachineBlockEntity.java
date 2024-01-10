package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.KineticMachineDefinition;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticEffectHandler;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote CreateKineticSourceBlockEntity
 */
public class KineticMachineBlockEntity extends KineticBlockEntity implements IMachineBlockEntity {
    public final MultiManagedStorage managedStorage = new MultiManagedStorage();
    @Getter
    public final MetaMachine metaMachine;
    private final long offset = GTValues.RNG.nextInt(20);
    public float workingSpeed;
    public boolean reActivateSource;

    protected KineticMachineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.metaMachine = getDefinition().createMetaMachine(this);
    }

    @ExpectPlatform
    public static KineticMachineBlockEntity create(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onBlockEntityRegister(BlockEntityType blockEntityType, NonNullSupplier<BiFunction<MaterialManager, KineticMachineBlockEntity, BlockEntityInstance<? super KineticMachineBlockEntity>>> instanceFactory, boolean renderNormally) {
        throw new AssertionError();
    }

    @Override
    public KineticMachineDefinition getDefinition() {
        return (KineticMachineDefinition) IMachineBlockEntity.super.getDefinition();
    }

    @Override
    public KineticMachineBlockEntity self() {
        return this;
    }

    @Override
    public boolean triggerEvent(int id, int para) {
        if (id == 1) { // chunk re render
            if (level != null && level.isClientSide) {
                scheduleRenderUpdate();
            }
            return true;
        }
        return false;
    }


    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public MultiManagedStorage getRootStorage() {
        return managedStorage;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        metaMachine.onUnload();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        metaMachine.onLoad();
    }

    @Override
    public boolean shouldRenderGrid(Player player, ItemStack held, Set<GTToolType> toolTypes) {
        return metaMachine.shouldRenderGrid(player, held, toolTypes);
    }

    @Override
    public ResourceTexture sideTips(Player player, Set<GTToolType> toolTypes, Direction side) {
        return metaMachine.sideTips(player, toolTypes, side);
    }

    //////////////////////////////////////
    //*********     Create     *********//
    //////////////////////////////////////

    public KineticEffectHandler getEffects() {
        return effects;
    }

    public float scheduleWorking(float su, boolean simulate) {
        if (getDefinition().isSource()) {
            float speed = Math.min(256f, su / getDefinition().getTorque());
            if (!simulate) {
                workingSpeed = speed;
                updateGeneratedRotation();
            }
            return speed * getDefinition().getTorque();
        }
        return 0;
    }

    public void scheduleWorking(float su) {
        scheduleWorking(su, false);
    }

    public void stopWorking() {
        if (getDefinition().isSource() && getGeneratedSpeed() != 0) {
            workingSpeed = 0;
            updateGeneratedRotation();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        return workingSpeed;
    }

    protected void notifyStressCapacityChange(float capacity) {
        this.getOrCreateNetwork().updateCapacityFor(this, capacity);
    }

    public void removeSource() {
        if (getDefinition().isSource() && this.hasSource() && this.isSource()) {
            this.reActivateSource = true;
        }
        super.removeSource();
    }

    public void setSource(BlockPos source) {
        super.setSource(source);
        if (!getDefinition().isSource()) return;
        BlockEntity tileEntity = this.level.getBlockEntity(source);
        if (tileEntity instanceof KineticBlockEntity sourceTe) {
            if (this.reActivateSource && Math.abs(sourceTe.getSpeed()) >= Math.abs(this.getGeneratedSpeed())) {
                this.reActivateSource = false;
            }
        }
    }

    public void tick() {
        super.tick();
        if (getDefinition().isSource() && this.reActivateSource) {
            this.updateGeneratedRotation();
            this.reActivateSource = false;
        }

    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        float stressBase = this.calculateAddedStressCapacity();
        if (stressBase != 0.0F && IRotate.StressImpact.isEnabled()) {
            Lang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
            Lang.translate("tooltip.capacityProvided").style(ChatFormatting.GRAY).forGoggles(tooltip);
            float speed = this.getTheoreticalSpeed();
            if (speed != this.getGeneratedSpeed() && speed != 0.0F) {
                stressBase *= this.getGeneratedSpeed() / speed;
            }

            speed = Math.abs(speed);
            float stressTotal = stressBase * speed;
            Lang.number(stressTotal).translate("generic.unit.stress").style(ChatFormatting.AQUA).space().add(Lang.translate("gui.goggles.at_current_speed").style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);
            added = true;
        }

        return added;
    }

    public void updateGeneratedRotation() {
        if (!getDefinition().isSource()) return;
        float speed = this.getGeneratedSpeed();
        float prevSpeed = this.speed;
        if (!this.level.isClientSide) {
            if (prevSpeed != speed) {
                if (!this.hasSource()) {
                    IRotate.SpeedLevel levelBefore = IRotate.SpeedLevel.of(this.speed);
                    IRotate.SpeedLevel levelafter = IRotate.SpeedLevel.of(speed);
                    if (levelBefore != levelafter) {
                        this.effects.queueRotationIndicators();
                    }
                }

                this.applyNewSpeed(prevSpeed, speed);
            }

            if (this.hasNetwork() && speed != 0.0F) {
                KineticNetwork network = this.getOrCreateNetwork();
                this.notifyStressCapacityChange(this.calculateAddedStressCapacity());
                this.getOrCreateNetwork().updateStressFor(this, this.calculateStressApplied());
                network.updateStress();
            }

            this.onSpeedChanged(prevSpeed);
            this.sendData();
        }
    }

    public void applyNewSpeed(float prevSpeed, float speed) {
        if (speed == 0.0F) {
            if (this.hasSource()) {
                this.notifyStressCapacityChange(0.0F);
                this.getOrCreateNetwork().updateStressFor(this, this.calculateStressApplied());
            } else {
                this.detachKinetics();
                this.setSpeed(0.0F);
                this.setNetwork(null);
            }
        } else if (prevSpeed == 0.0F) {
            this.setSpeed(speed);
            this.setNetwork(this.createNetworkId());
            this.attachKinetics();
        } else if (this.hasSource()) {
            if (Math.abs(prevSpeed) >= Math.abs(speed)) {
                if (Math.signum(prevSpeed) != Math.signum(speed)) {
                    this.level.destroyBlock(this.worldPosition, true);
                }
            } else {
                this.detachKinetics();
                this.setSpeed(speed);
                this.source = null;
                this.setNetwork(this.createNetworkId());
                this.attachKinetics();
            }
        } else {
            this.detachKinetics();
            this.setSpeed(speed);
            this.attachKinetics();
        }
    }

    public Long createNetworkId() {
        return this.worldPosition.asLong();
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putFloat("workingSpeed", workingSpeed);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        workingSpeed = compound.contains("workingSpeed") ? compound.getFloat("workingSpeed") : 0;
    }

}
