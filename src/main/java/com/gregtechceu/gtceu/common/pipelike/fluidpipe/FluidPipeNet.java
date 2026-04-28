package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.nbt.CompoundTag;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> {

    public FluidPipeNet(LevelPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    /////////////////////////////////////
    // *********** NBT ***********//
    /////////////////////////////////////

    @Override
    protected void writeNodeData(FluidPipeProperties nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.putInt("throughput", nodeData.getThroughput());
        tagCompound.putBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.putBoolean("acid_proof", nodeData.isAcidProof());
        tagCompound.putBoolean("cryo_proof", nodeData.isCryoProof());
        tagCompound.putBoolean("plasma_proof", nodeData.isPlasmaProof());
        tagCompound.putInt("channels", nodeData.getChannels());
    }

    @Override
    protected FluidPipeProperties readNodeData(CompoundTag tagCompound) {
        int maxTemperature = tagCompound.getIntOr("max_temperature", 0);
        int throughput = tagCompound.getIntOr("throughput", 0);
        boolean gasProof = tagCompound.getBooleanOr("gas_proof", false);
        boolean acidProof = tagCompound.getBooleanOr("acid_proof", false);
        boolean cryoProof = tagCompound.getBooleanOr("cryo_proof", false);
        boolean plasmaProof = tagCompound.getBooleanOr("plasma_proof", false);
        int channels = tagCompound.getIntOr("channels", 0);
        return new FluidPipeProperties(maxTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof,
                channels);
    }
}
