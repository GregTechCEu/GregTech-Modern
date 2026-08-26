package com.gregtechceu.gtceu.common.network.packets;

import com.google.common.collect.HashBasedTable;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import it.unimi.dsi.fastutil.ints.*;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.nio.charset.StandardCharsets;

public class CPacketTerminalSettings implements GTNetwork.INetPacket {

    private final InteractionHand hand;
    private final MultiblockMachineDefinition machineDefinition;
    private final Int2IntMap sliceRepeats;
    private final IntList dimensions;
    private final Long2ObjectMap<BlockState> globalPreferences;
    private final HashBasedTable<MultiPredicate, BasePredicate, BlockInfo> blockPreferences;
    private final HashBasedTable<MultiPredicate, BasePredicate, IntIntPair> minMaxPreferences;


    public CPacketTerminalSettings(InteractionHand hand, MultiblockMachineDefinition def, Int2IntMap sliceRepeats, IntList dimensions,
                                   Long2ObjectMap<BlockState> globalPreferences, HashBasedTable<MultiPredicate, BasePredicate, BlockInfo> blockPreferences,
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

        int size = buf.readVarInt();
        ResourceLocation resLoc = ResourceLocation.parse(buf.readCharSequence(size, StandardCharsets.UTF_8).toString());
        this.machineDefinition = (MultiblockMachineDefinition) GTRegistries.MACHINES.get(resLoc);

        int repeatCount = buf.readVarInt();
        this.sliceRepeats = new Int2IntArrayMap(repeatCount);
        for (int i = 0; i < repeatCount; i++) {
            this.sliceRepeats.put(buf.readVarInt(), buf.readVarInt());
        }

        int dimensionCount = buf.readVarInt();
        this.dimensions = new IntArrayList(dimensionCount);
        for (int i = 0; i < dimensionCount; i++) {
            this.dimensions.add(buf.readVarInt());
        }

        int globalPreferenceCount = buf.readVarInt();
        this.globalPreferences = new Long2ObjectOpenHashMap<>(globalPreferenceCount);
        for (int i = 0; i < globalPreferenceCount; i++) {
            long pos = buf.readLong();
            BlockState state = Block.stateById(buf.readVarInt());
            this.globalPreferences.put(pos, state);
        }

        blockPreferences = HashBasedTable.create();
        minMaxPreferences = HashBasedTable.create();
        IBlockPattern pattern = machineDefinition.getStructurePatterns().get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get();
        if (pattern instanceof BlockPattern blockPattern) {
            int preferenceSize = buf.readVarInt();
            for (int i = 0; i < preferenceSize; i++) {
                char c = buf.readChar();
                int baseIndex = buf.readVarInt();
                int candidateIndex = buf.readVarInt();

                MultiPredicate pred = blockPattern.getPredicates().get(c);
                BasePredicate base = pred.predicates().get(baseIndex);
                BlockInfo info = base.getCandidates().get(candidateIndex);

                this.blockPreferences.put(pred, base, info);
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

        buf.writeVarInt(this.machineDefinition.getId().toString().length());
        buf.writeCharSequence(this.machineDefinition.getId().toString(), StandardCharsets.UTF_8);

        buf.writeVarInt(this.sliceRepeats.size());
        for (var entry : this.sliceRepeats.int2IntEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            buf.writeVarInt(entry.getIntValue());
        }

        buf.writeVarInt(this.dimensions.size());
        for (int dimension : this.dimensions) {
            buf.writeVarInt(dimension);
        }

        buf.writeVarInt(this.globalPreferences.size());
        for (var entry : this.globalPreferences.long2ObjectEntrySet()) {
            buf.writeLong(entry.getLongKey());
            buf.writeVarInt(Block.getId(entry.getValue()));
        }

        IBlockPattern pattern = machineDefinition.getStructurePatterns().get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get();
        if (pattern instanceof BlockPattern blockPattern) {
            buf.writeVarInt(this.blockPreferences.rowKeySet().size());
            for (var entry : this.blockPreferences.cellSet()) {
                MultiPredicate pred = entry.getRowKey();
                BasePredicate base = entry.getColumnKey();

                char c = blockPattern.getPredicates().char2ObjectEntrySet()
                        .stream()
                        .filter(e -> e.getValue().equals(pred))
                        .findFirst()
                        .get().getCharKey();
                buf.writeChar(c);

                buf.writeVarInt(pred.predicates().indexOf(base));
                buf.writeVarInt(base.getCandidates().indexOf(entry.getValue()));
            }

            buf.writeVarInt(this.minMaxPreferences.rowKeySet().size());
            for (var entry : this.minMaxPreferences.cellSet()) {
                MultiPredicate pred = entry.getRowKey();
                BasePredicate base = entry.getColumnKey();

                char c = blockPattern.getPredicates().char2ObjectEntrySet()
                        .stream()
                        .filter(e -> e.getValue().equals(pred))
                        .findFirst()
                        .get().getCharKey();
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

        TerminalBehavior.applyUserPreferences(held, this.sliceRepeats, this.dimensions, this.globalPreferences, this.blockPreferences, this.minMaxPreferences);
    }
}
