package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import joptsimple.internal.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MachineConfigCopyBehaviour implements IInteractionItem, IAddInformation {

    private static final String NONE_DIRECTION = "null";
    private static final String CONFIG_DATA = "config_data";

    private static final Component ENABLED = Component.translatable("cover.voiding.label.enabled")
            .withStyle(ChatFormatting.GREEN);
    private static final Component DISABLED = Component.translatable("cover.voiding.label.disabled")
            .withStyle(ChatFormatting.RED);

    private static final String[] DIRECTION_STRINGS = {"§eDown§r", "§eUp§r", "§eNorth§r", "§eSouth§r", "§eWest§r", "§eEast§r"};

    private static void directionTooltip(List<Component> tooltips, CompoundTag tag, String langKey, String nbtKey) {
        if (!tag.contains(nbtKey)) return;
        Direction dir = stringToDirection(tag.getString(nbtKey));
        if (dir == null) return;
        tooltips.add(Component.translatable(langKey, Component.literal(DIRECTION_STRINGS[dir.ordinal()])));
    }

    private static void booleanTooltip(List<Component> tooltips, CompoundTag tag, String langKey, String nbtKey) {
        if (!tag.contains(nbtKey)) return;
        tooltips.add(Component.translatable(langKey, tag.getBoolean(nbtKey) ? ENABLED : DISABLED));
    }

    private static String directionToString(@Nullable Direction direction) {
        if (direction == null) return NONE_DIRECTION;
        return direction.getName();
    }

    private static @Nullable Direction stringToDirection(@Nullable String str) {
        if (Strings.isNullOrEmpty(str) || NONE_DIRECTION.equalsIgnoreCase(str)) return null;
        return Direction.byName(str);
    }

    private static Component directionListComponent(int directions) {
        List<String> dirStrings = new ArrayList<>();
        if ((directions & (1)) > 0) dirStrings.add(DIRECTION_STRINGS[0]);
        if ((directions & (1 << 1)) > 0) dirStrings.add(DIRECTION_STRINGS[1]);
        if ((directions & (1 << 2)) > 0) dirStrings.add(DIRECTION_STRINGS[2]);
        if ((directions & (1 << 3)) > 0) dirStrings.add(DIRECTION_STRINGS[3]);
        if ((directions & (1 << 4)) > 0) dirStrings.add(DIRECTION_STRINGS[4]);
        if ((directions & (1 << 5)) > 0) dirStrings.add(DIRECTION_STRINGS[5]);
        return Component.literal(String.join(", ", dirStrings));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        var blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        var player = context.getPlayer();

        if (blockEntity instanceof IMachineBlockEntity machineEntity) {
            if (!MachineOwner.canOpenOwnerMachine(context.getPlayer(), machineEntity.getMetaMachine())) {
                return InteractionResult.FAIL;
            }
            if (context.isSecondaryUseActive()) {
                stack.addTagElement(CONFIG_DATA, gatherMachineConfig(machineEntity.getMetaMachine()));
                if (player != null) player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.copied"), true);
            } else if (stack.getTagElement(CONFIG_DATA) != null) {
                if (player instanceof LocalPlayer) return InteractionResult.PASS;
                pasteMachineConfig((ServerPlayer)player, machineEntity.getMetaMachine(), Objects.requireNonNull(stack.getTagElement(CONFIG_DATA)));
                if (player != null) player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.pasted"), true);
            }
        }

        if (blockEntity instanceof PipeBlockEntity<?, ?> pipeBE) {
            if (context.isSecondaryUseActive()) {
                stack.addTagElement(CONFIG_DATA, gatherPipeConfig(pipeBE));
                if (player != null) player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.copied"), true);
            } else if (stack.getTagElement(CONFIG_DATA) != null) {
                if (player instanceof LocalPlayer) return InteractionResult.PASS;
                pastePipeConfig((ServerPlayer)player, pipeBE, Objects.requireNonNull(stack.getTagElement(CONFIG_DATA)));
                if (player != null) player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.pasted"), true);
            }
        }

        if (context.isSecondaryUseActive() && context.getLevel().getBlockState(context.getClickedPos()).isAir()) {
            stack.removeTagKey(CONFIG_DATA);
            if (player != null) player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.cleared"), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("behaviour.memory_card.tooltip.copy"));
        tooltipComponents.add(Component.translatable("behaviour.memory_card.tooltip.paste"));
        CompoundTag data = stack.getTagElement(CONFIG_DATA);
        if (data == null) return;
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(CommonComponents.EMPTY);
            addConfigTooltips(tooltipComponents, data);
        } else {
            tooltipComponents.add(Component.translatable("behaviour.memory_card.tooltip.view_stored"));
        }
    }


    //// Logic for actual config options

    // NBT keys for machine config values
    private static final String PIPE_CONNECTIONS = "pipe_connections";
    private static final String PIPE_BLOCKED_CONNECTIONS = "pipe_blocked_connections";

    private static final String COVER = "cover";
    private static final String FACING_DIR = "front_facing";

    private static final String ITEM_OUTPUT_SIDE = "output_direction_item";
    private static final String ITEM_AUTO_OUTPUT = "item_auto_output";
    private static final String ALLOW_ITEM_IN_FROM_OUT = "allow_input_from_output_item";

    private static final String FLUID_OUTPUT_SIDE = "output_direction_fluid";
    private static final String FLUID_AUTO_OUTPUT = "fluid_auto_output";
    private static final String ALLOW_FLUID_IN_FROM_OUT = "allow_input_from_output_fluid";

    private static final String MUFFLED = "muffled";
    private static final String CIRCUIT = "circuit_config";


    private static CompoundTag gatherPipeConfig(PipeBlockEntity<?, ?> pipe) {
        var tag = new CompoundTag();

        tag.putInt(PIPE_CONNECTIONS, pipe.getConnections());
        tag.putInt(PIPE_BLOCKED_CONNECTIONS, pipe.getBlockedConnections());

        return tag;
    }

    private static void pastePipeConfig(ServerPlayer player, PipeBlockEntity<?, ?> pipe, CompoundTag tag) {
        if (tag.contains(PIPE_CONNECTIONS)) {
            var connections = tag.getInt(PIPE_CONNECTIONS);

            // Connections have to be set like this due to pipenet jank.
            if (PipeBlockEntity.isConnected(connections, Direction.UP)) pipe.setConnection(Direction.UP, true, false);
            if (PipeBlockEntity.isConnected(connections, Direction.DOWN)) pipe.setConnection(Direction.DOWN, true, false);
            if (PipeBlockEntity.isConnected(connections, Direction.NORTH)) pipe.setConnection(Direction.NORTH, true, false);
            if (PipeBlockEntity.isConnected(connections, Direction.SOUTH)) pipe.setConnection(Direction.SOUTH, true, false);
            if (PipeBlockEntity.isConnected(connections, Direction.EAST)) pipe.setConnection(Direction.EAST, true, false);
            if (PipeBlockEntity.isConnected(connections, Direction.WEST)) pipe.setConnection(Direction.WEST, true, false);

        }
        if (tag.contains(PIPE_BLOCKED_CONNECTIONS)) {
            var blockedConnections = tag.getInt(PIPE_BLOCKED_CONNECTIONS);

            // Connections have to be set like this due to pipenet jank.
            if (PipeBlockEntity.isFaceBlocked(blockedConnections, Direction.UP)) pipe.setBlocked(Direction.UP, true);
            if (PipeBlockEntity.isFaceBlocked(blockedConnections, Direction.DOWN)) pipe.setBlocked(Direction.DOWN, true);
            if (PipeBlockEntity.isFaceBlocked(blockedConnections, Direction.NORTH)) pipe.setBlocked(Direction.NORTH, true);
            if (PipeBlockEntity.isFaceBlocked(blockedConnections, Direction.SOUTH)) pipe.setBlocked(Direction.SOUTH, true);
            if (PipeBlockEntity.isFaceBlocked(blockedConnections, Direction.EAST)) pipe.setBlocked(Direction.EAST, true);
            if (PipeBlockEntity.isFaceBlocked(blockedConnections, Direction.WEST)) pipe.setBlocked(Direction.WEST, true);

        }
    }

    private static CompoundTag gatherMachineConfig(MetaMachine machine) {
        var tag = new CompoundTag();

        tag.putString(FACING_DIR, directionToString(machine.getFrontFacing()));

        if (machine instanceof IAutoOutputItem autoOutputItem && autoOutputItem.getOutputFacingItems() != null) {
            tag.putString(ITEM_OUTPUT_SIDE, directionToString(autoOutputItem.getOutputFacingItems()));
            tag.putBoolean(ITEM_AUTO_OUTPUT, autoOutputItem.isAutoOutputItems());
            tag.putBoolean(ALLOW_ITEM_IN_FROM_OUT, autoOutputItem.isAllowInputFromOutputSideItems());
        }

        if (machine instanceof IAutoOutputFluid autoOutputFluid && autoOutputFluid.getOutputFacingFluids() != null) {
            tag.putString(FLUID_OUTPUT_SIDE, directionToString(autoOutputFluid.getOutputFacingFluids()));
            tag.putBoolean(FLUID_AUTO_OUTPUT, autoOutputFluid.isAutoOutputFluids());
            tag.putBoolean(ALLOW_FLUID_IN_FROM_OUT, autoOutputFluid.isAllowInputFromOutputSideFluids());
        }

        if (machine instanceof IMufflableMachine mufflableMachine) {
            tag.putBoolean(MUFFLED, mufflableMachine.isMuffled());
        }

        if (machine instanceof IHasCircuitSlot circuitMachine) {
            var circuit = IntCircuitBehaviour.getCircuitConfiguration(circuitMachine.getCircuitInventory().getStackInSlot(0));
            if (circuitMachine.isCircuitSlotEnabled() && circuit != 0) {
                tag.putInt(CIRCUIT, circuit);
            }
        }

        tag.put(COVER, machine.getCoverContainer().gatherConfig(new CompoundTag()));

        tag = machine.gatherConfig(tag);

        return tag;
    }


    private static void pasteMachineConfig(ServerPlayer player, MetaMachine machine, CompoundTag tag) {

        Direction facingDir = Direction.byName(tag.getString(FACING_DIR));
        if (facingDir != null) machine.setFrontFacing(facingDir);

        if (machine instanceof IAutoOutputItem autoOutputItem) {
            if (tag.contains(ITEM_OUTPUT_SIDE)) autoOutputItem.setOutputFacingItems(stringToDirection(tag.getString(ITEM_OUTPUT_SIDE)));
            if (tag.contains(ITEM_AUTO_OUTPUT)) autoOutputItem.setAutoOutputItems(tag.getBoolean(ITEM_AUTO_OUTPUT));
            if (tag.contains(ALLOW_ITEM_IN_FROM_OUT)) autoOutputItem.setAllowInputFromOutputSideItems(tag.getBoolean(ALLOW_ITEM_IN_FROM_OUT));
        }

        if (machine instanceof IAutoOutputFluid autoOutputFluid) {
            if (tag.contains(FLUID_OUTPUT_SIDE)) autoOutputFluid.setOutputFacingFluids(stringToDirection(tag.getString(FLUID_OUTPUT_SIDE)));
            if (tag.contains(FLUID_AUTO_OUTPUT)) autoOutputFluid.setAutoOutputFluids(tag.getBoolean(FLUID_AUTO_OUTPUT));
            if (tag.contains(ALLOW_FLUID_IN_FROM_OUT)) autoOutputFluid.setAllowInputFromOutputSideFluids(tag.getBoolean(ALLOW_FLUID_IN_FROM_OUT));
        }

        if (machine instanceof IMufflableMachine mufflableMachine) {
            if (tag.contains(MUFFLED)) mufflableMachine.setMuffled(tag.getBoolean(MUFFLED));
        }

        if (machine instanceof IHasCircuitSlot circuitMachine) {
            if (tag.contains(CIRCUIT)) circuitMachine.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(tag.getInt(CIRCUIT)));
        }

        machine.loadConfigTag(player, tag);

    }

    private static void addConfigTooltips(List<Component> tooltip, CompoundTag tag) {

        if (tag.contains(PIPE_CONNECTIONS) && tag.getInt(PIPE_CONNECTIONS) != 0) tooltip.add(Component.translatable("behaviour.setting.tooltip.pipe_connections", directionListComponent(tag.getInt(PIPE_CONNECTIONS))));
        if (tag.contains(PIPE_BLOCKED_CONNECTIONS) && tag.getInt(PIPE_BLOCKED_CONNECTIONS) != 0) tooltip.add(Component.translatable("behaviour.setting.tooltip.pipe_blocked_connections", directionListComponent(tag.getInt(PIPE_BLOCKED_CONNECTIONS))));

        directionTooltip(tooltip, tag, "behaviour.setting.tooltip.output_direction_item", ITEM_OUTPUT_SIDE);
        booleanTooltip(tooltip, tag, "behaviour.setting.tooltip.item_auto_output", ITEM_AUTO_OUTPUT);
        booleanTooltip(tooltip, tag, "behaviour.setting.tooltip.allow_input_from_output_item", ALLOW_ITEM_IN_FROM_OUT);

        directionTooltip(tooltip, tag, "behaviour.setting.tooltip.output_direction_fluid", FLUID_OUTPUT_SIDE);
        booleanTooltip(tooltip, tag, "behaviour.setting.tooltip.fluid_auto_output", FLUID_AUTO_OUTPUT);
        booleanTooltip(tooltip, tag, "behaviour.setting.tooltip.allow_input_from_output_fluid", ALLOW_FLUID_IN_FROM_OUT);

        booleanTooltip(tooltip, tag, "behaviour.setting.tooltip.muffled", MUFFLED);

        if (tag.contains(CIRCUIT)) tooltip.add(Component.translatable("behaviour.setting.tooltip.circuit_config", tag.getInt(CIRCUIT)));

    }

}
