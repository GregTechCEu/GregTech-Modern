package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.AutobuildHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketTerminalSettings;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import com.google.common.collect.HashBasedTable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder, IAddInformation {

    private static final String CONTROLLER_INFO_TAG = "controller";

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

        if (!tag.contains(CONTROLLER_INFO_TAG)) return InteractionResult.PASS;
        ControllerInfo info = ControllerInfo.CODEC.parse(NbtOps.INSTANCE, tag.getCompound(CONTROLLER_INFO_TAG))
                .getOrThrow(false, GTCEu.LOGGER::error);

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
                if (tag.contains("sliceRepeatKeys") && tag.contains("sliceRepeatValues")) {
                    var sliceRepeatKeys = tag.getIntArray("sliceRepeatKeys");
                    var sliceRepeatValues = tag.getIntArray("sliceRepeatValues");
                    var length = Math.min(sliceRepeatKeys.length, sliceRepeatValues.length);
                    for (int i = 0; i < length; i++) {
                        slices.put(sliceRepeatKeys[i], sliceRepeatValues[i]);
                    }
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
                MultiblockSchemaInfo schemaInfo = createSchemaInfoFromTag(stack, info);

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
            itemStack.setTag(new CompoundTag());
            itemStack.getOrCreateTag().put(CONTROLLER_INFO_TAG,
                    ControllerInfo.CODEC.encodeStart(NbtOps.INSTANCE, new ControllerInfo(controller)).getOrThrow(false,
                            GTCEu.LOGGER::error));
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
                return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), true);
            }
            ClientGUI.open(createScreen(guiData, clientPanel.get()));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
    }

    private Optional<ModularPanel<?>> clientPanel(ItemStack item, InteractionHand hand) {
        CompoundTag tag = item.getOrCreateTag();

        if (!tag.contains(CONTROLLER_INFO_TAG)) return Optional.empty();
        ControllerInfo controllerInfo = ControllerInfo.CODEC
                .parse(NbtOps.INSTANCE, tag.getCompound(CONTROLLER_INFO_TAG)).getOrThrow(false, GTCEu.LOGGER::error);

        MultiblockSchemaInfo info = createSchemaInfoFromTag(item, controllerInfo);

        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(controllerInfo.definition(), info,
                200, 200)
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

        Long2ObjectMap<BlockState> blockPreferences = new Long2ObjectOpenHashMap<>();
        for (var entry : schemaInfo.getUserGlobalBlockPreferences().long2ObjectEntrySet()) {
            blockPreferences.put(entry.getLongKey(), entry.getValue().getBlockState());
        }

        GTNetwork.sendToServer(new CPacketTerminalSettings(hand, definition, schemaInfo.getUserSliceRepeats(),
                schemaInfo.getUserDimensions(), blockPreferences, schemaInfo.getBlockPreferences(),
                schemaInfo.getMinMaxPreferences()));
    }

    public static MultiblockSchemaInfo createSchemaInfoFromTag(ItemStack item, ControllerInfo controllerInfo) {
        CompoundTag tag = item.getOrCreateTag();
        // TODO fix when trying to open overwritten info from another controller type
        MultiblockSchemaInfo info = new MultiblockSchemaInfo();
        if (tag.contains("sliceRepeatKeys") && tag.contains("sliceRepeatValues")) {
            int[] repeatKeys = tag.getIntArray("sliceRepeatKeys");
            int[] repeatValues = tag.getIntArray("sliceRepeatValues");
            for (int i = 0; i < repeatKeys.length; i++) {
                info.getUserSliceRepeats().put(repeatKeys[i], repeatValues[i]);
            }
        }

        if (tag.contains("dimensions")) {
            info.getUserDimensions().addAll(IntList.of(tag.getIntArray("dimensions")));
        }

        if (tag.contains("globalPreferences")) {
            ListTag preferences = tag.getList("globalPreferences", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < preferences.size(); i++) {
                CompoundTag blockTag = preferences.getCompound(i);
                long pos = blockTag.getLong("pos");
                BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),
                        blockTag.getCompound("state"));
                info.getUserGlobalBlockPreferences().put(pos, BlockInfo.fromBlockState(state));
            }
        }

        // TODO maybe move this as part of per pattern type encoding?
        if (tag.contains("blockPreferences")) {
            ListTag preferences = tag.getList("blockPreferences", CompoundTag.TAG_COMPOUND);
            BlockPattern blockPattern = (BlockPattern) controllerInfo.definition().getStructurePatterns()
                    .get(DEFAULT_STRUCTURE).get();
            for (int i = 0; i < preferences.size(); i++) {
                CompoundTag inner = preferences.getCompound(i);
                char c = (char) inner.getByte("p");
                CompoundTag blockState = inner.getCompound("b");

                MultiPredicate pred = blockPattern.getPredicates().get(c);
                BlockInfo blockInfo = BlockInfo
                        .fromBlockState(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), blockState));

                info.getBlockPreferences().put(pred, blockInfo);
            }
        }

        if (tag.contains("minMaxPreferences")) {
            ListTag minMaxPreferences = tag.getList("minMaxPreferences", CompoundTag.TAG_COMPOUND);
            BlockPattern blockPattern = (BlockPattern) controllerInfo.definition().getStructurePatterns()
                    .get(DEFAULT_STRUCTURE).get();
            for (int i = 0; i < minMaxPreferences.size(); i++) {
                CompoundTag inner = minMaxPreferences.getCompound(i);
                char c = (char) inner.getByte("p");
                int baseIndex = inner.getInt("b");
                int min = inner.getInt("min");
                int max = inner.getInt("min");

                MultiPredicate pred = blockPattern.getPredicates().get(c);
                BasePredicate base = pred.predicates().get(baseIndex);

                info.getMinMaxPreferences().put(pred, base, IntIntPair.of(min, max));
            }
        }
        return info;
    }

    public static void applyUserPreferences(ItemStack item, Int2IntMap sliceRepeats, IntList dimensions,
                                            Long2ObjectMap<BlockState> globalPreferences,
                                            Map<MultiPredicate, BlockInfo> blockPreferences,
                                            HashBasedTable<MultiPredicate, BasePredicate, IntIntPair> minMaxPreferences) {
        CompoundTag tag = item.getOrCreateTag();

        if (!tag.contains(CONTROLLER_INFO_TAG)) return;
        ControllerInfo info = ControllerInfo.CODEC.parse(NbtOps.INSTANCE, tag.getCompound(CONTROLLER_INFO_TAG))
                .getOrThrow(false, GTCEu.LOGGER::error);

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

        if (globalPreferences.isEmpty()) {
            tag.remove("globalPreferences");
        } else {
            ListTag preferences = new ListTag();
            for (var entry : globalPreferences.long2ObjectEntrySet()) {
                CompoundTag preference = new CompoundTag();
                preference.putLong("pos", entry.getLongKey());
                // TODO move to base predicate candidate index?
                preference.put("state", NbtUtils.writeBlockState(entry.getValue()));
                preferences.add(preference);
            }
            tag.put("globalPreferences", preferences);
        }

        if (blockPreferences.isEmpty()) {
            tag.remove("blockPreferences");
        } else {
            ListTag preferences = new ListTag();
            BlockPattern blockPattern = (BlockPattern) info.definition().getStructurePatterns().get(DEFAULT_STRUCTURE)
                    .get();
            for (var entry : blockPreferences.entrySet()) {
                CompoundTag preference = new CompoundTag();
                MultiPredicate pred = entry.getKey();

                char c = blockPattern.getPredicates().char2ObjectEntrySet()
                        .stream()
                        .filter(e -> e.getValue().equals(pred))
                        .findFirst()
                        .orElseThrow().getCharKey();

                preference.putByte("p", (byte) c);
                preference.put("b", NbtUtils.writeBlockState(entry.getValue().getBlockState()));
                preferences.add(preference);
            }
            /*
             * for (var entry : blockPreferences.cellSet()) {
             * CompoundTag preference = new CompoundTag();
             * MultiPredicate pred = entry.getRowKey();
             * BasePredicate base = entry.getColumnKey();
             * 
             * char c = blockPattern.getPredicates().char2ObjectEntrySet()
             * .stream()
             * .filter(e -> e.getValue().equals(pred))
             * .findFirst()
             * .get().getCharKey();
             * 
             * preference.putByte("p", (byte) c);
             * preference.putInt("b", pred.predicates().indexOf(base));
             * preference.putInt("i", base.getCandidates().indexOf(entry.getValue()));
             * preferences.add(preference);
             * }
             */
            tag.put("blockPreferences", preferences);
        }

        if (minMaxPreferences.isEmpty()) {
            tag.remove("minMaxPreferences");
        } else {
            ListTag minMaxs = new ListTag();
            BlockPattern blockPattern = (BlockPattern) info.definition().getStructurePatterns().get(DEFAULT_STRUCTURE)
                    .get();
            for (var entry : minMaxPreferences.cellSet()) {
                CompoundTag inner = new CompoundTag();
                MultiPredicate pred = entry.getRowKey();
                BasePredicate base = entry.getColumnKey();

                char c = blockPattern.getPredicates().char2ObjectEntrySet()
                        .stream()
                        .filter(e -> e.getValue().equals(pred))
                        .findFirst()
                        .orElseThrow().getCharKey();

                inner.putByte("p", (byte) c);
                inner.putInt("b", pred.predicates().indexOf(base));
                inner.putInt("min", entry.getValue().firstInt());
                inner.putInt("max", entry.getValue().secondInt());
                minMaxs.add(inner);
            }
            tag.put("minMaxPreferences", minMaxs);
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

        if (!tag.contains(CONTROLLER_INFO_TAG)) return;
        ControllerInfo info = ControllerInfo.CODEC.parse(NbtOps.INSTANCE, tag.getCompound(CONTROLLER_INFO_TAG))
                .getOrThrow(false, GTCEu.LOGGER::error);

        tooltipComponents
                .add(Component
                        .translatable("gtceu.top.buffer_bound_pos", info.pos().getX(), info.pos().getY(),
                                info.pos().getZ())
                        .withStyle(ChatFormatting.GOLD));

        tooltipComponents.add(info.definition().getBlock().getName());
    }

    public record ControllerInfo(MultiblockMachineDefinition definition, BlockPos pos, Direction facing,
                                 Direction upFace, boolean flipped) {

        // spotless:off
        public static final Codec<ControllerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GTRegistries.MACHINES.codec().comapFlatMap(def -> {
                    if (def instanceof MultiblockMachineDefinition mDef) return DataResult.success(mDef);
                    else return DataResult.error(() -> "%s is not a multiblock machine definition".formatted(def.getId()));
                }, v -> v).fieldOf("definition").forGetter(ControllerInfo::definition),
                BlockPos.CODEC.fieldOf("pos").forGetter(ControllerInfo::pos),
                Direction.CODEC.fieldOf("facing").forGetter(ControllerInfo::facing),
                Direction.CODEC.fieldOf("upFace").forGetter(ControllerInfo::upFace),
                Codec.BOOL.fieldOf("flipped").forGetter(ControllerInfo::flipped)
        ).apply(instance, ControllerInfo::new));
        //spotless:on

        public ControllerInfo(MultiblockControllerMachine machine) {
            this(machine.getDefinition(), machine.getBlockPos(), machine.getFrontFacing(), machine.getUpwardsFacing(),
                    machine.isFlipped());
        }
    }
}
