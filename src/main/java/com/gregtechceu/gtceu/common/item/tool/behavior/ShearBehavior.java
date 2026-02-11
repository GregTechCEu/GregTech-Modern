package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShearBehavior implements IToolBehavior {

    public static final ShearBehavior INSTANCE = new ShearBehavior();

    protected ShearBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == ToolActions.SHEARS_HARVEST || action == ToolActions.SHEARS_CARVE || action == ToolActions.SHEARS_DISARM;
    }

    @Override
    public @NotNull InteractionResult onItemUse(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        List<BlockPos> blocks;
        // only attempt to shear if the center block is shearable
        if (isBlockShearable(context)) {
            if (aoeDefinition.isZero()) {
                blocks = List.of(pos);
            } else {
                blocks = getShearableBlocks(aoeDefinition, context);
                blocks.add(0, context.getClickedPos());
            }
        } else {
            return InteractionResult.PASS;
        }

        boolean sheared = false;
        for (BlockPos blockPos : blocks) {
            BlockState state = level.getBlockState(blockPos);
            Block block = state.getBlock();

            if (block instanceof IForgeShearable shearable && shearable.isShearable(stack, level, blockPos)) {
                List<ItemStack> drops = new ArrayList<>(shearable.onSheared(player, stack, level, blockPos, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack)));
                if (!drops.isEmpty()) {
                    sheared = true;
                    ToolHelper.damageItem(stack, player);
                }
            } else if (blockPos != pos && block instanceof BeehiveBlock beehive && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                BeehiveBlock.dropHoneycomb(level, blockPos);
                if (!CampfireBlock.isSmokeyPos(level, blockPos)) {
                    if (hiveContainsBees(level, blockPos)) {
                        angerNearbyBees(level, blockPos);
                    }
                    beehive.releaseBeesAndResetHoneyLevel(level, state, blockPos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                } else {
                    beehive.resetHoneyLevel(level, state, blockPos);
                }
                sheared = true;
                ToolHelper.damageItem(stack, player);
            } else if (blockPos != pos && block instanceof PumpkinBlock) {
                Direction clickedFace = context.getClickedFace();
                Direction direction = clickedFace.getAxis() == Direction.Axis.Y ? player != null ? player.getDirection().getOpposite() : Direction.UP : clickedFace;
                level.setBlock(blockPos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction), 11);
                ItemEntity itementity = new ItemEntity(level, (double)blockPos.getX() + (double)0.5F + (double)direction.getStepX() * 0.65, (double)blockPos.getY() + 0.1, (double)blockPos.getZ() + (double)0.5F + (double)direction.getStepZ() * 0.65, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                itementity.setDeltaMovement(0.05 * (double)direction.getStepX() + level.random.nextDouble() * 0.02, 0.05, 0.05 * (double)direction.getStepZ() + level.random.nextDouble() * 0.02);
                level.addFreshEntity(itementity);
                sheared = true;
                ToolHelper.damageItem(stack, player);
            }
            if (stack.isEmpty()) break;
        }

        if (sheared) {
            level.playSound(player, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.SHEAR, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    protected List<BlockPos> getShearableBlocks(AoESymmetrical aoeDefinition, UseOnContext context) {
        return ToolHelper.iterateAoE(aoeDefinition, ShearBehavior::isBlockShearable, context);
    }

    protected static boolean isBlockShearable(UseOnContext context) {
        Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
        return block instanceof IForgeShearable || block instanceof BeehiveBlock || block instanceof PumpkinBlock;
    }

    private boolean hiveContainsBees(Level level, BlockPos pos) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof BeehiveBlockEntity beehiveblockentity) {
            return !beehiveblockentity.isEmpty();
        } else {
            return false;
        }
    }

    private void angerNearbyBees(Level level, BlockPos pos) {
        List<Bee> bees = level.getEntitiesOfClass(Bee.class, (new AABB(pos)).inflate(8.0F, 6.0F, 8.0F));
        if (!bees.isEmpty()) {
            List<Player> players = level.getEntitiesOfClass(Player.class, (new AABB(pos)).inflate(8.0F, 6.0F, 8.0F));
            if (players.isEmpty()) {
                return;
            }

            int i = players.size();

            for(Bee bee : bees) {
                if (bee.getTarget() == null) {
                    bee.setTarget(players.get(level.random.nextInt(i)));
                }
            }
        }
    }
}
