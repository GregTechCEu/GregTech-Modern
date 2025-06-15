package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.block.explosive.GTExplosiveBlock;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.AbstractCandleBlock.LIT;

public class LighterBehavior implements IDurabilityBar, IInteractionItem, IAddInformation {

    public static final String LIGHTER_OPEN = "lighterOpen";
    private static final String USES_LEFT = "usesLeft";
    private static final IntIntPair DURABILITY_BAR_COLORS = GradientUtil.getGradient(0xF07F1D, 10);
    private final boolean usesFluid;
    private final boolean hasMultipleUses;
    private final boolean canOpen;
    private Supplier<ItemStack> destroyItem = () -> ItemStack.EMPTY;

    private int maxUses = 0;

    public LighterBehavior(boolean useFluid, boolean hasMultipleUses, boolean canOpen, Supplier<ItemStack> destroyItem,
                           int maxUses) {
        this(useFluid, hasMultipleUses, canOpen);
        this.maxUses = maxUses;
        this.destroyItem = destroyItem;
    }

    public LighterBehavior(boolean useFluid, boolean hasMultipleUses, boolean canOpen) {
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
        CompoundTag tag = itemStack.getOrCreateTag();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if ((!canOpen || (tag.getBoolean(LIGHTER_OPEN)) && (player == null || !player.isShiftKeyDown()))) {
            if ((block instanceof TntBlock || block instanceof GTExplosiveBlock)) {
                if (!consumeFuel(player, itemStack)) return InteractionResult.PASS;

                state.onCaughtFire(level, pos, clickedFace, player);
                FluidState fluidState = level.getFluidState(pos);
                level.setBlock(pos, fluidState.createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if ((CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state))) {
                if (!consumeFuel(player, itemStack)) return InteractionResult.PASS;

                level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
                level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                        1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            BlockPos offset = pos.relative(clickedFace);
            if (BaseFireBlock.canBePlacedAt(level, offset, context.getHorizontalDirection())) {
                if (!consumeFuel(player, itemStack)) return InteractionResult.PASS;

                level.playSound(player, offset, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                        1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState fireState = BaseFireBlock.getState(level, offset);
                level.setBlock(offset, fireState, Block.UPDATE_ALL_IMMEDIATE);
                level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);

                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, offset, itemStack);
                    itemStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                  LivingEntity interactionTarget, InteractionHand usedHand) {
        CompoundTag tag = stack.getOrCreateTag();
        Level level = player.level();

        if ((!canOpen || tag.getBoolean(LIGHTER_OPEN)) && !player.isShiftKeyDown()) {
            if (interactionTarget instanceof Creeper creeper) {
                if (!consumeFuel(player, stack)) return InteractionResult.PASS;
                level.playSound(player, creeper, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                        1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                if (!level.isClientSide) {
                    creeper.ignite();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
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
                stack.shrink(1);
                ItemStack brokenStack = this.destroyItem.get();
                if (!player.addItem(brokenStack)) {
                    player.drop(brokenStack, true);
                }
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
    public @Nullable IntIntPair getDurabilityColorsForDisplay(ItemStack itemStack) {
        if (hasMultipleUses && usesFluid) {
            return DURABILITY_BAR_COLORS;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component
                .translatable(usesFluid ? "behaviour.lighter.fluid.tooltip" : "behaviour.lighter.tooltip.description"));
        tooltipComponents.add(Component.translatable("behaviour.lighter.tooltip.usage"));
        if (hasMultipleUses && !usesFluid) {
            tooltipComponents.add(Component.translatable("behaviour.lighter.uses", getUsesLeft(stack)));
        }
    }
}
