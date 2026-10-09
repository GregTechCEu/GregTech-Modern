package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.AutobuildHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketTerminalSettings;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import com.google.common.collect.HashBasedTable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder, IAddInformation {

    public static final String ACCESS_POINT_TAG = "access_pos";
    private static final String CONTROLLER_INFO_TAG = "controller";
    private static final String SCHEMA_INFO_TAG = "schema";

    // todo somewhere client panel warning if the structure to be built is invalid
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();

        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!(MetaMachine.getMachine(level, pos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }

        var info = loadControllerInfo(stack);
        if (info == null) return InteractionResult.PASS;

        if (controller.getDefinition() != info.definition()) {
            // TODO: Log errors in chat
            return InteractionResult.PASS;
        }

        PatternState state = controller.getDefaultPatternState();
        if (state.isFormed()) {
            return InteractionResult.PASS;
        }

        Direction frontFacing = controller.getFrontFacing();
        Direction upFacing = controller.getUpwardsFacing();
        boolean flipped = controller.isFlipped();

        if (!level.isClientSide) {
            MultiblockSchemaInfo schemaInfo = loadSchemaInfo(stack, controller.getDefinition());

            ServerPlayer serverPlayer = (ServerPlayer) player;
            // Partially copy pasted from MultiblockControllerMachine#onUse.
            // TODO: Probably extract into helper function
            Map<BlockPos, BlockInfo> resultStructure = new HashMap<>();
            AbstractStructureHelper structureHelper = null;
            IBlockPattern pattern = controller.getStructurePatterns().get(DEFAULT_STRUCTURE);
            if (pattern instanceof BlockPattern blockPattern) {
                Int2IntMap slices = new Int2IntArrayMap();
                for (int i = 0; i < blockPattern.getSlices().length; i++) {
                    slices.put(i, blockPattern.getSlices()[i].getMinRepeats());
                }
                slices.putAll(schemaInfo.getUserSliceRepeats());

                structureHelper = AbstractStructureHelper.blockPattern(slices);
            } else if (pattern instanceof ExpandablePattern expandablePattern) {
                IntList dims = new IntArrayList();
                if (expandablePattern.getBoundsConstraints() != null) {
                    expandablePattern.getBoundsConstraints().apply().stream()
                            .mapToInt(Pair::left)
                            .forEach(dims::add);
                }
                structureHelper = AbstractStructureHelper.expandable(dims);
            }

            if (structureHelper != null) {
                structureHelper.populate(schemaInfo, resultStructure, pattern,
                        schemaInfo.getUserGlobalBlockPreferences(), frontFacing,
                        upFacing,
                        flipped);
            }

            // Extract controller block offset
            Block controllerBlock = controller.getDefinition().getBlock();
            BlockPos schemaControllerPos = BlockPos.ZERO;
            for (var entry : resultStructure.entrySet()) {
                if (entry.getValue().getBlockState().is(controllerBlock)) {
                    schemaControllerPos = entry.getKey();
                    break;
                }
            }

            BlockPos controllerOffset = controller.getBlockPos().subtract(schemaControllerPos);
            if (player.isCreative()) {
                for (var entry : resultStructure.entrySet()) {
                    level.setBlockAndUpdate(entry.getKey().offset(controllerOffset), entry.getValue().getBlockState());
                }
            } else if (structureHelper != null) {
                AutobuildHelper.autobuild(serverPlayer, context.getItemInHand(), controller.getDefinition(), controller,
                        resultStructure, structureHelper);
            }

            // needed to force the multiblock to do a clean check, kinda sus
            controller.getDefaultPatternState().getCache().clear();
            controller.checkAndFormStructure();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        if (!(MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }

        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            player.displayClientMessage(Component.literal("Loaded controller information"), false);
        } else {
            itemStack.removeTagKey(CONTROLLER_INFO_TAG);
            itemStack.removeTagKey(SCHEMA_INFO_TAG);
            itemStack.getOrCreateTag().put(CONTROLLER_INFO_TAG,
                    ControllerInfo.CODEC.encodeStart(NbtOps.INSTANCE, new ControllerInfo(controller)).getOrThrow(false,
                            GTCEu.LOGGER::error));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public boolean shouldOpenUI(ItemStack item) {
        return item.getOrCreateTag().contains(CONTROLLER_INFO_TAG);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (!shouldOpenUI(player.getItemInHand(usedHand))) {
            if (level.isClientSide)
                player.displayClientMessage(Component.literal("No controller information loaded"), false);
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }

        if (level.isClientSide) {
            PlayerInventoryGuiData<?> guiData = PlayerInventoryGuiData.of(player, InventoryTypes.PLAYER, null,
                    usedHand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().selected);
            Optional<ModularPanel<?>> clientPanel = clientPanel(player.getItemInHand(usedHand), usedHand);
            if (clientPanel.isEmpty()) {
                return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), true);
            }
            ClientGUI.open(createScreen(guiData, clientPanel.get()));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
    }

    private Optional<ModularPanel<?>> clientPanel(ItemStack item, InteractionHand hand) {
        var controllerInfo = loadControllerInfo(item);
        if (controllerInfo == null) return Optional.empty();

        MultiblockSchemaInfo schemaInfo = loadSchemaInfo(item, controllerInfo.definition());

        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(controllerInfo.definition(), schemaInfo,
                200, 200, true)
                .setControllerPos(controllerInfo.pos())
                .setFrontFacing(controllerInfo.facing()).setUpFacing(controllerInfo.upFace())
                .setFlipped(controllerInfo.flipped());
        previewWidget.refreshSchema();

        return Optional.of(ModularPanel.defaultPanel("terminal")
                .coverChildren()
                .child(previewWidget)
                .onCloseAction(w -> writeMultiblockInfo(controllerInfo.definition(), hand, previewWidget)));
    }

    private void writeMultiblockInfo(MultiblockMachineDefinition definition, InteractionHand hand,
                                     MultiblockPreviewWidget previewWidget) {
        MultiblockSchemaInfo schemaInfo = previewWidget.getMultiblockSchemaInfo();

        GTNetwork.sendToServer(new CPacketTerminalSettings(hand, definition, schemaInfo.getUserSliceRepeats(),
                schemaInfo.getUserDimensions(), schemaInfo.getUserGlobalBlockPreferences(),
                schemaInfo.getBlockPreferences(), HashBasedTable.create()
                /* schemaInfo.getMinMaxPreferenceCharTable() */, previewWidget.isClearMulti(),
                previewWidget.isClearPreferences()));
    }

    public static void applyUserPreferences(ItemStack item, MultiblockSchemaInfo schemaInfo,
                                            boolean isClearMulti,
                                            boolean isClearPreferences) {
        CompoundTag tag = item.getOrCreateTag();
        if (isClearMulti) {
            tag.remove(CONTROLLER_INFO_TAG);
        } else if (isClearPreferences) {
            var controllerInfo = ControllerInfo.CODEC
                    .parse(NbtOps.INSTANCE, tag.getCompound(CONTROLLER_INFO_TAG))
                    .getOrThrow(false, GTCEu.LOGGER::error);
            tag.put(SCHEMA_INFO_TAG,
                    MultiblockSchemaInfo.CODEC
                            .encodeStart(NbtOps.INSTANCE, new MultiblockSchemaInfo(controllerInfo.definition))
                            .getOrThrow(false, GTCEu.LOGGER::error));
        } else {
            tag.put(SCHEMA_INFO_TAG, MultiblockSchemaInfo.CODEC.encodeStart(NbtOps.INSTANCE, schemaInfo)
                    .getOrThrow(false, GTCEu.LOGGER::error));
        }
    }

    public MultiblockSchemaInfo loadSchemaInfo(ItemStack stack, MultiblockMachineDefinition definition) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(SCHEMA_INFO_TAG)) {
            try {
                return MultiblockSchemaInfo.CODEC
                        .parse(NbtOps.INSTANCE, tag.getCompound(SCHEMA_INFO_TAG))
                        .getOrThrow(false, GTCEu.LOGGER::error);
            } catch (Exception e) {
                return new MultiblockSchemaInfo(definition);
            }
        } else {
            return new MultiblockSchemaInfo(definition);
        }
    }

    public @Nullable ControllerInfo loadControllerInfo(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(CONTROLLER_INFO_TAG)) {
            try {
                return ControllerInfo.CODEC
                        .parse(NbtOps.INSTANCE, tag.getCompound(CONTROLLER_INFO_TAG))
                        .getOrThrow(false, GTCEu.LOGGER::error);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getOrCreateTag();

        var info = loadControllerInfo(stack);
        if (info != null) {
            tooltipComponents.add(Component.translatable("gtceu.terminal.controller_bound",
                    info.pos().getX(), info.pos().getY(), info.pos().getZ())
                    .withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(info.definition().getBlock().getName());
        }

        GlobalPos aeBinding = getLinkedPos(stack);
        if (aeBinding != null) {
            tooltipComponents.add(Component.translatable("gtceu.terminal.network_bound", aeBinding.dimension(),
                    aeBinding.pos().getX(), aeBinding.pos().getY(), aeBinding.pos().getZ())
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    @Nullable
    public static GlobalPos getLinkedPos(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(ACCESS_POINT_TAG)) {
            try {
                return GlobalPos.CODEC
                        .parse(NbtOps.INSTANCE, tag.get(ACCESS_POINT_TAG))
                        .getOrThrow(false, GTCEu.LOGGER::error);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public record ControllerInfo(MultiblockMachineDefinition definition, BlockPos pos, Direction facing,
                                 Direction upFace, boolean flipped) {

        // spotless:off
        public static final Codec<ControllerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MultiblockMachineDefinition.CODEC.fieldOf("definition").forGetter(ControllerInfo::definition),
                BlockPos.CODEC.fieldOf("pos").forGetter(ControllerInfo::pos),
                Direction.CODEC.fieldOf("facing").forGetter(ControllerInfo::facing),
                Direction.CODEC.fieldOf("up").forGetter(ControllerInfo::upFace),
                Codec.BOOL.fieldOf("flipped").forGetter(ControllerInfo::flipped)
        ).apply(instance, ControllerInfo::new));
        //spotless:on

        public ControllerInfo(MultiblockControllerMachine machine) {
            this(machine.getDefinition(), machine.getBlockPos(), machine.getFrontFacing(), machine.getUpwardsFacing(),
                    machine.isFlipped());
        }
    }
}
