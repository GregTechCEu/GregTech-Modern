package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;

public class ToolModeSwitchBehavior implements IToolBehavior {

    public static final ToolModeSwitchBehavior INSTANCE = new ToolModeSwitchBehavior();

    protected ToolModeSwitchBehavior() {}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        var mode = WrenchModeType.values()[getBehaviorsTag(stack).getByte("Mode")];
        boolean canWrenchConfigureAll = action == GTToolActions.WRENCH_CONFIGURE_ALL;
        return action == GTToolActions.WRENCH_CONFIGURE || switch (mode) {
            case ITEM -> canWrenchConfigureAll || action == GTToolActions.WRENCH_CONFIGURE_ITEMS;
            case FLUID -> canWrenchConfigureAll || action == GTToolActions.WRENCH_CONFIGURE_FLUIDS;
            case BOTH -> GTToolActions.WRENCH_CONFIGURE_ACTIONS.contains(action);
        };
    }

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull CompoundTag tag) {
        var toolTypes = ToolHelper.getToolTypes(stack);
        if (toolTypes.contains(GTToolType.WRENCH)) {
            tag.putByte("Mode", (byte) WrenchModeType.BOTH.ordinal());
        }
        IToolBehavior.super.addBehaviorNBT(stack, tag);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level world, @NotNull Player player,
                                                                        @NotNull InteractionHand hand) {
        var itemStack = player.getItemInHand(hand);
        var tagCompound = getBehaviorsTag(itemStack);
        if (player.isShiftKeyDown()) {

            var toolTypes = ToolHelper.getToolTypes(itemStack);
            if (toolTypes.contains(GTToolType.WRENCH)) {
                tagCompound.putByte("Mode",
                        (byte) ((tagCompound.getByte("Mode") + 1) % WrenchModeType.values().length));
                player.displayClientMessage(Component.translatable("metaitem.machine_configuration.mode",
                        WrenchModeType.values()[tagCompound.getByte("Mode")].getName()), true);
            }
            return InteractionResultHolder.success(itemStack);
        }

        return IToolBehavior.super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        var tagCompound = getBehaviorsTag(stack);

        var toolTypes = ToolHelper.getToolTypes(stack);
        if (toolTypes.contains(GTToolType.WRENCH)) {
            tooltip.add(Component.translatable("metaitem.machine_configuration.mode",
                    WrenchModeType.values()[tagCompound.getByte("Mode")].getName()));
        }
    }

    @Getter
    public enum WrenchModeType {

        ITEM(Component.translatable("gtceu.mode.item")),
        FLUID(Component.translatable("gtceu.mode.fluid")),
        BOTH(Component.translatable("gtceu.mode.both"));

        private final Component name;

        WrenchModeType(Component name) {
            this.name = name;
        }

        public boolean isItem() {
            return this == ITEM || this == BOTH;
        }

        public boolean isFluid() {
            return this == FLUID || this == BOTH;
        }
    }
}
