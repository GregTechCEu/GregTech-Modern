package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.block.explosive.GTExplosiveBlock;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.AbstractCandleBlock.LIT;

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
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        boolean isOpen = item.getOrDefault(GTDataComponents.LIGHTER_OPEN, false);
        if (canOpen && player.isShiftKeyDown()) {
            item.set(GTDataComponents.LIGHTER_OPEN, !isOpen);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        Block block = state.getBlock();
        BlockPos offset = pos.offset(context.getClickedFace().getNormal());

        if ((!canOpen || (itemStack.getOrDefault(GTDataComponents.LIGHTER_OPEN, false)) && !player.isCrouching())) {
            if (block instanceof TntBlock tnt && consumeFuel(player, itemStack)) {
                tnt.onCaughtFire(null, context.getLevel(), pos, null, player);
                context.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL_IMMEDIATE);
                player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS,
                        1.0F,
                        GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                return InteractionResult.SUCCESS;
            }
            if (block instanceof GTExplosiveBlock explosive && consumeFuel(player, itemStack)) {
                explosive.explode(context.getLevel(), pos, player);
                context.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL_IMMEDIATE);
                player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS,
                        1.0F,
                        GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                return InteractionResult.SUCCESS;
            }
            if (block instanceof CandleBlock) {
                if (CandleBlock.canLight(state) && !CandleBlock.isLit(state) && consumeFuel(player, itemStack)) {
                    context.getLevel().setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
                    player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE,
                            SoundSource.PLAYERS, 1.0F,
                            GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                    return InteractionResult.SUCCESS;
                } else return InteractionResult.PASS;
            }
            if (block instanceof CandleCakeBlock) {
                if (CandleCakeBlock.canLight(state) && !CandleCakeBlock.isLit(state) &&
                        consumeFuel(player, itemStack)) {
                    context.getLevel().setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
                    player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE,
                            SoundSource.PLAYERS, 1.0F,
                            GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                    return InteractionResult.SUCCESS;
                } else return InteractionResult.PASS;
            }
            if (block instanceof CampfireBlock) {
                if (CampfireBlock.canLight(state) && !CampfireBlock.isLitCampfire(state) &&
                        consumeFuel(player, itemStack)) {
                    context.getLevel().setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
                    player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE,
                            SoundSource.PLAYERS, 1.0F,
                            GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                    return InteractionResult.SUCCESS;
                } else return InteractionResult.PASS;
            }
            if (block instanceof SoulSandBlock && block != Blocks.SOUL_FIRE && consumeFuel(player, itemStack)) {
                context.getLevel().setBlock(offset, Blocks.SOUL_FIRE.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                if (!context.getLevel().isClientSide) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, offset, itemStack);
                }
                player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS,
                        1.0F,
                        GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                return InteractionResult.PASS;
            }
            if (block == Blocks.SOUL_SOIL && block != Blocks.SOUL_FIRE && consumeFuel(player, itemStack)) {
                context.getLevel().setBlock(offset, Blocks.SOUL_FIRE.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                if (!context.getLevel().isClientSide) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, offset, itemStack);
                }
                player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS,
                        1.0F,
                        GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                return InteractionResult.PASS;
            }

            if (context.getLevel().isEmptyBlock(offset) &&
                    BaseFireBlock.canBePlacedAt(context.getLevel(), offset, context.getHorizontalDirection()) &&
                    block != Blocks.FIRE && block != Blocks.SOUL_FIRE && consumeFuel(player, itemStack)) {
                context.getLevel().setBlock(offset, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                if (!context.getLevel().isClientSide) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, offset, itemStack);
                }
                player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS,
                        1.0F,
                        GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
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
            IFluidHandlerItem fluidHandlerItem = stack.getCapability(Capabilities.FluidHandler.ITEM, null);
            if (fluidHandlerItem == null)
                return 0;

            FluidStack fluid = fluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            return fluid.isEmpty() ? 0 : fluid.getAmount();
        }
        if (hasMultipleUses) {
            return maxUses - stack.getOrDefault(DataComponents.DAMAGE, 0);
        }
        return stack.getCount();
    }

    private void setUsesLeft(Player player, @NotNull ItemStack stack, int usesLeft) {
        if (usesFluid) {
            IFluidHandlerItem fluidHandlerItem = stack.getCapability(Capabilities.FluidHandler.ITEM, null);
            if (fluidHandlerItem != null) {

                FluidStack fluid = fluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if (!fluid.isEmpty()) {
                    fluidHandlerItem.drain(fluid.getAmount() - usesLeft, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        } else if (hasMultipleUses) {
            if (usesLeft == 0) {
                stack.setCount(0);
                player.addItem(new ItemStack(destroyItem));
            } else {
                stack.set(DataComponents.DAMAGE, maxUses - usesLeft);
            }
        } else {
            stack.setCount(usesLeft);
        }
    }

    @Override
    public float getDurabilityForDisplay(ItemStack stack) {
        if (usesFluid) {
            IFluidHandlerItem fluidHandlerItem = stack.getCapability(Capabilities.FluidHandler.ITEM, null);
            if (fluidHandlerItem == null)
                return 0.0f;

            FluidStack fluid = fluidHandlerItem.getFluidInTank(0);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component
                .translatable(usesFluid ? "behaviour.lighter.fluid.tooltip" : "behaviour.lighter.tooltip.description"));
        tooltipComponents.add(Component.translatable("behaviour.lighter.tooltip.usage"));
        if (hasMultipleUses && !usesFluid) {
            tooltipComponents.add(Component.translatable("behaviour.lighter.uses", getUsesLeft(stack)));
        }
    }
}
