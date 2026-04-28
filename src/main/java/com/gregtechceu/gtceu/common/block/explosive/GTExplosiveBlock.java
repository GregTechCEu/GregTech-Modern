package com.gregtechceu.gtceu.common.block.explosive;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public abstract class GTExplosiveBlock extends Block {

    private final boolean canRedstoneActivate;
    private final boolean explodeOnMine;
    private final int fuseLength;

    /**
     * @param canRedstoneActivate whether redstone signal can prime this explosive
     * @param explodeOnMine       whether mining this block should prime it (sneak mine to drop normally)
     * @param fuseLength          explosion countdown after priming. Vanilla TNT is 80.
     */
    public GTExplosiveBlock(BlockBehaviour.Properties properties, boolean canRedstoneActivate, boolean explodeOnMine,
                            int fuseLength) {
        super(properties.isValidSpawn((state, level, pos, ent) -> false).explosionResistance(1.0f));
        this.canRedstoneActivate = canRedstoneActivate;
        this.explodeOnMine = explodeOnMine;
        this.fuseLength = fuseLength;
    }

    protected abstract GTExplosiveEntity createEntity(@NotNull Level world, @NotNull BlockPos pos,
                                                      @Nullable LivingEntity exploder);

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public boolean onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face,
                                @Nullable LivingEntity igniter) {
        explode(level, pos, igniter);
        return true;
    }

    public void explode(Level level, BlockPos pos, @Nullable LivingEntity exploder) {
        if (!level.isClientSide()) {
            GTExplosiveEntity entity = createEntity(level, pos, exploder);
            entity.setFuse(fuseLength);
            level.addFreshEntity(entity);
            level.playSound(null, entity, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(entity, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide()) {
            GTExplosiveEntity entity = createEntity(level, pos, explosion.getIndirectSourceEntity());
            entity.setFuse(level.getRandom().nextInt(fuseLength / 4) + fuseLength / 8);
            level.addFreshEntity(entity);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT)) {
            this.explode(level, pos, player);
            level.removeBlock(pos, false);
            if (stack.isDamageableItem()) {
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
            } else if (!player.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (explodeOnMine && !player.isShiftKeyDown()) {
            this.explode(level, pos, player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean bypassInsideEffects) {
        super.entityInside(state, level, pos, entity, effectApplier, bypassInsideEffects);
        if (!level.isClientSide() && entity instanceof Arrow arrow) {
            if (arrow.isOnFire()) {
                this.explode(level, pos, arrow.getOwner() instanceof LivingEntity living ? living : null);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (canRedstoneActivate) {
            if (level.hasNeighborSignal(pos)) {
                explode(level, pos, null);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (explodeOnMine) {
            Entity player = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
            if (player != null && !player.isShiftKeyDown()) {
                return Collections.emptyList();
            }
        }

        return super.getDrops(state, params);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        if (canRedstoneActivate) {
            if (level.hasNeighborSignal(pos)) {
                this.explode(level, pos, null);
                level.removeBlock(pos, false);
            }
        }
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        if (explodeOnMine) {
            tooltip.add(Component.translatable("block.gtceu.explosive.breaking_tooltip"));
        }
        if (!canRedstoneActivate) {
            tooltip.add(Component.translatable("block.gtceu.explosive.lighting_tooltip"));
        }
    }
}
