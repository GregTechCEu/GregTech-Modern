package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.block.explosive.GTExplosiveBlock;
import com.gregtechceu.gtceu.common.block.explosive.IndustrialTNTBlock;
import com.gregtechceu.gtceu.common.block.explosive.PowderbarrelBlock;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

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

        if ((!canOpen || tag.getBoolean(LIGHTER_OPEN)) && (player == null || !player.isShiftKeyDown())) {
            // check if it's "tnt-like" in that it implements the same method for igniting it
            if (classImplementsOnCaughtFire(block.getClass())) {
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

    public boolean consumeFuel(@Nullable Player player, ItemStack stack) {
        if (player != null && player.isCreative())
            return true;

        int usesLeft = getUsesLeft(stack) - 1;

        if (usesLeft >= 0) {
            setUsesLeft(player, stack, usesLeft);
            return true;
        }
        return false;
    }

    private int getUsesLeft(ItemStack stack) {
        if (usesFluid) {
            var handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).resolve();
            if (handler.isEmpty()) return 0;

            FluidStack fluid = handler.get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            return fluid.getAmount();
        } else if (hasMultipleUses) {
            CompoundTag compound = stack.getOrCreateTag();
            if (compound.contains(USES_LEFT)) {
                return compound.getInt(USES_LEFT);
            }
            compound.putInt(USES_LEFT, maxUses);
            // no need to get the value from the tag here when we set it just above
            return maxUses;
        } else {
            return stack.getCount();
        }
    }

    private void setUsesLeft(Player player, @NotNull ItemStack stack, final int usesLeft) {
        if (usesFluid) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
                FluidStack fluid = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if (!fluid.isEmpty()) {
                    handler.drain(fluid.getAmount() - usesLeft, IFluidHandler.FluidAction.EXECUTE);
                }
            });
        } else if (hasMultipleUses) {
            if (usesLeft <= 0) {
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
            var handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).resolve();
            if (handler.isEmpty()) return 0.0f;

            FluidStack fluid = handler.get().getFluidInTank(0);
            return (float) fluid.getAmount() / (float) handler.get().getTankCapacity(0);
        } else if (hasMultipleUses) {
            return (float) getUsesLeft(stack) / (float) maxUses;
        } else {
            return 0.0f;
        }
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
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component
                .translatable(usesFluid ? "behaviour.lighter.fluid.tooltip" : "behaviour.lighter.tooltip.description"));
        tooltipComponents.add(Component.translatable("behaviour.lighter.tooltip.usage"));
        if (hasMultipleUses && !usesFluid) {
            tooltipComponents.add(Component.translatable("behaviour.lighter.uses", getUsesLeft(stack)));
        }
    }

    private static boolean defaultsCacheInit = false;
    private static final Reference2BooleanMap<Class<?>> IMPLS_CACHE = new Reference2BooleanOpenHashMap<>();

    /**
     * This is the simplest way I could figure out to check if a block can "catch fire" like TNT does (i.e. explode.),
     * whilst still placing a fire block if the block can't do that.
     * <p>
     * Most modded TNTs implement this method in one way or another, but don't have tags or extend {@link TntBlock}.
     * 
     * @author screret
     */
    private static boolean classImplementsOnCaughtFire(Class<? extends Block> clazz) {
        if (!defaultsCacheInit) {
            defaultsCacheInit = true;
            initCacheDefaults();
        }
        // first, check if class is cached
        if (IMPLS_CACHE.containsKey(clazz)) {
            return IMPLS_CACHE.getBoolean(clazz);
        } else {
            // then, cached superclasses
            for (Class<?> cls = clazz; cls != Block.class; cls = cls.getSuperclass()) {
                if (IMPLS_CACHE.containsKey(cls)) {
                    boolean val = IMPLS_CACHE.getBoolean(cls);
                    IMPLS_CACHE.put(clazz, val);
                    return val;
                }
            }
        }
        boolean exists;

        // finally, actually check if the method exists.
        try {
            // find the method and check its modifiers.
            Method onCaughtFire = clazz.getDeclaredMethod("onCaughtFire",
                    BlockState.class, Level.class, BlockPos.class, Direction.class, LivingEntity.class);
            int modifiers = onCaughtFire.getModifiers();
            // the method is implemented if it's: 1. not a default method, 2. public, 3. not static, 4. not abstract
            exists = !onCaughtFire.getDeclaringClass().isInterface() && Modifier.isPublic(modifiers) &&
                    !Modifier.isStatic(modifiers) && !Modifier.isAbstract(modifiers);

            IMPLS_CACHE.put(onCaughtFire.getDeclaringClass(), exists);
        } catch (NoSuchMethodException e) {
            exists = false;
        }
        IMPLS_CACHE.put(clazz, exists);
        return exists;
    }

    private static void initCacheDefaults() {
        // self-test & default caching
        if (!classImplementsOnCaughtFire(TntBlock.class)) {
            throw new AssertionError("TntBlock doesn't implement IForgeBlock#onCaughtFire!" +
                    "Something is seriously wrong!" +
                    "Maybe check if the method name changed?");
        }
        classImplementsOnCaughtFire(GTExplosiveBlock.class);
        // expected subclasses of GTExplosiveBlock
        classImplementsOnCaughtFire(IndustrialTNTBlock.class);
        classImplementsOnCaughtFire(PowderbarrelBlock.class);
    }
}
