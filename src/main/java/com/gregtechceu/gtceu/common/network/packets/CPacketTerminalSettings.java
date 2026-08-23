package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class CPacketTerminalSettings implements GTNetwork.INetPacket {

    private final InteractionHand hand;
    private final Int2IntMap sliceRepeats;
    private final IntList dimensions;
    private final Long2ObjectMap<BlockState> blockPreferences;

    public CPacketTerminalSettings(InteractionHand hand, Int2IntMap sliceRepeats, IntList dimensions,
                                   Long2ObjectMap<BlockState> blockPreferences) {
        this.hand = hand;
        this.sliceRepeats = sliceRepeats;
        this.dimensions = dimensions;
        this.blockPreferences = blockPreferences;
    }

    public CPacketTerminalSettings(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);

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

        int preferenceCount = buf.readVarInt();
        this.blockPreferences = new Long2ObjectOpenHashMap<>(preferenceCount);
        for (int i = 0; i < preferenceCount; i++) {
            long pos = buf.readLong();
            BlockState state = Block.stateById(buf.readVarInt());
            this.blockPreferences.put(pos, state);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);

        buf.writeVarInt(this.sliceRepeats.size());
        for (var entry : this.sliceRepeats.int2IntEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            buf.writeVarInt(entry.getIntValue());
        }

        buf.writeVarInt(this.dimensions.size());
        for (int dimension : this.dimensions) {
            buf.writeVarInt(dimension);
        }

        buf.writeVarInt(this.blockPreferences.size());
        for (var entry : this.blockPreferences.long2ObjectEntrySet()) {
            buf.writeLong(entry.getLongKey());
            buf.writeVarInt(Block.getId(entry.getValue()));
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null) return;

        ItemStack held = sender.getItemInHand(this.hand);
        if (!GTItems.TERMINAL.isIn(held)) return;

        TerminalBehavior.applyUserPreferences(held, this.sliceRepeats, this.dimensions, this.blockPreferences);
    }
}
