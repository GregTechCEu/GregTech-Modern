package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import joptsimple.internal.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MachineConfigCopyBehaviour implements IInteractionItem, IAddInformation {

    private static final String NONE_DIRECTION = "null";
    private static final String CONFIG_DATA = "config_data";
    private static final String COPY_SOURCE = "copy_source";
    private static final String ITEMS_TO_PASTE = "items_to_paste";

    private static final Component ENABLED = Component.translatable("behaviour.memory_card.enabled");
    private static final Component DISABLED = Component.translatable("behaviour.memory_card.disabled");
    private static final String[] DIRECTION_STRINGS = { "§eDown§r", "§eUp§r", "§eNorth§r", "§eSouth§r", "§eWest§r",
            "§eEast§r" };

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

        if (player == null || player instanceof LocalPlayer) return InteractionResult.PASS;
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity &&
                !MachineOwner.canOpenOwnerMachine(context.getPlayer(), machineBlockEntity.getMetaMachine()))
            return InteractionResult.FAIL;

        if (context.isSecondaryUseActive()) {

            var configTag = stack.getOrCreateTagElement(CONFIG_DATA);

            if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
                configTag.putString(COPY_SOURCE,
                        (new ItemStack(blockEntity.getBlockState().getBlock().asItem())).getDisplayName().getString());
                configTag.merge(gatherMachineConfig(machineBlockEntity.getMetaMachine()));

                ListTag itemsTag = new ListTag();
                machineBlockEntity.getMetaMachine().getItemsRequiredToPaste()
                        .forEach(v -> itemsTag.add(v.serializeNBT()));
                configTag.put(ITEMS_TO_PASTE, itemsTag);
            } else if (blockEntity instanceof PipeBlockEntity<?, ?> pipeBE) {
                configTag.putString(COPY_SOURCE,
                        (new ItemStack(blockEntity.getBlockState().getBlock().asItem())).getDisplayName().getString());
                configTag.merge(gatherPipeConfig(pipeBE));

                ListTag itemsTag = new ListTag();
                pipeBE.getItemsRequiredToPaste().forEach(v -> itemsTag.add(v.serializeNBT()));
                configTag.put(ITEMS_TO_PASTE, itemsTag);
            } else {
                stack.removeTagKey(CONFIG_DATA);
                player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.cleared"), true);
                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.copied"), true);

        } else {
            var tag = stack.getTagElement(CONFIG_DATA);
            if (tag == null) return InteractionResult.FAIL;

            List<ItemStack> items = new ArrayList<>();
            tag.getList("itemsToPaste", CompoundTag.TAG_COMPOUND).forEach(t -> {
                if (t instanceof CompoundTag c) items.add(ItemStack.of(c));
            });

            if (!player.isCreative() && !GTTransferUtils.extractItemsFromPlayerInv(player, items, true)) {
                player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.missing_items"),
                        true);
                return InteractionResult.FAIL;
            } ;
            if (!player.isCreative()) GTTransferUtils.extractItemsFromPlayerInv(player, items, false);

            if (blockEntity instanceof IMachineBlockEntity machineBlockEntity)
                pasteMachineConfig((ServerPlayer) player, machineBlockEntity.getMetaMachine(), tag);
            if (blockEntity instanceof PipeBlockEntity<?, ?> pipeBE)
                pastePipeConfig((ServerPlayer) player, pipeBE, tag);

            player.displayClientMessage(Component.translatable("behaviour.memory_card.client_msg.pasted"), true);

        }

        return InteractionResult.SUCCESS;
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

            for (var dir : GTUtil.DIRECTIONS) {
                if (PipeBlockEntity.isConnected(connections, dir)) pipe.setConnection(dir, true, false);
            }

        }
        if (tag.contains(PIPE_BLOCKED_CONNECTIONS)) {
            var blockedConnections = tag.getInt(PIPE_BLOCKED_CONNECTIONS);

            for (var dir : GTUtil.DIRECTIONS) {
                if (PipeBlockEntity.isFaceBlocked(blockedConnections, dir)) pipe.setBlocked(dir, true);
            }

        }

        pipe.getCoverContainer().pasteConfig(player, tag.getCompound(COVER));
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
            var circuit = IntCircuitBehaviour
                    .getCircuitConfiguration(circuitMachine.getCircuitInventory().getStackInSlot(0));
            if (circuitMachine.isCircuitSlotEnabled() && circuit != 0) {
                tag.putInt(CIRCUIT, circuit);
            }
        }

        tag.put(COVER, machine.getCoverContainer().copyConfig(new CompoundTag()));

        tag = machine.copyConfig(tag);

        return tag;
    }

    private static void pasteMachineConfig(ServerPlayer player, MetaMachine machine, CompoundTag tag) {
        if (machine instanceof IAutoOutputItem autoOutputItem) {
            if (tag.contains(ITEM_OUTPUT_SIDE))
                autoOutputItem.setOutputFacingItems(stringToDirection(tag.getString(ITEM_OUTPUT_SIDE)));
            if (tag.contains(ITEM_AUTO_OUTPUT)) autoOutputItem.setAutoOutputItems(tag.getBoolean(ITEM_AUTO_OUTPUT));
            if (tag.contains(ALLOW_ITEM_IN_FROM_OUT))
                autoOutputItem.setAllowInputFromOutputSideItems(tag.getBoolean(ALLOW_ITEM_IN_FROM_OUT));
        }

        if (machine instanceof IAutoOutputFluid autoOutputFluid) {
            if (tag.contains(FLUID_OUTPUT_SIDE))
                autoOutputFluid.setOutputFacingFluids(stringToDirection(tag.getString(FLUID_OUTPUT_SIDE)));
            if (tag.contains(FLUID_AUTO_OUTPUT)) autoOutputFluid.setAutoOutputFluids(tag.getBoolean(FLUID_AUTO_OUTPUT));
            if (tag.contains(ALLOW_FLUID_IN_FROM_OUT))
                autoOutputFluid.setAllowInputFromOutputSideFluids(tag.getBoolean(ALLOW_FLUID_IN_FROM_OUT));
        }

        Direction facingDir = Direction.byName(tag.getString(FACING_DIR));
        if (facingDir != null) machine.setFrontFacing(facingDir);

        if (machine instanceof IMufflableMachine mufflableMachine) {
            if (tag.contains(MUFFLED)) mufflableMachine.setMuffled(tag.getBoolean(MUFFLED));
        }

        if (machine instanceof IHasCircuitSlot circuitMachine) {
            if (tag.contains(CIRCUIT))
                circuitMachine.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(tag.getInt(CIRCUIT)));
        }

        machine.getCoverContainer().pasteConfig(player, tag.getCompound(COVER));

        machine.pasteConfig(player, tag);
    }

    private static void addConfigTooltips(List<Component> tooltip, CompoundTag tag) {
        tooltip.add(Component.translatable("behaviour.memory_card.copy_target", tag.getString(COPY_SOURCE)));
        tooltip.add(Component.empty());

        if (tag.contains(PIPE_CONNECTIONS) && tag.getInt(PIPE_CONNECTIONS) != 0)
            tooltip.add(Component.translatable("behaviour.setting.tooltip.pipe_connections",
                    directionListComponent(tag.getInt(PIPE_CONNECTIONS))));
        if (tag.contains(PIPE_BLOCKED_CONNECTIONS) && tag.getInt(PIPE_BLOCKED_CONNECTIONS) != 0)
            tooltip.add(Component.translatable("behaviour.setting.tooltip.pipe_blocked_connections",
                    directionListComponent(tag.getInt(PIPE_BLOCKED_CONNECTIONS))));

        if (tag.contains(ITEM_OUTPUT_SIDE) && tag.contains(ITEM_AUTO_OUTPUT) && tag.contains(ALLOW_ITEM_IN_FROM_OUT)) {
            Component outputMode;
            if (tag.getBoolean(ITEM_AUTO_OUTPUT) && tag.getBoolean(ALLOW_ITEM_IN_FROM_OUT))
                outputMode = Component.translatable("behaviour.setting.tooltip.auto_output_allow_input");
            else if (tag.getBoolean(ITEM_AUTO_OUTPUT))
                outputMode = Component.translatable("behaviour.setting.tooltip.auto_output");
            else if (tag.getBoolean(ALLOW_ITEM_IN_FROM_OUT))
                outputMode = Component.translatable("behaviour.setting.tooltip.allow_input");
            else outputMode = Component.empty();

            Direction dir = stringToDirection(tag.getString(ITEM_OUTPUT_SIDE));
            if (dir == null) return;

            tooltip.add(Component.translatable("behaviour.setting.tooltip.item_io",
                    Component.literal(DIRECTION_STRINGS[dir.ordinal()]), outputMode));
        }

        if (tag.contains(FLUID_OUTPUT_SIDE) && tag.contains(FLUID_AUTO_OUTPUT) &&
                tag.contains(ALLOW_FLUID_IN_FROM_OUT)) {
            Component outputMode;
            if (tag.getBoolean(FLUID_AUTO_OUTPUT) && tag.getBoolean(ALLOW_FLUID_IN_FROM_OUT))
                outputMode = Component.translatable("behaviour.setting.tooltip.auto_output_allow_input");
            else if (tag.getBoolean(FLUID_AUTO_OUTPUT))
                outputMode = Component.translatable("behaviour.setting.tooltip.auto_output");
            else if (tag.getBoolean(ALLOW_FLUID_IN_FROM_OUT))
                outputMode = Component.translatable("behaviour.setting.tooltip.allow_input");
            else outputMode = Component.empty();

            Direction dir = stringToDirection(tag.getString(FLUID_OUTPUT_SIDE));
            if (dir == null) return;

            tooltip.add(Component.translatable("behaviour.setting.tooltip.fluid_io",
                    Component.literal(DIRECTION_STRINGS[dir.ordinal()]), outputMode));
        }

        if (tag.contains(MUFFLED)) tooltip.add(Component.translatable("behaviour.setting.tooltip.muffled",
                tag.getBoolean(MUFFLED) ? ENABLED : DISABLED));
        if (tag.contains(CIRCUIT)) tooltip.add(Component.translatable("behaviour.setting.tooltip.circuit_config")
                .append(Component.literal(Integer.toString(tag.getInt(CIRCUIT))).withStyle(ChatFormatting.YELLOW)));

        if (tag.contains("itemsToPaste")) {
            List<ItemStack> items = new ArrayList<>();
            tag.getList("itemsToPaste", CompoundTag.TAG_COMPOUND).forEach(t -> {
                if (t instanceof CompoundTag c) items.add(ItemStack.of(c));
            });

            if (items.isEmpty()) return;

            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("behaviour.memory_card.tooltip.items_to_paste"));

            for (var item : items) {
                tooltip.add(Component.literal("- " + item.getCount() + "x ").append(item.getDisplayName())
                        .withStyle(ChatFormatting.DARK_GREEN));
            }
        }
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
}
