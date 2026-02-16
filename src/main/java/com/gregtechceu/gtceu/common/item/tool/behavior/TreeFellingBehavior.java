package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviors;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Tree Felling Behavior must be handled in a special way in
 * {@link IGTTool#definition$onBlockStartBreak(ItemStack, BlockPos, Player)}
 */
public class TreeFellingBehavior implements IToolBehavior<TreeFellingBehavior> {

    // spotless:off
    public static final TreeFellingBehavior INSTANCE = new TreeFellingBehavior(true);
    public static final Codec<TreeFellingBehavior> CODEC = Codec.BOOL.xmap(TreeFellingBehavior::new, TreeFellingBehavior::isEnabled);
    public static final StreamCodec<ByteBuf, TreeFellingBehavior> STREAM_CODEC = ByteBufCodecs.BOOL.map(TreeFellingBehavior::new, TreeFellingBehavior::isEnabled);
    // spotless:on
    @Getter
    private final boolean enabled;

    protected TreeFellingBehavior(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ToolBehaviorType<TreeFellingBehavior> getType() {
        return GTToolBehaviors.TREE_FELLING;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return action == GTItemAbilities.AXE_FELL_TREE;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.tree_felling"));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level level,
                                                                        Player player, @NotNull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(held);
        }
        ToolBehaviors component = ToolHelper.getBehaviorsComponent(held);
        component.withBehavior(new TreeFellingBehavior(!this.enabled));
        player.sendSystemMessage(Component.translatable("item.gtceu.tool.behavior.tree_felling").append(" - ")
                .append(Component.translatable("cover.voiding.label." + (!enabled ? "enabled" : "disabled"))));
        return InteractionResultHolder.success(held);
    }
}
