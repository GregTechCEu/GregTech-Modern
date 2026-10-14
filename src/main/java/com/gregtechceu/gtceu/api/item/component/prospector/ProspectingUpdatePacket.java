package com.gregtechceu.gtceu.api.item.component.prospector;

import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Array;

public class ProspectingUpdatePacket<T> {

    public int chunkX;
    public int chunkZ;
    public ProspectorMode<T> mode;
    public T[][][] data;

    public ProspectingUpdatePacket(int chunkX, int chunkZ, ProspectorMode<T> mode) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.mode = mode;
        // noinspection unchecked
        this.data = (T[][][]) Array.newInstance(mode.getItemClass(), this.mode.cellSize, this.mode.cellSize, 0);
    }

    public static <T> ProspectingUpdatePacket<T> read(ProspectorMode<T> mode, FriendlyByteBuf buffer) {
        int chunkX = buffer.readVarInt();
        int chunkZ = buffer.readVarInt();

        ProspectingUpdatePacket<T> packet = new ProspectingUpdatePacket<>(chunkX, chunkZ, mode);

        for (int x = 0; x < mode.cellSize; x++) {
            for (int z = 0; z < mode.cellSize; z++) {
                int cellEntryCount = buffer.readVarInt();
                // noinspection unchecked
                packet.data[x][z] = (T[]) Array.newInstance(mode.getItemClass(), cellEntryCount);
                for (int i = 0; i < cellEntryCount; i++) {
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

                for (T item : data[x][z]) {
                    mode.serialize(item, buffer);
                }
            }
        }
    }
}
