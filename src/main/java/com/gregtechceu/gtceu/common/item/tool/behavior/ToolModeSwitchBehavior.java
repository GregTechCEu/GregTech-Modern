package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class ToolModeSwitchBehavior implements IToolBehavior<ToolModeSwitchBehavior> {

    public static final ToolModeSwitchBehavior INSTANCE = new ToolModeSwitchBehavior();

    public static final Codec<ToolModeSwitchBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, ToolModeSwitchBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected ToolModeSwitchBehavior() {}

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        var mode = stack.getOrDefault(GTDataComponents.TOOL_MODE, ModeType.BOTH);
        boolean canWrenchConfigureAll = action == GTItemAbilities.WRENCH_CONFIGURE_ALL;
        return action == GTItemAbilities.WRENCH_CONFIGURE || switch (mode) {
            case ITEM -> canWrenchConfigureAll || action == GTItemAbilities.WRENCH_CONFIGURE_ITEMS;
            case FLUID -> canWrenchConfigureAll || action == GTItemAbilities.WRENCH_CONFIGURE_FLUIDS;
            case BOTH -> GTItemAbilities.WRENCH_CONFIGURE_ACTIONS.contains(action);
        };
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

        if (player != null)
            world.getBlockState(pos).useItemOn(itemStack, world, player, context.getHand(), blockHitResult);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level world, @NotNull Player player,
                                                                        @NotNull InteractionHand hand) {
        var itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            var toolTypes = ToolHelper.getToolTypes(itemStack);
            if (toolTypes.contains(GTToolType.WRENCH)) {
                var newMode = itemStack.getOrDefault(GTDataComponents.TOOL_MODE, ModeType.BOTH).nextMode();
                itemStack.set(GTDataComponents.TOOL_MODE, newMode);

                player.displayClientMessage(
                        Component.translatable("metaitem.machine_configuration.mode", newMode.getName()),
                        true);
            }
            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public ToolBehaviorType<ToolModeSwitchBehavior> getType() {
        return GTToolBehaviors.MODE_SWITCH;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        ModeType behavior = stack.getOrDefault(GTDataComponents.TOOL_MODE, ModeType.BOTH);
        tooltip.add(Component.translatable("metaitem.machine_configuration.mode", behavior.getName()));
    }

    public enum ModeType implements StringRepresentable {

        ITEM("item", Component.translatable("gtceu.mode.item")),
        FLUID("fluid", Component.translatable("gtceu.mode.fluid")),
        BOTH("both", Component.translatable("gtceu.mode.both"));

        public static final Codec<ModeType> CODEC = StringRepresentable.fromEnum(ModeType::values);
        public static final StreamCodec<ByteBuf, ModeType> STREAM_CODEC = ByteBufCodecs.BYTE
                .map(aByte -> ModeType.values()[aByte], val -> (byte) val.ordinal());

        @Getter
        private final @NotNull String serializedName;
        @Getter
        private final Component name;

        ModeType(String id, Component name) {
            this.serializedName = id;
            this.name = name;
        }

        public ModeType nextMode() {
            return switch (this) {
                case ITEM -> FLUID;
                case FLUID -> BOTH;
                case BOTH -> ITEM;
            };
        }
    }
}
