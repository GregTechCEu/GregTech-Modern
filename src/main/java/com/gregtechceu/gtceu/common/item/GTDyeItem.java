package com.gregtechceu.gtceu.common.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import lombok.Getter;

public class GTDyeItem extends Item implements SignApplicator {

    @Getter
    private final DyeColor dyeColor;

    public GTDyeItem(DyeColor dyeColor, Item.Properties properties) {
        super(properties);
        this.dyeColor = dyeColor;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof Sheep sheep)) return InteractionResult.PASS;
        if (!sheep.isAlive() || sheep.isSheared() || sheep.getColor() == this.dyeColor) return InteractionResult.PASS;

        sheep.level().playSound(player, sheep, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (!player.level().isClientSide) {
            sheep.setColor(this.dyeColor);
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public boolean tryApplyToSign(Level level, SignBlockEntity sign, boolean isFront, Player player) {
        if (sign.updateText(text -> text.setColor(this.getDyeColor()), isFront)) {
            level.playSound(null, sign.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        } else {
            return false;
        }
    }
}
