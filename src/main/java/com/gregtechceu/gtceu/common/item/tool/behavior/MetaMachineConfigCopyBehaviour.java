package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import joptsimple.internal.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MetaMachineConfigCopyBehaviour implements IInteractionItem, IAddInformation {

    public static final String NONE_DIRECTION = "null";

    public static final String CONFIG_DATA = "config_data";
    public static final String ORIGINAL_FRONT = "front";
    public static final String ITEM_CONFIG = "item";
    public static final String FLUID_CONFIG = "fluid";
    public static final String DIRECTION = "direction";
    public static final String AUTO = "auto";
    public static final String INPUT_FROM_OUTPUT_SIDE = "in_from_out";
    public static final String MUFFLED = "muffled";
    public static final String PARALLEL = "parallel";
    public static final String COVERS = "covers";
    public static final String COVER_ITEM = "cover_item";
    public static final String COVER_CONFIG = "cover_config";
    public static final String ORIGINAL_COVER_SIDE = "cover_side";
    public static final String POSITION = "position";
    public static final String BLOCK_ITEM = "place_item";
    public static final String PARTS = "parts";
    public static final String MULTIBLOCK = "multiblock";
    public static final String CUSTOM_DATA = "custom_data";
    public static final String GHOST_CIRCUITS = "ghost_circuits";
    public static final String CIRCUIT_SLOT = "circuit_inventory";
    public static final String CIRCUIT_SLOT_ENABLED = "circuit_enabled";

    public static final Component ENABLED = Component.translatable("cover.voiding.label.enabled")
            .withStyle(ChatFormatting.GREEN);
    public static final Component DISABLED = Component.translatable("cover.voiding.label.disabled")
            .withStyle(ChatFormatting.RED);

    public static final Component[] DIRECTION_TOOLTIPS = {
            Component.translatable("gtceu.direction.tooltip.up").withStyle(ChatFormatting.YELLOW),
            Component.translatable("gtceu.direction.tooltip.down").withStyle(ChatFormatting.YELLOW),
            Component.translatable("gtceu.direction.tooltip.left").withStyle(ChatFormatting.YELLOW),
            Component.translatable("gtceu.direction.tooltip.right").withStyle(ChatFormatting.YELLOW),
            Component.translatable("gtceu.direction.tooltip.front").withStyle(ChatFormatting.YELLOW),
            Component.translatable("gtceu.direction.tooltip.back").withStyle(ChatFormatting.YELLOW),
    };

    public static String directionToString(@Nullable Direction direction) {
        if (direction == null) return NONE_DIRECTION;
        return direction.getSerializedName();
    }

    public static @Nullable Direction tagToDirection(@Nullable Tag tag) {
        if (tag instanceof StringTag string) {
            String name = string.getAsString();
            if (Strings.isNullOrEmpty(name) || NONE_DIRECTION.equalsIgnoreCase(name)) return null;
            return Direction.byName(name);
        } else if (tag instanceof NumericTag number) {
            // backwards compatibility
            int ordinal = number.getAsInt();
            return ordinal <= 0 || ordinal > Direction.values().length ? null : Direction.values()[ordinal - 1];
        }
        return null;
    }

    public static Component relativeDirectionComponent(Direction origFront, Direction origDirection) {
        RelativeDirection relative = RelativeDirection.findRelativeOf(origFront, origDirection);
        return DIRECTION_TOOLTIPS[relative.ordinal()];
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) {
            stack.removeTagKey(CONFIG_DATA);
            return InteractionResultHolder.success(stack);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.isSecondaryUseActive()) {
            return handleCopy(stack.getOrCreateTag(), context.getLevel(), context.getClickedPos());
        } else if (stack.getTagElement(CONFIG_DATA) != null) {
            return handlePaste(stack.getOrCreateTag(), context.getLevel(), context.getClickedPos(),
                    context.getPlayer() == null ? null :
                            new CustomItemStackHandler(context.getPlayer().getInventory().items));
        } else return InteractionResult.SUCCESS;
    }

    public static InteractionResult handleCopy(CompoundTag tag, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machineBE)
            return handleCopy(tag, machineBE.getMetaMachine());
        else if (level.getBlockEntity(pos) instanceof ICopyable copyable) {
            CompoundTag customData = new CompoundTag();
            copyable.copyConfig(customData);
            tag.put(CUSTOM_DATA, customData);
            return InteractionResult.SUCCESS;
        } else if (level.getBlockEntity(pos) == null) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                tag.remove(CUSTOM_DATA);
                return InteractionResult.SUCCESS;
            }
            tag.put(BLOCK_ITEM, new ItemStack(state.getBlock().asItem()).serializeNBT());
            // TODO copy entire block state (should this feature even exist?)
            return InteractionResult.SUCCESS;
        } else return InteractionResult.PASS;
    }

    public static InteractionResult handlePaste(CompoundTag tag, Level level, BlockPos pos,
                                                @Nullable IItemHandler itemHandler) {
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machineBE)
            return handlePaste(tag, machineBE.getMetaMachine(), itemHandler);
        else if (tag.contains(CUSTOM_DATA) && level.getBlockEntity(pos) instanceof ICopyable copyable) {
            if (consumeItems(itemHandler, copyable.getItemsRequiredForPaste(tag.getCompound(CUSTOM_DATA)))) {
                copyable.pasteConfig(tag.getCompound(CUSTOM_DATA));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        } else if (tag.contains(BLOCK_ITEM) && level.getBlockEntity(pos) == null) {
            if (!level.getBlockState(pos).isAir()) return InteractionResult.PASS;
            ItemStack stack = ItemStack.of(tag.getCompound(BLOCK_ITEM));
            if (stack.getItem() instanceof BlockItem item && consumeItems(itemHandler, Map.of(item, 1))) {
                level.setBlockAndUpdate(pos, item.getBlock().defaultBlockState());
                // TODO paste entire block state
                return InteractionResult.SUCCESS;
            } else return InteractionResult.PASS;
        } else return InteractionResult.PASS;
    }

    public static InteractionResult handleCopy(CompoundTag tag, MetaMachine machine) {
        CompoundTag configData = new CompoundTag();
        configData.putString(ORIGINAL_FRONT, directionToString(machine.getFrontFacing()));
        if (machine instanceof IAutoOutputItem autoOutputItem && autoOutputItem.getOutputFacingItems() != null) {
            configData.put(ITEM_CONFIG, copyOutputConfig(autoOutputItem.getOutputFacingItems(),
                    autoOutputItem.isAutoOutputItems(), autoOutputItem.isAllowInputFromOutputSideItems()));
        }
        if (machine instanceof IAutoOutputFluid autoOutputFluid && autoOutputFluid.getOutputFacingFluids() != null) {
            configData.put(FLUID_CONFIG, copyOutputConfig(autoOutputFluid.getOutputFacingFluids(),
                    autoOutputFluid.isAutoOutputFluids(), autoOutputFluid.isAllowInputFromOutputSideFluids()));
        }
        if (machine instanceof IMufflableMachine mufflableMachine) {
            configData.putBoolean(MUFFLED, mufflableMachine.isMuffled());
        }
        if (machine instanceof IParallelHatch parallelHatch) {
            configData.putInt(PARALLEL, parallelHatch.getCurrentParallel());
        }
        ListTag covers = new ListTag();
        for (Direction face : Direction.values()) {
            CoverBehavior cover = machine.getCoverContainer().getCoverAtSide(face);
            if (cover != null) {
                CompoundTag coverTag = new CompoundTag();
                CompoundTag coverConfig = new CompoundTag();
                cover.copyConfig(coverConfig);
                coverTag.putString(ORIGINAL_COVER_SIDE, directionToString(face));
                coverTag.put(COVER_CONFIG, coverConfig);
                coverTag.put(COVER_ITEM, cover.getAttachItem().serializeNBT());
                covers.add(coverTag);
            }
        }
        if (!covers.isEmpty()) configData.put(COVERS, covers);
        if (machine instanceof IHasCircuitSlot circuitSlotMachine) {
            CompoundTag circuitSlotTag = new CompoundTag();
            circuitSlotTag.put(CIRCUIT_SLOT, circuitSlotMachine.getCircuitInventory().storage.serializeNBT());
            circuitSlotTag.putBoolean(CIRCUIT_SLOT_ENABLED, circuitSlotMachine.isCircuitSlotEnabled());
            configData.put(GHOST_CIRCUITS, circuitSlotTag);
        }
        if (machine.getLevel() != null && machine instanceof MultiblockControllerMachine multiblock) {
            CompoundTag multiblockTag = new CompoundTag();
            ListTag parts = new ListTag();
            for (BlockPos partPos : multiblock.getPartPositions()) {
                CompoundTag partTag = new CompoundTag();
                partTag.put(POSITION, posToTag(partPos.subtract(machine.getPos())));
                partTag.put(BLOCK_ITEM,
                        new ItemStack(machine.getLevel().getBlockState(partPos).getBlock().asItem()).serializeNBT());
                CompoundTag partConfig = new CompoundTag();
                handleCopy(partConfig, machine.getLevel(), partPos);
                partTag.put(CONFIG_DATA, partConfig);
                if (machine.getLevel().getBlockEntity(partPos) instanceof IMachineBlockEntity machineBE)
                    partTag.putString(ORIGINAL_FRONT, directionToString(machineBE.getMetaMachine().getFrontFacing()));
                parts.add(partTag);
            }
            multiblockTag.put(PARTS, parts);
            configData.put(MULTIBLOCK, multiblockTag);
        }
        if (machine instanceof ICopyable copyable) {
            CompoundTag customData = new CompoundTag();
            copyable.copyConfig(customData);
            tag.put(CUSTOM_DATA, customData);
        }
        if (!configData.isEmpty()) {
            tag.put(CONFIG_DATA, configData);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult handlePaste(CompoundTag tag, MetaMachine machine,
                                                @Nullable IItemHandler itemHandler) {
        if (!tag.contains(CONFIG_DATA)) return InteractionResult.PASS;
        CompoundTag configData = tag.getCompound(CONFIG_DATA);
        Direction originalFront = tagToDirection(configData.get(ORIGINAL_FRONT));
        if (configData.contains(ITEM_CONFIG) && machine instanceof IAutoOutputItem autoOutputItem) {
            pasteOutputConfig(originalFront, machine.getFrontFacing(), configData.getCompound(ITEM_CONFIG),
                    autoOutputItem::setOutputFacingItems, autoOutputItem::setAutoOutputItems,
                    autoOutputItem::setAllowInputFromOutputSideItems);
        }
        if (configData.contains(FLUID_CONFIG) && machine instanceof IAutoOutputFluid autoOutputFluid) {
            pasteOutputConfig(originalFront, machine.getFrontFacing(), configData.getCompound(FLUID_CONFIG),
                    autoOutputFluid::setOutputFacingFluids, autoOutputFluid::setAutoOutputFluids,
                    autoOutputFluid::setAllowInputFromOutputSideFluids);
        }
        if (configData.contains(MUFFLED) && machine instanceof IMufflableMachine mufflableMachine) {
            mufflableMachine.setMuffled(configData.getBoolean(MUFFLED));
        }
        if (configData.contains(PARALLEL) && machine instanceof ParallelHatchPartMachine parallelHatch) {
            parallelHatch.setCurrentParallel(configData.getInt(PARALLEL));
        }
        if (configData.contains(COVERS)) {
            ListTag covers = configData.getList(COVERS, Tag.TAG_COMPOUND);
            for (Tag coverTag : covers) {
                if (!(coverTag instanceof CompoundTag compoundTag)) continue;
                ItemStack coverItem = ItemStack.of(compoundTag.getCompound(COVER_ITEM));
                if (!consumeItems(itemHandler, Map.of(coverItem.getItem(), 1))) continue;
                Direction originalFace = tagToDirection(compoundTag.get(ORIGINAL_COVER_SIDE));
                Direction face = RelativeDirection.getActualDirection(originalFront, machine.getFrontFacing(),
                        originalFace);
                CoverBehavior cover = null;
                if (coverItem.getItem() instanceof IComponentItem item) {
                    for (IItemComponent component : item.getComponents()) {
                        if (component instanceof CoverPlaceBehavior coverPlaceBehavior) {
                            cover = coverPlaceBehavior.coverDefinition()
                                    .createCoverBehavior(machine.getCoverContainer(), face);
                            machine.getCoverContainer().setCoverAtSide(cover, face);
                            break;
                        }
                    }
                }
                if (cover == null) continue;
                CompoundTag coverConfig = compoundTag.getCompound(COVER_CONFIG);
                if (consumeItems(itemHandler, cover.getItemsRequiredForPaste(coverConfig))) {
                    cover.pasteConfig(coverConfig);
                }
            }
        }
        if (configData.contains(GHOST_CIRCUITS) && machine instanceof IHasCircuitSlot circuitSlotMachine) {
            if (configData.getCompound(GHOST_CIRCUITS).getBoolean(CIRCUIT_SLOT_ENABLED))
                circuitSlotMachine.getCircuitInventory().storage
                        .deserializeNBT(configData.getCompound(GHOST_CIRCUITS).getCompound(CIRCUIT_SLOT));
        }
        if (configData.contains(MULTIBLOCK) && machine instanceof MultiblockControllerMachine multiblock &&
                machine.getLevel() != null) {
            Direction front = multiblock.getFrontFacing();
            Direction spin = multiblock.getUpwardsFacing();
            boolean flipped = multiblock.isFlipped();
            Direction right = RelativeDirection.RIGHT.getRelative(front, spin, flipped);
            Direction up = RelativeDirection.UP.getRelative(front, spin, flipped);
            CompoundTag multiblockTag = configData.getCompound(MULTIBLOCK);
            ListTag parts = multiblockTag.getList(PARTS, Tag.TAG_COMPOUND);
            Map<Item, Integer> requiredItems = new HashMap<>();
            for (Tag part : parts) if (part instanceof CompoundTag partTag) {
                Item reqItem = ItemStack.of(partTag.getCompound(BLOCK_ITEM)).getItem();
                requiredItems.put(reqItem, requiredItems.getOrDefault(reqItem, 0) + 1);
            }
            if (consumeItems(itemHandler, requiredItems)) {
                for (Tag part : parts) {
                    if (!(part instanceof CompoundTag partTag)) continue;
                    BlockPos relPos = posFromTag(partTag.get(POSITION));
                    BlockPos.MutableBlockPos pos = new BlockPos(relPos.get(front.getAxis()), relPos.get(up.getAxis()),
                            relPos.get(right.getAxis())).mutable();
                    pos.move(machine.getPos());
                    ItemStack stack = ItemStack.of(partTag.getCompound(BLOCK_ITEM));
                    if (stack.getItem() instanceof BlockItem blockItem) {
                        machine.getLevel().setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());
                        if (partTag.contains(CONFIG_DATA) &&
                                machine.getLevel().getBlockEntity(pos) instanceof IMachineBlockEntity machineBE) {
                            if (partTag.contains(ORIGINAL_FRONT))
                                machineBE.getMetaMachine().setFrontFacing(
                                        RelativeDirection.getActualDirection(
                                                originalFront, multiblock.getFrontFacing(),
                                                tagToDirection(partTag.get(ORIGINAL_FRONT))));
                        }
                        handlePaste(partTag.getCompound(CONFIG_DATA), machine.getLevel(), pos, itemHandler);
                    }
                }
            }
        }
        if (configData.contains(CUSTOM_DATA) && machine instanceof ICopyable copyable) {
            if (consumeItems(itemHandler, copyable.getItemsRequiredForPaste(configData.getCompound(CUSTOM_DATA)))) {
                copyable.pasteConfig(configData.getCompound(CUSTOM_DATA));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("behaviour.meta.machine.config.copy.tooltip"));
        tooltipComponents.add(Component.translatable("behaviour.meta.machine.config.paste.tooltip"));
        CompoundTag data = stack.getTagElement(CONFIG_DATA);
        if (data == null) return;
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(CommonComponents.EMPTY);
            if (data.contains(ORIGINAL_FRONT)) {
                var origFront = tagToDirection(data.get(ORIGINAL_FRONT));
                for (RecipeCapability<?> cap : GTRegistries.RECIPE_CAPABILITIES) {
                    if (!data.contains(cap.name)) continue;
                    var configData = data.getCompound(cap.name);
                    var component = cap.getColoredName();
                    addConfigTypeTooltips(tooltipComponents, component, configData, origFront);
                }
            }
            if (data.contains(MUFFLED)) {
                tooltipComponents.add(Component.translatable("behaviour.setting.muffled.tooltip",
                        data.getBoolean(MUFFLED) ? ENABLED : DISABLED));
            }
        } else {
            tooltipComponents.add(Component.translatable("item.toggle.advanced.info.tooltip"));
        }
    }

    private static void addConfigTypeTooltips(List<Component> tooltip, Component baseComponent,
                                              CompoundTag data, Direction origFront) {
        tooltip.add(Component.translatable("behaviour.setting.output.direction.tooltip",
                baseComponent, relativeDirectionComponent(origFront, tagToDirection(data.get(DIRECTION)))));
        tooltip.add(Component.translatable("behaviour.setting.item_auto_output.tooltip", baseComponent,
                data.getBoolean(AUTO) ? ENABLED : DISABLED));
        tooltip.add(Component.translatable("behaviour.setting.allow.input.from.output.tooltip", baseComponent,
                data.getBoolean(INPUT_FROM_OUTPUT_SIDE) ? ENABLED : DISABLED));
    }

    private static CompoundTag copyOutputConfig(Direction outputSide, boolean autoOutput,
                                                boolean allowInputFromOutputSide) {
        CompoundTag tag = new CompoundTag();
        tag.putString(DIRECTION, directionToString(outputSide));
        tag.putBoolean(AUTO, autoOutput);
        tag.putBoolean(INPUT_FROM_OUTPUT_SIDE, allowInputFromOutputSide);
        return tag;
    }

    private static void pasteOutputConfig(Direction originalFront, Direction currentFront, CompoundTag data,
                                          Consumer<Direction> outputSide, BooleanConsumer autoOutput,
                                          BooleanConsumer allowInputFromOutputSide) {
        outputSide.accept(RelativeDirection.getActualDirection(originalFront, currentFront,
                tagToDirection(data.get(DIRECTION))));
        autoOutput.accept(data.getBoolean(AUTO));
        allowInputFromOutputSide.accept(data.getBoolean(INPUT_FROM_OUTPUT_SIDE));
    }

    private static boolean consumeItems(@Nullable IItemHandler itemHandler,
                                        Map<Item, Integer> requiredItems) {
        if (itemHandler == null) return requiredItems.isEmpty();
        Map<Integer, Integer> itemCountBySlot = new HashMap<>();
        for (Item item : requiredItems.keySet()) {
            if (item == Items.AIR || requiredItems.get(item) <= 0) continue;
            boolean foundItem = false;
            int amount = requiredItems.get(item);
            for (int slot = 0; slot < itemHandler.getSlots() && !foundItem; slot++) {
                ItemStack stack = itemHandler.extractItem(slot, amount + itemCountBySlot.getOrDefault(slot, 0), true);
                if (stack.getCount() == itemCountBySlot.getOrDefault(slot, 0) + amount && stack.is(item)) {
                    foundItem = true;
                    itemCountBySlot.put(slot, itemCountBySlot.getOrDefault(slot, 0) + amount);
                } else if (stack.getCount() > itemCountBySlot.getOrDefault(slot, 0) && stack.is(item)) {
                    itemCountBySlot.put(slot, stack.getCount());
                    amount -= stack.getCount() - itemCountBySlot.getOrDefault(slot, 0);
                }
            }
            if (!foundItem) return false;
        }
        for (Integer slot : itemCountBySlot.keySet()) itemHandler.extractItem(slot, itemCountBySlot.get(slot), false);
        return true;
    }

    private static Tag posToTag(BlockPos pos) {
        return new IntArrayTag(List.of(pos.getX(), pos.getY(), pos.getZ()));
    }

    private static BlockPos posFromTag(Tag tag) {
        if (tag instanceof IntArrayTag intArrayTag)
            return new BlockPos(intArrayTag.get(0).getAsInt(), intArrayTag.get(1).getAsInt(),
                    intArrayTag.get(2).getAsInt());
        return new BlockPos(0, 0, 0);
    }
}
