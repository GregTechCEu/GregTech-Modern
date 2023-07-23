package com.gregtechceu.gtceu.common.machine.storage;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.pipenet.longdist.LongDistanceNetwork;
import gregtech.api.pipenet.longdist.LongDistancePipeType;
import gregtech.common.ConfigHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class MetaTileEntityLongDistanceEndpoint extends MetaTileEntity implements ILDEndpoint, IDataInfoProvider {

    private final LongDistancePipeType pipeType;
    private Type type = Type.NONE;
    private ILDEndpoint link;
    private boolean placed = false;

    public MetaTileEntityLongDistanceEndpoint(ResourceLocation metaTileEntityId, LongDistancePipeType pipeType) {
        super(metaTileEntityId);
        this.pipeType = pipeType;
    }

    public void updateNetwork() {
        LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
        if (network != null) {
            // manually remove this endpoint from the network
            network.onRemoveEndpoint(this);
        }

        // find networks on input and output face
        List<LongDistanceNetwork> networks = findNetworks();
        if (networks.isEmpty()) {
            // no neighbours found, create new network
            network = this.pipeType.createNetwork(getWorld());
            network.onPlaceEndpoint(this);
            setType(Type.NONE);
        } else if (networks.size() == 1) {
            // one neighbour network found, attach self to neighbour network
            networks.get(0).onPlaceEndpoint(this);
        } else {
            // two neighbour networks found, configuration invalid
            setType(Type.NONE);
        }
    }

    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        return super.onWrenchClick(playerIn, hand, wrenchSide.getOpposite(), hitResult);
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        this.placed = true;
        super.setFrontFacing(frontFacing);
        if (getWorld() != null && !getWorld().isRemote) {
            updateNetwork();
        }
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return !this.hasFrontFacing() || getFrontFacing() != facing;
    }

    @Override
    public void onRemoval() {
        if (link != null) {
            // invalidate linked endpoint
            link.invalidateLink();
            invalidateLink();
        }
        setType(Type.NONE);
        LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
        // remove endpoint from network
        if (network != null) network.onRemoveEndpoint(this);
    }

    @Override
    public void onNeighborChanged() {
        if (!placed || getWorld() == null || getWorld().isRemote) return;

        List<LongDistanceNetwork> networks = findNetworks();
        LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
        if (network == null) {
            // shouldn't happen
            if (networks.isEmpty()) {
                // create new network since there are no neighbouring networks
                network = this.pipeType.createNetwork(getWorld());
                network.onPlaceEndpoint(this);
            } else if (networks.size() == 1) {
                // add to neighbour network
                networks.get(0).onPlaceEndpoint(this);
            }
        } else {
            if (networks.size() > 1) {
                // suddenly there are more than one neighbouring networks, invalidate
                onRemoval();
            }
        }
        if (networks.size() != 1) {
            setType(Type.NONE);
        }
    }

    private List<LongDistanceNetwork> findNetworks() {
        List<LongDistanceNetwork> networks = new ArrayList<>();
        LongDistanceNetwork network;
        // only check input and output side
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getFrontFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            // found a network on the input face, therefore this is an output of the network
            networks.add(network);
            setType(Type.OUTPUT);
        }
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getOutputFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            // found a network on the output face, therefore this is an input of the network
            networks.add(network);
            setType(Type.INPUT);
        }
        return networks;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound nbt = super.writeToNBT(data);
        data.setByte("Type", (byte) type.ordinal());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.type = Type.values()[data.getByte("Type")];
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public ILDEndpoint getLink() {
        if (link == null) {
            LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
            if (network != null && network.isValid()) {
                this.link = network.getOtherEndpoint(this);
            }
        }
        return this.link;
    }

    @Override
    public void invalidateLink() {
        this.link = null;
    }

    @Override
    public EnumFacing getOutputFacing() {
        return getFrontFacing().getOpposite();
    }

    @Override
    public LongDistancePipeType getPipeType() {
        return pipeType;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.3"));
        if (pipeType.getMinLength() > 0) {
            tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.min_length", pipeType.getMinLength()));
        }
        if (ConfigHolder.machines.doTerrainExplosion && getIsWeatherOrTerrainResistant()) {
            tooltip.add("gregtech.universal.tooltip.terrain_resist");
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> textComponents = new ArrayList<>();
        LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
        if (network == null) {
            textComponents.add(new TextComponentString("No network found"));
        } else {
            textComponents.add(new TextComponentString("Network:"));
            textComponents.add(new TextComponentString(" - " + network.getTotalSize() + " pipes"));
            ILDEndpoint in = network.getActiveInputIndex(), out = network.getActiveOutputIndex();
            textComponents.add(new TextComponentString(" - input: " + (in == null ? "none" : in.getPos())));
            textComponents.add(new TextComponentString(" - output: " + (out == null ? "none" : out.getPos())));
        }
        if (isInput()) {
            textComponents.add(new TextComponentString("Input endpoint"));
        }
        if (isOutput()) {
            textComponents.add(new TextComponentString("Output endpoint"));
        }
        return textComponents;
    }
}
