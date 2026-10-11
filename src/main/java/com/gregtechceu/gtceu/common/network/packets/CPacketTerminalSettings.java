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
    private final boolean isFlipped;
    boolean isClearMulti;
    boolean isClearPreferences;
    boolean ignorePreviewCount;

    public CPacketTerminalSettings(InteractionHand hand, MultiblockMachineDefinition def, Int2IntMap sliceRepeats,
                                   IntList dimensions,
                                   Object2ObjectMap<BlockPos, BlockInfo> globalPreferences,
                                   Char2ObjectMap<BlockInfo> blockPreferences,
                                   boolean isFlipped,
                                   boolean isClearMulti,
                                   boolean isClearPreferences,
                                   boolean ignorePreviewCount) {
        this.hand = hand;
        this.machineDefinition = def;
        this.sliceRepeats = sliceRepeats;
        this.dimensions = dimensions;
        this.globalPreferences = globalPreferences;
        this.blockPreferences = blockPreferences;
        this.isFlipped = isFlipped;
        this.isClearMulti = isClearMulti;
        this.isClearPreferences = isClearPreferences;
        this.ignorePreviewCount = ignorePreviewCount;
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

        this.isFlipped = buf.readBoolean();
        this.isClearMulti = buf.readBoolean();
        this.isClearPreferences = buf.readBoolean();
        this.ignorePreviewCount = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);

        buf.writeResourceLocation(this.machineDefinition.getId());

        buf.writeMap(this.sliceRepeats, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::writeVarInt);
        buf.writeCollection(dimensions, FriendlyByteBuf::writeVarInt);
        buf.writeMap(globalPreferences, FriendlyByteBuf::writeBlockPos, blockInfoWriter);
        buf.writeMap(blockPreferences, (b, v) -> b.writeChar(v), blockInfoWriter);

        buf.writeBoolean(this.isFlipped);
        buf.writeBoolean(this.isClearMulti);
        buf.writeBoolean(this.isClearPreferences);
        buf.writeBoolean(this.ignorePreviewCount);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null) return;

        ItemStack held = sender.getItemInHand(this.hand);
        if (!GTItems.TERMINAL.isIn(held)) return;

        var schemaInfo = new MultiblockSchemaInfo(this.machineDefinition, this.sliceRepeats, this.dimensions,
                this.globalPreferences, this.blockPreferences, this.isFlipped, this.ignorePreviewCount);
        TerminalBehavior.applyUserPreferences(held, schemaInfo, this.isClearMulti, this.isClearPreferences);
    }
}
