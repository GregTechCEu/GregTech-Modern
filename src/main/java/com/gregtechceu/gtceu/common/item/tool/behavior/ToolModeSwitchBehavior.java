package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

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
    public @NotNull InteractionResult onItemUse(UseOnContext context) {
        Level world = context.getLevel();
        BlockHitResult blockHitResult = context.getHitResult();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockState state = world.getBlockState(blockHitResult.getBlockPos());
        BlockPos pos = blockHitResult.getBlockPos();
        Set<GTToolType> toolTypes = ToolHelper.getToolTypes(itemStack);
        // Copied and adapted from
        // https://github.com/Creators-of-Create/Create/blob/mc1.20.1/dev/src/main/java/com/simibubi/create/content/equipment/wrench/WrenchItem.java
        if (toolTypes.contains(GTToolType.WRENCH) && GTCEu.Mods.isCreateLoaded() &&
                state.is(CustomTags.CREATE_WRENCH_PICKUP)) {
            if (!(world instanceof ServerLevel serverLevel))
                return InteractionResult.SUCCESS;
            if (player != null && !player.isCreative())
                Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos), player, itemStack)
                        .forEach(stack -> player.getInventory().placeItemBackInInventory(stack));
            state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
            world.destroyBlock(pos, false);
            GTSoundEntries.WRENCH_TOOL.playOnServer(serverLevel, pos, 1, GTValues.RNG.nextFloat() * .5f + .5f);
            return InteractionResult.SUCCESS;
        }

        if (player != null) world.getBlockState(pos).use(world, player, context.getHand(), blockHitResult);
        return InteractionResult.SUCCESS;
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
