package com.gregtechceu.gtceu.common.machine.appeng;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.gui.widget.appeng.AEFluidConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author GlodBlock
 * @Description The Input Hatch that can auto fetch fluid ME storage network.
 * @Date 2023/4/20-21:21
 */
public class MetaTileEntityMEInputHatch extends MetaTileEntityAEHostablePart implements IMultiblockAbilityPart<IFluidTank> {

    public final static String FLUID_BUFFER_TAG = "FluidTanks";
    public final static String WORKING_TAG = "WorkingEnabled";
    private final static int CONFIG_SIZE = 16;
    private boolean workingEnabled;
    private ExportOnlyAEFluid[] aeFluidTanks;

    public MetaTileEntityMEInputHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.UHV, false);
        this.workingEnabled = true;
    }

    @Override
    protected void initializeInventory() {
        this.aeFluidTanks = new ExportOnlyAEFluid[CONFIG_SIZE];
        for (int i = 0; i < CONFIG_SIZE; i ++) {
            this.aeFluidTanks[i] = new ExportOnlyAEFluid(null, null, this.getController());
        }
        super.initializeInventory();
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, this.aeFluidTanks);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && this.shouldSyncME()) {
            if (this.updateMEStatus()) {
                try {
                    IMEMonitor<IAEFluidStack> aeNetwork = this.getProxy().getStorage().getInventory(FLUID_NET);
                    for (ExportOnlyAEFluid aeTank : this.aeFluidTanks) {
                        // Try to clear the wrong fluid
                        IAEFluidStack exceedFluid = aeTank.exceedStack();
                        if (exceedFluid != null) {
                            long total = exceedFluid.getStackSize();
                            IAEFluidStack notInserted = aeNetwork.injectItems(exceedFluid, Actionable.MODULATE, this.getActionSource());
                            if (notInserted != null && notInserted.getStackSize() > 0) {
                                aeTank.drain((int) (total - notInserted.getStackSize()), true);
                                continue;
                            } else {
                                aeTank.drain((int) total, true);
                            }
                        }
                        // Fill it
                        IAEFluidStack reqFluid = aeTank.requestStack();
                        if (reqFluid != null) {
                            IAEFluidStack extracted = aeNetwork.extractItems(reqFluid, Actionable.MODULATE, this.getActionSource());
                            if (extracted != null) {
                                aeTank.addStack(extracted);
                            }
                        }
                    }
                } catch (GridAccessException ignore) {
                }
            }
        }
    }

    @Override
    public void onRemoval() {
        try {
            IMEMonitor<IAEFluidStack> aeNetwork = this.getProxy().getStorage().getInventory(FLUID_NET);
            for (ExportOnlyAEFluid aeTank : this.aeFluidTanks) {
                IAEFluidStack stock = aeTank.stock;
                if (stock instanceof WrappedFluidStack) {
                    stock = ((WrappedFluidStack) stock).getAEStack();
                }
                if (stock != null) {
                    aeNetwork.injectItems(stock, Actionable.MODULATE, this.getActionSource());
                }
            }
        } catch (GridAccessException ignore) {
        }
        super.onRemoval();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEInputHatch(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND, 176, 18 + 18 * 4 + 94)
                .label(10, 5, getMetaFullName());
        // ME Network status
        builder.dynamicLabel(10, 15, () -> this.isOnline ?
                        I18n.format("gregtech.gui.me_network.online") :
                        I18n.format("gregtech.gui.me_network.offline"),
                0xFFFFFFFF);

        // Config slots
        builder.widget(new AEFluidConfigWidget(16, 25, this.aeFluidTanks));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * 4 + 12);
        return builder.build(this.getHolder(), entityPlayer);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(WORKING_TAG, this.workingEnabled);
        NBTTagList tanks = new NBTTagList();
        for (int i = 0; i < CONFIG_SIZE; i ++) {
            ExportOnlyAEFluid tank = this.aeFluidTanks[i];
            NBTTagCompound tankTag = new NBTTagCompound();
            tankTag.setInteger("slot", i);
            tankTag.setTag("tank", tank.serializeNBT());
            tanks.appendTag(tankTag);
        }
        data.setTag(FLUID_BUFFER_TAG, tanks);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
        if (data.hasKey(FLUID_BUFFER_TAG, 9)) {
            NBTTagList tanks = (NBTTagList) data.getTag(FLUID_BUFFER_TAG);
            for (NBTBase nbtBase : tanks) {
                NBTTagCompound tankTag = (NBTTagCompound) nbtBase;
                ExportOnlyAEFluid tank = this.aeFluidTanks[tankTag.getInteger("slot")];
                tank.deserializeNBT(tankTag.getCompoundTag("tank"));
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.ME_INPUT_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.fluid_import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> list) {
        list.addAll(Arrays.asList(this.aeFluidTanks));
    }

    public static class ExportOnlyAEFluid extends ExportOnlyAESlot<IAEFluidStack> implements IFluidTank, IFluidHandler, INotifiableHandler {
        private final List<MetaTileEntity> notifiableEntities = new ArrayList<>();

        public ExportOnlyAEFluid(IAEFluidStack config, IAEFluidStack stock, MetaTileEntity mte) {
            super(config, stock);
            this.notifiableEntities.add(mte);
        }

        public ExportOnlyAEFluid() {
            super();
        }

        @Override
        public IAEFluidStack requestStack() {
            IAEFluidStack result = super.requestStack();
            if (result instanceof WrappedFluidStack) {
                return ((WrappedFluidStack) result).getAEStack();
            } else {
                return result;
            }
        }

        @Override
        public IAEFluidStack exceedStack() {
            IAEFluidStack result = super.exceedStack();
            if (result instanceof WrappedFluidStack) {
                return ((WrappedFluidStack) result).getAEStack();
            } else {
                return result;
            }
        }

        @Override
        public void addStack(IAEFluidStack stack) {
            if (this.stock == null) {
                this.stock = WrappedFluidStack.fromFluidStack(stack.getFluidStack());
            } else {
                this.stock.add(stack);
            }
            trigger();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt.hasKey(CONFIG_TAG)) {
                this.config = WrappedFluidStack.fromNBT(nbt.getCompoundTag(CONFIG_TAG));
            }
            if (nbt.hasKey(STOCK_TAG)) {
                this.stock = WrappedFluidStack.fromNBT(nbt.getCompoundTag(STOCK_TAG));
            }
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            if (this.stock != null && this.stock instanceof WrappedFluidStack) {
                return ((WrappedFluidStack) this.stock).getDelegate();
            }
            return null;
        }

        @Override
        public int getFluidAmount() {
            return this.stock != null ? (int) this.stock.getStackSize() : 0;
        }

        @Override
        public int getCapacity() {
            // Its capacity is always 0.
            return 0;
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(this);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[] {
                    new FluidTankProperties(this.getFluid(), 0)
            };
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (this.getFluid() != null && this.getFluid().isFluidEqual(resource)) {
                return this.drain(resource.amount, doDrain);
            }
            return null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (this.stock == null) {
                return null;
            }
            int drained = (int) Math.min(this.stock.getStackSize(), maxDrain);
            FluidStack result = new FluidStack(this.stock.getFluid(), drained);
            if (doDrain) {
                this.stock.decStackSize(drained);
                if (this.stock.getStackSize() == 0) {
                    this.stock = null;
                }
                trigger();
            }
            return result;
        }

        @Override
        public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.notifiableEntities.add(metaTileEntity);
        }

        @Override
        public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.notifiableEntities.remove(metaTileEntity);
        }

        private void trigger() {
            for (MetaTileEntity metaTileEntity : this.notifiableEntities) {
                if (metaTileEntity != null && metaTileEntity.isValid()) {
                    this.addToNotifiedList(metaTileEntity, this, false);
                }
            }
        }

        @Override
        public ExportOnlyAEFluid copy() {
            return new ExportOnlyAEFluid(
                    this.config == null ? null : this.config.copy(),
                    this.stock == null ? null : this.stock.copy(),
                    null
            );
        }
    }

}
