package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

import java.util.*;

/**
 * @author KilaBash
 * @date 2023/3/11
 * @implNote FluidPipeNet
 */
public class FluidPipeNet extends PipeNet<FluidPipeData> {

    private final Map<BlockPos, List<PipeNetRoutePath>> NET_DATA = new HashMap<>();

    public FluidPipeNet(LevelPipeNet<FluidPipeData, ? extends PipeNet> world) {
        super(world);
        lastUpdate = world.getWorld().getGameTime();
    }

    public List<PipeNetRoutePath> getNetData(BlockPos pipePos) {
        List<PipeNetRoutePath> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = FluidPipeNetWalker.createNetData(this, pipePos);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(PipeNetRoutePath::getDistance));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(FluidPipeData nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("max_temperature", nodeData.properties.getMaxFluidTemperature());
        tagCompound.putLong("throughput", nodeData.properties.getThroughput());
        tagCompound.putBoolean("gas_proof", nodeData.properties.isGasProof());
        tagCompound.putBoolean("acid_proof", nodeData.properties.isAcidProof());
        tagCompound.putBoolean("cryo_proof", nodeData.properties.isCryoProof());
        tagCompound.putBoolean("plasma_proof", nodeData.properties.isPlasmaProof());
        tagCompound.putInt("channels", nodeData.properties.getChannels());
        tagCompound.putByte("connections", nodeData.connections());
    }

    @Override
    protected FluidPipeData readNodeData(CompoundTag tagCompound) {
        int maxTemperature = tagCompound.getInt("max_temperature");
        long throughput = tagCompound.getLong("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        boolean acidProof = tagCompound.getBoolean("acid_proof");
        boolean cryoProof = tagCompound.getBoolean("cryo_proof");
        boolean plasmaProof = tagCompound.getBoolean("plasma_proof");
        int channels = tagCompound.getInt("channels");
        return new FluidPipeData(new FluidPipeProperties(maxTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, channels), tagCompound.getByte("connections"));
    }

    //////////////////////////////////////
    //*******     Pipe Status    *******//
    //////////////////////////////////////

    private final Long2ObjectMap<Set<Fluid>> channelUsed = new Long2ObjectOpenHashMap<>();
    private final Long2LongMap throughputUsed = new Long2LongOpenHashMap();
    private long lastUpdate;

    private void updateTick() {
        var latestTime = getWorldData().getWorld().getGameTime();
        if (lastUpdate != latestTime) {
            channelUsed.clear();
            throughputUsed.clear();
        }
        lastUpdate = latestTime;
    }

    public Set<Fluid> getChannelUsed(BlockPos pos) {
        updateTick();
        return channelUsed.getOrDefault(pos.asLong(), Collections.emptySet());
    }

    public long getThroughputUsed(BlockPos pos) {
        updateTick();
        return throughputUsed.getOrDefault(pos.asLong(), 0);
    }

    public void useChannel(BlockPos pos, Fluid fluid) {
        updateTick();
        channelUsed.computeIfAbsent(pos.asLong(), p -> new HashSet<>()).add(fluid);
    }

    public void useThroughput(BlockPos pos, long filled) {
        updateTick();
        throughputUsed.put(pos.asLong(), throughputUsed.getOrDefault(pos.asLong(), 0) + filled);
    }

    public record Snapshot(Long2ObjectMap<Set<Fluid>> channelUsed, Long2LongMap throughputUsed) {

    }

    public Snapshot createSnapeShot() {
        Long2ObjectMap<Set<Fluid>> channelUsedCopied = new Long2ObjectOpenHashMap<>();
        channelUsed.forEach((k, v) -> channelUsedCopied.computeIfAbsent(k, key -> new HashSet<>(v)).addAll(v));
        Long2LongMap throughputUsedCopied = new Long2LongOpenHashMap();
        throughputUsedCopied.putAll(throughputUsed);
        return new Snapshot(channelUsedCopied, throughputUsedCopied);
    }

    public void resetData(Snapshot snapshot) {
        channelUsed.clear();
        throughputUsed.clear();
        snapshot.channelUsed.forEach((k, v) -> channelUsed.computeIfAbsent(k, key -> new HashSet<>(v)).addAll(v));
        throughputUsed.putAll(snapshot.throughputUsed);
    }
}
