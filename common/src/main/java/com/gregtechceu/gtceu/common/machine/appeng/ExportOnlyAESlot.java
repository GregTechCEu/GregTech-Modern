package com.gregtechceu.gtceu.common.machine.appeng;

import appeng.api.storage.data.IAEStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

/**
 * @author GlodBlock
 * @Description A export only slot to hold {@link IAEStack}
 * @date 2023/4/22-13:42
 */
public abstract class ExportOnlyAESlot<T extends IAEStack<T>> implements IConfigurableSlot<T>, INBTSerializable<NBTTagCompound> {
    protected final static String CONFIG_TAG = "config";
    protected final static String STOCK_TAG = "stock";
    protected T config;
    protected T stock;

    public ExportOnlyAESlot(T config, T stock) {
        this.config = config;
        this.stock = stock;
    }

    public ExportOnlyAESlot() {
        this(null, null);
    }

    @Nullable
    public T requestStack() {
        if (this.stock != null && !this.stock.isMeaningful()) {
            this.stock = null;
        }
        if (this.config == null || (this.stock != null && !this.config.equals(this.stock))) {
            return null;
        }
        if (this.stock == null) {
            return this.config.copy();
        }
        if (this.stock.getStackSize() < this.config.getStackSize()) {
            return this.config.copy().setStackSize(this.config.getStackSize() - this.stock.getStackSize());
        }
        return null;
    }

    @Nullable
    public T exceedStack() {
        if (this.stock != null && !this.stock.isMeaningful()) {
            this.stock = null;
        }
        if (this.config == null && this.stock != null) {
            return this.stock.copy();
        }
        if (this.config != null && this.stock != null) {
            if (this.config.equals(this.stock) && this.config.getStackSize() < this.stock.getStackSize()) {
                return this.stock.copy().setStackSize(this.stock.getStackSize() - this.config.getStackSize());
            }
            if (!this.config.equals(this.stock)) {
                return this.stock.copy();
            }
        }
        return null;
    }

    abstract void addStack(T stack);

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (this.config != null) {
            NBTTagCompound configTag = new NBTTagCompound();
            this.config.writeToNBT(configTag);
            tag.setTag(CONFIG_TAG, configTag);
        }
        if (this.stock != null) {
            NBTTagCompound stockTag = new NBTTagCompound();
            this.stock.writeToNBT(stockTag);
            tag.setTag(STOCK_TAG, stockTag);
        }
        return tag;
    }

    @Override
    public T getConfig() {
        return this.config;
    }

    @Override
    public T getStock() {
        return this.stock;
    }

    @Override
    public void setConfig(T val) {
        this.config = val;
    }

    @Override
    public void setStock(T val) {
        this.stock = val;
    }

}
