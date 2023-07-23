package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.ActiveTransformerWrapper;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityActiveTransformer extends MultiblockWithDisplayBase implements IControllable {

    private boolean isWorkingEnabled = true;
    private IEnergyContainer energyOutputContainer;
    private ActiveTransformerWrapper wrapper;
    private ILaserContainer laserInContainer;
    private boolean isActive = true;

    public MetaTileEntityActiveTransformer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.wrapper = null;
        this.laserInContainer = null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityActiveTransformer(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        setActive(true);
        if (wrapper == null || this.energyOutputContainer.getEnergyCapacity() == 0) {
            return;
        }

        if (isWorkingEnabled()) {
            wrapper.removeEnergy(energyOutputContainer.addEnergy(wrapper.getEnergyStored()));
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IEnergyContainer> inputEnergy = getAbilities(MultiblockAbility.INPUT_ENERGY);
        List<IEnergyContainer> outputEnergy = getAbilities(MultiblockAbility.OUTPUT_ENERGY);
        List<ILaserContainer> inputLaser = getAbilities(MultiblockAbility.INPUT_LASER);
        List<ILaserContainer> outputLaser = getAbilities(MultiblockAbility.OUTPUT_LASER);

        // Invalidate the structure if there is not at least one output and one input
        if (inputEnergy.size() + inputLaser.size() == 0 || outputEnergy.size() + outputLaser.size() == 0) {
            this.invalidateStructure();
            return;
        }

        if (outputEnergy.size() == 0 && inputEnergy.size() == 0) {
            return;
        }

        energyOutputContainer = new EnergyContainerList(outputEnergy);
        if (inputLaser.size() == 1) {
            laserInContainer = inputLaser.get(0);
        }

        wrapper = new ActiveTransformerWrapper(new EnergyContainerList(inputEnergy), laserInContainer);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.wrapper = null;
        this.laserInContainer = null;
        setActive(false);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XCX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('X', states(getCasingState()).setMinGlobalLimited(12).or(getHatchPredicates()))
                .where('S', selfPredicate())
                .where('C', states(MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)))
                .build();
    }

    private TraceabilityPredicate getHatchPredicates() {
        return abilities(MultiblockAbility.INPUT_ENERGY).setPreviewCount(2)
                .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setPreviewCount(2))
                .or(abilities(MultiblockAbility.INPUT_LASER).setMaxGlobalLimited(1))
                .or(abilities(MultiblockAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
                // Disallow the config maintenance hatch because that would probably break the conservation of energy
                .or(metaTileEntities(MetaTileEntities.MAINTENANCE_HATCH,
                        MetaTileEntities.AUTO_MAINTENANCE_HATCH, MetaTileEntities.CLEANING_MAINTENANCE_HATCH).setExactLimit(1));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HIGH_POWER_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.HIGH_POWER_CASING);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.DATA_BANK_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive && getNumMaintenanceProblems() == 0;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.isWorkingEnabled = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
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
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.active_transformer.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.active_transformer.tooltip2"));
        tooltip.add(I18n.format("gregtech.machine.active_transformer.tooltip3")
                + TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.active_transformer.tooltip3.5"));
    }

    public ILaserContainer getWrapper() {
        if (wrapper != null) {
            return wrapper;
        } else if (isStructureFormed() && getAbilities(MultiblockAbility.INPUT_LASER).size() == 1) {
            return getAbilities(MultiblockAbility.INPUT_LASER).get(0);
        }
        return null;
    }
}
