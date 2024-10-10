package com.gregtechceu.gtceu.api.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import org.jetbrains.annotations.NotNull;

/**
 * An optimised data structure backed by two arrays.
 * This is essentially equivalent to <code>List<Pair<Integer, byte[]>></code>, but more efficient.
 * {@link it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap} can not be used since it doesn't allow duplicate
 * discriminators.
 */
public class PacketDataList {

    private int[] discriminators;
    private byte[][] data;
    private int size = 0;

    public PacketDataList() {
        this.discriminators = new int[4];
        this.data = new byte[4][];
    }

    /**
     * Resizes the arrays to fit the required elements.
     *
     * @param s minimum size
     */
    private void ensureSize(int s) {
        if (this.discriminators.length < s) {
            int n = this.discriminators.length;
            int newCapacity = Math.max(n + 2, s); // there are rarely more than 2 elements in the list
            int[] temp = new int[newCapacity];
            byte[][] temp2 = new byte[newCapacity][];
            System.arraycopy(this.discriminators, 0, temp, 0, n);
            System.arraycopy(this.data, 0, temp2, 0, n);
            this.discriminators = temp;
            this.data = temp2;
        }
    }

    /**
     * Adds a discriminator - data pair to the list
     *
     * @param discriminator data id
     * @param data          data
     */
    public void add(int discriminator, byte[] data) {
        ensureSize(this.size + 1);
        this.discriminators[this.size] = discriminator;
        this.data[this.size] = data;
        this.size++;
    }

    /**
     * Adds all discriminator - data pairs from another list.
     * This does not check if the other list is empty or the same list.
     *
     * @param dataList other data list
     */
    public void addAll(PacketDataList dataList) {
        ensureSize(this.size + dataList.size);
        System.arraycopy(dataList.discriminators, 0, this.discriminators, this.size, dataList.size);
        System.arraycopy(dataList.data, 0, this.data, this.size, dataList.size);
        this.size += dataList.size;
    }

    /**
     * @return amount of data packets
     */
    public int size() {
        return size;
    }

    /**
     * @return true if there are no data packets
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * remove all data packets
     */
    public void clear() {
        for (int i = 0; i < this.size; i++) {
            this.data[i] = null;
        }
        this.size = 0;
    }

    /**
     * Writes all discriminator - data pairs to a nbt list.
     * Also removes all data packets from this list.
     *
     * @return nbt list with discriminators and data
     */
    @NotNull
    public ListTag dumpToNbt() {
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.size; i++) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putByteArray(Integer.toString(this.discriminators[i]), this.data[i]);
            listTag.add(entryTag);
            this.data[i] = null;
        }
        this.size = 0;
        return listTag;
    }
}
