package com.gregtechceu.gtceu.common.item.behavior;

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
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketTerminalSettings;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;
import static com.gregtechceu.gtceu.api.multiblock.util.AutobuildHelper.readBlockPreferences;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {

    // Strip these fields and only store the needed info as nbt on the item for now
    private MultiblockMachineDefinition multiblockDefinition = null;
    private BlockPos controllerPos;
    private Direction frontFacing;
    private Direction upFacing;
    private boolean isFlipped = false;
    // this one may be fine to keep? as the info gets rebuild based on nbt....
    private MultiblockSchemaInfo multiblockSchemaInfo;

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

        // TODO: Allow survival building and remove this check
        if (!player.isCreative()) {
            return InteractionResult.PASS;
        }

        if (!(MetaMachine.getMachine(level, pos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }
        if (!controller.getDefinition().getId().equals(ResourceLocation.parse(tag.getString("controller")))) {
            // TODO: Log errors in chat
            return InteractionResult.PASS;
        }

        PatternState state = controller.getDefaultPatternState();
        if (state.isFormed()) {
            return InteractionResult.PASS;
        }

        if (!tag.contains("facing", CompoundTag.TAG_BYTE) ||
                !tag.contains("upFacing", CompoundTag.TAG_BYTE) ||
                !tag.contains("flipped", CompoundTag.TAG_BYTE)) {
            return InteractionResult.PASS;
        }
        Direction frontFacing = Direction.values()[tag.getByte("facing")];
        Direction upFacing = Direction.values()[tag.getByte("upFacing")];
        boolean flipped = tag.getBoolean("flipped");

        if (!level.isClientSide) {
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
                structureHelper.populate(resultStructure, pattern, readBlockPreferences(tag), frontFacing, upFacing,
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

            BlockPos controllerOffset = controller.getBlockPos().subtract(schemaControllerPos);;
            for (var entry : resultStructure.entrySet()) {
                level.setBlockAndUpdate(entry.getKey().offset(controllerOffset), entry.getValue().getBlockState());
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
            writeControllerInfo(itemStack, controller);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public boolean shouldOpenUI(ItemStack item) {
        return item.getOrCreateTag().contains("controller");
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
                return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
            }
            ClientGUI.open(createScreen(guiData, clientPanel.get()));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
    }

    private Optional<ModularPanel<?>> clientPanel(ItemStack item, InteractionHand hand) {
        CompoundTag tag = item.getOrCreateTag();
        if (!tag.contains("controller")) {
            return Optional.empty();
        }
        ResourceLocation controllerLocation = ResourceLocation.parse(tag.getString("controller"));
        var definition = GTRegistries.MACHINES.get(controllerLocation);
        if (definition == null || !(definition instanceof MultiblockMachineDefinition multiblockDefinition)) {
            return Optional.empty();
        }
        if (!tag.contains("pos")) {
            return Optional.empty();
        }
        BlockPos controllerPos = BlockPos.of(tag.getLong("pos"));

        if (!tag.contains("facing", CompoundTag.TAG_BYTE) ||
                !tag.contains("upFacing", CompoundTag.TAG_BYTE) ||
                !tag.contains("flipped", CompoundTag.TAG_BYTE)) {
            return Optional.empty();
        }

        Direction frontFacing = Direction.values()[tag.getByte("facing")];
        Direction upFacing = Direction.values()[tag.getByte("upFacing")];
        boolean flipped = tag.getBoolean("flipped");

        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(multiblockDefinition,
                // TODO what to do with this
                this.multiblockSchemaInfo, 200, 200)
                .setControllerPos(controllerPos)
                .setFrontFacing(frontFacing).setUpFacing(upFacing).setFlipped(flipped);
        previewWidget.refreshSchema();

        return Optional.of(ModularPanel.defaultPanel("terminal")
                .coverChildren()
                .child(previewWidget)
                .onCloseAction(() -> writeMultiblockInfo(hand, previewWidget)));
    }

    private void writeMultiblockInfo(InteractionHand hand, MultiblockPreviewWidget previewWidget) {
        MultiblockSchemaInfo schemaInfo = previewWidget.getMultiblockSchemaInfo();

        Long2ObjectMap<BlockState> blockPreferences = new Long2ObjectOpenHashMap<>();
        for (var entry : schemaInfo.getUserGlobalBlockPreferences().long2ObjectEntrySet()) {
            blockPreferences.put(entry.getLongKey(), entry.getValue().getBlockState());
        }

        GTNetwork.sendToServer(new CPacketTerminalSettings(hand, schemaInfo.getUserSliceRepeats(),
                schemaInfo.getUserDimensions(), blockPreferences));
    }

    public static void writeControllerInfo(ItemStack item, MultiblockControllerMachine controller) {
        // TODO uuid gathering

        CompoundTag tag = item.getOrCreateTag();
        tag.putString("controller", controller.getDefinition().getId().toString());
        tag.putLong("pos", controller.getBlockPos().asLong());
        tag.putByte("facing", (byte) controller.getFrontFacing().ordinal());
        tag.putByte("upFacing", (byte) controller.getUpwardsFacing().ordinal());
        tag.putBoolean("flipped", controller.isFlipped());
    }

    public static void applyUserPreferences(ItemStack item, Int2IntMap sliceRepeats, IntList dimensions,
                                            Long2ObjectMap<BlockState> blockPreferences) {
        CompoundTag tag = item.getOrCreateTag();

        if (sliceRepeats.isEmpty()) {
            tag.remove("sliceRepeatKeys");
            tag.remove("sliceRepeatValues");
        } else {
            int[] keys = new int[sliceRepeats.size()];
            int[] values = new int[sliceRepeats.size()];
            int i = 0;
            for (var entry : sliceRepeats.int2IntEntrySet()) {
                keys[i] = entry.getIntKey();
                values[i] = entry.getIntValue();
                i++;
            }
            tag.putIntArray("sliceRepeatKeys", keys);
            tag.putIntArray("sliceRepeatValues", values);
        }

        if (dimensions.isEmpty()) {
            tag.remove("dimensions");
        } else {
            tag.putIntArray("dimensions", dimensions.toIntArray());
        }

        if (blockPreferences.isEmpty()) {
            tag.remove("blockPreferences");
        } else {
            ListTag preferences = new ListTag();
            for (var entry : blockPreferences.long2ObjectEntrySet()) {
                CompoundTag preference = new CompoundTag();
                preference.putLong("pos", entry.getLongKey());
                preference.put("state", NbtUtils.writeBlockState(entry.getValue()));
                preferences.add(preference);
            }
            tag.put("blockPreferences", preferences);
        }
    }

    private CompoundTag getMultiblockInfo(ItemStack item) {
        return item.getOrCreateTag();
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    private void refreshSchema() {
        this.multiblockSchemaInfo.refreshSchema(multiblockDefinition, frontFacing, upFacing, isFlipped, null);
    }
}
