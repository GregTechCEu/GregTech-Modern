package com.gregtechceu.gtceu.common.block.explosive;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;

import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public abstract class GTExplosiveBlock extends Block {

    public static final BooleanProperty CAN_REDSTONE_ACTIVATE = BooleanProperty.create("can_redstone_activate");
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

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
        this.registerDefaultState(this.defaultBlockState()
                .setValue(UNSTABLE, explodeOnMine)
                .setValue(CAN_REDSTONE_ACTIVATE, canRedstoneActivate));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNSTABLE, CAN_REDSTONE_ACTIVATE);
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
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face,
                             @Nullable LivingEntity igniter) {
        explode(level, pos, igniter);
    }

    public void explode(Level level, BlockPos pos, @Nullable LivingEntity exploder) {
        if (!level.isClientSide) {
            GTExplosiveEntity entity = createEntity(level, pos, exploder);
            entity.setFuse(fuseLength);
            level.addFreshEntity(entity);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TNT_PRIMED,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(entity, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            GTExplosiveEntity entity = createEntity(level, pos, explosion.getIndirectSourceEntity());
            entity.setFuse(level.random.nextInt(fuseLength / 4) + fuseLength / 8);
            level.addFreshEntity(entity);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE)) {
            onCaughtFire(state, level, pos, null, null);
            level.removeBlock(pos, false);
            if (stack.getItem() == Items.FLINT_AND_STEEL) {
                stack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(hand));
            } else if (!player.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (state.getValue(UNSTABLE) && !player.isShiftKeyDown()) {
            this.explode(level, pos, player);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && entity instanceof Arrow arrow) {
            if (arrow.isOnFire()) {
                this.explode(level, pos, arrow.getOwner() instanceof LivingEntity living ? living : null);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (state.getValue(CAN_REDSTONE_ACTIVATE)) {
            if (level.hasNeighborSignal(pos)) {
                explode(level, pos, null);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                boolean movedByPiston) {
        if (state.getValue(CAN_REDSTONE_ACTIVATE)) {
            if (level.hasNeighborSignal(pos)) {
                this.explode(level, pos, null);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        boolean explodeOnMine = this.explodeOnMine;
        boolean canRedstoneActivate = this.canRedstoneActivate;
        if (stack.hasTag() && stack.getTag().contains(BlockItem.BLOCK_STATE_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag blockStateTag = stack.getTag().getCompound(BlockItem.BLOCK_STATE_TAG);
            explodeOnMine = UNSTABLE.getValue(blockStateTag.getString(UNSTABLE.getName())).orElse(explodeOnMine);
            canRedstoneActivate = CAN_REDSTONE_ACTIVATE.getValue(
                    blockStateTag.getString(CAN_REDSTONE_ACTIVATE.getName()))
                    .orElse(canRedstoneActivate);
        }

        if (explodeOnMine) {
            tooltip.add(Component.translatable("block.gtceu.explosive.breaking_tooltip"));
        }
        if (!canRedstoneActivate) {
            tooltip.add(Component.translatable("block.gtceu.explosive.lighting_tooltip"));
        }
    }

    public static void createGTExplosiveLootTable(RegistrateBlockLootTables table, Block block) {
        table.add(block, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .when(ExplosionCondition.survivesExplosion())
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block)
                                .apply(CopyBlockState.copyState(block)
                                        .copy(UNSTABLE)
                                        .copy(CAN_REDSTONE_ACTIVATE))
                        )
                )
        );
    }
}
