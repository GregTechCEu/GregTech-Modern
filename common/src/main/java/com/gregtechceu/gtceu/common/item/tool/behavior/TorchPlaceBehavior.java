package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.TORCH_PLACING_KEY;

// TODO this currently voids the used tool as well as a torch. how fix?
public class TorchPlaceBehavior implements IToolBehavior {

    public static final TorchPlaceBehavior INSTANCE = new TorchPlaceBehavior();

    protected TorchPlaceBehavior() {/**/}

    @Override
    public @NotNull InteractionResult onItemUse(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();

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
                if (checkAndPlaceTorch(context, slotStack)) {
                    return InteractionResult.SUCCESS;
                }
            }
            for (int i = 0; i < player.getInventory().offhand.size(); i++) {
                slotStack = player.getInventory().offhand.get(i);
                if (checkAndPlaceTorch(context, slotStack)) {
                    behaviourTag.putInt(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY, -(i + 1));
                    return InteractionResult.SUCCESS;
                }
            }
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                slotStack = player.getInventory().items.get(i);
                if (checkAndPlaceTorch(context, slotStack)) {
                    behaviourTag.putInt(ToolHelper.TORCH_PLACING_CACHE_SLOT_KEY, i);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean checkAndPlaceTorch(UseOnContext context, ItemStack slotStack) {
        if (!slotStack.isEmpty()) {
            Item slotItem = slotStack.getItem();
            if (slotItem instanceof BlockItem slotItemBlock) {
                Block slotBlock = slotItemBlock.getBlock();
                if (slotBlock == Blocks.TORCH ||
                        slotStack.is(TagUtil.createItemTag("torches"))) {
                    BlockPos pos = context.getClickedPos();
                    BlockState state = context.getLevel().getBlockState(pos);
                    if (!state.canBeReplaced()) {
                        pos = pos.relative(context.getClickedFace());
                    }
                    if (context.getPlayer().mayUseItemAt(pos, context.getClickedFace(), slotStack)) {
                        var blockPlaceContext = new BlockPlaceContext(context);
                        BlockState slotState = slotBlock.getStateForPlacement(blockPlaceContext);
                        if (slotItemBlock.place(blockPlaceContext).consumesAction()) {
                            slotState = context.getLevel().getBlockState(pos);
                            SoundType soundtype = slotState.getBlock().getSoundType(slotState);
                            context.getLevel().playSound(context.getPlayer(), pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            if (!context.getPlayer().isCreative()) slotStack.shrink(1);
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
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.torch_place"));
    }
}