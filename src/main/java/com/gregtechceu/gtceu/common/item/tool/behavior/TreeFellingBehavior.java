package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The Tree Felling Behavior must be handled in a special way in
 * {@link IGTTool#definition$onBlockStartBreak(ItemStack, BlockPos, Player)}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TreeFellingBehavior implements IToolBehavior {

    public static final TreeFellingBehavior INSTANCE = new TreeFellingBehavior();

    protected TreeFellingBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == GTToolActions.AXE_FELL_TREE;
    }

    @Override
    public void addBehaviorNBT(ItemStack stack, CompoundTag tag) {
        tag.putBoolean(ToolHelper.TREE_FELLING_KEY, true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.tree_felling"));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> onItemRightClick(Level level, Player player,
                                                                        InteractionHand hand) {
        var held = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(held);
        }
        var tag = ToolHelper.getBehaviorsTag(held);
        var disable = tag.getBoolean(ToolHelper.DISABLE_TREE_FELLING_KEY);
        tag.putBoolean(ToolHelper.DISABLE_TREE_FELLING_KEY, !disable);
        player.sendSystemMessage(Component.translatable("item.gtceu.tool.behavior.tree_felling").append(" - ")
                .append(Component.translatable("cover.voiding.label." + (disable ? "enabled" : "disabled"))));
        return InteractionResultHolder.success(held);
    }
}
