package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardCleaner;
import com.gregtechceu.gtceu.common.blockentity.DuctPipeBlockEntity;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class DuctNetHandler implements IHazardParticleContainer {

    @Getter
    private DuctPipeNet net;
    private final DuctPipeBlockEntity pipe;
    private final Direction facing;

    public DuctNetHandler(DuctPipeNet net, @NotNull DuctPipeBlockEntity pipe, @Nullable Direction facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
    }

    public void updateNetwork(DuctPipeNet net) {
        this.net = net;
    }

    @Nullable
    private IHazardParticleContainer getInnerContainer() {
        if (net == null || pipe.isInValid() || facing == null || pipe.isBlocked(facing)) {
            return null;
        }

        final List<DuctRoutePath> data = net.getNetData(pipe.getPipePos(), facing);
        if (data == null) {
            return null;
        }

        return new IHazardParticleContainer() {

            @Override
            public boolean inputsHazard(Direction side, MedicalCondition condition) {
                return data.stream()
                        .map(path -> path.getHandler(net.getLevel()))
                        .filter(Objects::nonNull)
                        .anyMatch(handler -> handler.inputsHazard(side, condition));
            }

            @Override
            public float changeHazard(MedicalCondition condition, float differenceAmount) {
                float total = 0;
                for (DuctRoutePath path : data) {
                    IHazardParticleContainer handler = path.getHandler(net.getLevel());
                    if (handler == null && path.getTargetPipe().isConnected(path.getTargetFacing())) {
                        if (net.getLevel().getBlockEntity(path.getTargetPipePos()
                                .relative(path.getTargetFacing())) instanceof IMachineBlockEntity machineBE &&
                                machineBE.getMetaMachine() instanceof IEnvironmentalHazardCleaner cleaner) {
                            cleaner.cleanHazard(condition, differenceAmount);
                            break;
                        }

                        var savedData = EnvironmentalHazardSavedData.getOrCreate(net.getLevel());
                        savedData.addZone(path.getTargetPipePos().relative(path.getTargetFacing()),
                                differenceAmount, true, HazardProperty.HazardTrigger.INHALATION, condition);
                        total += differenceAmount;
                        emitPollutionParticles(net.getLevel(), path.getTargetPipePos(), path.getTargetFacing());
                        break;
                    } else if (handler == null) {
                        continue;
                    }
                    float change = handler.changeHazard(condition, differenceAmount);
                    differenceAmount -= change;
                    total += change;
                    if (differenceAmount <= 0) {
                        break;
                    }
                }
                return total;
            }

            @Override
            public float getHazardStored(MedicalCondition condition) {
                float total = 0;
                for (DuctRoutePath path : data) {
                    IHazardParticleContainer handler = path.getHandler(net.getLevel());
                    if (handler != null) {
                        total += handler.getHazardStored(condition);
                    }
                }
                return total;
            }

            @Override
            public float getHazardCapacity(MedicalCondition condition) {
                float total = 0;
                for (DuctRoutePath path : data) {
                    IHazardParticleContainer handler = path.getHandler(net.getLevel());
                    if (handler != null) {
                        total += handler.getHazardCapacity(condition);
                    } else if (path.getTargetPipe().isConnected(path.getTargetFacing())) {
                        total += Integer.MAX_VALUE;
                        break;
                    }
                }
                return total;
            }
        };
    }

    @Override
    public boolean inputsHazard(Direction side, MedicalCondition condition) {
        IHazardParticleContainer handler = getInnerContainer();
        if (handler == null) return false;
        return handler.inputsHazard(side, condition);
    }

    @Override
    public boolean outputsHazard(Direction side, MedicalCondition condition) {
        return true;
    }

    @Override
    public float changeHazard(MedicalCondition condition, float differenceAmount) {
        IHazardParticleContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.changeHazard(condition, differenceAmount);
    }

    @Override
    public float getHazardStored(MedicalCondition condition) {
        IHazardParticleContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getHazardStored(condition);
    }

    @Override
    public float getHazardCapacity(MedicalCondition condition) {
        IHazardParticleContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getHazardCapacity(condition);
    }

    public static void emitPollutionParticles(ServerLevel level, BlockPos pos, Direction frontFacing) {
        float xPos = frontFacing.getStepX() * 0.76F + pos.getX() + 0.25F;
        float yPos = frontFacing.getStepY() * 0.76F + pos.getY() + 0.25F;
        float zPos = frontFacing.getStepZ() * 0.76F + pos.getZ() + 0.25F;

        float ySpd = frontFacing.getStepY() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
        float xSpd;
        float zSpd;

        if (frontFacing.getStepY() == -1) {
            float temp = GTValues.RNG.nextFloat() * 2 * (float) Math.PI;
            xSpd = (float) Math.sin(temp) * 0.1F;
            zSpd = (float) Math.cos(temp) * 0.1F;
        } else {
            xSpd = frontFacing.getStepX() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
            zSpd = frontFacing.getStepZ() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                xPos + GTValues.RNG.nextFloat() * 0.5F,
                yPos + GTValues.RNG.nextFloat() * 0.5F,
                zPos + GTValues.RNG.nextFloat() * 0.5F,
                1,
                xSpd, ySpd, zSpd,
                0.1);
    }
}
