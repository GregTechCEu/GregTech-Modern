package com.gregtechceu.gtceu.common.machine.appeng;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.util.AEFluidStack;
import appeng.me.GridAccessException;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.gui.widget.appeng.AEFluidGridWidget;
import gregtech.common.inventory.appeng.SerializableFluidList;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author GlodBlock
 * @Description The Output Hatch that can directly send its contents to ME storage network.
 * @Date 2023/4/19-1:18
 */
public class MetaTileEntityMEOutputHatch extends MetaTileEntityAEHostablePart implements IMultiblockAbilityPart<IFluidTank> {

    public final static String FLUID_BUFFER_TAG = "FluidBuffer";
    public final static String WORKING_TAG = "WorkingEnabled";
    private boolean workingEnabled;
    private SerializableFluidList internalBuffer;

    public MetaTileEntityMEOutputHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.UHV, true);
        this.workingEnabled = true;
    }

    @Override
    protected void initializeInventory() {
        this.internalBuffer = new SerializableFluidList();
        super.initializeInventory();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && this.shouldSyncME()) {
            if (this.updateMEStatus()) {
                if (!this.internalBuffer.isEmpty()) {
                    try {
                        IMEMonitor<IAEFluidStack> aeNetwork = this.getProxy().getStorage().getInventory(FLUID_NET);
                        for (IAEFluidStack fluid : this.internalBuffer) {
                            IAEFluidStack notInserted = aeNetwork.injectItems(fluid.copy(), Actionable.MODULATE, this.getActionSource());
                            if (notInserted != null && notInserted.getStackSize() > 0) {
                                fluid.setStackSize(notInserted.getStackSize());
                            } else {
                                fluid.reset();
                            }
                        }
                    } catch (GridAccessException ignore) {
                    }
                }
            }
        }
    }

    @Override
    public void onRemoval() {
        try {
            IMEMonitor<IAEFluidStack> aeNetwork = this.getProxy().getStorage().getInventory(FLUID_NET);
            for (IAEFluidStack fluid : this.internalBuffer) {
                aeNetwork.injectItems(fluid.copy(), Actionable.MODULATE, this.getActionSource());
            }
        } catch (GridAccessException ignore) {
        }
        super.onRemoval();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEOutputHatch(this.metaTileEntityId);
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
        builder.label(10, 25, "gregtech.gui.waiting_list", 0xFFFFFFFF);
        builder.widget(new AEFluidGridWidget(10, 35, 3, this.internalBuffer));

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
        data.setTag(FLUID_BUFFER_TAG, this.internalBuffer.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
        if (data.hasKey(FLUID_BUFFER_TAG, 9)) {
            this.internalBuffer.deserializeNBT((NBTTagList) data.getTag(FLUID_BUFFER_TAG));
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.ME_OUTPUT_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.export.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.fluid_export.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.export.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.EXPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> list) {
        list.add(new InaccessibleInfiniteTank(this.internalBuffer, this.getController()));
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (controllerBase instanceof MultiblockWithDisplayBase) {
            ((MultiblockWithDisplayBase) controllerBase).enableFluidInfSink();
        }
    }

    private static class InaccessibleInfiniteTank implements IFluidTank, INotifiableHandler {
        private final IItemList<IAEFluidStack> internalBuffer;
        private final List<MetaTileEntity> notifiableEntities = new ArrayList<>();

        public InaccessibleInfiniteTank(IItemList<IAEFluidStack> internalBuffer, MetaTileEntity mte) {
            this.internalBuffer = internalBuffer;
            this.notifiableEntities.add(mte);
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return null;
        }

        @Override
        public int getFluidAmount() {
            return 0;
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE - 1;
        }

        @Override
        public FluidTankInfo getInfo() {
            return null;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) {
                return 0;
            }
            if (doFill) {
                this.internalBuffer.add(AEFluidStack.fromFluidStack(resource));
            }
            this.trigger();
            return resource.amount;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
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
                    this.addToNotifiedList(metaTileEntity, this, true);
                }
            }
        }
    }

}
