package com.gregtechceu.gtceu.common.machine.appeng;

import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import gregtech.api.GTValues;
import gregtech.api.capability.IControllable;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumSet;

/**
 * @Author GlodBlock
 * @Description It can connect to ME network.
 * @Date 2023/4/18-23:17
 */
public abstract class MetaTileEntityAEHostablePart extends MetaTileEntityMultiblockNotifiablePart implements IControllable {

    protected static final IStorageChannel<IAEItemStack> ITEM_NET = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    protected static final IStorageChannel<IAEFluidStack> FLUID_NET = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);

    private final static int ME_UPDATE_INTERVAL = ConfigHolder.compat.ae2.updateIntervals;
    private AENetworkProxy aeProxy;
    private int meUpdateTick;
    protected boolean isOnline;
    private final static int ONLINE_ID = 6666;

    public MetaTileEntityAEHostablePart(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.meUpdateTick = 0;
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            this.meUpdateTick++;
        }
    }

    /**
     * ME hatch will try to put its buffer back to me system when removal.
     * So there is no need to drop them.
     */
    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        // NO-OP
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.aeProxy != null) {
            buf.writeBoolean(true);
            NBTTagCompound proxy = new NBTTagCompound();
            this.aeProxy.writeToNBT(proxy);
            buf.writeCompoundTag(proxy);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(this.meUpdateTick);
        buf.writeBoolean(this.isOnline);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean() && this.aeProxy != null) {
            try {
                this.aeProxy.readFromNBT(buf.readCompoundTag());
            } catch (IOException ignore) {
            }
        }
        this.meUpdateTick = buf.readInt();
        this.isOnline = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == ONLINE_ID) {
            this.isOnline = buf.readBoolean();
        }
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            return this.hatchTexture = controller.getBaseTexture(this);
        } else if (this.hatchTexture != null) {
            if (hatchTexture != Textures.getInactiveTexture(hatchTexture)) {
                return this.hatchTexture = Textures.getInactiveTexture(hatchTexture);
            }
            return this.hatchTexture;
        } else {
            // Always display as EV casing
            return Textures.VOLTAGE_CASINGS[GTValues.EV];
        }
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation part) {
        if (part.getFacing() != this.frontFacing) {
            return AECableType.NONE;
        }
        return AECableType.SMART;
    }

    @Nullable
    @Override
    public AENetworkProxy getProxy() {
        if (this.aeProxy == null) {
            return this.aeProxy = this.createProxy();
        }
        if (!this.aeProxy.isReady() && this.getWorld() != null) {
            this.aeProxy.onReady();
        }
        return this.aeProxy;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.aeProxy != null) {
            this.aeProxy.setValidSides(EnumSet.of(this.getFrontFacing()));
        }
    }

    @Override
    public void gridChanged() {
        // NO-OP
    }

    /**
     * Update me network connection status.
     * @return the updated status.
     */
    public boolean updateMEStatus() {
        if (this.aeProxy != null) {
            this.isOnline = this.aeProxy.isActive() && this.aeProxy.isPowered();
        } else {
            this.isOnline = false;
        }
        writeCustomData(ONLINE_ID, buf -> buf.writeBoolean(this.isOnline));
        return this.isOnline;
    }

    protected boolean shouldSyncME() {
        return this.meUpdateTick % ME_UPDATE_INTERVAL == 0;
    }

    protected IActionSource getActionSource() {
        if (this.getHolder() instanceof IActionHost) {
            return new MachineSource((IActionHost) this.getHolder());
        }
        return new BaseActionSource();
    }

    @Nullable
    private AENetworkProxy createProxy() {
        if (this.getHolder() instanceof IGridProxyable) {
            AENetworkProxy proxy = new AENetworkProxy((IGridProxyable) this.getHolder(), "mte_proxy", this.getStackForm(), true);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
            proxy.setValidSides(EnumSet.of(this.getFrontFacing()));
            return proxy;
        }
        return null;
    }

}
