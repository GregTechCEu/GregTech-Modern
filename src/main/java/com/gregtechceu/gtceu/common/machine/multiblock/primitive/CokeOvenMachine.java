package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidUtil;

public class CokeOvenMachine extends PrimitiveWorkableMachine implements IUIMachine {

    public CokeOvenMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return CokeOvenMachineUI.createUI(this, entityPlayer);
    }

    @Override
    public void animateTick(RandomSource random) {
        if (this.isActive()) {
            final BlockPos pos = getBlockPos();
            float x = pos.getX() + 0.5F;
            float z = pos.getZ() + 0.5F;

            final var facing = getFrontFacing();
            final float horizontalOffset = GTValues.RNG.nextFloat() * 0.6F - 0.3F;
            final float y = pos.getY() + GTValues.RNG.nextFloat() * 0.375F + 0.3F;

            if (facing.getAxis() == Direction.Axis.X) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) x += 0.52F;
                else x -= 0.52F;
                z += horizontalOffset;
            } else if (facing.getAxis() == Direction.Axis.Z) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) z += 0.52F;
                else z -= 0.52F;
                x += horizontalOffset;
            }
            if (ConfigHolder.INSTANCE.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                getLevel().playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F,
                        false);
            }
            getLevel().addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            getLevel().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (!isRemote()) {
            if (super.onUseWithItem(context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
            if (FluidUtil.interactWithFluidHandler(context.getPlayer(), context.getHand(), exportFluids)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return super.onUseWithItem(context);
    }
}
