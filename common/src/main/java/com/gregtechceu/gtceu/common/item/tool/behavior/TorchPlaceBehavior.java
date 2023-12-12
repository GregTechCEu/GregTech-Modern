package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.TORCH_PLACING_KEY;

public class TorchPlaceBehavior implements IToolBehavior {

    public static final TorchPlaceBehavior INSTANCE = new TorchPlaceBehavior();

    protected TorchPlaceBehavior() {/**/}

    @Override
    public InteractionResult onItemUse(@NotNull Player player, @NotNull Level Level, @NotNull BlockPos pos,
                                       @NotNull InteractionHand hand, @NotNull Direction facing, float hitX, float hitY,
                                       float hitZ) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag behaviourTag = ToolHelper.getBehaviorsTag(stack);
        if (behaviourTag.getBoolean(TORCH_PLACING_KEY)) {
            int cachedTorchSlot;
            ItemStack slotStack;
            if (behaviourTag.getBoolean(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY)) {
                cachedTorchSlot = behaviourTag.getInt(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY);
                if (cachedTorchSlot < 0) {
                    slotStack = player.getInventory().offhand.get(0);
                } else {
                    slotStack = player.getInventory().items.get(cachedTorchSlot);
                }
                if (checkAndPlaceTorch(slotStack, player, Level, pos, hand, facing, hitX, hitY, hitZ)) {
                    return InteractionResult.SUCCESS;
                }
            }
            for (int i = 0; i < player.getInventory().offhand.size(); i++) {
                slotStack = player.getInventory().offhand.get(i);
                if (checkAndPlaceTorch(slotStack, player, Level, pos, hand, facing, hitX, hitY, hitZ)) {
                    behaviourTag.putInt(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY, -(i + 1));
                    return InteractionResult.SUCCESS;
                }
            }
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                slotStack = player.getInventory().items.get(i);
                if (checkAndPlaceTorch(slotStack, player, Level, pos, hand, facing, hitX, hitY, hitZ)) {
                    behaviourTag.putInt(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY, i);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean checkAndPlaceTorch(ItemStack slotStack, Player player, Level level, BlockPos pos,
                                              InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!slotStack.isEmpty()) {
            Item slotItem = slotStack.getItem();
            if (slotItem instanceof BlockItem slotItemBlock) {
                Block slotBlock = slotItemBlock.getBlock();
                if (slotBlock == Blocks.TORCH ||
                        slotStack.is(TagUtil.createItemTag("torches"))) {
                    BlockState state = level.getBlockState(pos);
                    Block block = state.getBlock();
                    if (!state.canBeReplaced()) {
                        pos = pos.relative(facing);
                    }
                    if (player.mayUseItemAt(pos, facing, slotStack)) {
                        var context = new BlockPlaceContext(player, hand, slotStack, new BlockHitResult(new Vec3(hitX, hitY, hitZ), facing, pos, false));
                        BlockState slotState = slotBlock.getStateForPlacement(context);
                        if (slotItemBlock.place(context).consumesAction()) {
                            slotState = level.getBlockState(pos);
                            SoundType soundtype = slotState.getBlock().getSoundType(slotState);
                            level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            if (!player.isCreative()) slotStack.shrink(1);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull CompoundTag tag) {
        tag.putBoolean(TORCH_PLACING_KEY, true);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level Level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gt.tool.behavior.torch_place"));
    }
}