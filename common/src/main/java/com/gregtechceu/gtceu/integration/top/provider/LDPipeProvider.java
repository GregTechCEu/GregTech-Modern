package com.gregtechceu.gtceu.integration.top.provider;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.pipenet.longdist.LongDistanceNetwork;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class LDPipeProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":ld_pipe_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer entityPlayer, @Nonnull World world,
                             IBlockState blockState, @Nonnull IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity iGregTechTileEntity) {
            MetaTileEntity metaTileEntity = iGregTechTileEntity.getMetaTileEntity();
            if (metaTileEntity instanceof ILDEndpoint endpoint) {
                LongDistanceNetwork network = LongDistanceNetwork.get(world, pos);
                if (network == null) {
                    probeInfo.text(TextStyleClass.ERROR + "{*gregtech.top.ld_pipe_no_network*}");
                } else {
                    ILDEndpoint other = endpoint.getLink();
                    if (other != null) {
                        probeInfo.text(TextStyleClass.OK + "{*gregtech.top.ld_pipe_connected*}");
                        probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.ld_pipe_length*} " +
                                TextStyleClass.INFOIMP + network.getTotalSize());

                        addIOText(probeInfo, endpoint);

                        if (entityPlayer.isSneaking()) {
                            BlockPos otherPos = other.getPos();
                            String prefix = null;
                            if (other.isInput()) {
                                prefix = "{*gregtech.top.ld_pipe_input_endpoint*}";
                            } else if (other.isOutput()) {
                                prefix = "{*gregtech.top.ld_pipe_output_endpoint*}";
                            }

                            if (prefix != null) {
                                probeInfo.text(TextStyleClass.INFO + prefix +
                                        TextStyleClass.INFO + " x: " + TextStyleClass.OK + otherPos.getX() +
                                        TextStyleClass.INFO + " y: " + TextStyleClass.OK + otherPos.getY() +
                                        TextStyleClass.INFO + " z: " + TextStyleClass.OK + otherPos.getZ());
                            }
                        }
                    } else {
                        probeInfo.text(TextStyleClass.WARNING + "{*gregtech.top.ld_pipe_incomplete*}");
                        addIOText(probeInfo, endpoint);
                    }
                }
            }
        }
    }

    private static void addIOText(@Nonnull IProbeInfo probeInfo, @Nonnull ILDEndpoint endpoint) {
        if (endpoint.isInput()) {
            probeInfo.text(TextStyleClass.INFOIMP + "{*gregtech.top.ld_pipe_input*}");
        } else if (endpoint.isOutput()) {
            probeInfo.text(TextStyleClass.INFOIMP + "{*gregtech.top.ld_pipe_output*}");
        }
    }
}
