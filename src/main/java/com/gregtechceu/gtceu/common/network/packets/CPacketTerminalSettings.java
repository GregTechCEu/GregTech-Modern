package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class CPacketTerminalSettings implements GTNetwork.INetPacket {

    private final InteractionHand hand;
    private final MultiblockMachineDefinition machineDefinition;
    private final Int2IntMap sliceRepeats;
    private final IntList dimensions;
    private final Object2ObjectMap<BlockPos, BlockInfo> globalPreferences;
    private final Char2ObjectMap<BlockInfo> blockPreferences;
    private final Table<Character, Integer, IntIntPair> minMaxPreferences;

    public CPacketTerminalSettings(InteractionHand hand, MultiblockMachineDefinition def, Int2IntMap sliceRepeats,
                                   IntList dimensions,
                                   Object2ObjectMap<BlockPos, BlockInfo> globalPreferences,
                                   Char2ObjectMap<BlockInfo> blockPreferences,
                                   Table<Character, Integer, IntIntPair> minMaxPreferences) {
        this.hand = hand;
        this.machineDefinition = def;
        this.sliceRepeats = sliceRepeats;
        this.dimensions = dimensions;
        this.globalPreferences = globalPreferences;
        this.blockPreferences = blockPreferences;
        this.minMaxPreferences = minMaxPreferences;
    }

    private static final FriendlyByteBuf.Reader<BlockInfo> blockInfoReader = (b) -> BlockInfo
            .fromBlockState(Block.stateById(b.readVarInt()));
    private static final FriendlyByteBuf.Writer<BlockInfo> blockInfoWriter = (b, v) -> b
            .writeVarInt(Block.getId(v.getBlockState()));

    public CPacketTerminalSettings(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);

        var resLoc = buf.readResourceLocation();
        this.machineDefinition = (MultiblockMachineDefinition) GTRegistries.MACHINES.get(resLoc);
        if (machineDefinition == null)
            throw new IllegalStateException("Unknown machine definition %s".formatted(resLoc));

        this.sliceRepeats = buf.readMap(Int2IntArrayMap::new, FriendlyByteBuf::readVarInt, FriendlyByteBuf::readVarInt);
        this.dimensions = buf.readCollection(IntArrayList::new, FriendlyByteBuf::readVarInt);
        this.globalPreferences = buf.readMap(Object2ObjectOpenHashMap::new, FriendlyByteBuf::readBlockPos,
                blockInfoReader);
        this.blockPreferences = buf.readMap(Char2ObjectArrayMap::new, FriendlyByteBuf::readChar, blockInfoReader);

        Char2ObjectMap<Int2ObjectMap<IntIntPair>> minMaxPreferenceMap = buf.readMap(Char2ObjectArrayMap::new,
                FriendlyByteBuf::readChar,
                (b) -> b.readMap(Int2ObjectArrayMap::new, FriendlyByteBuf::readVarInt,
                        (b1) -> IntIntPair.of(b1.readVarInt(), b1.readVarInt())));

        minMaxPreferences = HashBasedTable.create();
        for (var row : minMaxPreferenceMap.char2ObjectEntrySet()) {
            var rowKey = row.getCharKey();
            for (var col : row.getValue().int2ObjectEntrySet()) {
                minMaxPreferences.put(rowKey, col.getIntKey(), col.getValue());
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);

        buf.writeResourceLocation(this.machineDefinition.getId());

        buf.writeMap(this.sliceRepeats, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::writeVarInt);
        buf.writeCollection(dimensions, FriendlyByteBuf::writeVarInt);
        buf.writeMap(globalPreferences, FriendlyByteBuf::writeBlockPos, blockInfoWriter);
        buf.writeMap(blockPreferences, (b, v) -> b.writeChar(v), blockInfoWriter);

        buf.writeMap(minMaxPreferences.rowMap(), (b, v) -> b.writeChar(v),
                (b, v) -> b.writeMap(v, FriendlyByteBuf::writeVarInt, (b1, p) -> {
                    b1.writeVarInt(p.firstInt());
                    b1.writeVarInt(p.secondInt());
                }));
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null) return;

        ItemStack held = sender.getItemInHand(this.hand);
        if (!GTItems.TERMINAL.isIn(held)) return;

        var schemaInfo = new MultiblockSchemaInfo(this.machineDefinition, this.sliceRepeats, this.dimensions,
                this.globalPreferences, this.blockPreferences, this.minMaxPreferences);
        TerminalBehavior.applyUserPreferences(held, schemaInfo);
    }
}
