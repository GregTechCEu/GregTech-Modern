package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Tree Felling Behavior must be handled in a special way in
 * {@link IGTTool#definition$onBlockStartBreak(ItemStack, BlockPos, Player)}
 */
public class TreeFellingBehavior implements IToolBehavior {

    public static final TreeFellingBehavior INSTANCE = new TreeFellingBehavior();

    protected TreeFellingBehavior() {/**/}

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull CompoundTag tag) {
        tag.putBoolean(ToolHelper.TREE_FELLING_KEY, true);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gt.tool.behavior.tree_felling"));
    }
}