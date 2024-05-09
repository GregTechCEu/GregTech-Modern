package com.gregtechceu.gtceu.common.items.tool.behavior;

import com.gregtechceu.gtceu.api.items.IGTTool;
import com.gregtechceu.gtceu.api.items.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.items.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.data.GTToolBehaviors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Tree Felling Behavior must be handled in a special way in
 * {@link IGTTool#definition$onBlockStartBreak(ItemStack, BlockPos, Player)}
 */
public class TreeFellingBehavior implements IToolBehavior<TreeFellingBehavior> {

    public static final TreeFellingBehavior INSTANCE = new TreeFellingBehavior();
    public static final MapCodec<TreeFellingBehavior> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, TreeFellingBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected TreeFellingBehavior() {/**/}

    @Override
    public ToolBehaviorType<TreeFellingBehavior> getType() {
        return GTToolBehaviors.TREE_FELLING;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.tree_felling"));
    }
}