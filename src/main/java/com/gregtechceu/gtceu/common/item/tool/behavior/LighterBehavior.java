package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.block.explosive.GTExplosiveBlock;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

import javax.annotation.Nullable;

public class LighterBehavior implements IDurabilityBar, IInteractionItem, IAddInformation {

    public static final String LIGHTER_OPEN = "lighterOpen";
    private static final String USES_LEFT = "usesLeft";
    private static final Pair<Integer, Integer> DURABILITY_BAR_COLORS = GradientUtil.getGradient(0xF07F1D, 10);
    private final ResourceLocation overrideLocation;
    private final boolean usesFluid;
    private final boolean hasMultipleUses;
    private final boolean canOpen;
    private Item destroyItem = Items.AIR;

    private int maxUses = 0;

    public LighterBehavior(boolean useFluid, boolean hasMultipleUses, boolean canOpen) {
        this(null, useFluid, hasMultipleUses, canOpen);
    }

    public LighterBehavior(boolean useFluid, boolean hasMultipleUses, boolean canOpen, Item destroyItem, int maxUses) {
        this(null, useFluid, hasMultipleUses, canOpen);
        this.maxUses = maxUses;
        this.destroyItem = destroyItem;
    }

    public LighterBehavior(@Nullable ResourceLocation overrideLocation, boolean useFluid, boolean hasMultipleUses,
                           boolean canOpen) {
        this.overrideLocation = overrideLocation;
        this.usesFluid = useFluid;
        this.hasMultipleUses = hasMultipleUses;
        this.canOpen = canOpen;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        CompoundTag tag = itemStack.getOrCreateTag();
        if (canOpen && player.isCrouching()) {
            tag.putBoolean(LIGHTER_OPEN, !tag.getBoolean(LIGHTER_OPEN));
            itemStack.setTag(tag);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        // ItemStack itemStack = player.getItemInHand(usedHand);
        CompoundTag tag = itemStack.getOrCreateTag();
        Player player = context.getPlayer();
        if ((!canOpen || (tag.getBoolean(LIGHTER_OPEN)) && !player.isCrouching()) && consumeFuel(player, itemStack)) {
            player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F,
                    GTValues.RNG.nextFloat() * 0.4F + 0.8F);
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            Block block = state.getBlock();
            if (block instanceof TntBlock tnt) {
                tnt.onCaughtFire(null, context.getLevel(), context.getClickedPos(), null, player);
                context.getLevel().setBlock(context.getClickedPos(), Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL_IMMEDIATE);
                return InteractionResult.SUCCESS;
            }
            if (block instanceof GTExplosiveBlock explosive) {
                explosive.explode(context.getLevel(), context.getClickedPos(), player);
                context.getLevel().setBlock(context.getClickedPos(), Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL_IMMEDIATE);
                return InteractionResult.SUCCESS;
            }

            BlockPos offset = context.getClickedPos().offset(context.getClickedFace().getNormal());
            if (context.getLevel().isEmptyBlock(offset)) {
                context.getLevel().setBlock(offset, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                if (!context.getLevel().isClientSide) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, offset, itemStack);
                }
            }
            return InteractionResult.PASS;
        }

        return InteractionResult.FAIL;
    }

    public boolean consumeFuel(Player player, ItemStack stack) {
        if (player != null && player.isCreative())
            return true;

        int usesLeft = getUsesLeft(stack);

        if (usesLeft - 1 >= 0) {
            setUsesLeft(player, stack, usesLeft - 1);
            return true;
        }
        return false;
    }

    private int getUsesLeft(ItemStack stack) {
        if (usesFluid) {
            IFluidHandlerItem fluidHandlerItem = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null)
                    .resolve().orElse(null);
            if (fluidHandlerItem == null)
                return 0;

            net.minecraftforge.fluids.FluidStack fluid = fluidHandlerItem.drain(Integer.MAX_VALUE,
                    IFluidHandler.FluidAction.SIMULATE);
            return fluid.isEmpty() ? 0 : fluid.getAmount();
        }
        if (hasMultipleUses) {
            CompoundTag compound = stack.getOrCreateTag();
            if (compound.contains(USES_LEFT)) {
                return compound.getInt(USES_LEFT);
            }
            compound.putInt(USES_LEFT, maxUses);
            stack.setTag(compound);
            return compound.getInt(USES_LEFT);
        }
        return stack.getCount();
    }

    private void setUsesLeft(Player player, @NotNull ItemStack stack, int usesLeft) {
        if (usesFluid) {
            IFluidHandlerItem fluidHandlerItem = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null)
                    .resolve().orElse(null);
            if (fluidHandlerItem != null) {

                net.minecraftforge.fluids.FluidStack fluid = fluidHandlerItem.drain(Integer.MAX_VALUE,
                        IFluidHandler.FluidAction.SIMULATE);
                if (!fluid.isEmpty()) {
                    fluidHandlerItem.drain(fluid.getAmount() - usesLeft, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        } else if (hasMultipleUses) {
            if (usesLeft == 0) {
                stack.setCount(0);
                player.addItem(new ItemStack(destroyItem));
            } else {
                stack.getOrCreateTag().putInt(USES_LEFT, usesLeft);
            }
        } else {
            stack.setCount(usesLeft);
        }
    }

    @Override
    public float getDurabilityForDisplay(ItemStack stack) {
        if (usesFluid) {
            IFluidHandlerItem fluidHandlerItem = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null)
                    .resolve().orElse(null);
            if (fluidHandlerItem == null)
                return 0.0f;

            net.minecraftforge.fluids.FluidStack fluid = fluidHandlerItem.getFluidInTank(0);
            return fluid.isEmpty() ? 0.0f : (float) fluid.getAmount() / (float) fluidHandlerItem.getTankCapacity(0);
        } else if (hasMultipleUses) {
            return (float) getUsesLeft(stack) / (float) maxUses;
        }
        return 0.0f;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return usesFluid || hasMultipleUses;
    }

    @Override
    public boolean showEmptyBar(ItemStack itemStack) {
        return usesFluid || hasMultipleUses;
    }

    @Override
    public @Nullable Pair<Integer, Integer> getDurabilityColorsForDisplay(ItemStack itemStack) {
        if (hasMultipleUses && usesFluid) {
            return DURABILITY_BAR_COLORS;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(
                Component.translatable(usesFluid ? "behaviour.lighter.fluid.tooltip" : "behaviour.lighter.tooltip"));
        if (hasMultipleUses && !usesFluid) {
            tooltipComponents.add(Component.translatable("behaviour.lighter.uses", getUsesLeft(stack)));
        }
    }
}
