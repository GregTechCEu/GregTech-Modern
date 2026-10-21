package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.SyncSystemCodecs;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;
import com.gregtechceu.gtceu.utils.codec.GTStreamCodecUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;

@Accessors(fluent = true)
@Getter(AccessLevel.PRIVATE)
public class CPacketTerminalSettings implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("terminal_settings");
    public static final Type<CPacketTerminalSettings> TYPE = new Type<>(ID);

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

    // spotless:off
    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketTerminalSettings> CODEC = GTStreamCodecUtils.composite(
            GTStreamCodecUtils.enumStreamCodec(InteractionHand.class), CPacketTerminalSettings::hand,
            MultiblockMachineDefinition.STREAM_CODEC, CPacketTerminalSettings::machineDefinition,
            ByteBufCodecs.map(Int2IntArrayMap::new, ByteBufCodecs.VAR_INT, ByteBufCodecs.VAR_INT), CPacketTerminalSettings::sliceRepeats,
            ByteBufCodecs.collection(IntArrayList::new, ByteBufCodecs.VAR_INT), CPacketTerminalSettings::dimensions,
            ByteBufCodecs.map(Object2ObjectOpenHashMap::new, BlockPos.STREAM_CODEC, BlockInfo.STREAM_CODEC), CPacketTerminalSettings::globalPreferences,
            ByteBufCodecs.map(Char2ObjectArrayMap::new, SyncSystemCodecs.CHAR_STREAM, BlockInfo.STREAM_CODEC), CPacketTerminalSettings::blockPreferences,
            ByteBufCodecs.BOOL, CPacketTerminalSettings::isFlipped,
            ByteBufCodecs.BOOL, CPacketTerminalSettings::isClearMulti,
            ByteBufCodecs.BOOL, CPacketTerminalSettings::isClearPreferences,
            ByteBufCodecs.BOOL, CPacketTerminalSettings::ignorePreviewCount,
            CPacketTerminalSettings::new);
    // spotless:on

    public CPacketTerminalSettings(InteractionHand hand,
                                   MultiblockMachineDefinition def,
                                   Int2IntMap sliceRepeats,
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

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.SERVERBOUND) {
            ServerPlayer sender = (ServerPlayer) context.player();

            ItemStack held = sender.getItemInHand(this.hand);
            if (!GTItems.TERMINAL.isIn(held)) return;

            var schemaInfo = new MultiblockSchemaInfo(this.machineDefinition, this.sliceRepeats, this.dimensions,
                    this.globalPreferences, this.blockPreferences, this.isFlipped, this.ignorePreviewCount);
            TerminalBehavior.applyUserPreferences(held, schemaInfo, this.isClearMulti, this.isClearPreferences);
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
