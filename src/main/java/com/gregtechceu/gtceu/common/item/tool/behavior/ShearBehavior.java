package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.core.mixins.BeehiveBlockAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class ShearBehavior implements IToolBehavior {

    public static final ShearBehavior INSTANCE = new ShearBehavior();

    protected ShearBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == ToolActions.SHEARS_DISARM || action == ToolActions.SHEARS_HARVEST ||
                action == ToolActions.SHEARS_CARVE;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        List<BlockPos> blocks;
        // only attempt to shear if the center block is shearable
        if (isBlockShearable(context)) {
            if (aoeDefinition.isZero()) {
                blocks = List.of(pos);
            } else {
                blocks = getShearableBlocks(aoeDefinition, context);
            }
        } else {
            return InteractionResult.PASS;
        }

        boolean sheared = false;
        for (BlockPos blockPos : blocks) {
            BlockState state = level.getBlockState(blockPos);
            Block block = state.getBlock();

            // Clicked pos already handled by the use method of BeehiveBlock, PumpkinBlock and GrowingPlantHeadBlock so
            // skip it.
            if (blockPos.equals(pos) && !(block instanceof IForgeShearable)) {
                continue;
            }

            // Can handle MC special cases like Beehive/nest, Pumpkin, vines which can be sheared to prevent growing.
            // Best would be to patch Pumpkin, Beehive/nest and GrowingPlantHead blocks to implement
            // IForgeShearable and call onSheared when right-clicked with a shears like item, instead of implementing
            // the specific behavior in the block use method.
            if (block instanceof IForgeShearable shearable && shearable.isShearable(stack, level, blockPos) &&
                    player instanceof ServerPlayer serverPlayer) {
                if (ToolHelper.shearBlock(serverPlayer, stack, blockPos) != -1) {
                    sheared = true;
                }
            } else if (block instanceof BeehiveBlock beehive &&
                    state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                        BeehiveBlock.dropHoneycomb(level, blockPos);
                        if (!CampfireBlock.isSmokeyPos(level, blockPos)) {
                            BeehiveBlockAccessor accessor = (BeehiveBlockAccessor) beehive;
                            if (accessor.gtceu$hiveContainsBees(level, blockPos)) {
                                accessor.gtceu$angerNearbyBees(level, blockPos);
                            }
                            beehive.releaseBeesAndResetHoneyLevel(level, state, blockPos, player,
                                    BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                        } else {
                            beehive.resetHoneyLevel(level, state, blockPos);
                        }
                        sheared = true;
                        ToolHelper.damageItem(stack, player);
                    } else
                if (block instanceof PumpkinBlock) {
                    Direction clickedFace = context.getClickedFace();
                    Direction direction = clickedFace.getAxis() == Direction.Axis.Y ? player != null ?
                            player.getDirection().getOpposite() : Direction.UP : clickedFace;
                    level.setBlock(blockPos, Blocks.CARVED_PUMPKIN.defaultBlockState()
                            .setValue(CarvedPumpkinBlock.FACING, direction), 11);
                    double x0 = 0.5F + direction.getStepX() * 0.65;
                    double z0 = 0.5F + direction.getStepZ() * 0.65;
                    ItemEntity itementity = new ItemEntity(level, blockPos.getX() + x0, blockPos.getY() + 0.1,
                            blockPos.getZ() + z0, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                    double vx = 0.05 * direction.getStepX() + level.random.nextDouble() * 0.02;
                    double vz = 0.05 * direction.getStepZ() + level.random.nextDouble() * 0.02;
                    itementity.setDeltaMovement(vx, 0.05, vz);
                    level.addFreshEntity(itementity);
                    sheared = true;
                    ToolHelper.damageItem(stack, player);
                } else if (block instanceof GrowingPlantHeadBlock growingplantheadblock) {
                    if (!growingplantheadblock.isMaxAge(state)) {
                        BlockState maxAgeState = growingplantheadblock.getMaxAgeState(state);
                        level.setBlockAndUpdate(blockPos, maxAgeState);
                        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos,
                                GameEvent.Context.of(context.getPlayer(), maxAgeState));
                        sheared = true;
                        ToolHelper.damageItem(stack, player);
                    }
                }
            if (stack.isEmpty()) break;
        }

        if (sheared) {
            level.playSound(player, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.SHEAR, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onInteractLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
                                                    InteractionHand hand) {
        Level level = player.level();
        BlockPos pos = BlockPos.containing(interactionTarget.position());
        if (interactionTarget instanceof IForgeShearable shearableEntity &&
                shearableEntity.isShearable(stack, level, pos)) {
            List<ItemStack> drops = shearableEntity.onSheared(player, stack, level, pos,
                    stack.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));
            // If nothing comes from shearing return pass and don't use durability
            if (drops.isEmpty()) {
                return InteractionResult.PASS;
            }
            boolean relocateMinedBlocks = ToolHelper.hasBehaviorsTag(stack) &&
                    ToolHelper.getBehaviorsTag(stack).getBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY);
            Iterator<ItemStack> iter = drops.iterator();
            while (iter.hasNext()) {
                ItemStack drop = iter.next();
                if (relocateMinedBlocks && player.addItem(drop)) {
                    iter.remove();
                } else {
                    float f = 0.7F;
                    double xo = level.random.nextFloat() * f + 0.15D;
                    double yo = level.random.nextFloat() * f + 0.15D;
                    double zo = level.random.nextFloat() * f + 0.15D;
                    ItemEntity entityItem = new ItemEntity(level, pos.getX() + xo, pos.getY() + yo,
                            pos.getZ() + zo, drop);
                    entityItem.setDefaultPickUpDelay();
                    level.addFreshEntity(entityItem);
                }
            }
            ToolHelper.damageItem(stack, player, 1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected List<BlockPos> getShearableBlocks(AoESymmetrical aoeDefinition, UseOnContext context) {
        return ToolHelper.iterateAoE(aoeDefinition, ShearBehavior::isBlockShearable, context);
    }

    protected static boolean isBlockShearable(UseOnContext context) {
        Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
        return block instanceof IForgeShearable || block instanceof BeehiveBlock || block instanceof PumpkinBlock ||
                block instanceof GrowingPlantHeadBlock;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.shears"));
    }
}
