package com.gregtechceu.gtceu.common.item.behavior;

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
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.network.packets.CPacketTerminalSettings;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.ids.AEComponents;
import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.*;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder, IAddInformation {

    // todo somewhere client panel warning if the structure to be built is invalid
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!(MetaMachine.getMachine(level, pos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }

        var info = stack.get(GTDataComponents.TERMINAL_CONTROLLER_INFO);
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

        if (!level.isClientSide) {
            MultiblockSchemaInfo schemaInfo = stack.getOrDefault(GTDataComponents.MULTI_SCHEMA_INFO,
                    defaultSchemaInfo(info));
            boolean flipped = schemaInfo.isFlipped();

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
                    IntList userDims = schemaInfo.getUserDimensions();
                    List<IntIntPair> constraints = expandablePattern.getBoundsConstraints().apply();
                    for (int i = 0; i < constraints.size(); i++) {
                        IntIntPair bounds = constraints.get(i);
                        int dim = i < userDims.size() ? userDims.getInt(i) : bounds.leftInt();
                        dims.add(Mth.clamp(dim, bounds.leftInt(), bounds.rightInt()));
                    }
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
                        resultStructure, structureHelper, flipped);
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
            itemStack.set(GTDataComponents.TERMINAL_CONTROLLER_INFO, new ControllerInfo(controller));
            itemStack.remove(GTDataComponents.MULTI_SCHEMA_INFO);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public boolean shouldOpenUI(ItemStack item) {
        return item.has(GTDataComponents.TERMINAL_CONTROLLER_INFO);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
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
        var controllerInfo = item.get(GTDataComponents.TERMINAL_CONTROLLER_INFO);
        if (controllerInfo == null) return Optional.empty();

        MultiblockSchemaInfo schemaInfo = item.getOrDefault(GTDataComponents.MULTI_SCHEMA_INFO,
                defaultSchemaInfo(controllerInfo));

        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(controllerInfo.definition(), schemaInfo,
                200, 200, true)
                .setControllerPos(controllerInfo.pos())
                .setFrontFacing(controllerInfo.facing()).setUpFacing(controllerInfo.upFace())
                .setFlipped(schemaInfo.isFlipped());
        previewWidget.refreshSchema();

        return Optional.of(ModularPanel.defaultPanel("terminal")
                .coverChildren()
                .child(previewWidget)
                .onCloseAction(w -> writeMultiblockInfo(controllerInfo.definition(), hand, previewWidget)));
    }

    private void writeMultiblockInfo(MultiblockMachineDefinition definition, InteractionHand hand,
                                     MultiblockPreviewWidget previewWidget) {
        MultiblockSchemaInfo schemaInfo = previewWidget.getMultiblockSchemaInfo();

        PacketDistributor.sendToServer(new CPacketTerminalSettings(hand, definition, schemaInfo.getUserSliceRepeats(),
                schemaInfo.getUserDimensions(), schemaInfo.getUserGlobalBlockPreferences(),
                schemaInfo.getBlockPreferences(), previewWidget.isFlipped(), previewWidget.isClearMulti(),
                previewWidget.isClearPreferences(), schemaInfo.isIgnorePreviewCount()));
    }

    public static void applyUserPreferences(ItemStack item, MultiblockSchemaInfo schemaInfo,
                                            boolean isClearMulti,
                                            boolean isClearPreferences) {
        if (isClearMulti) {
            item.remove(GTDataComponents.TERMINAL_CONTROLLER_INFO);
        } else if (isClearPreferences) {
            var controllerInfo = item.get(GTDataComponents.TERMINAL_CONTROLLER_INFO);
            if (controllerInfo == null) return;
            item.set(GTDataComponents.MULTI_SCHEMA_INFO, defaultSchemaInfo(controllerInfo));
        } else {
            item.set(GTDataComponents.MULTI_SCHEMA_INFO, schemaInfo);
        }
    }

    private static MultiblockSchemaInfo defaultSchemaInfo(ControllerInfo controllerInfo) {
        MultiblockSchemaInfo schemaInfo = new MultiblockSchemaInfo(controllerInfo.definition());
        schemaInfo.setFlipped(controllerInfo.flipped());
        return schemaInfo;
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        var info = stack.get(GTDataComponents.TERMINAL_CONTROLLER_INFO);
        if (info != null) {
            tooltipComponents.add(Component.translatable("gtceu.terminal.controller_bound",
                    info.pos().getX(), info.pos().getY(), info.pos().getZ())
                    .withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(info.definition().getBlock().getName());
        }

        GlobalPos aeBinding = stack.get(AEComponents.WIRELESS_LINK_TARGET);
        if (aeBinding != null) {
            tooltipComponents.add(Component.translatable("gtceu.terminal.network_bound", aeBinding.dimension(),
                    aeBinding.pos().getX(), aeBinding.pos().getY(), aeBinding.pos().getZ())
                    .withStyle(ChatFormatting.GOLD));
        }
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

        public static final StreamCodec<RegistryFriendlyByteBuf, ControllerInfo> STREAM_CODEC = StreamCodec.composite(
                MultiblockMachineDefinition.STREAM_CODEC, ControllerInfo::definition,
                BlockPos.STREAM_CODEC, ControllerInfo::pos,
                Direction.STREAM_CODEC, ControllerInfo::facing,
                Direction.STREAM_CODEC, ControllerInfo::upFace,
                ByteBufCodecs.BOOL, ControllerInfo::flipped,
                ControllerInfo::new
        );
        //spotless:on

        public ControllerInfo(MultiblockControllerMachine machine) {
            this(machine.getDefinition(), machine.getBlockPos(), machine.getFrontFacing(), machine.getUpwardsFacing(),
                    machine.isFlipped());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ControllerInfo other)) return false;
            return definition.getId().equals(other.definition.getId()) && pos.equals(other.pos) &&
                    upFace.equals(other.upFace) && flipped == other.flipped;
        }

        @Override
        public int hashCode() {
            return Objects.hash(definition, pos, facing, upFace, flipped);
        }
    }
}
