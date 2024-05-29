package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;

public class ToolModeSwitchBehavior implements IToolBehavior {

    public static final ToolModeSwitchBehavior INSTANCE = new ToolModeSwitchBehavior();

    protected ToolModeSwitchBehavior() {}

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull CompoundTag tag) {
        tag.putByte("Mode", (byte) ModeType.BOTH.ordinal());
        IToolBehavior.super.addBehaviorNBT(stack, tag);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level world, @NotNull Player player,
                                                                        @NotNull InteractionHand hand) {
        var itemStack = player.getItemInHand(hand);
        var tagCompound = getBehaviorsTag(itemStack);
        if (player.isShiftKeyDown()) {
            tagCompound.putByte("Mode", (byte) ((tagCompound.getByte("Mode") + 1) % ModeType.values().length));

            player.displayClientMessage(Component.translatable("metaitem.machine_configuration.mode",
                    ModeType.values()[tagCompound.getByte("Mode")].getName()), true);
            return InteractionResultHolder.success(itemStack);
        }

        return IToolBehavior.super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        var tagCompound = getBehaviorsTag(stack);
        tooltip.add(Component.translatable("metaitem.machine_configuration.mode",
                ModeType.values()[tagCompound.getByte("Mode")].getName()));
    }

    public static enum ModeType {

        ITEM(Component.translatable("gtceu.mode.item")),
        FLUID(Component.translatable("gtceu.mode.fluid")),
        BOTH(Component.translatable("gtceu.mode.both"));

        @Getter
        private final Component name;

        private ModeType(Component name) {
            this.name = name;
        }
    }
}
