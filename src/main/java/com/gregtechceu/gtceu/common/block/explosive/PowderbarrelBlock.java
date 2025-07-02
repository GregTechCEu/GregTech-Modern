package com.gregtechceu.gtceu.common.block.explosive;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;
import com.gregtechceu.gtceu.common.entity.PowderbarrelEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PowderbarrelBlock extends GTExplosiveBlock {

    public PowderbarrelBlock(Properties properties) {
        super(properties, false, true, 100);
    }

    @Override
    protected GTExplosiveEntity createEntity(@NotNull Level world, @NotNull BlockPos pos,
                                             @Nullable LivingEntity exploder) {
        float x = pos.getX() + 0.5F, y = pos.getY(), z = pos.getZ() + 0.5F;
        return new PowderbarrelEntity(world, x, y, z, exploder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("block.gtceu.powderbarrel.drops_tooltip"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
