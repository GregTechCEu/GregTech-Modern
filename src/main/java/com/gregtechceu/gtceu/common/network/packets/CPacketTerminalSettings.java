package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

import com.google.common.collect.HashBasedTable;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;

public class CPacketTerminalSettings implements GTNetwork.INetPacket {

    private final InteractionHand hand;
    private final MultiblockMachineDefinition machineDefinition;
    private final Int2IntMap sliceRepeats;
    private final IntList dimensions;
    private final Object2ObjectMap<BlockPos, BlockInfo> globalPreferences;
    private final Map<MultiPredicate, BlockInfo> blockPreferences;
    private final HashBasedTable<MultiPredicate, BasePredicate, IntIntPair> minMaxPreferences;

    public CPacketTerminalSettings(InteractionHand hand, MultiblockMachineDefinition def, Int2IntMap sliceRepeats,
                                   IntList dimensions,
                                   Object2ObjectMap<BlockPos, BlockInfo> globalPreferences,
                                   Map<MultiPredicate, BlockInfo> blockPreferences,
                                   HashBasedTable<MultiPredicate, BasePredicate, IntIntPair> minMaxPreferences) {
        this.hand = hand;
        this.machineDefinition = def;
        this.sliceRepeats = sliceRepeats;
        this.dimensions = dimensions;
        this.globalPreferences = globalPreferences;
        this.blockPreferences = blockPreferences;
        this.minMaxPreferences = minMaxPreferences;
    }

    public CPacketTerminalSettings(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);

        var resLoc = buf.readResourceLocation();
        this.machineDefinition = (MultiblockMachineDefinition) GTRegistries.MACHINES.get(resLoc);
        if (machineDefinition == null)
            throw new IllegalStateException("Unknown machine definition %s".formatted(resLoc));

        this.sliceRepeats = buf.readMap(Int2IntArrayMap::new, FriendlyByteBuf::readVarInt, FriendlyByteBuf::readVarInt);
        this.dimensions = buf.readCollection(IntArrayList::new, FriendlyByteBuf::readVarInt);
        this.globalPreferences = buf.readMap(Object2ObjectOpenHashMap::new, FriendlyByteBuf::readBlockPos,
                (b) -> BlockInfo.fromBlockState(Block.stateById(buf.readVarInt())));

        blockPreferences = new Object2ObjectOpenHashMap<>();
        minMaxPreferences = HashBasedTable.create();

        IBlockPattern pattern = machineDefinition.getStructurePatterns()
                .get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get();

        if (pattern instanceof BlockPattern blockPattern) {
            int preferenceSize = buf.readVarInt();
            for (int i = 0; i < preferenceSize; i++) {
                char c = buf.readChar();
                MultiPredicate pred = blockPattern.getPredicates().get(c);
                BlockInfo info = BlockInfo
                        .fromBlockState(Objects.requireNonNull(buf.readById(Block.BLOCK_STATE_REGISTRY)));
                this.blockPreferences.put(pred, info);
            }

            int minMaxSize = buf.readVarInt();
            for (int i = 0; i < minMaxSize; i++) {
                char c = buf.readChar();
                int baseIndex = buf.readVarInt();

                MultiPredicate pred = blockPattern.getPredicates().get(c);
                BasePredicate base = pred.predicates().get(baseIndex);
                int min = buf.readVarInt();
                int max = buf.readVarInt();

                this.minMaxPreferences.put(pred, base, IntIntPair.of(min, max));
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);

        buf.writeResourceLocation(this.machineDefinition.getId());

        buf.writeMap(this.sliceRepeats, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::writeVarInt);
        buf.writeCollection(dimensions, FriendlyByteBuf::writeVarInt);
        buf.writeMap(globalPreferences, FriendlyByteBuf::writeBlockPos, (b, v) -> b.writeVarInt(Block.getId(v.getBlockState())));

        IBlockPattern pattern = machineDefinition.getStructurePatterns()
                .get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get();

        if (pattern instanceof BlockPattern blockPattern) {

            buf.writeVarInt(this.blockPreferences.size());
            for (var entry : this.blockPreferences.entrySet()) {
                MultiPredicate pred = entry.getKey();

                char c = blockPattern.getPredicates().char2ObjectEntrySet()
                        .stream()
                        .filter(e -> e.getValue().equals(pred))
                        .findFirst()
                        .orElseThrow().getCharKey();
                buf.writeChar(c);
                buf.writeId(Block.BLOCK_STATE_REGISTRY, entry.getValue().getBlockState());
            }

            buf.writeVarInt(this.minMaxPreferences.rowKeySet().size());
            for (var entry : this.minMaxPreferences.cellSet()) {
                MultiPredicate pred = entry.getRowKey();
                BasePredicate base = entry.getColumnKey();

                char c = blockPattern.getPredicates().char2ObjectEntrySet()
                        .stream()
                        .filter(e -> e.getValue().equals(pred))
                        .findFirst()
                        .orElseThrow().getCharKey();
                buf.writeChar(c);

                buf.writeVarInt(pred.predicates().indexOf(base));
                buf.writeVarInt(entry.getValue().firstInt());
                buf.writeVarInt(entry.getValue().secondInt());
            }
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null) return;

        ItemStack held = sender.getItemInHand(this.hand);
        if (!GTItems.TERMINAL.isIn(held)) return;

        var schemaInfo = new MultiblockSchemaInfo(this.machineDefinition);
        schemaInfo.getUserSliceRepeats().putAll(this.sliceRepeats);
        schemaInfo.getUserDimensions().addAll(this.dimensions);
        schemaInfo.getUserGlobalBlockPreferences().putAll(this.globalPreferences);
        schemaInfo.getBlockPreferences().putAll(this.blockPreferences);
        schemaInfo.getMinMaxPreferences().putAll(this.minMaxPreferences);

        TerminalBehavior.applyUserPreferences(held, schemaInfo);
    }
}
