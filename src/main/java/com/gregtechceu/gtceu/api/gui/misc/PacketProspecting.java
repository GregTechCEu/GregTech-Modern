package com.gregtechceu.gtceu.api.gui.misc;

import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Array;

public class PacketProspecting<T> {

    public int chunkX;
    public int chunkZ;
    public ProspectorMode<T> mode;
    public T[][][] data;

    public PacketProspecting(int chunkX, int chunkZ, ProspectorMode<T> mode) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.mode = mode;
        //noinspection unchecked
        this.data = (T[][][]) Array.newInstance(mode.getItemClass(), this.mode.cellSize, this.mode.cellSize, 0);
    }

    public static <T> PacketProspecting<T> readPacketData(ProspectorMode<T> mode, FriendlyByteBuf buffer) {
        PacketProspecting<T> packet = new PacketProspecting<>(buffer.readVarInt(), buffer.readVarInt(), mode);
        for (int x = 0; x < mode.cellSize; x++) {
            for (int z = 0; z < mode.cellSize; z++) {
                //noinspection unchecked
                packet.data[x][z] = (T[]) Array.newInstance(mode.getItemClass(), buffer.readVarInt());
                for (int i = 0; i < packet.data[x][z].length; i++) {
                    packet.data[x][z][i] = mode.deserialize(buffer);
                }
            }
        }
        return packet;
    }

    public void writePacketData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(chunkX);
        buffer.writeVarInt(chunkZ);
        for (int x = 0; x < mode.cellSize; x++) {
            for (int z = 0; z < mode.cellSize; z++) {
                buffer.writeVarInt(data[x][z].length);
                for (var item : data[x][z]) {
                    mode.serialize(item, buffer);
                }
            }
        }
    }
}
