package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaMachineConfigCopyBehaviour implements IInteractionItem, IAddInformation {

    public static final String CONFIG_DATA = "config_data";
    public static final String ORIGINAL_FRONT = "front";
    public static final String ITEM_CONFIG = "item";
    public static final String FLUID_CONFIG = "fluid";
    public static final String DIRECTION = "direction";
    public static final String AUTO = "auto";
    public static final String INPUT_FROM_OUTPUT_SIDE = "in_from_out";
    public static final String MUFFLED = "muffled";

    public static int directionToInt(@Nullable Direction direction) {
        return direction == null ? 0 : direction.ordinal() + 1;
    }

    public static Direction intToDirection(int ordinal) {
        return ordinal <= 0 || ordinal > Direction.values().length ? null : Direction.values()[ordinal - 1];
    }

    public static Component relativeDirectionComponent(Direction origFront, Direction origDirection) {
        if (origFront == origDirection) {
            return Component.translatable("gtceu.direction.tooltip.front").withStyle(ChatFormatting.YELLOW);
        }
        if (Direction.UP == origDirection) {
            return Component.translatable("gtceu.direction.tooltip.up").withStyle(ChatFormatting.YELLOW);
        }
        if (Direction.DOWN == origDirection) {
            return Component.translatable("gtceu.direction.tooltip.down").withStyle(ChatFormatting.YELLOW);
        }
        var face = origFront;
        int i;
        for (i = 0; i < 3; i++) {
            face = face.getClockWise();
            if (face == origDirection) break;
        }
        return switch (i) {
            case 0 -> Component.translatable("gtceu.direction.tooltip.right").withStyle(ChatFormatting.YELLOW);
            case 1 -> Component.translatable("gtceu.direction.tooltip.back").withStyle(ChatFormatting.YELLOW);
            case 2 -> Component.translatable("gtceu.direction.tooltip.left").withStyle(ChatFormatting.YELLOW);
            default -> Component.literal("");
        };
    }

    public static Direction getRelativeDirection(Direction originalFront, Direction currentFacing, Direction face) {
        if ((currentFacing == null || originalFront == null) || (currentFacing == originalFront) ||
                (face == Direction.UP || face == Direction.DOWN))
            return face;

        Direction newFace = originalFront;
        int i;
        for (i = 0; i < 4 && newFace != currentFacing; i++) newFace = newFace.getClockWise();

        newFace = face;
        for (int j = 0; j < i; j++) newFace = newFace.getClockWise();
        return newFace;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) {
            stack.setTag(null);
            return InteractionResultHolder.success(stack);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity blockEntity) {
            if (!IMachineOwner.canOpenOwnerMachine(context.getPlayer(), blockEntity))
                return InteractionResult.FAIL;
            MetaMachine machine = blockEntity.getMetaMachine();
            if (context.isSecondaryUseActive())
                return handleCopy(stack, machine);
            else if (stack.hasTag() && stack.getTag().contains(CONFIG_DATA))
                return handlePaste(stack, machine);
        } else
            if (context.isSecondaryUseActive() && context.getLevel().getBlockState(context.getClickedPos()).isAir()) {
                stack.setTag(null);
                return InteractionResult.SUCCESS;
            }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult handleCopy(ItemStack stack, MetaMachine machine) {
        CompoundTag configData = new CompoundTag();
        configData.putInt(ORIGINAL_FRONT, directionToInt(machine.getFrontFacing()));
        if (machine instanceof IAutoOutputItem autoOutputItem && autoOutputItem.getOutputFacingItems() != null) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt(DIRECTION, directionToInt(autoOutputItem.getOutputFacingItems()));
            itemTag.putBoolean(AUTO, autoOutputItem.isAutoOutputItems());
            itemTag.putBoolean(INPUT_FROM_OUTPUT_SIDE, autoOutputItem.isAllowInputFromOutputSideItems());
            configData.put(ITEM_CONFIG, itemTag);
        }
        if (machine instanceof IAutoOutputFluid autoOutputFluid && autoOutputFluid.getOutputFacingFluids() != null) {
            CompoundTag fluidTag = new CompoundTag();
            fluidTag.putInt(DIRECTION, directionToInt(autoOutputFluid.getOutputFacingFluids()));
            fluidTag.putBoolean(AUTO, autoOutputFluid.isAutoOutputFluids());
            fluidTag.putBoolean(INPUT_FROM_OUTPUT_SIDE, autoOutputFluid.isAllowInputFromOutputSideFluids());
            configData.put(FLUID_CONFIG, fluidTag);
        }
        if (machine instanceof IMufflableMachine mufflableMachine) {
            configData.putBoolean(MUFFLED, mufflableMachine.isMuffled());
        }
        if (!configData.isEmpty()) {
            stack.getOrCreateTag().put(CONFIG_DATA, configData);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult handlePaste(ItemStack stack, MetaMachine machine) {
        if (!stack.hasTag() || !stack.getTag().contains(CONFIG_DATA)) return InteractionResult.PASS;
        CompoundTag root = stack.getOrCreateTag();
        CompoundTag configData = root.getCompound(CONFIG_DATA);
        Direction originalFront = intToDirection(configData.getInt(ORIGINAL_FRONT));
        if (configData.contains(ITEM_CONFIG) && machine instanceof IAutoOutputItem autoOutputItem) {
            CompoundTag itemData = configData.getCompound(ITEM_CONFIG);
            autoOutputItem.setOutputFacingItems(getRelativeDirection(originalFront, machine.getFrontFacing(),
                    intToDirection(itemData.getInt(DIRECTION))));
            autoOutputItem.setAutoOutputItems(itemData.getBoolean(AUTO));
            autoOutputItem.setAllowInputFromOutputSideItems(itemData.getBoolean(INPUT_FROM_OUTPUT_SIDE));
        }
        if (configData.contains(FLUID_CONFIG) && machine instanceof IAutoOutputFluid autoOutputFluid) {
            CompoundTag fluidData = configData.getCompound(FLUID_CONFIG);
            autoOutputFluid.setOutputFacingFluids(getRelativeDirection(originalFront, machine.getFrontFacing(),
                    intToDirection(fluidData.getInt(DIRECTION))));
            autoOutputFluid.setAutoOutputFluids(fluidData.getBoolean(AUTO));
            autoOutputFluid.setAllowInputFromOutputSideFluids(fluidData.getBoolean(INPUT_FROM_OUTPUT_SIDE));
        }
        if (configData.contains(MUFFLED) && machine instanceof IMufflableMachine mufflableMachine) {
            mufflableMachine.setMuffled(configData.getBoolean(MUFFLED));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("behaviour.meta.machine.config.copy.tooltip"));
        tooltipComponents.add(Component.translatable("behaviour.meta.machine.config.paste.tooltip"));
        if (!stack.hasTag()) return;
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.literal(""));
            var tag = stack.getOrCreateTag();
            if (tag.contains(CONFIG_DATA)) {
                var data = tag.getCompound(CONFIG_DATA);
                var enabledComponent = Component.translatable("cover.voiding.label.enabled")
                        .withStyle(ChatFormatting.GREEN);
                var disabledComponent = Component.translatable("cover.voiding.label.disabled")
                        .withStyle(ChatFormatting.RED);
                if (data.contains(ORIGINAL_FRONT)) {
                    var origFront = intToDirection(data.getInt(ORIGINAL_FRONT));
                    if (data.contains(ITEM_CONFIG)) {
                        var itemData = data.getCompound(ITEM_CONFIG);
                        var itemComponent = Component.translatable("recipe.capability.item.name")
                                .withStyle(ChatFormatting.GOLD);
                        tooltipComponents.add(Component.translatable("behaviour.setting.output.direction.tooltip",
                                itemComponent,
                                relativeDirectionComponent(origFront, intToDirection(itemData.getInt(DIRECTION)))));
                        tooltipComponents
                                .add(Component.translatable("behaviour.setting.item_auto_output.tooltip", itemComponent,
                                        (itemData.getBoolean(AUTO) ? enabledComponent : disabledComponent)));
                        tooltipComponents.add(Component.translatable(
                                "behaviour.setting.allow.input.from.output.tooltip", itemComponent,
                                (itemData.getBoolean(INPUT_FROM_OUTPUT_SIDE) ? enabledComponent : disabledComponent)));
                    }
                    if (data.contains(FLUID_CONFIG)) {
                        var fluidData = data.getCompound(FLUID_CONFIG);
                        var fluidComponent = Component.translatable("recipe.capability.fluid.name")
                                .withStyle(ChatFormatting.BLUE);
                        tooltipComponents.add(Component.translatable("behaviour.setting.output.direction.tooltip",
                                fluidComponent,
                                relativeDirectionComponent(origFront, intToDirection(fluidData.getInt(DIRECTION)))));
                        tooltipComponents.add(
                                Component.translatable("behaviour.setting.item_auto_output.tooltip", fluidComponent,
                                        (fluidData.getBoolean(AUTO) ? enabledComponent : disabledComponent)));
                        tooltipComponents.add(Component.translatable(
                                "behaviour.setting.allow.input.from.output.tooltip", fluidComponent,
                                (fluidData.getBoolean(INPUT_FROM_OUTPUT_SIDE) ? enabledComponent : disabledComponent)));
                    }
                }
                if (data.contains(MUFFLED)) {
                    tooltipComponents.add(Component.translatable("behaviour.setting.muffled.tooltip",
                            data.getBoolean(MUFFLED) ? enabledComponent : disabledComponent));
                }
            }
        } else {
            tooltipComponents.add(Component.translatable("item.toggle.advanced.info.tooltip"));
        }
    }
}
