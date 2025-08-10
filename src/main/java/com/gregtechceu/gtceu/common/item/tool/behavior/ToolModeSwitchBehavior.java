package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketToolRightClick;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import com.simibubi.create.AllTags;
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
        ClipContext ctx = new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getViewVector(0).scale(player.isCreative() ? 5 : 4.5)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                null);
        BlockHitResult blockHitResult = world.clip(ctx);
        if (player.isShiftKeyDown() && blockHitResult.getType() == HitResult.Type.MISS) {
            var toolTypes = ToolHelper.getToolTypes(itemStack);
            if (toolTypes.contains(GTToolType.WRENCH)) {
                tagCompound.putByte("Mode",
                        (byte) ((tagCompound.getByte("Mode") + 1) % WrenchModeType.values().length));
                player.displayClientMessage(Component.translatable("metaitem.machine_configuration.mode",
                        WrenchModeType.values()[tagCompound.getByte("Mode")].getName()), true);
            }
            return InteractionResultHolder.success(itemStack);
        } else if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockState state = world.getBlockState(blockHitResult.getBlockPos());
            BlockPos pos = blockHitResult.getBlockPos();
            Set<GTToolType> toolTypes = ToolHelper.getToolTypes(itemStack);
            if (toolTypes.contains(GTToolType.WRENCH) && GTCEu.Mods.isCreateLoaded() &&
                    AllTags.AllBlockTags.WRENCH_PICKUP.matches(state)) {
                if (!(world instanceof ServerLevel serverLevel))
                    return InteractionResultHolder.success(itemStack);
                if (!player.isCreative())
                    Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos), player, itemStack)
                            .forEach(stack -> player.getInventory().placeItemBackInInventory(stack));
                state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
                world.destroyBlock(pos, false);
                GTSoundEntries.WRENCH_TOOL.playOnServer(serverLevel, pos, 1, GTValues.RNG.nextFloat() * .5f + .5f);
                return InteractionResultHolder.success(itemStack);
            }

            if (world.isClientSide()) {
                if (Minecraft.getInstance().hitResult instanceof BlockHitResult clientHitResult)
                    GTNetwork.sendToServer(new CPacketToolRightClick(clientHitResult));
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
