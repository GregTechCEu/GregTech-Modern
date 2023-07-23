package com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.storage.MetaTileEntityLongDistanceEndpoint;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

public class MetaTileEntityLDFluidEndpoint extends MetaTileEntityLongDistanceEndpoint {

    private static final FluidTank DEFAULT_TANK = new FluidTank(10000) {
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drainInternal(int maxDrain, boolean doDrain) {
            return null;
        }
    };

    public MetaTileEntityLDFluidEndpoint(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, LDFluidPipeType.INSTANCE);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLDFluidEndpoint(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (getWorld().isRemote || side != getFrontFacing() || !isInput()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(DEFAULT_TANK);
            }
            ILDEndpoint endpoint = getLink();
            if (endpoint != null) {
                EnumFacing outputFacing = endpoint.getOutputFacing();
                TileEntity te = getWorld().getTileEntity(endpoint.getPos().offset(outputFacing));
                if (te != null) {
                    T t = te.getCapability(capability, outputFacing.getOpposite());
                    if (t != null) {
                        return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper((IFluidHandler) t));
                    }
                }
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(DEFAULT_TANK);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[GTValues.LV].render(renderState, translation, colouredPipeline);
        Textures.LD_FLUID_PIPE.renderOrientedState(renderState, translation, pipeline, frontFacing, false, false);
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getOutputFacing(), renderState, translation, pipeline);
        Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getOutputFacing(), renderState, translation, pipeline);
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[GTValues.LV].getParticleSprite(), 0xFFFFFF);
    }

    private static class FluidHandlerWrapper extends FluidHandlerDelegate {

        public FluidHandlerWrapper(IFluidHandler delegate) {
            super(delegate);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }
    }
}
