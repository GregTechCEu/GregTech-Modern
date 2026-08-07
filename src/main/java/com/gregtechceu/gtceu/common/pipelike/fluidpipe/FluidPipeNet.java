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
        int maxTemperature = tagCompound.getInt("max_temperature");
        int throughput = tagCompound.getInt("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        boolean acidProof = tagCompound.getBoolean("acid_proof");
        boolean cryoProof = tagCompound.getBoolean("cryo_proof");
        boolean plasmaProof = tagCompound.getBoolean("plasma_proof");
        int channels = tagCompound.getInt("channels");
        return new FluidPipeProperties(maxTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof,
                channels);
    }
}
